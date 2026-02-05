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


package lab.azure.secret.enterprise.app.rotation.services.blob;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lab.azure.secret.commons.exceptions.SecretRotationEnterpriseAppException;
import lab.azure.secret.commons.services.credentials.AzureCredentialService;
import lab.azure.secret.enterprise.app.rotation.domain.BlobVO;
import lab.azure.secret.enterprise.app.rotation.services.PreferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@ApplicationScoped
public class BlobService {


    protected final AzureCredentialService credentialService;
    protected final PreferenceService preferenceService;


    protected BlobServiceClient serviceClient;


    @PostConstruct
    void postConstruct() {
        this.serviceClient = new BlobServiceClientBuilder()
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .endpoint(preferenceService.getStorageAccountBlobEnterpriseAppSecretEndpoint().toExternalForm())
                .credential(credentialService.getDefaultAzureCredential())
                .buildClient();
    }

    /**
     * @param containerName formatted as "ApplicationName/OwnerUpn"
     * @param blobName
     * @param data
     * @return
     */
    public BlobVO store(@Nonnull String ownerUpn, @Nonnull String containerName, @Nonnull String blobName, @Nonnull String data) {
        return store(ownerUpn, containerName, blobName, data, Map.of());
    }

    public BlobVO store(@Nonnull String ownerUpn, @Nonnull String containerName, @Nonnull String blobName, @Nonnull String data, @Nonnull Map<String, String> tags) {

        String localContainerName = "a-container";
        try {
            log.info("creating if not exists container: {}", containerName);
            BlobContainerClient containerClient = serviceClient.createBlobContainerIfNotExists(containerName);
            log.info("creating blob {}", blobName);
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            //blobClient.setTags(tags);
            log.info("updating value {}", data);
            blobClient.upload(BinaryData.fromString(data));
            return new BlobVO(blobClient.getAccountName(), blobClient.getContainerName(), blobClient.getBlobName());
        } catch (Exception e) {
            log.error("error occurred: {}", e.getMessage());
            throw new SecretRotationEnterpriseAppException(e);
        }

    }


}
