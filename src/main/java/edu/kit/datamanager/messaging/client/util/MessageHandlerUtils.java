/*
 * Copyright 2019 Karlsruhe Institute of Technology.
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
package edu.kit.datamanager.messaging.client.util;

import edu.kit.datamanager.entities.messaging.BasicMessage;
import edu.kit.datamanager.entities.repo.ContentInformation;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * @author jejkal
 */
public class MessageHandlerUtils{

  private final static Logger LOGGER = LoggerFactory.getLogger(MessageHandlerUtils.class);

  public static boolean wasUploadedByPrincipal(String repoBaseUrl, String resourceId, String relativePath, String principal){
    LOGGER.trace("Calling wasUploadedByPrincipal({}, {}, {}, {}).", repoBaseUrl, resourceId, relativePath, principal);
    RestTemplate restTemplate = new RestTemplate();

    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Arrays.asList(ContentInformation.CONTENT_INFORMATION_MEDIA_TYPE));

    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(headers);
    String destinationUri = repoBaseUrl + resourceId + "/data/" + relativePath;

    UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(destinationUri);

    destinationUri = uriBuilder.toUriString();

    LOGGER.trace("Performing HTTP GET to {}.", destinationUri);
    ResponseEntity<ContentInformation> response = restTemplate.exchange(destinationUri, HttpMethod.GET, requestEntity, ContentInformation.class);
    LOGGER.trace("Content information access returned with response {}.", response);
    if(response != null && response.getStatusCodeValue() == 200 && response.getBody() != null){
      String uploader = response.getBody().getUploader();
      LOGGER.trace("Content information found, comparing uploader {} with provided principal {}.", uploader, principal);
      return principal.equals(uploader);
    }

    LOGGER.trace("No content information or error state ({}) returned. Returning 'false'.", response.getStatusCodeValue());
    //no content found
    return false;
  }

  public static boolean isAddressed(String handlerIdentifier, BasicMessage message){
    return message.getAddressees() == null || message.getAddressees().isEmpty() || message.getAddressees().contains(handlerIdentifier);
  }

}
