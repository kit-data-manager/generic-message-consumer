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

import java.util.Arrays;
import java.util.List;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 *
 * @author jejkal
 */
@SpringBootApplication
public class Application{

  @Bean
  public ConnectionFactory connectionFactory(){
    return new CachingConnectionFactory("localhost");
  }

  @Bean
  public AmqpAdmin amqpAdmin(){
    return new RabbitAdmin(connectionFactory());
  }

  @Bean
  public RabbitTemplate rabbitTemplate(){
    return new RabbitTemplate(connectionFactory());
  }

  @Bean
  public Queue myQueue(){
    return new Queue("myqueue2");
  }

  @Bean
  TopicExchange exchange(){
    return new TopicExchange("repository_events");
  }

  @Bean
  List<Binding> bindings(Queue queue, TopicExchange exchange){
    return Arrays.asList(BindingBuilder.bind(queue).to(exchange).with("dataresource.update.#"));
  }

  public static void main(String[] args) throws InterruptedException{
    SpringApplication.run(Application.class, args);
    //How to launch
//   java \
//-cp fat_app.jar \
//-Dloader.path=<path_to_your_additional_jars> \
//org.springframework.boot.loader.PropertiesLauncher
  }

}
