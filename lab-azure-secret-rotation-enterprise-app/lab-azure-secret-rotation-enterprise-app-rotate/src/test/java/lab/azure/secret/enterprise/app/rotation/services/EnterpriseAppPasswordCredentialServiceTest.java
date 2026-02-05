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


package lab.azure.secret.enterprise.app.rotation.services;

import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridEvent;
import lab.azure.secret.commons.enums.enterpriceapp.EnterpriseAppEventEnum;
import lab.azure.secret.enterprise.app.rotation.annotations.EnterpriseAppPasswordCredentialExpired;
import lab.azure.secret.enterprise.app.rotation.annotations.EnterpriseAppPasswordCredentialExpiredAndCreate;
import lab.azure.secret.enterprise.app.rotation.annotations.EnterpriseAppPasswordCredentialNearExpiry;
import lab.azure.secret.enterprise.app.rotation.annotations.EnterpriseAppPasswordCredentialNearExpiryAndCreate;
import lab.azure.secret.enterprise.app.rotation.domain.EnterpriseAppEventGridEvent;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

@QuarkusTest @Slf4j
class EnterpriseAppPasswordCredentialServiceTest {

    static String mocked = "mocked";
    static int invocations = 1;


    private static final String NEAR_EXPIRY_EVENT_PATH = "events/password.credential.near.expiry.json";
    private static final String EXPIRED_EVENT_PATH = "events/password.credential.expired.json";

    @InjectMock
    @EnterpriseAppPasswordCredentialNearExpiry
    IRotationService nearExpiryRotationService;


    @InjectMock
    @EnterpriseAppPasswordCredentialNearExpiryAndCreate
      IRotationService nearExpiryRotationServiceAndCreate;




    @InjectMock
    @EnterpriseAppPasswordCredentialExpired
    IRotationService expiredRotationService;




    @InjectMock
    @EnterpriseAppPasswordCredentialExpiredAndCreate
    IRotationService expiredRotationServiceAndCreate;
    
    @Inject
    EnterpriseAppPasswordCredentialService service;
    EventGridEvent event;


    @BeforeEach
    public void beforeEach() {

        Mockito.clearInvocations(nearExpiryRotationService);
        Mockito.clearInvocations(expiredRotationService);

        event = new EventGridEvent(
                mocked,
                mocked,
                BinaryData.fromString("{ \"%s\" : \"%s\"}".formatted(mocked, mocked)),
                "1.0"
        );

        // when
        Mockito.when(nearExpiryRotationService.handleEvent(Mockito.any(EnterpriseAppEventGridEvent.class))).thenReturn(event);
        Mockito.when(expiredRotationService.handleEvent(Mockito.any(EnterpriseAppEventGridEvent.class))).thenReturn(event);
    }

    @Test
    @Order(1)
    public void when_near_expiry_event_then_call_near_expiry_service() {

        // given
        InputStream inputStream = null;
        BufferedReader reader;
        String fileContent = null;
        List<EventGridEvent> eventList = null;

        inputStream = EnterpriseAppPasswordCredentialServiceTest.class.getClassLoader().getResourceAsStream(NEAR_EXPIRY_EVENT_PATH);
        reader = new BufferedReader(new InputStreamReader(inputStream));
        fileContent = reader.lines().collect(Collectors.joining());
        eventList = EventGridEvent.fromString(fileContent);
        Assertions.assertNotNull(eventList);
        Assertions.assertEquals(1, eventList.size());

        EnterpriseAppEventGridEvent eaEvent = EnterpriseAppEventGridEvent.from(eventList.get(0));
        Assertions.assertEquals(EnterpriseAppEventEnum.PasswordCredentialNearExpiry, eaEvent.getEnterpriseAppEventEnum());

        // then
        EventGridEvent result = service.handleEvent(eaEvent);
        Assertions.assertEquals(event, result);

        Mockito.verify(nearExpiryRotationService, Mockito.times(invocations)).handleEvent(eaEvent);
        Mockito.verify(nearExpiryRotationServiceAndCreate, Mockito.never()).handleEvent(eaEvent);
        Mockito.verify(expiredRotationService, Mockito.never()).handleEvent(eaEvent);
        Mockito.verify(expiredRotationServiceAndCreate, Mockito.never()).handleEvent(eaEvent);
    }

    @Test
    @Order(2)
    public void when_expired_event_then_call_expired_service() {

        // given
        InputStream inputStream = null;
        BufferedReader reader;
        String fileContent = null;
        List<EventGridEvent> eventList = null;

        inputStream = EnterpriseAppPasswordCredentialServiceTest.class.getClassLoader().getResourceAsStream(EXPIRED_EVENT_PATH);
        reader = new BufferedReader(new InputStreamReader(inputStream));
        fileContent = reader.lines().collect(Collectors.joining());
        eventList = EventGridEvent.fromString(fileContent);
        Assertions.assertNotNull(eventList);
        Assertions.assertEquals(1, eventList.size());

        EnterpriseAppEventGridEvent eaEvent = EnterpriseAppEventGridEvent.from(eventList.get(0));
        Assertions.assertEquals(EnterpriseAppEventEnum.PasswordCredentialExpired, eaEvent.getEnterpriseAppEventEnum());

        // then
        EventGridEvent result = service.handleEvent(eaEvent);
        Assertions.assertEquals(event, result);
        // then

        Assertions.assertEquals(event, result);

        Mockito.verify(nearExpiryRotationService, Mockito.never()).handleEvent(eaEvent);
        Mockito.verify(nearExpiryRotationServiceAndCreate, Mockito.never()).handleEvent(eaEvent);
        Mockito.verify(expiredRotationService, Mockito.times(invocations)).handleEvent(eaEvent);
        Mockito.verify(expiredRotationServiceAndCreate, Mockito.never()).handleEvent(eaEvent);
    }

}