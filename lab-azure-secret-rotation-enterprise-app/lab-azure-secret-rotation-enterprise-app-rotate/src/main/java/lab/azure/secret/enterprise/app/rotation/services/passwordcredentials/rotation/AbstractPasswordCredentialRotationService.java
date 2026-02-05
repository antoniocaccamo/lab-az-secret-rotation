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


package lab.azure.secret.enterprise.app.rotation.services.passwordcredentials.rotation;

import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import lab.azure.secret.commons.SecretRotationConstants;
import lab.azure.secret.commons.enums.enterpriceapp.EnterpriseAppEventEnum;
import lab.azure.secret.commons.exceptions.SecretRotationEnterpriseAppException;
import lab.azure.secret.commons.services.graph.CommonGraphServiceClient;
import lab.azure.secret.enterprise.app.rotation.domain.BlobVO;
import lab.azure.secret.enterprise.app.rotation.domain.EnterpriseAppEventGridEvent;
import lab.azure.secret.enterprise.app.rotation.services.IRotationService;
import lab.azure.secret.enterprise.app.rotation.services.PreferenceService;
import lab.azure.secret.enterprise.app.rotation.services.authorization.AuthorizationManagerService;
import lab.azure.secret.enterprise.app.rotation.services.blob.BlobService;
import com.microsoft.graph.models.Application;
import com.microsoft.graph.models.PasswordCredential;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.text.MessageFormat;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 *
 * {@link EnterpriseAppEventGridEvent} will be managed <br/>
 *
 * <pre>
 *                             expiring           actions to do
 *                             period
 *                              |   |
 *   *******+**********+++*     |   |
 *   |      expired       |     |   |     -> | 1. delete
 *   *******+**********+++*     |   |        | 2. inform
 *                              |   |        | 3. create a new one if no valid found
 *                              |   |
 *          *******+**********++|** |
 *          |     expiring      | | |     -> | 1. inform
 *          *******+************|** |        | 2. create a new one if no valid found
 *                              |   |
 *                              |   |
 *                              |   |
 *                              |   |
 *                              |   |
 *  ---------------------------------------------> t
 *                            now
 * </pre>
 *
 *
 * @auhtor antonio.caccamo on 2023-12-12 @ 11:52
 *
 */
@Slf4j
public abstract class AbstractPasswordCredentialRotationService implements IRotationService {


    protected final EnterpriseAppEventEnum enterpriseAppEventEnum;

    @Inject
    protected PreferenceService preferenceService;
    @Inject
    protected CommonGraphServiceClient graphClientService;
    @Inject
    protected BlobService blobService;

    @Inject
    protected AuthorizationManagerService authorizationManagerService;


    @ConfigProperty(name = "app.message.format.pattern.scope.storage.account")
    String scopeStorageAccountMessageFormatPattern;

    @ConfigProperty(name = "app.message.format.pattern.scope.storage.account.container")
    String scopeBlobContainerMessageFormatterPattern;

    protected AbstractPasswordCredentialRotationService(EnterpriseAppEventEnum enterpriseAppEventEnum) {
        this.enterpriseAppEventEnum = enterpriseAppEventEnum;
    }


    /**
     * Handle an EnterpriseApp Event Grid Event
     *
     * @param event
     * @return event to be sent
     * @throws SecretRotationEnterpriseAppException
     */
    @Override
    public abstract EventGridEvent handleEvent(@NonNull EnterpriseAppEventGridEvent event) throws SecretRotationEnterpriseAppException;


    /**
     *
     * @param event
     * @return
     * @throws SecretRotationEnterpriseAppException
     */
    public EnterpriseAppEventGridEvent matchEvent(@NonNull EnterpriseAppEventGridEvent event)
            throws SecretRotationEnterpriseAppException{

        log.info("step: matchEvent");
        if ( ! this.enterpriseAppEventEnum.equals(event.getEnterpriseAppEventEnum())) {
           throw  new SecretRotationEnterpriseAppException(
                    String.format("event type [%s] doesn't match [%s]",
                            event.getEnterpriseAppEventEnum(), this.enterpriseAppEventEnum));

        }
        return event;
    }


    /**
     * Verify input parameters
     * @param event
     * @return
     * @throws SecretRotationEnterpriseAppException
     */
    protected Application verify(@NonNull EnterpriseAppEventGridEvent event) throws SecretRotationEnterpriseAppException {
        log.info("step: verify");
        matchEvent(event);
        Map<String, Object> map = event.getData();
        String ownerUpn = (String) map.get(SecretRotationConstants.EventGrid.EndUsersRecipientsTag);
        String appDisplayName = (String) map.get(SecretRotationConstants.EnterpriseApp.EventGridEvent.Keys.ApplicationDisplayName);
        String appId = (String) map.get(SecretRotationConstants.EnterpriseApp.EventGridEvent.Keys.ApplicationId);
        String appAppId = (String) map.get(SecretRotationConstants.EnterpriseApp.EventGridEvent.Keys.ApplicationAppId);
        return graphClientService.userOwnsApplication(ownerUpn, appId)
                       .orElseThrow(() -> new SecretRotationEnterpriseAppException(
                               String.format("application [%s] ( objectId [%s] clientId [%s]) not owned by [%s]",
                                       appDisplayName, appId, appAppId, ownerUpn))
                       );
    }


    /**
     * Create a new {@link PasswordCredential} for {@link  Application}
     *
     * @param event
     * @param application
     * @return
     */
    protected PasswordCredential rotate(
            @NonNull EnterpriseAppEventGridEvent event,
            @Nullable Application application
    ) throws SecretRotationEnterpriseAppException {
        log.info("step: rotate");

        if ( Objects.isNull(application) ) {
            throw new SecretRotationEnterpriseAppException("application is null");
        }
        String oldPasswordCredentialId = (String) event.getData().get(SecretRotationConstants.EnterpriseApp.EventGridEvent.Keys.PasswordCredentialId);
        if (StringUtils.isEmpty(oldPasswordCredentialId)) {
            throw new SecretRotationEnterpriseAppException("oldPasswordCredentialId is null");
        }

        // create new
        String pcDisplayName = (String) event.getData().get(SecretRotationConstants.EnterpriseApp.EventGridEvent.Keys.PasswordCredentialDisplayName);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        PasswordCredential passwordCredential = new PasswordCredential();
        passwordCredential.displayName = String.format("%s-created-by-%s-on-%s",
                pcDisplayName,
                SecretRotationConstants.ServiceName,
                SecretRotationConstants.DateTimeFormatter.OffsetDateTimeFormatter.format(now)
        );
        passwordCredential.startDateTime = now;
        passwordCredential.endDateTime = now.plus(preferenceService.getPasswordCredentialValidForDays(), ChronoUnit.DAYS);
        PasswordCredential newPasswordCredential =
                graphClientService.applicationAddPasswordCredential(application, passwordCredential);


        return newPasswordCredential;
    }



    /**
     * Store the new created  {@link PasswordCredential} for {@link  Application} in a {@link com.azure.storage.blob.BlobClient}
     *
     * @param event
     * @param application
     * @param newPasswordCredential
     * @return
     * @throws SecretRotationEnterpriseAppException
     */
    protected BlobVO storeOnBlob(
            @NonNull EnterpriseAppEventGridEvent event,
            @NonNull Application application,
            @NonNull PasswordCredential newPasswordCredential
    ) throws SecretRotationEnterpriseAppException {
        log.info("step: storeOnBlob");

        String ownerUpn = (String) event.getData().get(SecretRotationConstants.EventGrid.EndUsersRecipientsTag);
        String containerName = SecretRotationConstants.EnterpriseApp.Transform.transformEmailForBlobName(
                application.displayName
        ) ;
        String blobName = String.format("%s/%s", SecretRotationConstants.EnterpriseApp
                                                         .Transform.transformEmailForBlobName(ownerUpn),
                SecretRotationConstants.EnterpriseApp
                        .Transform.transformEmailForBlobName(newPasswordCredential.displayName)

        );
        String blobContent = newPasswordCredential.secretText;

        // TODO : adjust tags map
        return blobService.store(ownerUpn, containerName, blobName, blobContent);
    }


    /**
     * Assign to ownerUpn role BuiltInRole READER on Storage Account
     *
     * @param event
     * @param blob
     */
    protected void assignRoleOnStorageAccount(@NonNull EnterpriseAppEventGridEvent event, @NonNull BlobVO blob) throws SecretRotationEnterpriseAppException {
        log.info("step: assignRoleOnStorageAccount");
        String ownerUpn = (String) event.getData().get(SecretRotationConstants.EventGrid.EndUsersRecipientsTag);
        BuiltInRole role = BuiltInRole.READER;
        String scope = new MessageFormat(scopeStorageAccountMessageFormatPattern).format(new Object[]{
                preferenceService.getBaseScope(),
                blob.storageAccount()
        });
        assign(ownerUpn, role, scope);
    }

    /**
     * Assign to ownerUpn role BuiltInRole STORAGE_BLOB_DATA_READER on Storage Account
     *
     * @param event
     * @param blob
     */
    protected  void assignRoleOnBlobContainer(@NonNull EnterpriseAppEventGridEvent event, @NonNull BlobVO blob) throws SecretRotationEnterpriseAppException {
        log.info("step: assignRoleOnBlobContainer");
        String ownerUpn = (String) event.getData().get(SecretRotationConstants.EventGrid.EndUsersRecipientsTag);
        BuiltInRole role = BuiltInRole.STORAGE_BLOB_DATA_READER;
        String scope = new MessageFormat(scopeBlobContainerMessageFormatterPattern).format(new Object[]{
                preferenceService.getBaseScope(),
                blob.storageAccount(),
                blob.containerName()
        });
        assign(ownerUpn, role, scope);
    }


    /**
     * Build an immutable map
     *
     * @param event
     * @return
     * @throws SecretRotationEnterpriseAppException
     */
    protected Map<String, Object> prepareTemplateParams(
            @NonNull EnterpriseAppEventGridEvent event
    ) throws SecretRotationEnterpriseAppException {
        log.info("step: prepareTemplateParams");
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        Map<String, Object> params = new LinkedHashMap<>(event.getData());
        params.put(SecretRotationConstants.EventGrid.Subject, event.getSubject());
        params.put(SecretRotationConstants.EventGrid.EventType, event.getEventType());
        params.put(SecretRotationConstants.EnterpriseApp.EventGridEvent.Keys.Now,
                SecretRotationConstants.DateTimeFormatter.OffsetDateTimeFormatter.format(now));
        return Map.copyOf(params);
    }


    /**
     * Build immutable map for template parameters
     *
     * @param event
     * @param passwordCredential
     * @param blob
     * @return
     * @throws SecretRotationEnterpriseAppException
     */

    protected Map<String, Object> prepareTemplateParams(
            @NonNull EnterpriseAppEventGridEvent event,
            @NonNull PasswordCredential passwordCredential,
            @NonNull BlobVO blob
    ) throws SecretRotationEnterpriseAppException {
        log.info("step: prepareTemplateParams");

        Map<String, Object> params = new LinkedHashMap<>(event.getData());
        params.put(SecretRotationConstants.EventGrid.Subject, event.getSubject());
        params.put(SecretRotationConstants.EventGrid.EventType, event.getEventType());
        params.put(
                String.format("New%s", SecretRotationConstants.EnterpriseApp.EventGridEvent.Keys.PasswordCredentialDisplayName),
                passwordCredential.displayName
        );
        params.put(
                String.format("New%s", SecretRotationConstants.EnterpriseApp.EventGridEvent.Keys.Expires), SecretRotationConstants.DateTimeFormatter.
                                                                                                                   OffsetDateTimeWithNanoFormatter.format(passwordCredential.endDateTime)
        );
        params.put("storageAccount", blob.storageAccount());
        params.put("containerName", blob.containerName());
        params.put("blobName", blob.blobName());
        return params;
    }

    /**
     * Delete the  {@link PasswordCredential} of {@link  Application} specified in {@link EnterpriseAppEventGridEvent}
     *
     * @param event
     * @param application
     */
    protected void deletePasswordCredential(@NonNull EnterpriseAppEventGridEvent event,
                                            @NonNull Application application) {

        log.info("step: deletePasswordCredential");
        String passwordCredentialId = (String) event.getData().get(SecretRotationConstants.EnterpriseApp.EventGridEvent.Keys.PasswordCredentialId);
        if (StringUtils.isEmpty(passwordCredentialId)) {
            throw new SecretRotationEnterpriseAppException("oldPasswordCredentialId is null");
        }

        graphClientService.applicationDeletePasswordCredential(application, UUID.fromString(passwordCredentialId));
    }


    /**
     *
     * @param params
     * @return
     * @throws SecretRotationEnterpriseAppException
     */
    protected abstract String buildEmailBodyHtml(Map<String, Object> params) throws SecretRotationEnterpriseAppException;


    /**
     * Build Mail Send topic {@link EventGridEvent}
     * @param event
     * @param bodyHTML
     * @return
     * @throws SecretRotationEnterpriseAppException
     */
    protected EventGridEvent buildEvent(
            @NonNull EnterpriseAppEventGridEvent event,
            @Nonnull String bodyHTML
    ) throws SecretRotationEnterpriseAppException {
        log.info("step: buildEvent");
        Map<String, Object> params = new LinkedHashMap<>(event.getData());
        params.put(SecretRotationConstants.EventGrid.Subject, event.getSubject());
        params.put(SecretRotationConstants.EventGrid.EventType, event.getEventType());
        params.put(SecretRotationConstants.EventGrid.BodyHtml, bodyHTML);
        EventGridEvent result = new EventGridEvent(event.getSubject(), event.getEventType(), BinaryData.fromObject(params), event.getEventGridEvent().getDataVersion());
        return result;
    }


    /**
     * Assign to ownerUpn the role on scope
     *
     * @param ownerUpn
     * @param role
     * @param scope
     */
    protected void assign(String ownerUpn, BuiltInRole role, String scope) {
        authorizationManagerService.assign(ownerUpn, role, scope);
    }

}

