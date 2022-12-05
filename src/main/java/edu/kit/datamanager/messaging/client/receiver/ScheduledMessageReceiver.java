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
package edu.kit.datamanager.messaging.client.receiver;

import edu.kit.datamanager.configuration.RabbitMQConfiguration;
import edu.kit.datamanager.entities.messaging.BasicMessage;
import edu.kit.datamanager.messaging.client.configuration.RabbitMQConsumerConfiguration;
import edu.kit.datamanager.messaging.client.handler.IMessageHandler;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 *
 * @author jejkal
 */
@Component
public class ScheduledMessageReceiver {

  private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledMessageReceiver.class);

  private final RabbitTemplate rabbitTemplate;

  private RabbitMQConfiguration configuration;

  private IMessageHandler[] messageHandlers;
  private final List<IMessageHandler> endorsedHandlers = new ArrayList<>();
  private boolean INITIALIZED = false;
  private boolean MESSAGING_DISABLED_WARNING_EMITTED = false;
  private boolean NO_HANDLER_WARNING_EMITTED = false;

  @Autowired
  public ScheduledMessageReceiver(Optional<IMessageHandler[]> messageHandlers, RabbitTemplate rabbitTemplate, RabbitMQConfiguration configuration) {
    this.rabbitTemplate = rabbitTemplate;
    this.configuration = configuration;
    if (messageHandlers.isPresent()) {
      this.messageHandlers = messageHandlers.get();
    } else {
      this.messageHandlers = null;
    }
  }

  @Autowired
  private RabbitMQConsumerConfiguration config;

  @Scheduled(fixedRateString = "${repo.schedule.rate}")
  public void receiveNextMessage() {
    if (!configuration.isMessagingEnabled()) {
      if (!MESSAGING_DISABLED_WARNING_EMITTED) {
        LOGGER.warn("No messaging  handlers registered. Skip receiving all messages.");
        MESSAGING_DISABLED_WARNING_EMITTED = true;
      }
      return;
    }
    if (messageHandlers == null) {
      if (!NO_HANDLER_WARNING_EMITTED) {
        LOGGER.warn("No message handlers registered. Skip receiving all messages.");
        NO_HANDLER_WARNING_EMITTED = true;
      }
      return;
    }

    if (!INITIALIZED) {
      //if not initialized, check all handlers for endorsement
      //this is done before handling the first message as at this point, the repository is running in any case, also if the receiver is part of the repository
      //this allows the handler to check for the repository
      for (IMessageHandler handler : messageHandlers) {
        LOGGER.trace("Trying to configure handler {}.", handler.getHandlerIdentifier());
        if (handler.configure()) {
          LOGGER.trace("Adding handler {} to list of endorsed handlers.", handler.getHandlerIdentifier());
          endorsedHandlers.add(handler);
        } else {
          LOGGER.warn("Dropping handler {} due to misconfiguration.", handler.getHandlerIdentifier());
        }
      }
      INITIALIZED = true;
    }
    Message msg = null;
    // Receive messages until there is no further message available.
    do {
      LOGGER.trace("Performing receiveNextMessage() from queue {}.", config.queue().getName());
      msg = rabbitTemplate.receive(config.queue().getName(), 1000);

      if (msg != null) {
        try {
          BasicMessage message = BasicMessage.fromJson(new String(msg.getBody()));
          LOGGER.trace("Processing received message using {} registered handler(s).", messageHandlers.length);
          boolean messageHandledByOne = false;
          for (IMessageHandler handler : endorsedHandlers) {
            LOGGER.trace("Processing message by handler {}.", handler.getClass());
            IMessageHandler.RESULT result = handler.handle(message);

            switch (result) {
              case SUCCEEDED:
                LOGGER.trace("Message {} has been successfully processed by handler {}.", message, handler.getHandlerIdentifier());
                messageHandledByOne = true;
                break;
              case FAILED:
                LOGGER.trace("Processing message {} by handler {} has been failed.", message, handler.getHandlerIdentifier());
                if (preserveUnhandledMessage(handler.getHandlerIdentifier(), message)) {
                  //preservation done, message handled and must not be preserved at the end again
                  messageHandledByOne = true;
                }
                break;
              case REJECTED:
                LOGGER.trace("Message {} has been rejected by handler {}.", message, handler.getHandlerIdentifier());
            }
          }

          if (!messageHandledByOne) {
            LOGGER.debug("Message {} has been rejected by all configured handlers. Message will be discarded.", message);
          }
        } catch (IOException ex) {
          LOGGER.error("Failed to deserialize message " + msg + ".", ex);
        }
      }
    } while (msg != null);
  }

  /**
   * Preserve messages that could not be processed by a certain handler. The
   * messages are written into a textfile in the current folder and can be
   * processed later separately.
   *
   * @param handlerIdentifier The handler which failed to process the message.
   * @param message The message failed to be processed.
   *
   * @return TRUE if the message could be preserved (and can be assumed as
   * 'handled' in some sense), FALSE otherwise.
   */
  private boolean preserveUnhandledMessage(String handlerIdentifier, BasicMessage message) {
    boolean result = false;
    LOGGER.warn("Preserving message {} for handler {}. Writing new entry to failed_message_handles.csv.", message, handlerIdentifier);
    String entry = handlerIdentifier + ", " + message + "\n";
    try {
      Path logfile = Paths.get("failed_message_handles.csv");
      if (!Files.exists(logfile)) {
        Files.write(logfile, "message handler, message\n".getBytes(), StandardOpenOption.CREATE);
      }

      Files.write(logfile, entry.getBytes(), StandardOpenOption.APPEND);
      result = true;
    } catch (IOException ex) {
      LOGGER.error("Failed to write entry " + entry + " to message error log 'failed_message_handles.csv'.", ex);
    }
    return result;
  }
}
