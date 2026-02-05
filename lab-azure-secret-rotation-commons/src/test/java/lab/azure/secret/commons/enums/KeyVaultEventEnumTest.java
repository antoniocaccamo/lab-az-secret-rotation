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


package lab.azure.secret.commons.enums;


import lab.azure.secret.commons.enums.keyvault.KeyVaultEventEnum;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class KeyVaultEventEnumTest {



    @Test
    public void when_microsoft_keyvault_secret_event_newversioncreated_then_ok() {
        // given
        String fullname = "Microsoft.KeyVault.SecretNearExpiry";
        // when
        KeyVaultEventEnum secret = KeyVaultEventEnum.from(fullname);
        // then
        Assertions.assertThat(secret).isEqualTo(KeyVaultEventEnum.SecretNearExpiry);
    }

    @Test
    public void when_microsoft_keyvault_event_vaultaccesspolicychanged_then_ok() {
        // given
        String fullname = "Microsoft.KeyVault.VaultAccessPolicyChanged";
        // when
        KeyVaultEventEnum secret = KeyVaultEventEnum.from(fullname);
        // then
        Assertions.assertThat(secret).isEqualTo(KeyVaultEventEnum.VaultAccessPolicyChanged);

//        Assertions.assertThat(secret).isEqualTo(EventGridNotificationEnum.KeyVault.SecretKind.None);
//        Assertions.assertThat(secret).extracting(EventGridNotificationEnum.KeyVault.SecretKind::emailTemplate).isNull();
    }


    @Test()
    public void when_not_keyvault_evente_nono_then_ko() {
        // given
        final String fullname = "Microsoft.XXXX.YYYYY";

        // then

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () ->  KeyVaultEventEnum.from(fullname)
        );

    }

}