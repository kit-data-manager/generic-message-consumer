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

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


/**
 * Consumer binding used to configure queue and routing keys used to receive
 * messages from a topic exchange. All configuration keys are assumed to be
 * located below repo.messaging.binding
 *
 * @author jejkal
 */
@Component
@Data
public class ConsumerBinding{

  /**
   * The topic exchange to connect to.
   */
  @Value("${repo.messaging.topic:repository_events}")
  private String exchange;
  /**
   * The queue name used to receive messages from the configured exchange.
   */
  private String queue;
  /**
   * A list of routing keys qualifying a message to be queued in the configured
   * queue.
   */
  private String[] routingKeys;

}
