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

import jakarta.annotation.Nonnull;

import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SecretRotationConstants {


    public static String ServiceName = "secret-rotation";

    private SecretRotationConstants(){}


    public static final String InvocationId = "context.invocationId";

    public static final String EndUsersRecipientsSeparator = ";";

    
    public static class KeyVault {

        private KeyVault(){}



        public static class Secret {



            private Secret() {
            }

            public static int DefaultValidForDays = 90;
            public static int DefaultRandomStart = '0';
            public static int DefaultRandomEnd = 'z';

            public static int DefaultRandomLength = 20;
            public static final String  StringRandomStart = "KEYVAULT_SECRET_STRING_RANDOM_START";
            public static final String  StringRandomEnd = "KEYVAULT_SECRET_STRING_RANDOM_END";
            public static final String  StringRandomLength = "KEYVAULT_SECRET_STRING_RANDOM_LENGTH";

            public static final String Duration = "KEYVAULT_SECRET_DURATION";
        }
    }

    public static class EventGrid {
        private EventGrid(){}


        public static final String OriginalInvocatiotId = "OriginalContextId";

        /**
         * Env vars containing event grid endpoint form mail send logicapp
         */
        public static final String MailSendEventGridEndpoint = "EVENT_GRID_LOGIC_APP_MAIL_SEND_ENDPOINT";
        public static final String EnterpriseAppEventGridEndpoint  = "EVENT_GRID_ENTERPRISE_APP_ENDPOINT";

        public static final String Subject = "subject";

        public static final String EventType = "eventType";

        public static final String BodyHtml = "bodyHtml";
        public static final String EndUsersRecipientsTag = "endusers-recipients";



        // public static class Topic {
        //     private Topic(){}

        //     public static final String EnterpriseAppSecretRotation ="evtgrid-entrpriceapp-secret-rotation";
        //     public static final String MailSend = "evtgrid-mail-send";
        // }
    }

    public static class EnterpriseApp {
        private EnterpriseApp() {
        }

        public static class Transform {

            private Transform(){}

            public static String transformEmailForBlobName(@Nonnull String ownerUpn) {

                return ownerUpn.toLowerCase()
                        .codePoints()
                        .mapToObj( ch -> Character.isLetterOrDigit(ch) ? Character.toString(ch) : "-" )
                        .collect(Collectors.joining());
            }
        }

        public static class EventGridEvent {
            private EventGridEvent() {
            }

            public static final String DataVersion = "1";

            public static class Keys {
                private Keys() {}

                public static final String ApplicationId = "ApplicationId";
                public static final String ApplicationAppId = "ApplicationAppId";
                public static final String ApplicationDisplayName = "ApplicationDisplayName";
                public static final String PasswordCredentialId = "PasswordCredentialId";
                public static final String PasswordCredentialDisplayName = "PasswordCredentialDisplayName";
                public static final String Expires = "EXP";

                public static final String Now = "NOW";
            }
        }

        public static class Check {
            private Check() {
            }

            public static final String Schedule = "0 0 */1 * * *";



            public static final Integer DefaultExpiringPeriod = 30;

            private static final String EmailRegex = "^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
            public static final Pattern EmailPattern = Pattern.compile(EmailRegex);


            public static final String ExpiringPeriod = "ENTERPRISE_APPS_CHECK_EXPIRING_PERIOD";
            public static final String Check = "ENTERPRISE_APPS_CHECK";

            public static final String MailRecipients = "ENTERPRISE_APPS_MAIL_RECIPIENTS";

            static final String regex = "^.+(from-\\d{4}-\\d{2}-\\d{2}-to-\\d{4}-\\d{2}-\\d{2})$";
            public static final Pattern PasswordCredentialPattern =
                    Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        }

        public static class Rotation {

            private Rotation(){}

            public static int DefaultValidForDays = 180;
            public static final String  ValidForDays
                    = "ENTERPRISE_APPS_ROTATION_VALID_FOR_DAYS";

            public static final String  StorageAccountSubscription
                    = "ENTERPRISE_APPS_ROTATION_STORAGE_ACCOUNT_SUBSCRIPTION";
            public static final String  StorageAccountResourceGroup
                    = "ENTERPRISE_APPS_ROTATION_STORAGE_ACCOUNT_RESOURCE_GROUP";

            public static final String  StorageAccountName
                    = "ENTERPRISE_APPS_ROTATION_STORAGE_ACCOUNT_NAME";

            public static final String RoleAssignmentExists = "RoleAssignmentExists";
        }
    }


    public static class Patters {

        private Patters(){}

        public static final String OffsetDateTimeWithNanoFormatterPatter = "yyyy-MM-dd'T'HH:mm:ss'.'nX";

        public static final String OffsetDateTimeFormatterPatter = "yyyy-MM-dd'T'HH:mm:ssX";
    }

    public static class DateTimeFormatter{

        public static final java.time.format.DateTimeFormatter OffsetDateTimeFormatter
                = java.time.format.DateTimeFormatter.ofPattern(Patters.OffsetDateTimeFormatterPatter)
        //        java.time.format.DateTimeFormatter.ISO_INSTANT
        ;
        public static final java.time.format.DateTimeFormatter OffsetDateTimeWithNanoFormatter
                = java.time.format.DateTimeFormatter.ofPattern(Patters.OffsetDateTimeWithNanoFormatterPatter);
    }

}
