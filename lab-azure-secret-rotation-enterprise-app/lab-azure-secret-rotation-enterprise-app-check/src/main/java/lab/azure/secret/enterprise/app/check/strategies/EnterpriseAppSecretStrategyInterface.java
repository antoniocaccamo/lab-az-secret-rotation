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


package lab.azure.secret.enterprise.app.check.strategies;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.microsoft.graph.models.Application;
import com.microsoft.graph.models.PasswordCredential;

import jakarta.annotation.Nonnull;
import lab.azure.secret.commons.SecretRotationConstants;
import lab.azure.secret.commons.enums.enterpriceapp.EnterpriseAppEventEnum;

public interface EnterpriseAppSecretStrategyInterface {

    /**
     * Evaluates credentials of {@link Application} and build a {@link List} of {@link EventGridEvent} to send
     *
     * @param application
     * @param ownerUpn
     * @param now
     * @return A empty list if no message needs to be sent
     */
    List<EventGridEvent> evaluateCredentials(@Nonnull String ownerUpn, @Nonnull Application application,  @Nonnull OffsetDateTime now);




    /**
     * Create an {@link EventGridEvent} to be sent
     *
     * @param endUsersRecipients
     * @param application
     * @param passwordCredential
     * @param now
     * @param createNewOne
     * @return
     */

    default EventGridEvent passwordCredentialEventMapper(
            @Nonnull String endUsersRecipients,
            @Nonnull Application application,
            @Nonnull PasswordCredential passwordCredential,
            @Nonnull OffsetDateTime now,
            @Nonnull Boolean createNewOne
    ) {

//        String topic = "applications/%s/passwordCredentials/%s".formatted(
//                application.appId,
//                passwordCredential.keyId.toString()
//        );
        String subject = String.format("Application [%s] PasswordCredential [%s]", application.displayName, passwordCredential.displayName);
        EnterpriseAppEventEnum enterpriseAppEventEnum = passwordCredential.endDateTime.isBefore(now) ?
            ( createNewOne ? EnterpriseAppEventEnum.PasswordCredentialExpiredAndCreate    : EnterpriseAppEventEnum.PasswordCredentialExpired) :
            ( createNewOne ? EnterpriseAppEventEnum.PasswordCredentialNearExpiryAndCreate : EnterpriseAppEventEnum.PasswordCredentialNearExpiry)
        ;

        Map<String, String> data = Map.of(

                SecretRotationConstants.EventGrid.EndUsersRecipientsTag, endUsersRecipients,

                SecretRotationConstants.EnterpriseApp.
                        EventGridEvent.Keys.ApplicationId, application.id,
                SecretRotationConstants.EnterpriseApp.
                        EventGridEvent.Keys.ApplicationAppId, application.appId,
                SecretRotationConstants.EnterpriseApp.
                        EventGridEvent.Keys.ApplicationDisplayName, application.displayName,

                SecretRotationConstants.EnterpriseApp.
                        EventGridEvent.Keys.PasswordCredentialId, passwordCredential.keyId.toString(),
                SecretRotationConstants.EnterpriseApp.
                        EventGridEvent.Keys.PasswordCredentialDisplayName, passwordCredential.displayName,

                SecretRotationConstants.EnterpriseApp.
                        EventGridEvent.Keys.Expires, SecretRotationConstants.DateTimeFormatter.
                                                             OffsetDateTimeWithNanoFormatter.format(passwordCredential.endDateTime)
        );

        return new EventGridEvent(
                subject,
                enterpriseAppEventEnum.getFullname(),
                BinaryData.fromObject(data),
                SecretRotationConstants.EnterpriseApp.EventGridEvent.DataVersion

        )//.setTopic(topic)
                       .setEventTime(now);

    }
}
