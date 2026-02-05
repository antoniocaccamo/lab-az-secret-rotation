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


package lab.azure.secret.keyvault.rotation.service;


import lab.azure.secret.commons.SecretRotationConstants;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;

import java.net.MalformedURLException;
import java.net.URL;

@Slf4j @Getter
@ApplicationScoped
public class PreferenceService {

    private  final int keyVaultSecretStringRandomStart;
    private  final int keyVaultSecretStringRandomEnd;
    private  final int keyVaultSecretStringRandomLength;
    private  final int keyVaultSecretDuration;

    @Nonnull
    private  final URL  mailSendEventGridEndpoint;


    public PreferenceService() throws NumberFormatException, MalformedURLException {
        String endpoint  = System.getenv(SecretRotationConstants.EventGrid.MailSendEventGridEndpoint);


        Assertions.assertThat(endpoint).isNotEmpty();
        this.mailSendEventGridEndpoint = new URL(endpoint);
        String start = System.getenv(SecretRotationConstants.KeyVault.Secret.StringRandomStart);
        this.keyVaultSecretStringRandomStart = StringUtils.isEmpty(start) ?
                SecretRotationConstants.KeyVault.Secret.DefaultRandomStart: Integer.parseInt(start);

        String end = System.getenv(SecretRotationConstants.KeyVault.Secret.StringRandomEnd);
        this.keyVaultSecretStringRandomEnd = StringUtils.isEmpty(end) ?
                SecretRotationConstants.KeyVault.Secret.DefaultRandomEnd : Integer.parseInt(end);


        String length = System.getenv(SecretRotationConstants.KeyVault.Secret.StringRandomLength);
        this.keyVaultSecretStringRandomLength = StringUtils.isEmpty(length) ?
            SecretRotationConstants.KeyVault.Secret.DefaultRandomLength: Integer.parseInt(length);

        String duration = System.getenv(SecretRotationConstants.KeyVault.Secret.Duration);
        this.keyVaultSecretDuration = StringUtils.isEmpty(duration) ?
                SecretRotationConstants.KeyVault.Secret.DefaultValidForDays : Integer.parseInt(duration);

    }

}
