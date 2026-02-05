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


package lab.azure.secret.keyvault.rotation.service.keyvault.rotation;

import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.KeyVaultSecretIdentifier;
import lab.azure.secret.commons.SecretRotationConstants;
import lab.azure.secret.commons.exceptions.SecretRotationKeyVaulException;
import lab.azure.secret.commons.services.credentials.AzureCredentialService;
import lab.azure.secret.keyvault.rotation.domain.KeyVaultSecretEventGridEvent;
import lab.azure.secret.keyvault.rotation.service.IRotationService;
import lab.azure.secret.keyvault.rotation.service.PreferenceService;
import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Rotation Service for a {@link KeyVaultSecret}
 */
@Slf4j
public abstract class AbstractKeyVaultSecretRotationService implements IRotationService {

    @Inject
    AzureCredentialService credentialService;

    @Inject
    PreferenceService preferenceService;




    /**
     * Handle a {@link KeyVaultSecretEventGridEvent}
     *
     * @param notification
     * @return event handled, empty string if not
     */

    /**
     * @param kvse
     * @return event handled, empty string if not
     */




    /**
     * Build {@link EventGridEvent} to be sent
     * @param kvse
     * @param kvsi
     * @param keyVaultSecret
     * @return {@link EventGridEvent}
     */
    protected EventGridEvent buildEvent(KeyVaultSecretEventGridEvent kvse, KeyVaultSecretIdentifier kvsi, KeyVaultSecret keyVaultSecret) {
        // recipients
        String endUsersRecipients = keyVaultSecret.getProperties().getTags()
                .getOrDefault(SecretRotationConstants.EventGrid.EndUsersRecipientsTag, StringUtils.EMPTY);

        // email body
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("keyvault", kvsi.getVaultUrl());
        context.put("secret", keyVaultSecret.getName());
        context.put("secretCreatedOn", SecretRotationConstants.DateTimeFormatter.OffsetDateTimeWithNanoFormatter
                .format(keyVaultSecret.getProperties().getCreatedOn())
        );
//        context.put("secretNotBefore", SecretRotationConstants.DateTimeFormatter.OffsetDateTimeWithNanoFormatter
//                .format(keyVaultSecret.getProperties().getNotBefore())
//        );
        context.put("secretExpDate", SecretRotationConstants.DateTimeFormatter.OffsetDateTimeWithNanoFormatter
                .format(keyVaultSecret.getProperties().getExpiresOn())
        );


        String bodyHtml = builEmailBodyHtml(context);
        log.info("bodyHtml {}", bodyHtml);


        Map<String, Object> data = new LinkedHashMap<>(kvse.getEventGridEventData());
        data.put(SecretRotationConstants.EventGrid.EndUsersRecipientsTag, endUsersRecipients);
        data.put(SecretRotationConstants.EventGrid.BodyHtml, bodyHtml);

        EventGridEvent eventGridEvent = new EventGridEvent(
                kvse.getSubject(),
                kvse.getEventType(),
                BinaryData.fromObject(data),
                kvse.getDataVersion()
        );

        return eventGridEvent;
    }


    /**
     *
     * @param notification
     * @return
     * @throws SecretRotationKeyVaulException
     */
    @Override
    public abstract EventGridEvent handleEvent(KeyVaultSecretEventGridEvent notification) throws SecretRotationKeyVaulException;

    protected abstract String builEmailBodyHtml(Map<String, Object> params) ;



    /**
     * Return an info string  about a {@link KeyVaultSecret}
     *
     * @param kvs
     * @return
     */
    protected static String printKeyVaultSecretInfo(@Nonnull KeyVaultSecret kvs) {
        return new ToStringBuilder(kvs, ToStringStyle.JSON_STYLE)
                .append("id", kvs.getId())
                .append("name", kvs.getName())
                .append("enabled", kvs.getProperties().isEnabled())
                .append("expiresOn", SecretRotationConstants.DateTimeFormatter.OffsetDateTimeWithNanoFormatter
                        .format(kvs.getProperties().getExpiresOn())
                ).append("tags", kvs.getProperties().getTags())
                .build();
    }


}


