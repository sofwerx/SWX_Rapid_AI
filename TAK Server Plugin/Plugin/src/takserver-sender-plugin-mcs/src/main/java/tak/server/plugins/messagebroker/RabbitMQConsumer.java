package tak.server.plugins.messagebroker;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tak.server.plugins.processing.*;
import tak.server.plugins.PluginConfiguration;
import tak.server.plugins.McsSenderPlugin;

public class RabbitMQConsumer {
    private MessageProducer _producer;
    private String _exchangeName = "dragonfly";
    private String _routingKey = "dragonfly.*";
    private String _rabbitHost = "some-rabbit";
    private String _password = "some-password";
    private String _username = "some-username";
    private boolean _useRapidX = false;
    
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQConsumer.class);
    private Channel _channel;
    private String _consumerTag = "";
    
    public void SetupConsumption(MessageProducer producer, PluginConfiguration config) {
        logger.info("Connecting to rabbitMQ");

        SetupConfiguration(config);

        try {
            _producer = producer;
            ConnectionFactory factory = new ConnectionFactory();
            logger.info("Use RapidX settings" + ":" + _useRapidX);
            if (_useRapidX) {
                factory.setUsername(_username); 
                factory.setPassword(_password);
                factory.setHost(_rabbitHost);
            }
            else {
                factory.setHost(_rabbitHost);
            }

            Connection connection = factory.newConnection();
            _channel = connection.createChannel();
    
            _channel.exchangeDeclare(_exchangeName, "topic", true);
            String queueName = _channel.queueDeclare().getQueue();
            _channel.queueBind(queueName, _exchangeName, _routingKey);


            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                if (McsSenderPlugin.VerboseLogging)
                    logger.info("Msg Received '" + delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
                _producer.AddMessage(message);
            };

            _consumerTag = _channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
            logger.info("RabbitMQ consumer setup : " + _consumerTag);
        }
        catch (Exception e) 
		{
			logger.error("error initializing rabbitMQ", e);
		}
    }

    public void Stop() throws Exception {
        if (_channel != null) _channel.basicCancel(_consumerTag);
    }

    private void SetupConfiguration(PluginConfiguration configuration) {
        logger.info("Reading configuration");

        if (configuration.containsProperty("rabbitmq.exchange_name")) {
            _exchangeName = (String)configuration.getProperty("rabbitmq.exchange_name");
        }

        if (configuration.containsProperty("rabbitmq.routing_key")) {
            _routingKey = (String)configuration.getProperty("rabbitmq.routing_key");
        }

        if (configuration.containsProperty("rabbitmq.hostname")) {
            _rabbitHost = (String)configuration.getProperty("rabbitmq.hostname");
        }

        if (configuration.containsProperty("rabbitmq.username")) {
            _username = (String)configuration.getProperty("rabbitmq.username");
        }

        if (configuration.containsProperty("rabbitmq.password")) {
            _password = (String)configuration.getProperty("rabbitmq.password");
        }

        if (configuration.containsProperty("useRapidX")) {
            _useRapidX = (boolean)configuration.getProperty("useRapidX");
        }
    }
}
