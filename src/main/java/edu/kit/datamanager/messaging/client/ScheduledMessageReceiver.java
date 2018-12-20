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
package edu.kit.datamanager.messaging.client;

import edu.kit.datamanager.entities.messaging.BasicMessage;
import edu.kit.datamanager.messaging.client.configuration.RabbitMQConsumerConfiguration;
import edu.kit.datamanager.messaging.client.handler.IMessageHandler;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 *
 * @author jejkal
 */
@Component
public class ScheduledMessageReceiver{

  private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledMessageReceiver.class);

  private final RabbitTemplate rabbitTemplate;

  @Autowired(required = false)
  private IMessageHandler[] messageHandlers;

  public ScheduledMessageReceiver(RabbitTemplate rabbitTemplate){
    this.rabbitTemplate = rabbitTemplate;
  }

  @Autowired
  private RabbitMQConsumerConfiguration config;

  @Scheduled(fixedRateString = "${repo.schedule.rate}")
  public void receiveNextMessage(){
    if(messageHandlers == null){
      LOGGER.info("No message handlers registered. Skip receiving any messages.");
      return;
    }
    LOGGER.trace("Performing receiveNextMessage().");
    Message msg = rabbitTemplate.receive(config.queue().getName(), 1000);
    if(msg != null){
      try{
        BasicMessage message = BasicMessage.fromJson(new String(msg.getBody()));
        LOGGER.trace("Processing received message using {} registered handler(s).", messageHandlers.length);
        for(IMessageHandler handler : messageHandlers){
          LOGGER.trace("Processing message by handler {}.", handler.getClass());
          handler.handle(message);
        }
      } catch(IOException ex){
        LOGGER.error("Failed to process message " + msg + ".", ex);
      }
    }
  }
}
