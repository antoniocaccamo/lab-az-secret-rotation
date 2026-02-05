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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;


/**
 * @auhtor antonio.caccamo on 2024-01-10 @ 14:39
 */


@DisplayName("EnterpriseApp param")
public class EnterpriceAppTest {

    static String sapps = """
            [   
                { "applicationId": "app01",  "name" : "E App01" , "recipients" : "email.01@example.org" },
                { "applicationId": "app02",  "name" : "E App02" , "recipients" :"email.02@example.org"}
            ]
            """;


    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void whe_read_then_ok() throws JsonProcessingException {

        // when
        List<EnterpriceAppVO> apps = mapper.readValue(sapps, new TypeReference<List<EnterpriceAppVO>>() {
        });

        // then

        Assertions.assertThat(apps).size().isEqualTo(2);
        Assertions.assertThat(apps).asList().first()
                .extracting("name").isEqualTo("E App01");
    }
}