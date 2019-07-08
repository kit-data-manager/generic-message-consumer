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

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.kit.datamanager.clients.MultiResourceAccessClient;
import edu.kit.datamanager.clients.SimpleRepositoryClient;
import edu.kit.datamanager.clients.SingleResourceAccessClient;
import edu.kit.datamanager.entities.messaging.DataResourceMessage;
import edu.kit.datamanager.entities.repo.ContentInformation;
import edu.kit.datamanager.entities.repo.DataResource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 *
 * @author jejkal
 */
@SpringBootApplication
//@ComponentScan({"edu.kit.datamanager.messaging.client"})
//@EnableScheduling
public class Application implements ApplicationRunner{

  private static Logger LOG = LoggerFactory.getLogger(Application.class);

  @Autowired
  private RabbitTemplate rabbitTemplate;

  public static void main(String[] args) throws InterruptedException{
    SpringApplication.run(Application.class, args);
  }

  @Override
  public void run(ApplicationArguments args) throws Exception{
    if(!args.getOptionNames().contains("action")){
      System.err.print("Argument --action is missing. Must be one of 'create', 'update', 'fix', 'revoke' or 'delete'.");
      System.exit(1);
    }

    DataResourceMessage.ACTION action = null;
    try{
      action = DataResourceMessage.ACTION.valueOf(args.getOptionValues("action").get(0).toUpperCase());
    } catch(IllegalArgumentException ex){
      LOG.error("Invalid action provided. Value is " + args.getOptionValues("action") + ", allowed values are " + Arrays.asList(DataResourceMessage.ACTION.values()));
      System.exit(1);
    }

    DataResourceMessage.SUB_CATEGORY subcategory = null;
    if(args.getOptionNames().contains("subCategory")){
      try{
        subcategory = DataResourceMessage.SUB_CATEGORY.valueOf(args.getOptionValues("subCategory").get(0).toUpperCase());
      } catch(IllegalArgumentException ex){
        LOG.error("Invalid subCategory provided. Value is " + args.getOptionValues("subCategory") + ", allowed values are " + Arrays.asList(DataResourceMessage.SUB_CATEGORY.values()));
        System.exit(1);
      }
    }

    String baseUrl = "http://localhost:8090/api/v1/dataresources/";
    if(args.getOptionNames().contains("baseUrl")){
      baseUrl = args.getOptionValues("baseUrl").get(0);
    }

    List<String> resourceIds = new ArrayList<>();
    if(args.getOptionNames().contains("resourceId")){
      resourceIds.add(args.getOptionValues("resourceId").get(0));
    }

    if(resourceIds.isEmpty()){
      //no resource ids provided, obtain all resource ids
      int page = 0;
      MultiResourceAccessClient client = SimpleRepositoryClient.createClient(baseUrl).elementsPerPage(100).withPage(page);

      DataResource[] resources = client.getResources();

      while(resources.length > 0){
        for(DataResource resource : resources){
          resourceIds.add(resource.getId());
        }
        page++;
        resources = client.withPage(page).getResources();
      }
    }

    List<String> addressees = (args.getOptionNames().contains("addressees")) ? args.getOptionValues("addressees") : new ArrayList();

    for(String resourceId : resourceIds){
      SingleResourceAccessClient client = SimpleRepositoryClient.createClient(baseUrl).withResourceId(resourceId);

      if(subcategory != null && DataResourceMessage.SUB_CATEGORY.DATA.equals(subcategory)){
        //data message...check contentPath
        Map<String, Map<String, String>> contentPaths = new HashMap<>();
        if(args.getOptionNames().contains("contentPath")){
          for(String contentPath : args.getOptionValues("contentPath")){
            ContentInformation[] info = client.getContentInformation(contentPath);
            if(info == null || info.length == 0){
              LOG.error("No content information found for content path {}.", contentPath);
              System.exit(1);
            } else{
              Map<String, String> contentInfoMap = new HashMap<>();
              contentInfoMap.put(DataResourceMessage.CONTENT_PATH_PROPERTY, contentPath);
              contentInfoMap.put(DataResourceMessage.CONTENT_URI_PROPERTY, info[0].getContentUri());
              contentInfoMap.put(DataResourceMessage.CONTENT_TYPE_PROPERTY, info[0].getMediaType());
              contentPaths.put(contentPath, contentInfoMap);
            }
          }
        } else{
          //obtain all content information, content uri and type
          ContentInformation[] infos = client.getContentInformation("/");
          for(ContentInformation info : infos){
            Map<String, String> contentInfoMap = new HashMap<>();
            contentInfoMap.put(DataResourceMessage.CONTENT_PATH_PROPERTY, info.getRelativePath());
            contentInfoMap.put(DataResourceMessage.CONTENT_URI_PROPERTY, info.getContentUri());
            contentInfoMap.put(DataResourceMessage.CONTENT_TYPE_PROPERTY, info.getMediaType());
            contentPaths.put(info.getRelativePath(), contentInfoMap);
          }
        }

        Set<String> contentKeys = contentPaths.keySet();

        for(String contentPath : contentKeys){
          Map<String, String> map = contentPaths.get(contentPath);
          sendDataMessage(resourceId, action, map, addressees);
        }
      } else{
        sendMetadataMessage(resourceId, action, addressees);
      }
    }

    System.exit(0);
  }

  private void sendDataMessage(String resourceId, DataResourceMessage.ACTION action, Map<String, String> properties, List<String> addressees) throws Exception{
    DataResourceMessage msg = DataResourceMessage.createSubCategoryMessage(resourceId, action, DataResourceMessage.SUB_CATEGORY.DATA, properties, "CommandlineMessager", "CommandlineMessager");
    if(addressees != null && !addressees.isEmpty()){
      msg.getAddressees().addAll(addressees);
    }
    sendMessage(msg);
  }

  private void sendMetadataMessage(String resourceId, DataResourceMessage.ACTION action, List<String> addressees) throws Exception{
    DataResourceMessage msg = DataResourceMessage.createMessage(resourceId, action, "CommandlineMessager", "CommandlineMessager");
    if(addressees != null && !addressees.isEmpty()){
      msg.getAddressees().addAll(addressees);
    }
    sendMessage(msg);
  }

  private void sendMessage(DataResourceMessage msg) throws JsonProcessingException{

    rabbitTemplate.convertAndSend("repository_events", msg.getRoutingKey(), msg.toJson().getBytes());
  }

}
