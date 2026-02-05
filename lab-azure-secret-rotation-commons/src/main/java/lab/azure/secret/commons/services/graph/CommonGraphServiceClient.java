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


package lab.azure.secret.commons.services.graph;

import lab.azure.secret.commons.exceptions.SecretRotationEnterpriseAppException;
import lab.azure.secret.commons.services.credentials.AzureCredentialService;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.http.GraphServiceException;
import com.microsoft.graph.models.*;
import com.microsoft.graph.options.HeaderOption;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.requests.ApplicationCollectionPage;
import com.microsoft.graph.requests.GraphServiceClient;
import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.List;

@Slf4j

@ApplicationScoped
public class CommonGraphServiceClient {

    @Inject
    protected AzureCredentialService credentialService;
    protected GraphServiceClient graphServiceClient;


    @PostConstruct
    void postConstruct() {
        this.graphServiceClient = GraphServiceClient.builder()
                .authenticationProvider(new TokenCredentialAuthProvider(credentialService.getDefaultAzureCredential()))
                .buildClient();

    }

    /**
     * @param upn
     * @return
     * @throws SecretRotationEnterpriseAppException
     */
    public Optional<User> userByUpn(@Nonnull String upn) throws SecretRotationEnterpriseAppException {
        log.debug("looking for user with upn [{}]", upn);
        User user = null;
        try {
            user = graphServiceClient.users().byId(upn).buildRequest().get();
        } catch (NullPointerException npe) {
            log.error("NullPointerException occurred: [{}]", npe);
            throw new SecretRotationEnterpriseAppException(npe);
        } catch (GraphServiceException gse) {
            switch (HttpStatus.valueOf(gse.getResponseCode())) {
                case NOT_FOUND -> log.info("no user found with upn [{}]", upn);
                default -> throw new SecretRotationEnterpriseAppException(gse);
            }
        }
        return Optional.ofNullable(user);
    }

    /**
     * @param user
     * @return
     * @throws SecretRotationEnterpriseAppException
     */
    public List<Application> userEnterpriseApps(@Nonnull User user) throws SecretRotationEnterpriseAppException {

        List<Application> applications = new LinkedList<>();
        try {
            Objects.requireNonNull(user.userPrincipalName);
            log.debug("looking for Enterprise Apps owned by user with email [{}]", user.userPrincipalName);
            for (ApplicationCollectionPage page = graphServiceClient.users()
                    .byId(user.userPrincipalName)
                    .ownedObjectsAsApplication()
                    .buildRequest()
                    .get()
                 ;
                 Objects.nonNull(page);
            ) {
                applications.addAll(page.getCurrentPage());
                page = Objects.nonNull(page.getNextPage()) ? page.getNextPage().buildRequest().get() : null;
            }
        } catch (NullPointerException npe) {
            log.error("NullPointerException occurred: [{}]", npe);
            throw new SecretRotationEnterpriseAppException(npe);
        } catch (GraphServiceException gse) {
            switch (HttpStatus.valueOf(gse.getResponseCode())) {
                case NOT_FOUND -> log.info("nor user/applications found with upn [{}]", user.userPrincipalName);
                default -> throw new SecretRotationEnterpriseAppException(gse);
            }
        }
        log.info("found [{}] applications owned by user [{}]", applications.size(), user.userPrincipalName);
        return applications;
    }

    public Optional<Application> userOwnsApplication(String ownerUpn, String applicationId) {
        Application application = null;
        try {
            application = graphServiceClient.users(ownerUpn).ownedObjectsAsApplication()
                    .byId(applicationId)
                    .buildRequest()
                    .get();

        } catch (NullPointerException npe) {
            log.error("NullPointerException occurred: [{}]", npe);
            throw new SecretRotationEnterpriseAppException(npe);
        } catch (GraphServiceException gse) {
            switch (HttpStatus.valueOf(gse.getResponseCode())) {
                case NOT_FOUND -> log.info("no user found with applicationId [{}]", applicationId);
                default -> throw new SecretRotationEnterpriseAppException(gse);
            }
        }
        return Optional.ofNullable(application);
    }

    /**
     * Retrieve an {@link Application} by id
     * @param id Azure AD App Registered ObjectID
     * @return
     */
    public Optional<Application> applicationById(@Nonnull String id) {

        Application application = null;
        try {
            application = graphServiceClient.applications()
                    .byId(id)
                    .buildRequest()
                    .get();
        } catch (NullPointerException npe) {
            log.error("NullPointerException occurred: [{}]", npe);
            throw new SecretRotationEnterpriseAppException(npe);
        } catch (GraphServiceException gse) {
            switch (HttpStatus.valueOf(gse.getResponseCode())) {
                case NOT_FOUND -> log.info("no user found with ObjectID [{}]", id);
                default -> throw new SecretRotationEnterpriseAppException(gse);
            }
        }
        return Optional.ofNullable(application);

    }

    /**
     * Retrieve an {@link Application} by application(client) ID
     *
     * @param appID Azure AD Application(Client) ID
     * @return
     */
    public Optional<Application> applicationByAppId(@Nonnull String appID) {

        Application application = null;
        try {
            ApplicationCollectionPage page = graphServiceClient.applications()
                                  .buildRequest()
                                  .filter("appId eq '%s'".formatted(appID))
                                  .get();
            return page.getCurrentPage().stream().findFirst();
        } catch (NullPointerException npe) {
            log.error("NullPointerException occurred: [{}]", npe);
            throw new SecretRotationEnterpriseAppException(npe);
        } catch (GraphServiceException gse) {
            switch (HttpStatus.valueOf(gse.getResponseCode())) {
                case NOT_FOUND -> log.info("no user found with application(client) id[{}]", appID);
                default -> throw new SecretRotationEnterpriseAppException(gse);
            }
        }
        return Optional.ofNullable(application);

    }


    /**
     *
     * @param applicationName
     * @return
     */
    public Optional<Application> applicationByName(@Nonnull String applicationName) {

        Optional<Application> application = Optional.empty() ;
        try {
            LinkedList<Option> requestOptions = new LinkedList<Option>();
            requestOptions.add(new HeaderOption("ConsistencyLevel", "eventual"));
            ApplicationCollectionPage page = graphServiceClient.applications()
                                  .buildRequest(requestOptions)
                                  .filter("startswith(displayName, '%s')".formatted(applicationName))
                                   .top(1)
                                  .get();
            application = page.getCurrentPage().stream().findFirst();
        } catch (NullPointerException npe) {
            log.error("NullPointerException occurred: [{}]", npe);
            throw new SecretRotationEnterpriseAppException(npe);
        } catch (GraphServiceException gse) {
            switch (HttpStatus.valueOf(gse.getResponseCode())) {
                case NOT_FOUND -> log.info("no user found with application [{}]", applicationName);
                default -> throw new SecretRotationEnterpriseAppException(gse);
            }
        }
        return application;
    }


    /**
     * Add {@link PasswordCredential} to an {@link Application}
     * @param application
     * @param passwordCredential
     * @return
     */
    public PasswordCredential applicationAddPasswordCredential(
            @Nonnull Application application,
            @Nonnull PasswordCredential passwordCredential
    ) {
        return applicationAddPasswordCredential(application.id, passwordCredential);
    }


    /**
     * Add {@link PasswordCredential} to an {@link Application}
     * @param applicationId
     * @param passwordCredential
     * @return
     */
    protected PasswordCredential applicationAddPasswordCredential(
            @Nonnull String applicationId,
            @Nonnull PasswordCredential passwordCredential
    ) {
        return graphServiceClient.applications(applicationId)
                .addPassword(ApplicationAddPasswordParameterSet
                        .newBuilder()
                        .withPasswordCredential(passwordCredential)
                        .build())
                .buildRequest()
                .post();
    }

    /**
     * Remove a {@link PasswordCredential} from an {@link Application}
     *
     * @param application
     * @param passwordCredentialId
     */
    public Boolean applicationDeletePasswordCredential(
            @Nonnull Application application,
            @Nonnull UUID passwordCredentialId
    ){
        return applicationDeletePasswordCredential(application.id, passwordCredentialId);
    }

    /**
     * Remove a {@link PasswordCredential} from an {@link Application}
     *
     * @param applicationId
     * @param passwordCredentialId
     * @return
     */
    protected Boolean applicationDeletePasswordCredential(
            @Nonnull String applicationId,
            @Nonnull UUID passwordCredentialId
    ) {
        graphServiceClient.applications(applicationId)
                .removePassword(ApplicationRemovePasswordParameterSet.newBuilder()
                        .withKeyId(passwordCredentialId)
                        .build())
                .buildRequest()
                .post();
        return Boolean.TRUE;
    }
}
