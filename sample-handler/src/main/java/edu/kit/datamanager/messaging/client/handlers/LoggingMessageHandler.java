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
package edu.kit.datamanager.messaging.client.handlers;

import edu.kit.datamanager.entities.messaging.BasicMessage;
import edu.kit.datamanager.messaging.client.handler.IMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 * @author jejkal
 */
@Component
public class LoggingMessageHandler implements IMessageHandler{

  private static final Logger LOGGER = LoggerFactory.getLogger(LoggingMessageHandler.class);

  @Override
  public RESULT handle(BasicMessage message){
    LOGGER.debug("Successfully received message {}.", message);
    //Reject message in order to preserve message if another, misconfigured handler is used
    //If RESULT.SUCCEEDED is returned, the message is assumed to be processed properly.
    return RESULT.REJECTED;
  }

  @Override
  public boolean configure(){
    //no configuration necessary
    return true;
  }

}
