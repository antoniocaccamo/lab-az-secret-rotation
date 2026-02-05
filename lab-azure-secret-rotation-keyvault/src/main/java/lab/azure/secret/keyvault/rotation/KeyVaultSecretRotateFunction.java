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


package lab.azure.secret.keyvault.rotation;

import com.azure.messaging.eventgrid.EventGridEvent;
import lab.azure.secret.commons.SecretRotationConstants;
import lab.azure.secret.commons.exceptions.SecretRotationKeyVaulException;
import lab.azure.secret.commons.services.eventgrid.EventGridPublisher;
import lab.azure.secret.commons.services.eventgrid.EventGridPublisherBuilder;
import lab.azure.secret.keyvault.rotation.domain.KeyVaultSecretEventGridEvent;
import lab.azure.secret.keyvault.rotation.service.IRotationService;
import lab.azure.secret.keyvault.rotation.service.PreferenceService;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.EventGridTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;
import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.inject.Default;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;



/**
 * Azure Functions with EventGridTrigger Trigger.
 */

@Slf4j
@RequiredArgsConstructor
public class KeyVaultSecretRotateFunction {

    @Nonnull
    protected final PreferenceService preferenceService;
    @Nonnull
    protected final EventGridPublisherBuilder eventGridPublisherBuilder;

    @Nonnull @Default
    protected final IRotationService rotationService;

    protected EventGridPublisher eventGridPublisher;


    @PostConstruct
    public void postConstruct () {
        eventGridPublisher = eventGridPublisherBuilder.build(preferenceService.getMailSendEventGridEndpoint());
    }

    @FunctionName("keyVaultSecretRotate")
    public void keyVaultSecretRotate(
            @EventGridTrigger(name = "event") Optional<String> event,
            final ExecutionContext context
    ) {


        MDC.put(SecretRotationConstants.InvocationId, context.getInvocationId());

        try {
            List<EventGridEvent> events = event.map( evt -> EventGridEvent.fromString(evt)
                .stream()
                .map(KeyVaultSecretEventGridEvent::from)
                .filter(KeyVaultSecretRotateFunction::isNotificationNeeded)
                .map(rotationService::handleEvent)
                .collect(Collectors.toList())
            ).orElse(List.of());
            log.info("sending # events: {}", events.size());
            eventGridPublisher.publish(events);
        } catch(SecretRotationKeyVaulException e) {
            log.error("error occurred", e);
            throw e;
        }

    }

    private static boolean isNotificationNeeded(KeyVaultSecretEventGridEvent kvse) {

        boolean needsNotification = false;

        switch (kvse.getKeyVaultEventEnum()) {
            case  SecretExpired, SecretNearExpiry-> needsNotification = true;
            default -> needsNotification = false;
        }
        return needsNotification;
    }
}


