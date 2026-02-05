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


import lab.azure.secret.commons.services.credentials.AzureCredentialService;
import com.microsoft.graph.models.Application;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

@Slf4j
@DisplayName("MS Graph Client Service Test")
class GraphClientServiceTest {


    private static  AzureCredentialService azureCredentialService ;
    CommonGraphServiceClient graphClientService;

    @BeforeAll
    public static void beforeAll() {
        azureCredentialService = new AzureCredentialService();
        azureCredentialService.postContruct();
    }

    @BeforeEach
    public void beforeEach() {
        graphClientService = new CommonGraphServiceClient();
        graphClientService.credentialService = azureCredentialService;
        graphClientService.postConstruct();
    }



    @ParameterizedTest
    @ValueSource(strings = {
            "254207e6-8e18-45f5-8f99-922c67c2319a"
    })
    public void when_appID_then_user_ok(String appID) {

        System.out.println("looking for Azure AD Registered App with appID %s".formatted(appID));

        Optional<Application> oa = graphClientService.applicationByAppId(appID);
        System.out.println(
                oa.map( app -> "Registered App: %s".formatted(app.displayName))
                        .orElse("Registered App with appID: %s".formatted(appID))
        );
    }

}