package tak.server.plugins;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import atakmap.commoncommo.protobuf.v1.MessageOuterClass.Message;
import tak.server.plugins.dto.EntityDto;
import tak.server.plugins.interfaces.PublishCallback;
import tak.server.plugins.takprocessing.TakCoTConsumer;
import tak.server.plugins.takprocessing.TakCoTProducer;
import tak.server.plugins.utilities.BrokerHelper;
import tak.server.plugins.utilities.CoTMcsConverter;

@TakServerPlugin(name = "MCS COP Receiver Plugin", description = "TAK Server plugin that consumes TAK CoT messages, converts them to MCS COP messages, and sends them to the MCS COP Message Broker")
public class McsLoggerReceiverPlugin extends MessageReceiverBase implements PublishCallback {

	private static final Logger _logger = LoggerFactory.getLogger(McsLoggerReceiverPlugin.class);

	public static Boolean VerboseLogging = false;
	private static int _queueSize = 10;

	private TakCoTProducer _messageProducer;
	private TakCoTConsumer _messageConsumer;
	private BlockingQueue<Message> _blockingQueue; 
    private ExecutorService _executor = Executors.newFixedThreadPool(2);

	public McsLoggerReceiverPlugin() throws ReservedConfigurationException {
		_logger.info("create " + getClass().getName());
		if (config.containsProperty("verboseLogging")){
			VerboseLogging = (boolean)config.getProperty("verboseLogging");
		}
		
		_logger.info("logging = " + VerboseLogging.toString());

		_blockingQueue = new LinkedBlockingDeque<>(_queueSize);
		_messageProducer = new TakCoTProducer(_executor, _blockingQueue);
		_messageConsumer = new TakCoTConsumer(_executor, _blockingQueue, this);
	}

	@Override
	public void start() {
		_logger.info(getClass().getName() + " started");
		_logger.info("Configuration Properties: " + config.getProperties());
		_messageProducer.Start();
		_messageConsumer.Start();
	}

	@Override
	public void stop() {
		try {
			if (_messageConsumer != null) _messageConsumer.Stop();
			if (_messageProducer != null) _messageProducer.Stop();
			_executor.shutdown();
		}
		catch(Exception e){
			_logger.error("Error stopping", e);
		}
	}

	@Override
	public void onMessage(Message message) {
		if (VerboseLogging)
		_logger.info("plugin message received: " + message);

		if(CoTMcsConverter.messageFromSender(message)) {
			if (VerboseLogging)
				_logger.info("Message is from TAK Plugin");
				//Bail - TODO it would be nice maybe to check flowtags or something else
			return;
		}
		
		_messageProducer.AddMessage(message);
	}

	@Override
	public void publishEntityMessage(String message) {
		if (BrokerHelper.currentclient == null) return;
		BrokerHelper.currentclient.publishEntityMessage(message);
	}
}