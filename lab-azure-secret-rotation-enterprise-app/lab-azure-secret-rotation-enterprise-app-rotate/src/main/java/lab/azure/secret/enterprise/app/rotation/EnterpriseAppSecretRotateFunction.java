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


package lab.azure.secret.enterprise.app.rotation;


import com.azure.messaging.eventgrid.EventGridEvent;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.EventGridTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;
import jakarta.annotation.PostConstruct;
import lab.azure.secret.commons.SecretRotationConstants;
import lab.azure.secret.commons.exceptions.SecretRotationEnterpriseAppException;
import lab.azure.secret.commons.services.credentials.AzureCredentialService;
import lab.azure.secret.commons.services.eventgrid.EventGridPublisher;
import lab.azure.secret.commons.services.eventgrid.SecretRotationEventGridPublisherClient;
import lab.azure.secret.enterprise.app.rotation.domain.EnterpriseAppEventGridEvent;
import lab.azure.secret.enterprise.app.rotation.services.IRotationService;
import lab.azure.secret.enterprise.app.rotation.services.PreferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class EnterpriseAppSecretRotateFunction {


    protected final PreferenceService preferenceService;
    protected final AzureCredentialService azureCredentialService;
    protected final IRotationService rotationService;

    protected EventGridPublisher publisher;

    @PostConstruct
    void postConstruct() {
        publisher = SecretRotationEventGridPublisherClient.create(
                azureCredentialService.getDefaultAzureCredential(),
                preferenceService.getMailSendEventGridEndpoint()
        );
    }

    @FunctionName("enterpriseAppSecretRotate")
    public void enterpriseAppSecretRotation(
            @EventGridTrigger(name = "event") Optional<String> event, ExecutionContext context
    ) {
        MDC.put(SecretRotationConstants.InvocationId, context.getInvocationId());
       try {
            List<EventGridEvent> events = event.map( evt -> EventGridEvent.fromString(evt)
                .stream()
                .map(EnterpriseAppEventGridEvent::from)
                .filter(EnterpriseAppSecretRotateFunction::isEventManaged)
                .map(rotationService::handleEvent)
                .collect(Collectors.toList())
            ).orElse(List.of());
           log.info("sending # events: {}", events.size());
           publisher.publish(events);
       } catch (SecretRotationEnterpriseAppException e) {
        log.error("error occurred", e);
        throw e;
       }
    }

    @FunctionName("logggg")
    public void logggg(
            @EventGridTrigger(name = "event") Optional<String> event, ExecutionContext context
    ) {
        event.ifPresent(evt -> log.warn("event {}", evt));
    }

    protected static boolean isEventManaged(EnterpriseAppEventGridEvent event) {
        return event.getEnterpriseAppEventEnum().isPasswordCredentialNearExpiry() ||
                event.getEnterpriseAppEventEnum().isPasswordCredentialExpired();
    }
}
