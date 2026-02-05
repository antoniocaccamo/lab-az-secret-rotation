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

import com.azure.messaging.eventgrid.EventGridEvent;
import lab.azure.secret.commons.exceptions.SecretRotationEnterpriseAppException;
import lab.azure.secret.enterprise.app.rotation.domain.EnterpriseAppEventGridEvent;

/**
 * 
 */
public interface IRotationService {

    /**
     * Handle an EnterpriceApp Event Grid Event 
     * 
     * @param event
     * @return event to be sent
     * @throws SecretRotationEnterpriseAppException
     */
    EventGridEvent handleEvent(EnterpriseAppEventGridEvent event) throws SecretRotationEnterpriseAppException;

}
