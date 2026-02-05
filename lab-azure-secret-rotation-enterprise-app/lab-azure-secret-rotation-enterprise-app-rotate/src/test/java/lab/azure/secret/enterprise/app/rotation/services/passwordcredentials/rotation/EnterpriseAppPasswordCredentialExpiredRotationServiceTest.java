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

import com.azure.core.util.serializer.TypeReference;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.microsoft.graph.models.Application;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lab.azure.secret.commons.SecretRotationConstants;
import lab.azure.secret.commons.enums.enterpriceapp.EnterpriseAppEventEnum;
import lab.azure.secret.commons.exceptions.SecretRotationEnterpriseAppException;
import lab.azure.secret.commons.services.graph.CommonGraphServiceClient;
import lab.azure.secret.enterprise.app.rotation.annotations.EnterpriseAppPasswordCredentialExpired;
import lab.azure.secret.enterprise.app.rotation.domain.EnterpriseAppEventGridEvent;
import lab.azure.secret.enterprise.app.rotation.services.PreferenceService;
import lab.azure.secret.enterprise.app.rotation.services.authorization.AuthorizationManagerService;
import lab.azure.secret.enterprise.app.rotation.services.blob.BlobService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;



@Slf4j
@QuarkusTest
class EnterpriseAppPasswordCredentialExpiredRotationServiceTest {

    private static final String EVENT_PATH = "events/password.credential.expired.json";
    private static final String EVENT_PATH_KO = "events/password.credential.expiredAndCreate.json";
    private static String fileContent;

    private static String fileContentKO;


    @InjectMock
    protected PreferenceService preferenceService;
    @InjectMock
    protected CommonGraphServiceClient graphClientService;
    @InjectMock
    protected BlobService blobService;

    @InjectMock
    protected AuthorizationManagerService authorizationManagerService;


    @Inject @EnterpriseAppPasswordCredentialExpired
    EnterpriseAppPasswordCredentialExpiredRotationService rotationService;

    @BeforeAll
    public static void beforeAll() {

        InputStream inputStream = EnterpriseAppPasswordCredentialExpiredRotationServiceTest.class.getClassLoader().getResourceAsStream(EVENT_PATH_KO);

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        fileContentKO = reader.lines().collect(Collectors.joining());

        inputStream = EnterpriseAppPasswordCredentialExpiredRotationServiceTest.class.getClassLoader().getResourceAsStream(EVENT_PATH);

        reader = new BufferedReader(new InputStreamReader(inputStream));
        fileContent = reader.lines().collect(Collectors.joining());

    }

    @Test @Order(1)
    public void when_event_KO_then_KO() {

        Application application = new Application();


        Mockito.when(graphClientService.userOwnsApplication(Mockito.anyString(), Mockito.anyString()))
                .thenReturn( Optional.ofNullable(application));

        List<EventGridEvent> eventList = EventGridEvent.fromString(fileContentKO);
        Assertions.assertNotNull(eventList);
        Assertions.assertEquals(1, eventList.size());

        EnterpriseAppEventGridEvent event = EnterpriseAppEventGridEvent.from(eventList.get(0));
        Assertions.assertNotEquals(EnterpriseAppEventEnum.PasswordCredentialExpired, event.getEnterpriseAppEventEnum());


        // when
        Throwable t = Assertions.assertThrows(SecretRotationEnterpriseAppException.class, () ->{
            rotationService.handleEvent(event);
        });
        log.info("{}", t.getLocalizedMessage());

    }

    @Test @Order(2)
    public void when_event_ok_then_ok() {

        Application application = new Application();


        Mockito.when(graphClientService.userOwnsApplication(Mockito.anyString(), Mockito.anyString()))
                .thenReturn( Optional.ofNullable(application));

        // Mockito.doNothing().when(graphClientService.applicationDeletePasswordCredential(Mockito.mock(Application.class), Mockito.))  ;


        List<EventGridEvent> eventList = EventGridEvent.fromString(fileContent);
        Assertions.assertNotNull(eventList);
        Assertions.assertEquals(1, eventList.size());

        EnterpriseAppEventGridEvent event = EnterpriseAppEventGridEvent.from(eventList.get(0));
        Assertions.assertEquals(EnterpriseAppEventEnum.PasswordCredentialExpired, event.getEnterpriseAppEventEnum());


        // when
        EventGridEvent result = rotationService.handleEvent(event);

        // then
        Map<String, Object> map = result.getData().toObject(new TypeReference<Map<String, Object>>() {});
        log.info("mail : {}", map.get(SecretRotationConstants.EventGrid.BodyHtml));

        Mockito.verify(graphClientService, Mockito.times(1)).userOwnsApplication(Mockito.anyString(), Mockito.anyString());
    }
}