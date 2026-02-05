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

import com.azure.messaging.eventgrid.EventGridEvent;
import lab.azure.secret.commons.SecretRotationConstants;
import lab.azure.secret.commons.exceptions.SecretRotationEnterpriseAppException;
import lab.azure.secret.commons.services.eventgrid.EventGridPublisher;
import lab.azure.secret.commons.services.eventgrid.EventGridPublisherBuilder;
import lab.azure.secret.enterprise.app.check.services.EnterpriseAppSecretCheckGraphClientService;
import lab.azure.secret.enterprise.app.check.services.PreferenceService;
import lab.azure.secret.enterprise.app.check.strategies.Enhanced;
import lab.azure.secret.enterprise.app.check.strategies.EnterpriseAppSecretStrategyInterface;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.EventGridTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;
import io.quarkus.arc.profile.UnlessBuildProfile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Azure Functions with Time Trigger
 */
@Slf4j
public class EnterpriseAppSecretCheckFunction {

    @Nonnull
    @Inject
    protected PreferenceService preferenceService;
    @Nonnull
    @Inject
    protected EnterpriseAppSecretCheckGraphClientService eaGraphClientService;
    @Nonnull
    @Inject
    protected EventGridPublisherBuilder eventGridPublisherBuilder;

    @Nonnull @Inject
    @Enhanced
    protected EnterpriseAppSecretStrategyInterface enterpriseAppSecretStrategy;




    protected EventGridPublisher eventGridPublisher;


    @PostConstruct
    public void postConstruct() {
        eventGridPublisher = eventGridPublisherBuilder.build(preferenceService.getMailSendEventGridEndpoint());
    }

    @FunctionName("enterpriseAppsSecretCheck")
    public void enterpriseAppsSecretCheck(
            @TimerTrigger(name = "warmupTrigger", schedule = SecretRotationConstants.EnterpriseApp.Check.Schedule) String timerInfo,
            ExecutionContext context) {
        doFunction(context);
    }

    @UnlessBuildProfile("prod")
    @FunctionName("debug")
    public void keyVaultSecretRotate(
            @EventGridTrigger(name = "event") Optional<String> s,
            final ExecutionContext context) {
        log.warn("!!! --- FOR TESTING --- !!!!");
        doFunction(context);

    }

    protected void doFunction(ExecutionContext context) {

        MDC.put(SecretRotationConstants.InvocationId, context.getInvocationId());
        try {
            final OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

//            for (String ownerUpn : preferenceService.getOwnersUpns()) {
//                log.info("looking for enterprise apps owned by {} with credential expired or near to expiry on next {} days",
//                        ownerUpn, preferenceService.getExpiringPeriod()
//                );
//                final List<EventGridEvent> events  =
//                        service.getUserByUpn(ownerUpn)
//                        .map(service::getEnterpriseAppsOwnedByUser)
//                        .orElse(List.of())
//                        .stream()
//                        .map(app -> this.enterpriseAppSecretStrategy
//                                    .evaluateCredentials(ownerUpn, app, now)
//                        ).flatMap(Collection::stream)
//                        .collect(Collectors.toList());
//                log.info("owner upn {} : sending # events: {}", ownerUpn, events.size());
//                publisherClientService.publish(events);
//            }
//
//
//            final List<EventGridEvent> events = Arrays.asList("")
//                                                        .stream()
//                                                        .map(upn -> graphClientService.getUserByUpn(upn)
//                                                                            .map(graphClientService::getEnterpriseAppsOwnedByUser).orElse(List.of())
//                                                                            .stream()
//                                                                            .map(app -> enterpriseAppSecretStrategy.evaluateCredentials(upn, app, now))
//                                                                            .flatMap(Collection::stream)
//                                                                            .collect(Collectors.toList())
//                                                        ).flatMap(Collection::stream)
//                                                        .collect(Collectors.toList());

            final List<EventGridEvent> events =
                    preferenceService.getEnterpriceApps()
                        .stream()
                        .map(ea -> eaGraphClientService.getEnterpriseAppByAppId(ea)
                                    .map(app -> enterpriseAppSecretStrategy
                                                        .evaluateCredentials(ea.getRecipients() , app, now)
                                    ).orElse(List.of())
                        )
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
            log.info("sending # events: {}", events.size());
            eventGridPublisher.publish(events);

        } catch (SecretRotationEnterpriseAppException e) {
            log.error("error occurred: {}", e.getMessage());
            throw e;
        }
    }

}
