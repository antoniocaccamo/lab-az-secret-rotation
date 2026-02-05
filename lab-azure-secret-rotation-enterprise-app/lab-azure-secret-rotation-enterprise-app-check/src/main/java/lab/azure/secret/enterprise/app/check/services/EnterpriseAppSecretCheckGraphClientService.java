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


package lab.azure.secret.enterprise.app.check.services;

import lab.azure.secret.commons.exceptions.SecretRotationEnterpriseAppException;
import lab.azure.secret.commons.services.graph.CommonGraphServiceClient;
import lab.azure.secret.enterprise.app.check.dto.EnterpriceAppVO;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.graph.http.GraphServiceException;
import com.microsoft.graph.models.Application;
import com.microsoft.graph.models.User;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

/**
 *
 */
@ApplicationScoped
@Slf4j
public class EnterpriseAppSecretCheckGraphClientService {

    @Inject
    protected CommonGraphServiceClient commonGraphServiceClient;


    /**
     *
     * @param upn
     * @return
     * @throws SecretRotationEnterpriseAppException
     */
    public Optional<User> getUserByUpn(String upn) throws SecretRotationEnterpriseAppException {
        try {
            return commonGraphServiceClient.userByUpn(upn);
        } catch (NullPointerException npe) {
            log.error("NullPointerException occurred: [{}]", npe);
            throw new SecretRotationEnterpriseAppException(npe);
        } catch (GraphServiceException gse) {
            switch (HttpStatus.valueOf(gse.getResponseCode())) {
                case NOT_FOUND -> log.info("not user found with upn [{}]", upn);
                default -> throw new SecretRotationEnterpriseAppException(gse);
            }
        }
        return Optional.empty();
    }


    /**
     *
     */
    /**
     *
     * @param user
     * @return
     * @throws SecretRotationEnterpriseAppException
     */
    public List<Application> getEnterpriseAppsOwnedByUser(@Nonnull User user) throws SecretRotationEnterpriseAppException {
        List<Application> applications = null;
        try {
            log.debug("looking for Enterprise Apps owned by user with email [{}]", user.userPrincipalName);
            applications = commonGraphServiceClient.userEnterpriseApps(user);
        } catch (NullPointerException npe) {
            log.error("NullPointerException occurred: [{}]", npe);
            throw new SecretRotationEnterpriseAppException(npe);
        } catch (GraphServiceException gse) {
            switch (HttpStatus.valueOf(gse.getResponseCode())) {
                case NOT_FOUND ->  applications = List.of();
                default -> throw new SecretRotationEnterpriseAppException(gse);
            }
        }
        log.info("found [{}] applications owned by user [{}]", applications.size(), user.userPrincipalName);
        return applications;
    }


    /**
     * Retrieve an {@link Application} by application(client) ID
     *
     * @param appVO
     * @return
     */
    public Optional<Application> getEnterpriseAppByAppId(EnterpriceAppVO appVO) {
        try {
            log.debug("looking for Enterprise Apps owned by user with application(client) id [{}]", appVO.getApplicationId());
            return commonGraphServiceClient.applicationByAppId(appVO.getApplicationId());
        } catch (NullPointerException npe) {
            log.error("NullPointerException occurred: [{}]", npe);
            throw new SecretRotationEnterpriseAppException(npe);
        } catch (GraphServiceException gse) {
            switch (HttpStatus.valueOf(gse.getResponseCode())) {
                case NOT_FOUND -> log.info("not application found : {}", appVO);
                default -> throw new SecretRotationEnterpriseAppException(gse);
            }
        }
        return Optional.empty();
    }

//    /**
//     *
//     * @param appVO
//     * @return
//     */
//    public Optional<Application> getEnterpriseAppByDisplayName(EnterpriceAppVO appVO) {
//        try {
//            return graphClientService.applicationByName(appVO.getName());
//        } catch (NullPointerException npe) {
//            log.error("NullPointerException occurred: [{}]", npe);
//            throw new SecretRotationEnterpriseAppException(npe);
//        } catch (GraphServiceException gse) {
//            switch (HttpStatus.valueOf(gse.getResponseCode())) {
//                case NOT_FOUND -> log.info("not application found : {}", appVO);
//                default -> throw new SecretRotationEnterpriseAppException(gse);
//            }
//        }
//        return Optional.empty();
//    }
}
