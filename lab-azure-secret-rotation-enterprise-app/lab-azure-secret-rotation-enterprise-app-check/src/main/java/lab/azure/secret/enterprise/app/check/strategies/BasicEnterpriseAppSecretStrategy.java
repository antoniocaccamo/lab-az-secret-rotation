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
import lab.azure.secret.enterprise.app.check.services.PreferenceService;
import com.microsoft.graph.models.Application;
import com.microsoft.graph.models.PasswordCredential;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Evaulates  {@link PasswordCredential} of {@link Application} needs to be notified for renewing <br/>
 * <p>
 * Algorithm : <br/>
 * <pre>
 *  |---| expiringPeriod
 *                                       now
 *                                        |
 *                                        |---|| now + expiringPeriod
 *  PC01: start date                 end date
 *
 *       |------------------------------|
 *
 *  PC02: start date                      end date
 *            |------------------------------|
 *
 *  PC02:                   start date                      end date
 *                               | ------------------------------|
 *
 *  =======================================================================> t
 * </pre>
 *
 */

@Basic
@ApplicationScoped
@Slf4j
public class BasicEnterpriseAppSecretStrategy implements EnterpriseAppSecretStrategyInterface {

    @Inject
    protected PreferenceService preferenceService;


    /**
     * Evaluates credentials of {@link Application} and build a {@link List} of {@link EventGridEvent} to send
     *
     * @param recipients
     * @param application
     * @param now
     * @return A empty list if no message needs to be sent
     */
    @Override
    public List<EventGridEvent> evaluateCredentials(@Nonnull String recipients, @Nonnull Application application, @Nonnull OffsetDateTime now) {



        List<PasswordCredential> credentials = Objects.nonNull(application.passwordCredentials) ?
                                                       application.passwordCredentials : List.of();

        final boolean createNewOne = needsToCreateANewOne(credentials, now);

        List<EventGridEvent> events = credentials.stream()
                  .filter(pc -> pc.endDateTime.isBefore(now) ||
                                        now.plus(preferenceService.getExpiringPeriod(), ChronoUnit.DAYS).isAfter(pc.endDateTime)
                  )
                  .map(pc -> passwordCredentialEventMapper(recipients, application, pc, now, createNewOne)
                  ).collect(Collectors.toList());

        log.info(
                "application [{} (appId[{}])] # passwordCredentials [{}]  # events to send [{}]",
                application.displayName, application.appId,
                credentials.size(), events.size()
        );
        return events;
    }


    /**
     *
     * @param credentials
     * @param now
     * @return
     */
    protected boolean needsToCreateANewOne(@Nonnull List<PasswordCredential> credentials, @Nonnull OffsetDateTime now) {
        return false;
    }

}
