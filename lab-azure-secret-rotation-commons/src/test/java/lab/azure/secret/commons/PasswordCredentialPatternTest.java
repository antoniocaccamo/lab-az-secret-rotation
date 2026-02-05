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


package lab.azure.secret.commons;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.regex.Matcher;

@Slf4j
public class PasswordCredentialPatternTest {


    @ParameterizedTest
    @ValueSource(strings = {
            "xxxxx-from-2023-11-01-to-2024-02-01",
            "xxxxxfrom-2023-11-01-to-2024-02-01"

    })
    public void when_passwordCredentialName_then_ok(String passwordCredentialName){

        final Matcher matcher = SecretRotationConstants.EnterpriseApp.Check.
                                        PasswordCredentialPattern.matcher(passwordCredentialName);
        Assertions.assertEquals(matcher.matches(), true);

    }
}
