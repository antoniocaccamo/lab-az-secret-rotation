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


package lab.azure.secret.keyvault.rotation.service.keyvault;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.identity.DefaultAzureCredential;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.KeyVaultSecretIdentifier;
import lab.azure.secret.commons.exceptions.SecretRotationKeyVaulException;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * Client for a KeyVault Secret identified by a {@link KeyVaultSecretIdentifier}
 */
@Slf4j
public class KeyVaultSecretClient {


    private final KeyVaultSecretIdentifier keyVaultSecretIdentifier;
    private final SecretClient secretClient;


    /**
     *
     * @param kvsi
     */
    protected KeyVaultSecretClient(@Nonnull DefaultAzureCredential credential, @Nonnull KeyVaultSecretIdentifier kvsi) {
        this.keyVaultSecretIdentifier = kvsi;
        this.secretClient = new SecretClientBuilder()
            .credential( credential)
            .vaultUrl(kvsi.getVaultUrl())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildClient();
        log.info("{} built",getClass().getSimpleName());
    }


    /**
     * Retrieve the secret
     * @return
     * @throws SecretRotationKeyVaulException
     */
    public Optional<KeyVaultSecret> retrieveSecret() throws SecretRotationKeyVaulException {
        KeyVaultSecret keyVaultSecret = null;
        try {
            log.info("retriving key vault secret {}", keyVaultSecretIdentifier.getName());
            keyVaultSecret = secretClient.getSecret(this.keyVaultSecretIdentifier.getName());
        } catch (Exception e ) {
            log.error("error occurred: {}", e);
           throw new SecretRotationKeyVaulException(e);
        }
        return Optional.ofNullable(keyVaultSecret);
    }

    /**
     * Update the secret
     *
     * @param keyVaultSecret
     * @return
     * @throws SecretRotationKeyVaulException
     */
    public KeyVaultSecret updateSecret(KeyVaultSecret keyVaultSecret) throws SecretRotationKeyVaulException {
        try {
            log.info("updating key vault secret {}", keyVaultSecret.getName());
            return secretClient.setSecret(keyVaultSecret);
        } catch (Exception e ) {
            log.error("error occurred: {}", e);
            throw new SecretRotationKeyVaulException(e);
        }
    }

    /**
     * Generate a new {@link KeyVaultSecretClient} from {@link DefaultAzureCredential} and {@link KeyVaultSecretIdentifier}
     * @param credential
     * @param kvsi
     * @return
     */
    public static KeyVaultSecretClient from(@Nonnull DefaultAzureCredential credential, @Nonnull KeyVaultSecretIdentifier kvsi) {
        return new KeyVaultSecretClient(credential, kvsi);
    }
}
