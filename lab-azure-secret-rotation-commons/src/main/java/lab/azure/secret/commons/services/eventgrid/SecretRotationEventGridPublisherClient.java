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


package lab.azure.secret.commons.services.eventgrid;


import com.azure.core.credential.TokenCredential;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;
import lab.azure.secret.commons.utils.SecretRotationUtils;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.List;


/**
 *
 */
@Slf4j
public class SecretRotationEventGridPublisherClient implements  EventGridPublisher{

    protected final com.azure.messaging.eventgrid.EventGridPublisherClient<EventGridEvent> publisherClient;

    private SecretRotationEventGridPublisherClient(@Nonnull TokenCredential tokenCredential, @Nonnull URL endpoint) {

        this.publisherClient = new EventGridPublisherClientBuilder()
                .credential(tokenCredential)
                .endpoint(endpoint.toExternalForm())
                .buildEventGridEventPublisherClient();
    }

    /**
     * send eventgrid event
     *
     * @param events
     * @return
     */
    @Override
    public List<EventGridEvent> publish(@Nonnull List<EventGridEvent> events) {
        log.debug("sending # events: {}", SecretRotationUtils.eventGridEventToString(events));
        switch (events.size()) {
            case 0:
                break;
            default:

                publisherClient.sendEvents(events);
                break;
        }
        return events;
    }


    /**
     * Create a {@link SecretRotationEventGridPublisherClient}
     * @param tokenCredential
     * @param endpoint
     * @return
     */
    public static SecretRotationEventGridPublisherClient create(@Nonnull TokenCredential tokenCredential, @Nonnull URL endpoint) {
        return  new SecretRotationEventGridPublisherClient(tokenCredential, endpoint);
    }
}
