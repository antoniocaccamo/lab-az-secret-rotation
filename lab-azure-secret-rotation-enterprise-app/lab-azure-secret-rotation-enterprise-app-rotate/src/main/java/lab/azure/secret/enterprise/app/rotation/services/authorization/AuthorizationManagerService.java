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


package lab.azure.secret.enterprise.app.rotation.services.authorization;


import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lab.azure.secret.commons.SecretRotationConstants;
import lab.azure.secret.commons.exceptions.SecretRotationEnterpriseAppException;
import lab.azure.secret.commons.services.credentials.AzureCredentialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@ApplicationScoped
public class AuthorizationManagerService {


    protected final AzureCredentialService credentialService;
    protected AuthorizationManager arm;


    @PostConstruct
    void postConstruct() {
        this.arm = AuthorizationManager
                .configure()
                .withLogLevel(HttpLogDetailLevel.HEADERS)
                .authenticate(
                        credentialService.getDefaultAzureCredential(),
                        new AzureProfile(AzureEnvironment.AZURE)
                );
    }

    /**
     * Role Assigment
     *
     * @param role
     * @param scope /subscriptions/{subid}/resourceGroups/{rgName}/providers/Microsoft.Storage/storageAccounts/{stName}
     * @param upn
     * @return
     */
    public void assign(@Nonnull String upn, @Nonnull BuiltInRole role, @Nonnull String scope) throws SecretRotationEnterpriseAppException {
        log.info("assign to [{}] role [{}] with scope [{}]", upn, role, scope);
        try {
            arm.roleAssignments()
                    .define(UUID.randomUUID().toString())
                    .forUser(upn)
                    .withBuiltInRole(role)
                    .withScope(scope)
                    .create();
        } catch (ManagementException me) {
            log.warn("management exception {}", me.getMessage());
            if ( ! SecretRotationConstants.EnterpriseApp.Rotation.RoleAssignmentExists.equals(me.getValue().getCode())) {
                throw new SecretRotationEnterpriseAppException(me);
            }
        }
    }

}
