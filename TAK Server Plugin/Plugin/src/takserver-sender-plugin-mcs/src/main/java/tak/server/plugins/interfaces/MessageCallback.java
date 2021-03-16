package tak.server.plugins.interfaces;

public interface MessageCallback {
    void messageReceived(String topic, String message);
}
