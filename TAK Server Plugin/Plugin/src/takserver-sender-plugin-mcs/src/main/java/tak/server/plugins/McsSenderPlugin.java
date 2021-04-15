package tak.server.plugins;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.UUID;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import java.text.SimpleDateFormat;  
import java.util.Date;  

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import atakmap.commoncommo.protobuf.v1.MessageOuterClass.Message;
import tak.server.plugins.dto.EventDto;
import tak.server.plugins.dto.EntityDto;
import tak.server.plugins.interfaces.MessageCallback;
import tak.server.plugins.messagebroker.RabbitMQClient;
import tak.server.plugins.missionapi.TakServerCoTApi;
import tak.server.plugins.missionapi.TakServerCoTCallback;
import tak.server.plugins.processing.MessageConsumer;
import tak.server.plugins.processing.MessageProducer;
import tak.server.plugins.processing.ProcessingMessage;
import tak.server.plugins.utilities.BrokerHelper;
import tak.server.plugins.utilities.McsCoTConverter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;


@TakServerPlugin(name = "MCS COP Sender Plugin", description = "TAK Server plugin that consumes MCS COP Events, converts them to CoT messages, and sends them to TAK Server")
public class McsSenderPlugin extends MessageSenderBase implements MessageCallback {

	private static int _queueSize = 10;

	private static final Logger _logger = LoggerFactory.getLogger(McsSenderPlugin.class);
	private RabbitMQClient _rabbitMqConsumer;
	private MessageProducer _messageProducer;
	private MessageConsumer _messageConsumer;
	private BlockingQueue<ProcessingMessage> _blockingQueue; 
    private ExecutorService _executor = Executors.newFixedThreadPool(2);
	
	public static Boolean VerboseLogging = false;
	
	@SuppressWarnings("unchecked")
	public McsSenderPlugin() {
		_logger.info("properties: " + config.getProperties());

		if (config.containsProperty("processing_queue_size"))
			_queueSize = (int)config.getProperty("processing_queue_size");

		if (config.containsProperty("verboseLogging")) {
			VerboseLogging = (boolean)config.getProperty("verboseLogging");
			_logger.info("VerboseLogging: " + VerboseLogging);
		}
		
		_blockingQueue = new LinkedBlockingDeque<>(_queueSize);
		
		_rabbitMqConsumer = new RabbitMQClient();
		_messageProducer = new MessageProducer(_executor, _blockingQueue);
		_messageConsumer = new MessageConsumer(_executor, _blockingQueue, this);
	}

	@Override
	public void start() {
		try {
			_logger.info("Configuration Properties: " + config.getProperties());

			setupConnection();
		} 
		catch (Exception e) {
			_logger.error("error initializing periodic data sender", e);
		}
	}

	private void setupConnection(){
		_rabbitMqConsumer.SetupConsumption(_messageProducer, config);
		BrokerHelper.currentclient = _rabbitMqConsumer;
		_messageProducer.Start();
		_messageConsumer.Start();
	}

	@Override
	public void stop()  {
		try {
			if (_rabbitMqConsumer != null) _rabbitMqConsumer.Stop();
			BrokerHelper.currentclient = null;
			if (_messageConsumer != null) _messageConsumer.Stop();
			if (_messageProducer != null) _messageProducer.Stop();
			_executor.shutdown();
		}
		catch(Exception e){
			_logger.error("Error stopping", e);
		}
	}

	@Override
	public void messageReceived(String topic, String message){
		try {
			Message takMessage = null;
			//Eventually the topic will be specific towards a message type (entity vs alert)
			//For now we will look for 'cot_uid' which is a key in an alert message
			if(message.contains("cot_uid")) { 
				EventDto event = McsCoTConverter.convertToEvent(message, config);
				if (event == null){
					_logger.error("error converting message to event");
					return;
				}

				//TODO - park it in prefs
				String targetAddress = "127.0.0.1";
            	int targetPort = 8080;
				TakServerCoTApi.queryForCotEvent(targetAddress, targetPort, event.getUid(), 
					new TakServerCoTCallback(){
						@Override
						public void cotResult(Boolean success, String cot) {
							try {
								Message eventMessage = null;
								if (success) {
									final Message parentMessage = getConverter().cotStringToDataMessage(cot, null, Integer.toString(System.identityHashCode(this)));
									eventMessage = McsCoTConverter.convertToLinkedMessage(event, parentMessage.getPayload().getCotEvent(), config);
								}
								else
									eventMessage = McsCoTConverter.convertToMessage(event, config);
	
								send(eventMessage);
							}
							catch (Exception exception) {
								_logger.error("error converting to linked message", exception);
							}
						}
					});
			}
			else {
				EntityDto entity = McsCoTConverter.convertToEntity(message, config);
				if (entity == null){
					_logger.error("error converting message to event");
					return;
				}

				takMessage = McsCoTConverter.convertToMessage(entity, config);
				send(takMessage);
			}

			if (takMessage == null){
				_logger.error("error converting event to protobuf message");
				return;
			}

			if(VerboseLogging)
				_logger.info("TAK message converted: " + takMessage);
				
		} catch (Exception exception) {
			_logger.error("error converting message ", message, exception);
		}
	}
}
