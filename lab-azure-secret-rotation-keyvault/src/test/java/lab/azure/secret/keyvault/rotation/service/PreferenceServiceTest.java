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
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;


class PreferenceServiceTest {


    @Test
    public void when_new_then_ok() throws MalformedURLException {
        // given

        // when
        PreferenceService preferenceService = new PreferenceService();
        // then
        Assertions.assertThat(preferenceService)
                        .extracting(PreferenceService::getMailSendEventGridEndpoint).isNotNull();
        Assertions.assertThat(preferenceService)
                .extracting(PreferenceService::getKeyVaultSecretStringRandomStart)
                .isEqualTo(SecretRotationConstants.KeyVault.Secret.DefaultRandomStart);
        Assertions.assertThat(preferenceService)
                .extracting(PreferenceService::getKeyVaultSecretStringRandomEnd)
                .isEqualTo(SecretRotationConstants.KeyVault.Secret.DefaultRandomEnd);
        Assertions.assertThat(preferenceService)
                .extracting(PreferenceService::getKeyVaultSecretStringRandomLength)
                .isEqualTo(SecretRotationConstants.KeyVault.Secret.DefaultRandomLength);
        Assertions.assertThat(preferenceService)
                .extracting(PreferenceService::getKeyVaultSecretDuration)
                .isEqualTo(SecretRotationConstants.KeyVault.Secret.DefaultValidForDays);
    }
}