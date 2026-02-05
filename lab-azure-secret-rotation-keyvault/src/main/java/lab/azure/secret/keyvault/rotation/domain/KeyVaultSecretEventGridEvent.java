/*
 *
 * Copyright 2017-2026 antoniocaccamo
 *
 * Licensed under the Mozilla Public License Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */


package lab.azure.secret.keyvault.rotation.domain;

import com.azure.core.util.serializer.TypeReference;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.security.keyvault.secrets.models.KeyVaultSecretIdentifier;
import lab.azure.secret.commons.enums.keyvault.KeyVaultEventEnum;
import lab.azure.secret.commons.eventgrid.SecretEvent;
import lombok.Getter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Map;
import java.util.Objects;

@Getter
public class KeyVaultSecretEventGridEvent implements SecretEvent {

     private static final String ERR = "---";
     private static final String KEY_VAULT = "VaultName";
     private static final String SECRET = "ObjectName";
     private static final String KEY_VAULT_SECRET_ID = "Id";

     private final KeyVaultEventEnum keyVaultEventEnum;
     private final EventGridEvent eventGridEvent;
     private final Map<String, Object> eventGridEventData;

     protected KeyVaultSecretEventGridEvent(
             EventGridEvent eventGridEvent
     ) {
          Objects.requireNonNull(eventGridEvent);
          this.keyVaultEventEnum = KeyVaultEventEnum.from(eventGridEvent.getEventType());
          this.eventGridEvent = eventGridEvent;
          this.eventGridEventData = eventGridEvent.getData().toObject(new TypeReference<Map<String, Object>>() {});

     }

     @Override
     public Map<String, Object> getData() {
          return  Map.copyOf(eventGridEventData);
     }

     @Override
     public String getSubject() {
          return eventGridEvent.getSubject();
     }

     @Override
     public String getEventType() {
          return eventGridEvent.getEventType();
     }

     public String getDataVersion() {
          return eventGridEvent.getDataVersion();
     }

     @Override
     public String toString() {
          return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                  .append("id", eventGridEvent.getId())
                  .append("eventType", eventGridEvent.getEventType())
                  .append("subject", eventGridEvent.getSubject())
                  .append("topic", eventGridEvent.getTopic())
                  .toString();
     }

     public static final KeyVaultSecretIdentifier buildKeyVaultSecretIdentifier(KeyVaultSecretEventGridEvent eventGridEvent) {
          return new KeyVaultSecretIdentifier((String) eventGridEvent.getEventGridEventData().getOrDefault(KEY_VAULT_SECRET_ID, ERR));
     }

     public static final KeyVaultSecretEventGridEvent from(
             EventGridEvent evt
     ) {
          Objects.requireNonNull(evt);
          return new KeyVaultSecretEventGridEvent(evt);
     }



}
