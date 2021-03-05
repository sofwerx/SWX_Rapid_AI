package tak.server.plugins.processing;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

import tak.server.plugins.McsSenderPlugin;
import tak.server.plugins.interfaces.*;

public class MessageConsumer {
    private ExecutorService _executor;
    private BlockingQueue<String> _queue;
    private static final Logger _logger = LoggerFactory.getLogger(MessageConsumer.class);
    private MessageCallback _callback;
    private AtomicBoolean _running = new AtomicBoolean(false);

    public MessageConsumer(ExecutorService executorService, BlockingQueue<String> queue, MessageCallback callback) {
        _executor = executorService;
        _queue = queue;
        _callback = callback;
    }

    public boolean isRunning() {
        return _running.get();
    }

    public void Start(){
        _logger.info("Starting MessageConsumer");
        _running.set(true);
        Runnable consumerTask = () -> {
            try {
                while(_running.get()) {
                    String message = _queue.take();
                    if (McsSenderPlugin.VerboseLogging)
                        _logger.info("message received in consumer sending to callback");
                    _callback.messageReceived(message);
                }
            } 
            catch (InterruptedException e) {
                _logger.info("thread interrupted", e);
            }
            catch (Exception e) {
                _logger.error("error taking message from queue", e);
            }
        };

        _executor.execute(consumerTask);
    }

    public void Stop(){
        _logger.info("Stopping MessageConsumer");
        _running.set(false);
    }

}
