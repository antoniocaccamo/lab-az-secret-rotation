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

import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.KeyVaultSecretIdentifier;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import lab.azure.secret.commons.exceptions.SecretRotationKeyVaulException;
import lab.azure.secret.keyvault.rotation.annotations.KeyVaultSecretExpired;
import lab.azure.secret.keyvault.rotation.domain.KeyVaultSecretEventGridEvent;
import lab.azure.secret.keyvault.rotation.service.keyvault.KeyVaultSecretClient;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.quarkiverse.freemarker.TemplatePath;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.StringWriter;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * Expired KeyVaul Secret Service
 *
 */
@ApplicationScoped
@KeyVaultSecretExpired
@Slf4j
public class ExpiredKeyVaultSecretRotationService extends AbstractKeyVaultSecretRotationService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();


    @Inject
    @TemplatePath("email/Microsoft.KeyVault.SecretExpired.ftl")
    Template template;

    /**
     * @param kvse
     * @return
     * @throws SecretRotationKeyVaulException
     */
    @Override
    public EventGridEvent handleEvent(KeyVaultSecretEventGridEvent kvse) throws SecretRotationKeyVaulException {

        log.info("handling kvse: {}", kvse);
        final KeyVaultSecretIdentifier kvsi = KeyVaultSecretEventGridEvent.buildKeyVaultSecretIdentifier(kvse);

        KeyVaultSecretClient keyVaultSecretClient = KeyVaultSecretClient.from(credentialService.getDefaultAzureCredential(), kvsi);

        EventGridEvent output = keyVaultSecretClient.retrieveSecret()
                .map( this::createKeyVaultSecret)
                .map(keyVaultSecretClient::updateSecret)
                .map(kvs -> buildEvent(kvse, kvsi, kvs))
                .orElseThrow( ()-> new SecretRotationKeyVaulException("secret not found: %s".formatted(kvsi.getSourceId())));
        return output;
    }

    /**
     * Rotate expires keyvault secret
     * @param kvs
     * @return
     */
    protected KeyVaultSecret createKeyVaultSecret(KeyVaultSecret prevSecret) {
        log.warn("### will be rotate : keyVaultSecret: {} ", printKeyVaultSecretInfo(prevSecret));
        String newSecretValue =
            SECURE_RANDOM.ints(
                preferenceService.getKeyVaultSecretStringRandomStart(),
                preferenceService.getKeyVaultSecretStringRandomEnd()
            ).limit(preferenceService.getKeyVaultSecretStringRandomLength())
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString()
        ;
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        SecretProperties properties = new SecretProperties(  )
                .setNotBefore(now)
                .setTags(prevSecret.getProperties().getTags())
                .setExpiresOn( now.plus( preferenceService.getKeyVaultSecretDuration(), ChronoUnit.DAYS))
                .setEnabled(Boolean.TRUE)
                .setContentType(prevSecret.getProperties().getContentType())
            ;
        KeyVaultSecret secret = new KeyVaultSecret(prevSecret.getName(), newSecretValue)
                .setProperties(properties);
        return secret;
    }



    @Override
    protected String builEmailBodyHtml(Map<String, Object> params)  {
        try {
            StringWriter sw = new StringWriter();
            template.process(params, sw);
            return sw.toString();
        } catch (TemplateException | IOException e) {
            throw new SecretRotationKeyVaulException(e);
        }
    }


}
