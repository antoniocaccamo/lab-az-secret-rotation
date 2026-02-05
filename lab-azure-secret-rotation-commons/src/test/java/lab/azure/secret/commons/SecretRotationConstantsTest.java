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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class SecretRotationConstantsTest {



    @Test @Order(1)
    public void when_parse_offsetdatetime_then_ok() {
        // given
        System.out.println(SecretRotationConstants.DateTimeFormatter.OffsetDateTimeWithNanoFormatter.format(Instant.now().atOffset(ZoneOffset.UTC)));

        String instant = "2019-07-25T01:08:33.1036736Z";
        // when
        OffsetDateTime time = OffsetDateTime.parse(instant, SecretRotationConstants.DateTimeFormatter.OffsetDateTimeWithNanoFormatter);
        System.out.println(String.format(
                "%s is equal to %s ? %s",
                instant,
                SecretRotationConstants.DateTimeFormatter.OffsetDateTimeWithNanoFormatter.format(time),
                instant.equals(SecretRotationConstants.DateTimeFormatter.OffsetDateTimeWithNanoFormatter.format(time)
                )
        ));

        // then

        Assertions.assertThat(instant)
                .isEqualTo(SecretRotationConstants.DateTimeFormatter.OffsetDateTimeWithNanoFormatter.format(time));
    }

    @Test @Order(1)
    public void when_parse_offsetdatetime_with_nano_then_ok() {
        final String instant = "2019-07-25T01:08:33.1036736Z";
        final String toparse = "2023-08-30T13:09:31Z";

        OffsetDateTime instantParsed = OffsetDateTime.parse(instant, SecretRotationConstants.DateTimeFormatter.OffsetDateTimeWithNanoFormatter);
        OffsetDateTime parsed = OffsetDateTime.parse(toparse, SecretRotationConstants.DateTimeFormatter.OffsetDateTimeFormatter);

    }



}