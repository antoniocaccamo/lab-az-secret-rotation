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


package lab.azure.secret.commons.utils;

import com.azure.core.util.BinaryData;
import com.azure.core.util.serializer.TypeReference;
import com.azure.messaging.eventgrid.EventGridEvent;
import lab.azure.secret.commons.SecretRotationConstants;
import com.microsoft.graph.models.PasswordCredential;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
/**
 * Utility class
 */
public final class SecretRotationUtils {

    private SecretRotationUtils(){}
    
    public static final  Map<String, Object> binaryDataToStringObjectMap(BinaryData data) {
        return data.toObject(new TypeReference<Map<String, Object>>() {});
    }

    public static final  String applicationPasswordCredentialToString(List<PasswordCredential> credentials) {
        return "[%s]".formatted(
                credentials.stream()
                        .map(SecretRotationUtils::applicationPasswordCredentialToString)
                        .collect(Collectors.joining(","))
        );
    }

    public static final  String applicationPasswordCredentialToString(PasswordCredential passwordCredential) {
        return new ToStringBuilder(passwordCredential, ToStringStyle.JSON_STYLE)
                .append("keyId", passwordCredential.keyId)
                .append("displayName", passwordCredential.displayName)
                .append("endDateTime", SecretRotationConstants.DateTimeFormatter.OffsetDateTimeFormatter.format(passwordCredential.endDateTime))
                .build();
    }

    public static final  String eventGridEventToString(List<EventGridEvent> events) {
        return "[%s]".formatted(
                events.stream()
                        .map(SecretRotationUtils::eventGridEventToString)
                        .collect(Collectors.joining(","))
        );
    }

    public static final  String eventGridEventToString(EventGridEvent event) {
        return new ToStringBuilder(event, ToStringStyle.JSON_STYLE)
                .append("subject", event.getSubject())
                .append("topic", event.getTopic())
                .append("eventType", event.getEventType())
                .append("evenTime", SecretRotationConstants.DateTimeFormatter.OffsetDateTimeFormatter.format(event.getEventTime()))
                .append("data", BinaryData.fromObject(event.getData()))
                .build();
    }
}
