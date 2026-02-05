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


package lab.azure.secret.commons.aop;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Logged
@Priority(2020)
@Interceptor
public class LoggingAroundInvokeInterceptor {

    public static final Logger log = LoggerFactory.getLogger(LoggingAroundInvokeInterceptor.class);

    @AroundInvoke
    Object logInvocation(InvocationContext context) throws Exception {
        log.warn("### {}.{} : entering",
                context.getMethod().getDeclaringClass().getSimpleName(),
                context.getMethod().getName()
        );
        long start = System.currentTimeMillis();
        Object ret = context.proceed();
        log.warn("### {}.{} : exited ({} ms)",
                context.getMethod().getDeclaringClass().getSimpleName(),
                context.getMethod().getName(),
                System.currentTimeMillis()-start
        );
        return ret;
    }

}