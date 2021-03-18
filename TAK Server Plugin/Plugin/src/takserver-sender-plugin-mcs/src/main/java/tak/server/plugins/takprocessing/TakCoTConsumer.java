package tak.server.plugins.takprocessing;

import tak.server.plugins.McsLoggerReceiverPlugin;
import tak.server.plugins.dto.EntityDto;
import tak.server.plugins.interfaces.PublishCallback;
import tak.server.plugins.utilities.CoTMcsConverter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

import atakmap.commoncommo.protobuf.v1.MessageOuterClass.Message;

public class TakCoTConsumer {
    private ExecutorService _executor;
    private BlockingQueue<Message> _queue;
    private static final Logger _logger = LoggerFactory.getLogger(TakCoTConsumer.class);
    private PublishCallback _callback;
    private AtomicBoolean _running = new AtomicBoolean(false);

    public TakCoTConsumer(ExecutorService executorService, BlockingQueue<Message> queue, PublishCallback callback) {
        _executor = executorService;
        _queue = queue;
        _callback = callback;
    }

    public boolean isRunning() {
        return _running.get();
    }

    public void Start(){
        _logger.info("Starting TakCoTConsumer");
        _running.set(true);
        Runnable consumerTask = () -> {
            try {
                while(_running.get()) {
                    Message cotMessage = _queue.take();
                    if (McsLoggerReceiverPlugin.VerboseLogging)
                        _logger.info("CoT message received in consumer converting to Entity and sending to publish");

                        try {
                            //TODO - Rework so that nested Try catches don't have to exist
                            EntityDto entityDto = CoTMcsConverter.convertToEntityDto(cotMessage);
                            if (entityDto == null) continue;
                            String json = CoTMcsConverter.convertToJson(entityDto);
                            _callback.publishEntityMessage(json);
                        }
                        catch (Exception e) {
                            _logger.error("error converting message from queue", e);
                        }
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
        _logger.info("Stopping TakCoTConsumer");
        _running.set(false);
    }
}
