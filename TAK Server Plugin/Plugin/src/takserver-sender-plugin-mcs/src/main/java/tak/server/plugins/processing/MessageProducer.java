package tak.server.plugins.processing;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageProducer {
    
    private ExecutorService _executor;
    private BlockingQueue<ProcessingMessage> _queue;
    private static final Logger _logger = LoggerFactory.getLogger(MessageProducer.class);
    
    public MessageProducer(ExecutorService executorService, BlockingQueue<ProcessingMessage> queue) {
        _executor = executorService;
        _queue = queue;
    }

    public void Start(){
        //Notional
        _logger.info("Starting MessageProducer");
    }

    public void Stop(){
        //Notional
        _logger.info("Stopping MessageProducer");
    }

    public void AddMessage(String topic, String message) {
        Runnable producerTask = () -> {
            try {
                _queue.put(new ProcessingMessage(topic, message));
            }
            catch (InterruptedException e) {
                _logger.info("thread interrupted", e);
            }
            catch (Exception e) {
                _logger.error("error putting message on queue", e);
            }
        };

        _executor.submit(producerTask);
    }
}
