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


package lab.azure.secret.enterprise.app.rotation.services.passwordcredentials.rotation;

import com.azure.messaging.eventgrid.EventGridEvent;
import lab.azure.secret.commons.exceptions.SecretRotationEnterpriseAppException;
import lab.azure.secret.commons.enums.enterpriceapp.EnterpriseAppEventEnum;
import lab.azure.secret.commons.exceptions.SecretRotationKeyVaulException;
import lab.azure.secret.enterprise.app.rotation.annotations.EnterpriseAppPasswordCredentialNearExpiryAndCreate;
import lab.azure.secret.enterprise.app.rotation.domain.BlobVO;
import lab.azure.secret.enterprise.app.rotation.domain.EnterpriseAppEventGridEvent;
import com.microsoft.graph.models.Application;
import com.microsoft.graph.models.PasswordCredential;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.quarkiverse.freemarker.TemplatePath;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 *
 * {@link EnterpriseAppEventGridEvent} will be managed <br/>
 *
 * <pre>
 *                             expiring           actions to do
 *                             period
 *
 *                              |   |
 *                              |   |
 *          *******+**********++|** |
 *          |     expiring      | | |     -> | 1. inform
 *          *******+************|** |        | 2. create a new one if no valid found
 *                              |   |
 *                              |   |
 *                              |   |
 *                              |   |
 *                              |   |
 *  ---------------------------------------------> t
 *                            now
 * </pre>
 *
 *
 * @auhtor antonio.caccamo on 2023-12-12 @ 11:52
 *
 */
@Slf4j
@EnterpriseAppPasswordCredentialNearExpiryAndCreate
@ApplicationScoped
public class EnterpriseAppPasswordCredentialNearExpiryAndCreateRotationService extends AbstractPasswordCredentialRotationService {

    protected static final String TemplatePath = "email/EnterpriseApp.PasswordCredentialNearExpiryAndCreate.ftl";


    @Inject
    @TemplatePath(TemplatePath)
    Template template ;


    public EnterpriseAppPasswordCredentialNearExpiryAndCreateRotationService() {
        super(EnterpriseAppEventEnum.PasswordCredentialNearExpiryAndCreate);
    }


    /**
     * Handle an EnterpriceApp Event Grid Event
     *
     * @param event
     * @return event to be sent
     * @throws SecretRotationEnterpriseAppException
     */
    @Override
    public EventGridEvent handleEvent(@NonNull EnterpriseAppEventGridEvent event) throws SecretRotationEnterpriseAppException {
        log.info("handling event {} for {}", event.getEventType(), event.getSubject());
        Application application = verify(event);
        PasswordCredential newPasswordCredential = rotate(event, application);
        BlobVO blob = storeOnBlob(event, application, newPasswordCredential);
        assignRoleOnStorageAccount(event, blob);
        assignRoleOnBlobContainer(event, blob);
        Map<String, Object> params = prepareTemplateParams(event, newPasswordCredential, blob);
        String htmlBody = buildEmailBodyHtml(params);
        return buildEvent(event, htmlBody);
    }

    /**
     *
     * @param params
     * @return
     * @throws SecretRotationEnterpriseAppException
     */
    protected String buildEmailBodyHtml(Map<String, Object> params) throws SecretRotationEnterpriseAppException {
        log.info("step: buildEmailBodyHtml");
        try {
            StringWriter sw = new StringWriter();
            template.process(params, sw);
            return sw.toString();
        } catch (TemplateException | IOException e) {
            throw new SecretRotationKeyVaulException(e);
        }
    }
}
