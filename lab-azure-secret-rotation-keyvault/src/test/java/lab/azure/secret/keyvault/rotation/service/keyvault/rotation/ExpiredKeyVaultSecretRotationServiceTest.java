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


package lab.azure.secret.keyvault.rotation.service.keyvault.rotation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;

@Slf4j
class ExpiredKeyVaultSecretRotationServiceTest {

    private int randomStart = '0';
    private int randomEnd = 'z';

    private int randomLength = 20;


    @Test
    public void xx () {
        long start =System.currentTimeMillis();
        SecureRandom secureRandom = new SecureRandom();

        String s01 =secureRandom.ints(randomStart, randomEnd)
                .limit(randomLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        String s02 =secureRandom.ints(randomStart, randomEnd)
                .limit(randomLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        log.info("ms {}, random string: {} {}",  System.currentTimeMillis()-start, s01, s02);

        Assertions.assertNotEquals(s01, s02);
    }
}

// :c8rjVt6p1RWF=KRyeX7 I7Y=[ClV_1j=nvTo1ex^