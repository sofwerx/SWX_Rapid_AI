package tak.server.plugins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import atakmap.commoncommo.protobuf.v1.MessageOuterClass.Message;
import tak.server.plugins.dto.EntityDto;
import tak.server.plugins.utilities.CoTMcsConverter;

@TakServerPlugin(name = "MCS COP Receiver Plugin", description = "TAK Server plugin that consumes TAK CoT messages, converts them to MCS COP messages, and sends them to the MCS COP Message Broker")
public class McsLoggerReceiverPlugin extends MessageReceiverBase {

	private static final Logger _logger = LoggerFactory.getLogger(McsLoggerReceiverPlugin.class);

	private Boolean _verboseLogging = false;

	public McsLoggerReceiverPlugin() throws ReservedConfigurationException {
		_logger.info("create " + getClass().getName());
		if (config.containsProperty("verboseLogging")){
			_verboseLogging = (boolean)config.getProperty("verboseLogging");
		}
		
		_logger.info("logging = " + _verboseLogging.toString());
	}

	@Override
	public void start() {
		_logger.info(getClass().getName() + " started");
	}

	@Override
	public void onMessage(Message message) {
		if (_verboseLogging)
		_logger.info("plugin message received: " + message);

		if(CoTMcsConverter.messageFromSender(message))
			_logger.info("Message is from TAK Plugin");

		EntityDto EntityDto = CoTMcsConverter.convertToEntityDto(message);
		String json = CoTMcsConverter.convertToJson(EntityDto);
		_logger.info(json);
	}
}