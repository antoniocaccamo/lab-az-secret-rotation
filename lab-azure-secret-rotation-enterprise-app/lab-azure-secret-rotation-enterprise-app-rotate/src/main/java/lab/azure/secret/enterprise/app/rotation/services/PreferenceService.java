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


package lab.azure.secret.enterprise.app.rotation.services;


import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Objects;

import lab.azure.secret.commons.SecretRotationConstants;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;



import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Getter
@ApplicationScoped
public class PreferenceService {


    @ConfigProperty(name="app.message.format.pattern.storage.account.blob.endpoint")
    String storageAccountBlobMessageFormatPattern;

    @ConfigProperty(name="app.message.format.pattern.storage.account.resource.group")
    String baseScopeMessageFormatPattern;

    private  URL mailSendEventGridEndpoint;
    private  URL storageAccountBlobEnterpriseAppSecretEndpoint;


    private Integer passwordCredentialValidForDays;

    private String baseScope;


    @PostConstruct
    void postConstruct() throws NumberFormatException, MalformedURLException {

        String sMailSendEventGridEndpoint =
                System.getenv(SecretRotationConstants.EventGrid.MailSendEventGridEndpoint);
        String storageAccountSubscription =
                System.getenv(SecretRotationConstants.EnterpriseApp.Rotation.StorageAccountSubscription);
        String storageAccountResourceGroup =
                System.getenv(SecretRotationConstants.EnterpriseApp.Rotation.StorageAccountResourceGroup);
        String storageAccountName =
                System.getenv(SecretRotationConstants.EnterpriseApp.Rotation.StorageAccountName);
        String validForDays =
                System.getenv(SecretRotationConstants.EnterpriseApp.Rotation.ValidForDays);


        Objects.requireNonNull(storageAccountSubscription , "storageAccountSubscription is null");
        Objects.requireNonNull(storageAccountResourceGroup , "storageAccountResourceGroup is null");
        Objects.requireNonNull(storageAccountName , "storageAccountName is null");
        Objects.requireNonNull(sMailSendEventGridEndpoint , "mailSendEventGridEndpoint is null");

        this.mailSendEventGridEndpoint = new URL(sMailSendEventGridEndpoint);

        this.baseScope = new MessageFormat(baseScopeMessageFormatPattern)
            .format(new Object[]{storageAccountSubscription, storageAccountResourceGroup});

        String endpoint = new MessageFormat(storageAccountBlobMessageFormatPattern)
                .format(new Object[]{storageAccountName});
        this.storageAccountBlobEnterpriseAppSecretEndpoint = new URL(endpoint);


        try {
            passwordCredentialValidForDays = StringUtils.isEmpty(validForDays) ?
                    SecretRotationConstants.EnterpriseApp.Rotation.DefaultValidForDays :
                    Integer.parseInt(validForDays);
        } catch (NumberFormatException e) {
            log.warn("error parsing env var {} : using default {}",
                    SecretRotationConstants.EnterpriseApp.Rotation.ValidForDays,
                    SecretRotationConstants.EnterpriseApp.Rotation.DefaultValidForDays
            );
            passwordCredentialValidForDays =
                    SecretRotationConstants.EnterpriseApp.Rotation.DefaultValidForDays;
        }

        log.info( "pref > ENTERPRISE_APPS_ROTATION_BASE_SCOPE : {}",  this.baseScope );

        log.info( "pref > ENTERPRISE_APPS_ROTATION_STORAGE_ACCOUNT_BLOB  : {}",
                this.storageAccountBlobEnterpriseAppSecretEndpoint.toExternalForm() );

        log.info( "pref > {} : {}", SecretRotationConstants.EnterpriseApp.Rotation.ValidForDays,
                this.passwordCredentialValidForDays);

        log.info( "pref > {} : {}", SecretRotationConstants.EventGrid.MailSendEventGridEndpoint,
                this.mailSendEventGridEndpoint.toExternalForm() );


    }

}
