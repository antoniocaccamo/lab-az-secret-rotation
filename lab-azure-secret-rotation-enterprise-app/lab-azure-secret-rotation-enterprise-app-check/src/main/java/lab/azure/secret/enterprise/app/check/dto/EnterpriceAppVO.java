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


package lab.azure.secret.enterprise.app.check.dto;


import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @auhtor antonio.caccamo on 2024-01-10 @ 14:39
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnterpriceAppVO {

    @Nonnull
    private String applicationId;

    @Nullable
    private String name;

    @Nonnull
    private String recipients;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                       .append("applicationId", applicationId)
                       .append("name", name)
                       .append("recipients", recipients)
                       .toString();
    }
}
