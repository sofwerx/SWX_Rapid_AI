package tak.server.plugins.processing;

public class ProcessingMessage {
    public String topic;
    public String payload;

    public ProcessingMessage(String topicValue, String payloadValue) {
        topic = topicValue;
        payload = payloadValue;
    }
}
