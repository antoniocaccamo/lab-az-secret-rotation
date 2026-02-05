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


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lab.azure.secret.commons.SecretRotationConstants;
import lab.azure.secret.enterprise.app.check.dto.EnterpriceAppVO;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;


@ApplicationScoped
@Slf4j
@Getter
public class PreferenceService {

    private final ObjectMapper mapper = new ObjectMapper();

    protected URL  mailSendEventGridEndpoint;
    protected Integer expiringPeriod ;
    protected List<EnterpriceAppVO> enterpriceApps;
//    protected List<String> ownersUpns;
//    protected String mailRecipients;

    @PostConstruct
    void postConstruct() throws NumberFormatException, MalformedURLException, JsonProcessingException {

        String endpoint  = System.getenv(SecretRotationConstants.EventGrid.EnterpriseAppEventGridEndpoint);
        Assertions.assertThat(endpoint).isNotEmpty();
        this.mailSendEventGridEndpoint = new URL(endpoint);

        Integer expiring;
        try {
            String s = System.getenv(SecretRotationConstants.EnterpriseApp.Check.ExpiringPeriod);
            expiring = StringUtils.isNotEmpty(s) ? Integer.parseInt(s) : SecretRotationConstants.EnterpriseApp.Check.DefaultExpiringPeriod;
        }catch (NumberFormatException e) {
            log.warn("warning : expiringPeriod setted to default {}: {}",SecretRotationConstants.EnterpriseApp.Check.DefaultExpiringPeriod, e);
            expiring = SecretRotationConstants.EnterpriseApp.Check.DefaultExpiringPeriod;
        }
        this.expiringPeriod = expiring;

        String apps = StringUtils.trimToEmpty(System.getenv(SecretRotationConstants.EnterpriseApp.Check.Check));
        Assertions.assertThat(apps).isNotEmpty();

        this.enterpriceApps = mapper.readValue(apps, new TypeReference<List<EnterpriceAppVO>>(){});

        log.info("pref > {} : {}", SecretRotationConstants.EnterpriseApp.Check.Check, this.enterpriceApps);
        log.info("pref > {} : {}", SecretRotationConstants.EnterpriseApp.Check.ExpiringPeriod, this.expiringPeriod);
        log.info("pref > {} : {}", SecretRotationConstants.EventGrid.EnterpriseAppEventGridEndpoint, this.mailSendEventGridEndpoint);

    }

}
