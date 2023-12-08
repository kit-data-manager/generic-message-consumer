/*
 * Copyright 2018 Karlsruhe Institute of Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.datamanager.messaging.client.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ consumer configuration bean. The configuration allows to configure
 * the RabbitMQ server hostname as well as the binding used to connect to a
 * certain exchange. Furthermore, this bean contains all messaging-related
 * beans, e.g. {@link #rabbitTemplate()}, {@link #queue()} and {@link #exchange()
 * }. The configuration is assumed to be located below repo.messaging
 *
 * @author jejkal
 */
@Configuration
@ConfigurationProperties(prefix = "repo.messaging")
@Data
public class RabbitMQConsumerConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQConsumerConfiguration.class);

    /**
     * The hostname of the RabbitMQ server. (default: localhost)
     */
    @Value("${repo.messaging.hostname:localhost}")
    private String hostname;
    /**
     * The port of the RabbitMQ server. (default: 5672)
     */
    @Value("${repo.messaging.port:5672}")
    private int port;
    /**
     * The username to connect of the RabbitMQ server. (default: guest)
     */
    @Value("${repo.messaging.username:guest}")
    private String username;
    /**
     * The password to connect of the RabbitMQ server. (default: guest)
     */
    @Value("${repo.messaging.password:guest}")
    private String password;

    /**
     * The consumer binding used to connect to a certain exchange, establishing
     * a queue and linking both by one or more routing keys.
     */
    private Optional<ConsumerBinding> receiver;

    @Bean
    public ConnectionFactory rabbitMQConnectionFactory() {
        LOGGER.trace("Connecting to RabbitMQ service at host {} and port {}.", hostname, port);
        CachingConnectionFactory factory = new CachingConnectionFactory(hostname, port);
        factory.setUsername(username);
        factory.setPassword(password);
        return factory;
    }

    @Bean
    public RabbitTemplate rabbitMQTemplate() {
        LOGGER.trace("Get RabbitMQ template");
        return new RabbitTemplate(rabbitMQConnectionFactory());
    }

    @Bean
    public Queue queue() {
        if (receiver != null && receiver.isPresent()) {
            return new Queue(receiver.get().getQueue());
        }
        return null;
    }

    @Bean
    public TopicExchange exchange() {
        if (receiver != null && receiver.isPresent()) {
            return new TopicExchange(receiver.get().getExchange());
        }
        return null;
    }

    @Bean
    public Declarables topicBindings() {
        if (receiver != null && receiver.isPresent()) {
            LOGGER.trace("Configuring exchange {} with queue {}.", exchange(), queue());
            List<Binding> amqpBindings = new ArrayList<>();

            Declarables declarables = new Declarables();
            LOGGER.trace("Adding queue {} to list of declarables.", queue());
            declarables.getDeclarables().add(queue());
            LOGGER.trace("Adding exchange {} to list of declarables.", exchange());
            declarables.getDeclarables().add(exchange());
            for (String routingKey : receiver.get().getRoutingKeys()) {
                LOGGER.trace("Adding binding via routing key {} to declarables.", routingKey);
                amqpBindings.add(
                        BindingBuilder
                                .bind(queue())
                                .to(exchange())
                                .with(routingKey)
                );
            }
            declarables.getDeclarables().addAll(amqpBindings);
            return declarables;
        }
        return null;
    }
}
