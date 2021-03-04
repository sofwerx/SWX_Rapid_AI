package tak.server.plugins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import atakmap.commoncommo.protobuf.v1.MessageOuterClass.Message;

@TakServerPlugin(name = "MCS COP Receiver Plugin", description = "TAK Server plugin that consumes TAK CoT messages, converts them to MCS COP messages, and sends them to the MCS COP Message Broker")
public class McsLoggerReceiverPlugin extends MessageReceiverBase {

	private static final Logger logger = LoggerFactory.getLogger(McsLoggerReceiverPlugin.class);

	public McsLoggerReceiverPlugin() throws ReservedConfigurationException {
		logger.info("create " + getClass().getName());
	}

	@Override
	public void start() {
		logger.info(getClass().getName() + " started");
	}

	@Override
	public void onMessage(Message message) {

		logger.info("plugin message received: " + message);
	}
}