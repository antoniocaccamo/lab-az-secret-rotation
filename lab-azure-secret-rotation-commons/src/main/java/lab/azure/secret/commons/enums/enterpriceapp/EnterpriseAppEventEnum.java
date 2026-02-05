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


package lab.azure.secret.commons.enums.enterpriceapp;

import lombok.Getter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Getter
public enum EnterpriseAppEventEnum {

    PasswordCredentialNewVersionCreated("EnterpriseApp.PasswordCredentialNewVersionCreated", "Password Credential New Version Created"),
    PasswordCredentialNearExpiry("EnterpriseApp.PasswordCredentialNearExpiry", "Password Credential Near Expiry"),
    PasswordCredentialNearExpiryAndCreate("EnterpriseApp.PasswordCredentialNearExpiryAndCreate", "Password Credential Near Expiry. Requires a new one to be created"),
    PasswordCredentialExpired("EnterpriseApp.PasswordCredentialExpired", "Password Credential Expired"),
    PasswordCredentialExpiredAndCreate("EnterpriseApp.PasswordCredentialExpiredAndCreate", "Password Credential Expired. Requires a new one to be created");

    private final String fullname;
    private final String displayName;

    private EnterpriseAppEventEnum(String fm, String display) {
        this.fullname = fm;
        this.displayName = display;
    }


    public static EnterpriseAppEventEnum from(String s) throws IllegalArgumentException{
        for (EnterpriseAppEventEnum st : EnterpriseAppEventEnum.values()) {
            if (st.fullname.equals(s)) {
                return st;
            }
        }
        throw new IllegalArgumentException("EnterpriseAppEventEnum not found: %s".formatted(s));
    }

    public  boolean isPasswordCredentialNearExpiry() {
        return EnterpriseAppEventEnum.PasswordCredentialNearExpiry.equals(this);
    }

    public  boolean isPasswordCredentialExpired() {
        return EnterpriseAppEventEnum.PasswordCredentialExpired.equals(this);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("fullname", fullname)
                .append("displayName", displayName)
                .toString();

    }
}
