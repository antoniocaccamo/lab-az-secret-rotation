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


package lab.azure.secret.enterprise.app.check.services;

import lab.azure.secret.commons.services.graph.CommonGraphServiceClient;
import com.microsoft.graph.models.User;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

@QuarkusTest
class EnterpriseAppSecretCheckGraphClientServiceTest {

    @InjectMock
    CommonGraphServiceClient userClientService ;

    @Inject
    EnterpriseAppSecretCheckGraphClientService msGraphClientService;


    @Test
    public void when_upn_ok_then_user_ok() {

        // given
        String correctUpn = "correct.upn@example.it";
        User correctUser = new User();
        correctUser.userPrincipalName = correctUpn;
        String wrongUpn = "wrong.upn@example.it";
        User wrongUser = null;

        // when

        Mockito.when(userClientService.userByUpn(correctUpn)).thenReturn(Optional.of(correctUser));
        Mockito.when(userClientService.userByUpn(wrongUpn)).thenReturn(Optional.empty());



        // then
        Assertions.assertThat(msGraphClientService.getUserByUpn(correctUpn)).isNotEmpty();
        Assertions.assertThat(msGraphClientService.getUserByUpn(correctUpn)).get()
                .hasFieldOrProperty("userPrincipalName").extracting("userPrincipalName").isEqualTo(correctUpn);

        Assertions.assertThat(msGraphClientService.getUserByUpn(wrongUpn)).isEmpty();

    }
}