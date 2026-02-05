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
import com.microsoft.graph.models.Application;
import com.microsoft.graph.models.PasswordCredential;
import lab.azure.secret.commons.SecretRotationConstants;
import lab.azure.secret.commons.enums.enterpriceapp.EnterpriseAppEventEnum;
import lab.azure.secret.enterprise.app.check.services.PreferenceService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

class EnhancedEnterpriseAppSecretStrategyTest {


    static OffsetDateTime now = OffsetDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()));
    PreferenceService preferenceService = Mockito.mock(PreferenceService.class);

    EnhancedEnterpriseAppSecretStrategy enhancedEnterpriseAppSecretStrategy;

    static String recipients = "an.email@example.org";

    @BeforeEach
    public void beforeEach() {
        Mockito.when(preferenceService.getExpiringPeriod())
                .thenReturn(SecretRotationConstants.EnterpriseApp.Check.DefaultExpiringPeriod);

//        Mockito.when(preferenceService.getMailRecipients())
//                .thenReturn("group@example.org");

        enhancedEnterpriseAppSecretStrategy =
                new EnhancedEnterpriseAppSecretStrategy();
        enhancedEnterpriseAppSecretStrategy.preferenceService = preferenceService;
    }

    @ParameterizedTest @Order(1)
    @CsvSource({
       // days before now, days after now
        "30,  30",
        "0 ,   1"
    })
    public void when_valid_PasswordCredential_then_OK(Integer daysBeforeNow, Integer daysAfterBeforeNow) {
        //given

//        Mockito.when(preferenceService.getExpiringPeriod())
//                .thenReturn(SecretRotationConstants.EnterpriseApp.Check.DefaultExpiringPeriod);

        PasswordCredential passwordCredential = new PasswordCredential();
        passwordCredential.startDateTime = now.minus(daysBeforeNow, ChronoUnit.DAYS);
        passwordCredential.endDateTime   = now.plus(preferenceService.getExpiringPeriod(),ChronoUnit.DAYS)
                                                   .plus(daysAfterBeforeNow, ChronoUnit.DAYS);

        // when

        boolean isValid = enhancedEnterpriseAppSecretStrategy.isPasswordCredentialValid(
                passwordCredential, now, preferenceService.getExpiringPeriod()
        );

        // then
        Assertions.assertTrue(isValid);
    }


    @ParameterizedTest @Order(2)
    @CsvSource({
            // days before now, days after now
            "0 ,  -5",
            "0 ,  -1"
    })
    public void when_not_valid_PasswordCredential_then_KO(Integer daysBeforeNow, Integer daysAfterBeforeNow) {
        //given

//        Mockito.when(preferenceService.getExpiringPeriod())
//                .thenReturn(SecretRotationConstants.EnterpriseApp.Check.DefaultExpiringPeriod);

        PasswordCredential passwordCredential = new PasswordCredential();
        passwordCredential.startDateTime = now.minus(daysBeforeNow, ChronoUnit.DAYS);
        passwordCredential.endDateTime   = now.plus(preferenceService.getExpiringPeriod(),ChronoUnit.DAYS)
                                                   .plus(daysAfterBeforeNow, ChronoUnit.DAYS);

        // when
        boolean isValid = enhancedEnterpriseAppSecretStrategy.isPasswordCredentialValid(
                passwordCredential, now, preferenceService.getExpiringPeriod()
        );

        // then
        Assertions.assertFalse(isValid);
    }


    @Test @Order(3)
    public void when_credentials_NOT_hasApplicationAValidPasswordCredential_then_KO(){
        // given
        PasswordCredential expiring = new PasswordCredential();
        expiring.startDateTime = now.minus(50, ChronoUnit.DAYS);
        expiring.endDateTime   = now.plus(5, ChronoUnit.DAYS);

        // when
        List<PasswordCredential> credentials = List.of(
                expiring
        );

        // then
        Boolean createANewOne = enhancedEnterpriseAppSecretStrategy.needsToCreateANewOne(
                credentials, now
        );
        Assertions.assertTrue(createANewOne);
    }

    @Test @Order(4)
    public void when_credentials_hasApplicationAValidPasswordCredential_then_OK(){
        // given
        PasswordCredential expiring = new PasswordCredential();
        expiring.startDateTime = now.minus(50, ChronoUnit.DAYS);
        expiring.endDateTime   = now.plus(5, ChronoUnit.DAYS);

        PasswordCredential valid = new PasswordCredential();
        valid.startDateTime = now.minus(50, ChronoUnit.DAYS);
        valid.endDateTime   = now.plus(5, ChronoUnit.DAYS)
                                 .plus(preferenceService.getExpiringPeriod(), ChronoUnit.DAYS);

        // when
        List<PasswordCredential> credentials = List.of(
                expiring,
                valid
        );

        // then
        Optional<PasswordCredential> opt = enhancedEnterpriseAppSecretStrategy.hasApplicationAValidPasswordCredential(
                credentials, now, preferenceService.getExpiringPeriod()
        );
        Assertions.assertFalse(opt.isEmpty());
        Assertions.assertEquals(valid, opt.get());
    }


    @Test @Order(5)
    @DisplayName("when application has only expiring credential then PasswordCredentialExpiredAndCreate")
    public void when_application_then_PasswordCredentialExpiredAndCreate() {
        // given
        PasswordCredential expired = new PasswordCredential();
        expired.keyId = UUID.randomUUID();
        expired.displayName = "expired";
        expired.startDateTime = now.minus(50, ChronoUnit.DAYS);
        expired.endDateTime   = now.minus(5, ChronoUnit.DAYS);


        // when
        Application application = new Application();
        application.id = UUID.randomUUID().toString();
        application.appId = UUID.randomUUID().toString();
        application.displayName = "application";
        application.passwordCredentials = List.of(
                expired
        );

        // then
        List<EventGridEvent> events = enhancedEnterpriseAppSecretStrategy.evaluateCredentials(
               recipients, application, now
        );
        Assertions.assertEquals(1, events.size());
        Assertions.assertEquals(EnterpriseAppEventEnum.PasswordCredentialExpiredAndCreate,
                EnterpriseAppEventEnum.from(events.get(0).getEventType())
        );

    }

    @Test @Order(5)
    @DisplayName("when application has expiring and  valid credential then PasswordCredentialExpired")
    public void when_application_then_PasswordCredentialExpired() {
        // given
        PasswordCredential expired = new PasswordCredential();
        expired.keyId = UUID.randomUUID();
        expired.displayName = "expired";
        expired.startDateTime = now.minus(50, ChronoUnit.DAYS);
        expired.endDateTime   = now.minus(5, ChronoUnit.DAYS);

        PasswordCredential valid = new PasswordCredential();
        valid.keyId = UUID.randomUUID();
        valid.displayName = "expiring";
        valid.startDateTime = now.minus(50, ChronoUnit.DAYS);
        valid.endDateTime   = now.plus(5, ChronoUnit.DAYS)
                                      .plus(preferenceService.getExpiringPeriod(), ChronoUnit.DAYS);

        // when
        Application application = new Application();
        application.id = UUID.randomUUID().toString();
        application.appId = UUID.randomUUID().toString();
        application.displayName = "application";
        application.passwordCredentials = List.of(
                expired, valid
        );

        // then
        List<EventGridEvent> events = enhancedEnterpriseAppSecretStrategy.evaluateCredentials(
                recipients, application, now
        );
        Assertions.assertEquals(1, events.size());
        Assertions.assertEquals(EnterpriseAppEventEnum.PasswordCredentialExpired,
                EnterpriseAppEventEnum.from(events.get(0).getEventType())
        );

    }


    @Test @Order(5)
    @DisplayName("when application has only expiring credential then PasswordCredentialNearExpiryAndCreate")
    public void when_application_then_PasswordCredentialNearExpiryAndCreate() {
        // given
        PasswordCredential expiring = new PasswordCredential();
        expiring.keyId = UUID.randomUUID();
        expiring.displayName = "expiring";
        expiring.startDateTime = now.minus(50, ChronoUnit.DAYS);
        expiring.endDateTime   = now.plus(5, ChronoUnit.DAYS);


        // when
        Application application = new Application();
        application.id = UUID.randomUUID().toString();
        application.appId = UUID.randomUUID().toString();
        application.displayName = "application";
        application.passwordCredentials = List.of(
                expiring
        );

        // then
        List<EventGridEvent> events = enhancedEnterpriseAppSecretStrategy.evaluateCredentials(
                recipients, application, now
        );
        Assertions.assertEquals(1, events.size());
        Assertions.assertEquals(EnterpriseAppEventEnum.PasswordCredentialNearExpiryAndCreate,
                EnterpriseAppEventEnum.from(events.get(0).getEventType())
        );

    }

    @Test @Order(6)
    @DisplayName("when application has expiring and  valid credential then PasswordCredentialNearExpiry")
    public void when_application_then_PasswordCredentialNearExpiry() {
        // given
        PasswordCredential expiring = new PasswordCredential();
        expiring.keyId = UUID.randomUUID();
        expiring.displayName = "expiring";
        expiring.startDateTime = now.minus(50, ChronoUnit.DAYS);
        expiring.endDateTime   = now.plus(5, ChronoUnit.DAYS);

        PasswordCredential valid = new PasswordCredential();
        valid.keyId = UUID.randomUUID();
        valid.displayName = "expiring";
        valid.startDateTime = now.minus(50, ChronoUnit.DAYS);
        valid.endDateTime   = now.plus(5, ChronoUnit.DAYS)
                                      .plus(preferenceService.getExpiringPeriod(), ChronoUnit.DAYS);

        // when
        Application application = new Application();
        application.id = UUID.randomUUID().toString();
        application.appId = UUID.randomUUID().toString();
        application.displayName = "application";
        application.passwordCredentials = List.of(
                expiring, valid
        );

        // then
        List<EventGridEvent> events = enhancedEnterpriseAppSecretStrategy.evaluateCredentials(
                recipients, application, now
        );
        Assertions.assertEquals(1, events.size());
        Assertions.assertEquals(EnterpriseAppEventEnum.PasswordCredentialNearExpiry,
                EnterpriseAppEventEnum.from(events.get(0).getEventType())
        );

    }

}