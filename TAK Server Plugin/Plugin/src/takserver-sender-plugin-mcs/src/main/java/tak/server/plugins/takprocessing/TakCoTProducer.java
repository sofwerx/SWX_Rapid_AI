package tak.server.plugins.takprocessing;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import atakmap.commoncommo.protobuf.v1.MessageOuterClass.Message;

public class TakCoTProducer {
    private ExecutorService _executor;
    private BlockingQueue<Message> _queue;
    private static final Logger _logger = LoggerFactory.getLogger(TakCoTProducer.class);
    
    public TakCoTProducer(ExecutorService executorService, BlockingQueue<Message> queue) {
        _executor = executorService;
        _queue = queue;
    }

    public void Start(){
        //Notional
        _logger.info("Starting TakCoTProducer");
    }

    public void Stop(){
        //Notional
        _logger.info("Stopping TakCoTProducer");
    }

    public void AddMessage(Message message) {
        Runnable producerTask = () -> {
            try {
                _queue.put(message);
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
