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


package lab.azure.secret.enterprise.app.check.strategies;

import com.azure.messaging.eventgrid.EventGridEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microsoft.graph.models.Application;
import com.microsoft.graph.models.PasswordCredential;
import lab.azure.secret.commons.SecretRotationConstants;
import lab.azure.secret.commons.enums.enterpriceapp.EnterpriseAppEventEnum;
import lab.azure.secret.commons.utils.SecretRotationUtils;
import lab.azure.secret.enterprise.app.check.services.PreferenceService;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.slf4j.MDC;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Password Credential Secret Strategy Test")
class BasicEnterpriseAppSecretStrategyTest {

    static Gson gson = new GsonBuilder()
            .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeTimeGsonTypeAdapter4Test())
            .create();
    static PreferenceService preferenceService = Mockito.mock(PreferenceService.class);

    Application application;

    String ownerUpn = "an.upn@example.com";

//    static ObjectMapper Mapper = new ObjectMapper()
//            .registerModule( new SimpleModule()
//                    .addSerializer(   OffsetDateTime.class, new OffsetDateTimeJsonSerializer())
//                    .addDeserializer( OffsetDateTime.class, new OffsetDateTimeJsonDeserializer())
//            );


    @BeforeAll
    public static void beforeAll() {

        Mockito.when(preferenceService.getExpiringPeriod())
                .thenReturn(SecretRotationConstants.EnterpriseApp.Check.DefaultExpiringPeriod);

//        Mockito.when(preferenceService.getMailRecipients())
//                .thenReturn("group@example.org");
    }

    @BeforeEach
    public void beforeEach() {
        application = readBaseApplication();
    }


    @Test
    @Order(1)
    public void when_no_password_credentials_then_no_event() {
        String contextId = "when_noPasswordCredentials_then_noEvent";

        MDC.put(SecretRotationConstants.InvocationId, contextId);

        // given
        BasicEnterpriseAppSecretStrategy strategy =
                new BasicEnterpriseAppSecretStrategy();
        strategy.preferenceService = preferenceService;
        Assertions.assertThat(application.passwordCredentials).asList().isEmpty();

        // when
        List<EventGridEvent> event = strategy.evaluateCredentials(ownerUpn, application, OffsetDateTime.now());

        // then
        Assertions.assertThat(event).isEmpty();
    }


    @Test @Order(2)
    public void when_password_credentials_expire_in_future_then_no_event() {
        String contextId = "when_PasswordCredentialsExpireInFuture_then_noEvent";
        MDC.put(SecretRotationConstants.InvocationId, contextId);
        // given
        int after = 10;
        PasswordCredential passwordCredential = new PasswordCredential();
        passwordCredential.keyId = UUID.randomUUID();
        passwordCredential.endDateTime = OffsetDateTime.now(ZoneOffset.UTC)
                .plus(preferenceService.getExpiringPeriod(), ChronoUnit.DAYS)
                .plus(after, ChronoUnit.DAYS);

        application.passwordCredentials = List.of(passwordCredential);
        Assertions.assertThat(application.passwordCredentials).asList().isNotEmpty().size().isEqualTo(1);

        // when
        BasicEnterpriseAppSecretStrategy strategy =
                new BasicEnterpriseAppSecretStrategy();
        strategy.preferenceService = preferenceService;
        List<EventGridEvent> event = strategy.evaluateCredentials(ownerUpn, application, OffsetDateTime.now());

        // then
        Assertions.assertThat(event).isEmpty();
    }


    @Test @Order(3)
    public void when_password_credentials_expired_then_event() {

        String contextId = "when_PasswordCredentialsExpired_then_Event";
        MDC.put(SecretRotationConstants.InvocationId, contextId);

        // given
        int before = 10;
        OffsetDateTime offsetDateTime = OffsetDateTime.now(ZoneOffset.UTC)
                .minus(before, ChronoUnit.DAYS);
        UUID uuid = UUID.randomUUID();
        PasswordCredential passwordCredential = new PasswordCredential();
        passwordCredential.keyId = uuid;
        passwordCredential.displayName = "when_PasswordCredentialsExpired_then_Event";
        passwordCredential.endDateTime = offsetDateTime;
        application.passwordCredentials = List.of(passwordCredential);
        Assertions.assertThat(application.passwordCredentials).asList().isNotEmpty().size().isEqualTo(1);

        // when
        BasicEnterpriseAppSecretStrategy strategy =
                new BasicEnterpriseAppSecretStrategy();
        strategy.preferenceService = preferenceService;
        List<EventGridEvent> events = strategy.evaluateCredentials(ownerUpn, application, OffsetDateTime.now());

        // then
        Assertions.assertThat(events).isNotEmpty();
//        Assertions.assertThat(event).get()
//                .extracting("topic").asString().contains(uuid.toString());
        Assertions.assertThat(events).asList().size().isEqualTo(1);
        Assertions.assertThat(events).asList().element(0)
                .extracting("eventType").asString().isEqualTo(EnterpriseAppEventEnum.PasswordCredentialExpired.getFullname());
        log.info("event {}", SecretRotationUtils.eventGridEventToString(events));
    }


    @Test @Order(4)
    public void when_one_password_credentials_expiry_soon_then_one_event() {
        String contextId = "when_OnePasswordCredentialsExpirySoon_then_Event";
        MDC.put(SecretRotationConstants.InvocationId, contextId);
        // given
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        int after = 5;
        String string5 = "offsetDateTime5";
        OffsetDateTime offsetDateTime5 =
                now.plus(preferenceService.getExpiringPeriod(), ChronoUnit.DAYS).minus(after, ChronoUnit.DAYS);
        UUID uuid = UUID.randomUUID();
        PasswordCredential passwordCredential = new PasswordCredential();
        passwordCredential.keyId = uuid;
        passwordCredential.displayName = string5;
        passwordCredential.endDateTime = offsetDateTime5;

        application.passwordCredentials = List.of(passwordCredential);
        Assertions.assertThat(application.passwordCredentials).asList().isNotEmpty().size().isEqualTo(1);

        // when
        BasicEnterpriseAppSecretStrategy strategy =
                new BasicEnterpriseAppSecretStrategy();
        strategy.preferenceService = preferenceService;
        List<EventGridEvent> events = strategy.evaluateCredentials(ownerUpn, application, now);

        // then
        Assertions.assertThat(events).size().isEqualTo(1);
//        Assertions.assertThat(event).get()
//                .extracting("topic").asString().contains(uuid.toString());
        Assertions.assertThat(events).asList().element(0)
                .extracting("eventType").asString().isEqualTo(EnterpriseAppEventEnum.PasswordCredentialNearExpiry.getFullname());
        Map<String, Object> data = SecretRotationUtils.binaryDataToStringObjectMap(events.get(0).getData());
        Assertions.assertThat(data).extracting(SecretRotationConstants.EnterpriseApp.EventGridEvent.Keys.ApplicationAppId)
                .asString().isEqualTo(application.appId);
        Assertions.assertThat(data).extracting(SecretRotationConstants.EnterpriseApp.EventGridEvent.Keys.PasswordCredentialId)
                .asString().isEqualTo(uuid.toString());
        log.info("event {}", SecretRotationUtils.eventGridEventToString(events));
    }
//
//
//    @Test
//    public void when_MorePasswordCredentialsExpired_then_Event() {
//        MDC.put(SecretRotationConstants.ContextId, "when_MorePasswordCredentialsExpired_then_Event");
//        // given
//        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
//
//        int before01 = 5;
//        String string01 = "nowMinus%d".formatted(before01);
//        OffsetDateTime offsetDateTime01 = now.minus(before01, ChronoUnit.DAYS);
//        UUID uuid01 = UUID.randomUUID();
//        PasswordCredential passwordCredential01 = new PasswordCredential();
//        passwordCredential01.keyId = uuid01;
//        passwordCredential01.displayName = string01;
//        passwordCredential01.endDateTime = offsetDateTime01;
//
//        int before02 = 8;
//        String string02 = "nowMinus%d".formatted(before02);
//        OffsetDateTime offsetDateTime02 = now.minus(before02, ChronoUnit.DAYS);
//        UUID uuid02 = UUID.randomUUID();
//        PasswordCredential passwordCredential02 = new PasswordCredential();
//        passwordCredential02.keyId = uuid02;
//        passwordCredential02.displayName = string02;
//        passwordCredential02.endDateTime = offsetDateTime02;
//
//        application.passwordCredentials = List.of(passwordCredential01, passwordCredential02);
//        Assertions.assertThat(application.passwordCredentials).asList().isNotEmpty().size().isEqualTo(2);
//        log.info("application.passwordCredentials {}", logApplicationPasswordCredential(application.passwordCredentials));
//
//        // when
//        PasswordCredentialEnterpriseAppSecretStrategy strategy =
//                new PasswordCredentialEnterpriseAppSecretStrategy(preferenceService);
//        List<EventGridEvent> event = strategy.evaluateCredentials(application, ownerUpn, now);
//
//        // then
//        Assertions.assertThat(event).isNotEmpty();
//        Assertions.assertThat(event).get()
//                .extracting("subject").asString().contains(string01);
////        Assertions.assertThat(event).get()
////                .extracting("topic").asString().contains(uuid.toString());
//        Assertions.assertThat(event).get()
//                .extracting("eventType").asString().isEqualTo(EnterpriseAppEventEnum.PasswordCredentialExpired.getFullname());
//        Map<String, Object> data = event.get().getData().toObject(new TypeReference<Map<String, Object>>() {
//        });
//        Assertions.assertThat(data).extracting(SecretRotationConstants.EnterpriseApp.EventGridEvent.Keys.PasswordCredentialId)
//                .asString().isEqualTo(uuid01.toString());
//        log.info("event {}", logEventGridEvent(event.get()));
//    }
//
//
//    @Test
//    public void when_OnePasswordCredentialsExpiredOnePasswordCredentialsNeaExpiry_then_Event() {
//        MDC.put(SecretRotationConstants.ContextId, "when_OnePasswordCredentialsExpiredOnePasswordCredentialsNeaExpiry_then_Event");
//        // given
//        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
//
//        int before01 = 5;
//        String string01 = "nowMinus%d".formatted(before01);
//        OffsetDateTime offsetDateTime01 = now.minus(before01, ChronoUnit.DAYS);
//        UUID uuid01 = UUID.randomUUID();
//        PasswordCredential passwordCredential01 = new PasswordCredential();
//        passwordCredential01.keyId = uuid01;
//        passwordCredential01.displayName = string01;
//        passwordCredential01.endDateTime = offsetDateTime01;
//
//        int before02 = 8;
//        String string02 = "nowPlus%dMinus%d".formatted(preferenceService.getExpiringPeriod(), before02);
//        OffsetDateTime offsetDateTime02 = now.plus(preferenceService.getExpiringPeriod(), ChronoUnit.DAYS).minus(before02, ChronoUnit.DAYS);
//        UUID uuid02 = UUID.randomUUID();
//        PasswordCredential passwordCredential02 = new PasswordCredential();
//        passwordCredential02.keyId = uuid02;
//        passwordCredential02.displayName = string02;
//        passwordCredential02.endDateTime = offsetDateTime02;
//
//        application.passwordCredentials = List.of(passwordCredential01, passwordCredential02);
//        Assertions.assertThat(application.passwordCredentials).asList().isNotEmpty().size().isEqualTo(2);
//        log.info("application.passwordCredentials {}", logApplicationPasswordCredential(application.passwordCredentials));
//
//        // when
//        PasswordCredentialEnterpriseAppSecretStrategy strategy =
//                new PasswordCredentialEnterpriseAppSecretStrategy(preferenceService);
//        List<EventGridEvent> event = strategy.evaluateCredentials(application, ownerUpn, now);
//
//        // then
//        Assertions.assertThat(event).isNotEmpty();
//        Assertions.assertThat(event).get()
//                .extracting("subject").asString().contains(string02);
////        Assertions.assertThat(event).get()
////                .extracting("topic").asString().contains(uuid.toString());
//        Assertions.assertThat(event).get()
//                .extracting("eventType").asString().isEqualTo(EnterpriseAppEventEnum.PasswordCredentialNearExpiry.getFullname());
//        Map<String, Object> data = event.get().getData().toObject(new TypeReference<Map<String, Object>>() {
//        });
//        Assertions.assertThat(data).extracting(SecretRotationConstants.EnterpriseApp.EventGridEvent.Keys.PasswordCredentialId)
//                .asString().isEqualTo(uuid02.toString());
//        log.info("event {}", logEventGridEvent(event.get()));
//    }
//
//
//    @Test
//    public void when_OnePasswordCredentialsExpiredMorePasswordCredentialsNeaExpiry_then_Event() {
//        MDC.put(SecretRotationConstants.ContextId, "when_OnePasswordCredentialsExpiredOnePasswordCredentialsNeaExpiry_then_Event");
//        // given
//        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
//
//        int before01 = 5;
//        String string01 = "nowMinus%d".formatted(before01);
//        OffsetDateTime offsetDateTime01 = now.minus(before01, ChronoUnit.DAYS);
//        UUID uuid01 = UUID.randomUUID();
//        PasswordCredential passwordCredential01 = new PasswordCredential();
//        passwordCredential01.keyId = uuid01;
//        passwordCredential01.displayName = string01;
//        passwordCredential01.endDateTime = offsetDateTime01;
//
//        int before02 = 8;
//        String string02 = "nowPlus%dMinus%d".formatted(preferenceService.getExpiringPeriod(), before02);
//        OffsetDateTime offsetDateTime02 = now.plus(preferenceService.getExpiringPeriod(), ChronoUnit.DAYS).minus(before02, ChronoUnit.DAYS);
//        UUID uuid02 = UUID.randomUUID();
//        PasswordCredential passwordCredential02 = new PasswordCredential();
//        passwordCredential02.keyId = uuid02;
//        passwordCredential02.displayName = string02;
//        passwordCredential02.endDateTime = offsetDateTime02;
//
//        int before03 = 2;
//        String string03 = "nowPlus%dMinus%d".formatted(preferenceService.getExpiringPeriod(), before03);
//        OffsetDateTime offsetDateTime03 = now.plus(preferenceService.getExpiringPeriod(), ChronoUnit.DAYS).minus(before03, ChronoUnit.DAYS);
//        UUID uuid03 = UUID.randomUUID();
//        PasswordCredential passwordCredential03 = new PasswordCredential();
//        passwordCredential03.keyId = uuid03;
//        passwordCredential03.displayName = string03;
//        passwordCredential03.endDateTime = offsetDateTime03;
//
//        application.passwordCredentials = List.of(passwordCredential01, passwordCredential02, passwordCredential03);
//        Assertions.assertThat(application.passwordCredentials).asList().isNotEmpty().size().isEqualTo(3);
//        log.info("application.passwordCredentials {}", logApplicationPasswordCredential(application.passwordCredentials));
//
//        // when
//        PasswordCredentialEnterpriseAppSecretStrategy strategy =
//                new PasswordCredentialEnterpriseAppSecretStrategy(preferenceService);
//        List<EventGridEvent> event = strategy.evaluateCredentials(application, ownerUpn, now);
//
//        // then
//        Assertions.assertThat(event).isNotEmpty();
//        Assertions.assertThat(event).get()
//                .extracting("subject").asString().contains(string03);
////        Assertions.assertThat(event).get()
////                .extracting("topic").asString().contains(uuid.toString());
//        Assertions.assertThat(event).get()
//                .extracting("eventType").asString().isEqualTo(EnterpriseAppEventEnum.PasswordCredentialNearExpiry.getFullname());
//        Map<String, Object> data = event.get().getData().toObject(new TypeReference<Map<String, Object>>() {
//        });
//        Assertions.assertThat(data).extracting(SecretRotationConstants.EnterpriseApp.EventGridEvent.Keys.PasswordCredentialId)
//                .asString().isEqualTo(uuid03.toString());
//        log.info("event {}", logEventGridEvent(event.get()));
//    }


private static Application readBaseApplication() {

        final String path = "application/no.password.credentials.json";
        InputStream inputStream = BasicEnterpriseAppSecretStrategyTest.class.getClassLoader().getResourceAsStream(path);
        Assertions.assertThat(inputStream).isNotEmpty();
        return gson.fromJson(new BufferedReader(new InputStreamReader(inputStream)), Application.class);
    }


}