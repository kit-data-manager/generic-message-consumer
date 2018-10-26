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
package hello;

/**
 *
 * @author jejkal
 */
import edu.kit.datamanager.messaging.client.configuration.ConsumerBinding;
import java.util.Arrays;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class Runner implements CommandLineRunner{

  private final RabbitTemplate rabbitTemplate;
  private final Receiver receiver;
  private final ConfigurableApplicationContext context;

  @Autowired
  private ConsumerBinding[] consumerBindings;

  public Runner(Receiver receiver, RabbitTemplate rabbitTemplate,
          ConfigurableApplicationContext context){
    this.receiver = receiver;
    this.rabbitTemplate = rabbitTemplate;
    this.context = context;
  }

  @Override
  public void run(String... args) throws Exception{
    System.out.println("BINDING " + Arrays.asList(consumerBindings));
    System.out.println("Receiving message...");
    System.out.println(rabbitTemplate.receive("myqueue2", 100000));
    //receiver.getLatch().await(100000, TimeUnit.MILLISECONDS);
    context.close();
  }

}
