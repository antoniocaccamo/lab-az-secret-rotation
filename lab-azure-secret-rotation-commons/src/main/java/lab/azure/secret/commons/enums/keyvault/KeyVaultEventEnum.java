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


package lab.azure.secret.commons.enums.keyvault;

import lombok.Getter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * KeyVault Events: <br/>
 * <dl>
 * <dt>Microsoft.KeyVault.CertificateNewVersionCreated</dt>    <dd>Certificate New Version Created 	Triggered when a new certificate or new certificate version is created.</dd>
 * <dt>Microsoft.KeyVault.CertificateNearExpiry</dt>           <dd>Certificate Near Expiry 	        Triggered when the current version of certificate is about to expire. (The event is triggered 30 days before the expiration date.)</dd>
 * <dt>Microsoft.KeyVault.CertificateExpired</dt>              <dd>Certificate Expired 	            Triggered when the current version of a certificate is expired.</dd>
 * <dt>Microsoft.KeyVault.KeyNewVersionCreated</dt>            <dd>Key New Version Created 	        Triggered when a new key or new key version is created.</dd>
 * <dt>Microsoft.KeyVault.KeyNearExpiry</dt>                   <dd>Key Near Expiry 	                Triggered when the current version of a key is about to expire. The event time can be configured using key rotation policy</dd>
 * <dt>Microsoft.KeyVault.KeyExpired</dt>                      <dd>Key Expired 	                    Triggered when the current version of a key is expired.</dd>
 * <dt>Microsoft.KeyVault.SecretNewVersionCreated</dt>         <dd>Secret New Version Created 	        Triggered when a new secret or new secret version is created.</dd>
 * <dt>Microsoft.KeyVault.SecretNearExpiry</dt>                <dd>Secret Near Expiry 	                Triggered when the current version of a secret is about to expire. (The event is triggered 30 days before the expiration date.)</dd>
 * <dt>Microsoft.KeyVault.SecretExpired</dt>                   <dd>Secret Expired 	                    Triggered when the current version of a secret is expired.</dd>
 * <dt>Microsoft.KeyVault.VaultAccessPolicyChanged</dt>        <dd>Vault Access Policy Changed 	    Triggered when an access policy on Key Vault changed. It includes a scenario when Key Vault permission model is changed to/from Azure role-based access control.</dd>
 * </dl>
 */
@Getter
public enum KeyVaultEventEnum {


    CertificateNewVersionCreated("Microsoft.KeyVault.CertificateNewVersionCreated", ""),
    CertificateNearExpiry("Microsoft.KeyVault.CertificateNearExpiry", ""),
    CertificateExpired("Microsoft.KeyVault.CertificateExpired", ""),

    KeyNewVersionCreated("Microsoft.KeyVault.CertificateNewVersionCreated", ""),
    KeyNearExpiry("Microsoft.KeyVault.CertificateNearExpiry", ""),
    KeyExpired("Microsoft.KeyVault.CertificateExpired", ""),

    SecretNewVersionCreated("Microsoft.KeyVault.SecretNewVersionCreated", "Secret New Version Created"),
    SecretNearExpiry("Microsoft.KeyVault.SecretNearExpiry", "Secret Near Expiry"),
    SecretExpired("Microsoft.KeyVault.SecretExpired", "Secret Expired"),


    VaultAccessPolicyChanged("Microsoft.KeyVault.VaultAccessPolicyChanged", "Secret Expired");

    private final String fullname;
    private final String displayName;



    private KeyVaultEventEnum(String fm, String display) {
        this.fullname = fm;
        this.displayName = display;
//        Path path = Paths.get("templates/email", "%s.vm".formatted(_fullname));

//            this._emailTemplate = Velocity.getTemplate( path.normalize().toString()) ;
    }


    /**
     * Build KeyVaultEventEnum from a string
     * @param s
     * @return {@link KeyVaultEventEnum}
     * @throws IllegalArgumentException
     */
    public static KeyVaultEventEnum from(String s) throws IllegalArgumentException{
        KeyVaultEventEnum secret = null;

        for (KeyVaultEventEnum st : KeyVaultEventEnum.values()) {
            if (st.fullname.equals(s)) {
                return st;
            }
        }
        throw new IllegalArgumentException("KeyVaultEventEnum not found: %s".formatted(s));
    }

    public boolean isSecretNearExpiry() {
        return SecretNearExpiry.equals(this);
    }

    public boolean isSecretExpired() {
        return SecretExpired.equals(this);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("fullname", fullname)
                .append("displayName", displayName)
                .toString();

    }
}

