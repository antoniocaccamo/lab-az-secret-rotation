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


package lab.azure.secret.enterprise.app.rotation.services.passwordcredentials.rotation;

import lab.azure.secret.commons.services.graph.CommonGraphServiceClient;
import lab.azure.secret.enterprise.app.rotation.annotations.EnterpriseAppPasswordCredentialExpiredAndCreate;
import lab.azure.secret.enterprise.app.rotation.services.PreferenceService;
import lab.azure.secret.enterprise.app.rotation.services.authorization.AuthorizationManagerService;
import lab.azure.secret.enterprise.app.rotation.services.blob.BlobService;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeAll;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
 * @auhtor antonio.caccamo on 2023-12-12 @ 15:39
 */
class EnterpriseAppPasswordCredentialExpiredAndCreateRotationServiceTest {


    private static final String EVENT_PATH = "events/password.credential.expired.json";
    private static String fileContent;


    @InjectMock
    protected PreferenceService preferenceService;
    @InjectMock
    protected CommonGraphServiceClient graphClientService;
    @InjectMock
    protected BlobService blobService;

    @InjectMock
    protected AuthorizationManagerService authorizationManagerService;


    @Inject
    @EnterpriseAppPasswordCredentialExpiredAndCreate
    EnterpriseAppPasswordCredentialExpiredAndCreateRotationService rotationService;


    @BeforeAll
    public static void beforeAll() {

        InputStream inputStream = EnterpriseAppPasswordCredentialExpiredRotationServiceTest.class.getClassLoader().getResourceAsStream(EVENT_PATH);

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        fileContent = reader.lines().collect(Collectors.joining());

    }

}