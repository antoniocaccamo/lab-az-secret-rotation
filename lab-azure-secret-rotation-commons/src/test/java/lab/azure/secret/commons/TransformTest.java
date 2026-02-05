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
import org.junit.jupiter.api.Test;


class TransformTest {

    @Test
    public void when_email_01_then_ok() {

        // given
        String email = "appowner@caccamoantonio@gmail.onmicrosoft.com";

        // when
        String transformed = SecretRotationConstants.EnterpriseApp.Transform.transformEmailForBlobName(email);

        Assertions.assertThat(transformed).containsPattern("[\\d\\w-]+");


    }


    @Test
    public void when_email_02_then_ok() {

        // given
        String email = "app.owner-01r@caccamoantonio@gmail.onmicrosoft.com";

        // when
        String transformed = SecretRotationConstants.EnterpriseApp.Transform.transformEmailForBlobName(email);

        Assertions.assertThat(transformed).containsPattern("[\\d\\w-]+");



    }

}