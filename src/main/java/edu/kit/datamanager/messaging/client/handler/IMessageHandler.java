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
package edu.kit.datamanager.messaging.client.handler;

import edu.kit.datamanager.entities.messaging.BasicMessage;

/**
 *
 * @author jejkal
 */
public interface IMessageHandler{

  public enum RESULT{
    //a message has been handled successfulle
    SUCCEEDED,
    //the message could not be handled properly
    FAILED,
    //the handler is not responsible for the provided type of message
    REJECTED;
  }

  /**
   * Returns a (unique) identifier of this handler. By default, it's the
   * classname.
   *
   * @return The handler identifier.
   */
  default String getHandlerIdentifier(){
    return this.getClass().getSimpleName();
  }

  /**
   * Configure the message handler. It's up to the implementation to choose a
   * configuration source, e.g. a properties file. If the configuration was
   * successful, TRUE should be returned indicating, that the handler is usable.
   * Otherwise, FALSE should be returned in order to disable the handler to
   * avoid dropping events by misconfigured handlers.
   *
   * @return TRUE if the handler is operational after configuration, FALSE
   * otherwise.
   */
  boolean configure();

  /**
   * Handle a single message received from the message queue. Which messages are
   * received in configured by the receiver. As a receiver may accept more
   * messages than a certain handler, a handler may return RESULT.REJECTED to
   * decline processing the message. If a message was successfully processed,
   * RESULT.SUCCEEDED should be returned, otherwise RESULT.FAILED is returned.
   *
   * @param message The received message.
   *
   * @return The handler result.
   */
  RESULT handle(BasicMessage message);

}
