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
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


/**
 * At any execution, every  {@link com.microsoft.graph.models.PasswordCredential} could be <br/>
 * <pre>
 *                             expiring           actions to do
 *                             period
 *
 *                             -\---\-
 *                              |   |
 *                       *******|***|****+++*
 *                       |      valid       |   -> | nope
 *                       *******|***|****+++*
 *                              |   |
 *   *******+**********+++*     |   |
 *   |      expired       |     |   |           -> | 1. delete
 *   *******+**********+++*     |   |              | 2. inform
 *                              |   |              | 3. create a new one if no valid found
 *                              |   |
 *          *******+**********++|** |
 *          |     expiring      | | |           -> | 1. inform
 *          *******+************|** |              | 2. create a new one if no valid found
 *                              |   |
 *                              |   |
 *                              |   |
 *                              |   |
 *                              |   |
 *  ---------------------------------------------> t
 *                            now
 * </pre>
 */

@Enhanced
@ApplicationScoped
@Slf4j
public class EnhancedEnterpriseAppSecretStrategy extends BasicEnterpriseAppSecretStrategy {





    @Override
    public List<EventGridEvent> evaluateCredentials(@Nonnull String ownerUpn, @Nonnull Application application, @Nonnull OffsetDateTime now) {
        return super.evaluateCredentials(ownerUpn, application, now);
    }

    /**
     * Return {@link  Boolean#FALSE} if there's a valid {@link com.microsoft.graph.models.PasswordCredential}
     *
     * @param credentials
     * @param now
     * @return
     */
    @Override
    protected boolean needsToCreateANewOne(@Nonnull List<PasswordCredential> credentials, @Nonnull OffsetDateTime now) {
        return hasApplicationAValidPasswordCredential(credentials, now, preferenceService.getExpiringPeriod())
                       .map(passwordCredential -> Boolean.FALSE)
                       .orElse(Boolean.TRUE);
    }

    /**
     * Check if there's a valid {@link com.microsoft.graph.models.PasswordCredential} in list
     *
     *
     * <pre>
     *
     *         expiring
     *         period
     *
     *         -\---\-
     *          |   |
     *   *******|***|****+++*
     *   |      valid       |
     *   *******|***|****+++*
     *          |   |
     * --------------------------> t
     *         now
     * </pre>
     *
     * @param credentials
     * @param now
     * @param expiringPeriod
     * @return
     */
    protected Optional<PasswordCredential> hasApplicationAValidPasswordCredential(
            @Nonnull List<PasswordCredential> credentials,
            @Nonnull OffsetDateTime now,
            @Nonnull Integer expiringPeriod
    ) {

        return credentials.stream()
                       .filter(pc -> isPasswordCredentialValid(pc, now, expiringPeriod))
                       .findFirst();
    }


    /**
     * Check if a {@link com.microsoft.graph.models.PasswordCredential} is valid
     *
     *
     * <pre>
     *
     *         expiring
     *         period
     *
     *         -\---\-
     *          |   |
     *   *******|***|****+++*
     *   |      valid       |
     *   *******|***|****+++*
     *          |   |
     * --------------------------> t
     *         now
     * </pre>
     *
     * @param passwordCredential
     * @param now
     * @param expiringPeriod
     * @return
     */
    protected boolean isPasswordCredentialValid(@Nonnull PasswordCredential passwordCredential, @Nonnull OffsetDateTime now, @Nonnull Integer expiringPeriod) {
        final OffsetDateTime start = Objects.nonNull(passwordCredential.startDateTime) ? passwordCredential.startDateTime : now;
        final OffsetDateTime end = now.plusDays(expiringPeriod);

        return !start.isAfter(now) && !passwordCredential.endDateTime.isBefore(end);
    }


}
