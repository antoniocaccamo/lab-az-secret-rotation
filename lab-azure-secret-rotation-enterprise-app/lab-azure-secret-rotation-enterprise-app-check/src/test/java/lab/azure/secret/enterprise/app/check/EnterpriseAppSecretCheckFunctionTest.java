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


package lab.azure.secret.enterprise.app.check;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lab.azure.secret.commons.services.eventgrid.EventGridPublisher;
import lab.azure.secret.commons.services.eventgrid.EventGridPublisherBuilder;
import lab.azure.secret.commons.services.graph.CommonGraphServiceClient;
import lab.azure.secret.enterprise.app.check.dto.EnterpriceAppVO;
import lab.azure.secret.enterprise.app.check.services.EnterpriseAppSecretCheckGraphClientService;
import lab.azure.secret.enterprise.app.check.services.PreferenceService;
import lab.azure.secret.enterprise.app.check.strategies.Enhanced;
import lab.azure.secret.enterprise.app.check.strategies.EnterpriseAppSecretStrategyInterface;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.graph.models.Application;
import com.microsoft.graph.models.PasswordCredential;
import com.microsoft.graph.models.User;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;

@QuarkusTest
@DisplayName("EnterpriseApp Secret Check Function Test")
class EnterpriseAppSecretCheckFunctionTest {

    static String sapps = """
            [   
                { "applicationId": "app01",  "name" : "E App01" , "recipients" : "email.01@example.org" },
                { "applicationId": "app02",  "name" : "E App02" , "recipients" : "email.02@example.org"}
            ]
            """;

    @InjectMock
    CommonGraphServiceClient graphClientService;

    @InjectMock
    PreferenceService preferenceService ;

    @InjectMock
    EventGridPublisherBuilder eventGridPublisherBuilder ;

    EventGridPublisher eventGridPublisher = Mockito.mock(EventGridPublisher.class);
    ExecutionContext context = Mockito.mock(ExecutionContext.class);

    @Inject
    @Enhanced
    EnterpriseAppSecretStrategyInterface strategy;

    @Inject
    EnterpriseAppSecretCheckGraphClientService eaGraphClient;

    EnterpriseAppSecretCheckFunction function;


    ObjectMapper mapper = new ObjectMapper();


    @BeforeEach
    public void beforeEach() throws MalformedURLException, JsonProcessingException {

        function = new EnterpriseAppSecretCheckFunction();
        function.preferenceService=preferenceService;
        function.eaGraphClientService = eaGraphClient;
        function.enterpriseAppSecretStrategy=strategy;
        function.eventGridPublisherBuilder=eventGridPublisherBuilder;

        Mockito.when(preferenceService.getMailSendEventGridEndpoint())
                .thenReturn( new URL("https://an.url.com"));
        List<EnterpriceAppVO> apps =
                mapper.readValue(sapps, new TypeReference<List<EnterpriceAppVO>>() {});
        Mockito.when(preferenceService.getEnterpriceApps()).thenReturn(apps);

        Mockito.when(context.getInvocationId())
                .thenReturn( "TEST-%s".formatted(UUID.randomUUID().toString().substring(0,5)));

        Mockito.when(eventGridPublisher.publish(Mockito.anyList()))
                .thenReturn(List.of());
        Mockito.when(eventGridPublisherBuilder.build(Mockito.any(URL.class)))
                .thenReturn(eventGridPublisher);

    }

    @Test
    public void when_upn_ok_then_user_ok() throws MalformedURLException {

        // given
        String correctUpn = "correct.upn@example.it";
        User correctUser = new User();
        correctUser.userPrincipalName = correctUpn;
        String wrongUpn = "wrong.upn@example.it";
        User wrongUser = null;

        // when
        prepareApps();


        function.postConstruct();
        function.doFunction(context);

        // then

    }

    private  void prepareApps() {
        Application app = null;
        String appId = null;
        EnterpriceAppVO eavo = null;
        List<EnterpriceAppVO> eavos = new ArrayList<>();

        Mockito.when(preferenceService.getEnterpriceApps())
                .thenReturn(eavos);

        appId="appid01";
        eavo = EnterpriceAppVO.builder().applicationId(appId).recipients("recipients.01@example.org").build();
        eavos.add(eavo);

        app = new Application();
        app.displayName = "Enterprise App 01";
        app.id = UUID.randomUUID().toString();
        app.appId = appId;
        PasswordCredential pc01_App01 = new PasswordCredential();
        pc01_App01.keyId = UUID.randomUUID();
        pc01_App01.displayName = "%s : pc01".formatted(app.displayName);
        pc01_App01.startDateTime = OffsetDateTime.now(ZoneOffset.UTC).minus(1, ChronoUnit.MONTHS);
        pc01_App01.endDateTime = OffsetDateTime.now(ZoneOffset.UTC)
                                         .plus(preferenceService.getExpiringPeriod(), ChronoUnit.DAYS)
                                         .minus(5, ChronoUnit.DAYS);
        app.passwordCredentials = List.of(pc01_App01);

        Mockito.when(graphClientService.applicationById(appId))
                .thenReturn(Optional.ofNullable(app));

        appId="appid02";
        eavo = EnterpriceAppVO.builder().applicationId(appId).recipients("recipients.02@example.org").build();
        eavos.add(eavo);
        app = new Application();
        app.displayName = "Enterprise App 02";
        app.id = UUID.randomUUID().toString();
        app.appId = appId;
        PasswordCredential pc01_App02 = new PasswordCredential();
        pc01_App02.keyId = UUID.randomUUID();
        pc01_App02.displayName = "%s : pc02".formatted(app.displayName);
        pc01_App02.startDateTime = OffsetDateTime.now(ZoneOffset.UTC).minus(1, ChronoUnit.MONTHS);
        pc01_App02.endDateTime = OffsetDateTime.now(ZoneOffset.UTC)
                                         .plus(preferenceService.getExpiringPeriod(), ChronoUnit.DAYS);
        app.passwordCredentials = List.of(pc01_App02);

        Mockito.when(graphClientService.applicationById(appId))
                .thenReturn(Optional.ofNullable(app));



    }

}