package tak.server.plugins.messagebroker;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tak.server.plugins.processing.*;
import tak.server.plugins.utilities.McsCoTConverter;
import tak.server.plugins.PluginConfiguration;
import tak.server.plugins.McsSenderPlugin;

public class RabbitMQClient {
    private MessageProducer _producer;
    private String _exchangeName = "dragonfly";
    private Boolean _enableEntityRouting = true;
    private Boolean _enableEventRouting = true;
    private String _entityRoutingKey = "dragonfly.demo_entities";
    private String _eventRoutingKey = "dragonfly.events";
    private String _rabbitHost = "some-rabbit";
    private String _password = "some-password";
    private String _username = "some-username";
    private boolean _useRapidX = false;
    
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQClient.class);
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

            if (_enableEventRouting == true)
                _channel.queueBind(queueName, _exchangeName, _eventRoutingKey);
            else
                logger.info("Event Routing disabled");
           
            if (_enableEntityRouting == true)
                _channel.queueBind(queueName, _exchangeName, _entityRoutingKey);
            else
                logger.info("Entity Routing disabled");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                String topic = delivery.getEnvelope().getRoutingKey(); 
                if (McsSenderPlugin.VerboseLogging)
                    logger.info("Msg Received '" + topic + "':'" + message + "'");
                
                if (McsCoTConverter.messageIsFromPlugin(message))
                    return;
                    
                _producer.AddMessage(topic, message);
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

    public Boolean isEntityKey(String key)
    {
        return key.equals(_entityRoutingKey);
    }

    public Boolean isEventKey(String key)
    {
        return key.equals(_eventRoutingKey);
    }

    public void publishEntityMessage(String message)
    {
        if (!_channel.isOpen()) return;

        try {
            if (McsSenderPlugin.VerboseLogging)
            logger.info("Publishing to " + _entityRoutingKey + " message: " + message);

            byte[] entityBytes = message.getBytes(StandardCharsets.UTF_8);
            _channel.basicPublish(_exchangeName, _entityRoutingKey, null, entityBytes);
        }
        catch (Exception e) 
		{
			logger.error("error publishing to rabbitMQ", e);
		}
    }

    private void SetupConfiguration(PluginConfiguration configuration) {
        logger.info("Reading configuration");

        if (configuration.containsProperty("rabbitmq.exchange_name")) {
            _exchangeName = (String)configuration.getProperty("rabbitmq.exchange_name");
        }

        if (configuration.containsProperty("rabbitmq.entityKey")) {
            _entityRoutingKey = (String)configuration.getProperty("rabbitmq.entityKey");
        }

        if (configuration.containsProperty("rabbitmq.eventKey")) {
            _eventRoutingKey = (String)configuration.getProperty("rabbitmq.eventKey");
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

        if (configuration.containsProperty("rabbitmq.enableEntityRouting")) {
            _enableEntityRouting = (boolean)configuration.getProperty("rabbitmq.enableEntityRouting");
        }

        if (configuration.containsProperty("rabbitmq.enableEventRouting")) {
            _enableEventRouting = (boolean)configuration.getProperty("rabbitmq.enableEventRouting");
        }
    }
}
