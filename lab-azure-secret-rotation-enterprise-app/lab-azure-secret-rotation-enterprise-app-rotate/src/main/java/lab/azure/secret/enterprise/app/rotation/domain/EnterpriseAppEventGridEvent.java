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


package lab.azure.secret.enterprise.app.rotation.domain;

import com.azure.core.util.serializer.TypeReference;
import com.azure.messaging.eventgrid.EventGridEvent;
import lab.azure.secret.commons.enums.enterpriceapp.EnterpriseAppEventEnum;
import lab.azure.secret.commons.eventgrid.SecretEvent;
import jakarta.annotation.Nonnull;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Map;


public class EnterpriseAppEventGridEvent implements SecretEvent {
    
    protected final EventGridEvent wrapped;
    protected final EnterpriseAppEventEnum enterpriseAppEventEnum;
    protected final Map<String, Object> data;


    protected EnterpriseAppEventGridEvent(@Nonnull EventGridEvent event) {
        this.wrapped = event;
        this.enterpriseAppEventEnum = EnterpriseAppEventEnum.from(event.getEventType());
        this.data = event.getData().toObject(new TypeReference<Map<String, Object>>() {});

    }

    @Override
    public EventGridEvent getEventGridEvent() {
        return wrapped;
    }

    @Override
    public String getSubject() {
        return wrapped.getSubject();
    }

    @Override
    public String getEventType() {
        return enterpriseAppEventEnum.getFullname();
    }

    public EnterpriseAppEventEnum getEnterpriseAppEventEnum() {
        return enterpriseAppEventEnum;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
        .append("subject", wrapped.getSubject())
        .append("eventType", wrapped.getEventType())
        .build();
    }

    @Override
    public Map<String, Object> getData() {
        return  Map.copyOf(data);
    }

    public static EnterpriseAppEventGridEvent from(@Nonnull EventGridEvent event) {
        return new EnterpriseAppEventGridEvent(event);
    }


}
