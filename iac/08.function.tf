#data "archive_file" "func-keyvault-secret-rotation-zip" {
#  output_path = "../dist/fnc-keyvault-secret-rotation.zip"
#  type        = "zip"
#
#  source_dir = "../java/keyvault-secret-rotation/target/azure-functions"
#}


resource "azurerm_linux_function_app" "func-keyvault-secret-rotation" {
  name                = "func-keyvault-secret-rotation"
  resource_group_name = azurerm_resource_group.rg-secret-rotation.name
  location            = azurerm_resource_group.rg-secret-rotation.location
  service_plan_id     = azurerm_service_plan.service-plan.id

  storage_account_name       = azurerm_storage_account.st-func-kv-secret-rotation.name
  storage_account_access_key = azurerm_storage_account.st-func-kv-secret-rotation.primary_access_key


  public_network_access_enabled = true

  identity {
    type         = "UserAssigned"
    identity_ids = [azurerm_user_assigned_identity.mi-secret-rotation-keyvault.id]
  }

  app_settings = {
    "FUNCTIONS_WORKER_RUNTIME"                = "java"
    "AzureWebJobsDisableHomepage"             = "true"
    "AZURE_CLIENT_ID"                         = azurerm_user_assigned_identity.mi-secret-rotation-keyvault.client_id
    "EVENT_GRID_LOGIC_APP_MAIL_SEND_ENDPOINT" = azurerm_eventgrid_topic.evt-grid-logicapp-sendmail.endpoint
  }

  site_config {
    application_insights_key = azurerm_application_insights.ai-secret-rotation.instrumentation_key
    application_stack {
      java_version = 17
    }
    cors {
      allowed_origins = ["https://portal.azure.com"]
    }
  }

  virtual_network_subnet_id = azurerm_subnet.sbn-ne-0-x-001-secret.id
  tags                      = local.common_tags
}


#resource "azurerm_monitor_diagnostic_setting" "ds-func-keyvault-secret-rotation" {
#  name               = "ds-func-keyvault-secret-rotation"
#  target_resource_id = azurerm_linux_function_app.func-keyvault-secret-rotation.id
#  log_analytics_workspace_id = azurerm_log_analytics_workspace.law-secret-rotation.id
#
#
#  enabled_log {
#    category = "FunctionAppLogs"
#
#    retention_policy {
#      enabled = false
#    }
#  }
#
#  metric {
#    category = "AllMetrics"
#    enabled  = false
#
#    retention_policy {
#      days    = 0
#      enabled = false
#    }
#  }
#}
#
#
#resource "null_resource" "deploy-func-keyvault-secret-rotation" {
#  provisioner "local-exec" {
#    // add az cli command to add certificate contact to keyvault when it does not exists already
#    //command = "az functionapp deployment source config-zip -g ${azurerm_linux_function_app.func-keyvault-secret-rotation.resource_group_name} -n ${azurerm_linux_function_app.func-keyvault-secret-rotation.name} --src ${data.archive_file.func-keyvault-secret-rotation-zip.output_path}"
#    command = "mvn -V clean package azure-functions:deploy -f ../java/keyvault-secret-rotation"
#  }
#  depends_on = [
#    azurerm_monitor_diagnostic_setting.ds-func-keyvault-secret-rotation
#  ]
#}


#------------------------------------------------------------------------------

resource "azurerm_linux_function_app" "func-enterprise-app-secret-check" {
  name                = "func-enterprise-app-secret-check"
  resource_group_name = azurerm_resource_group.rg-secret-rotation.name
  location            = azurerm_resource_group.rg-secret-rotation.location
  service_plan_id     = azurerm_service_plan.service-plan.id

  storage_account_name       = azurerm_storage_account.st-func-enterprise-app-secret-check.name
  storage_account_access_key = azurerm_storage_account.st-func-enterprise-app-secret-check.primary_access_key

  virtual_network_subnet_id = azurerm_subnet.sbn-ne-0-x-001-secret.id

  identity {
    type         = "UserAssigned"
    identity_ids = [azurerm_user_assigned_identity.mi-secret-rotation-enterprise-app-check.id]
  }

  app_settings = {
    "FUNCTIONS_WORKER_RUNTIME"           = "java"
    "AzureWebJobsDisableHomepage"        = "true"
    "AZURE_CLIENT_ID"                    = azurerm_user_assigned_identity.mi-secret-rotation-enterprise-app-check.client_id
    "EVENT_GRID_ENTERPRISE_APP_ENDPOINT" = azurerm_eventgrid_topic.evt-grid-enterprice-app.endpoint
    "ENTERPRISE_APPS_CHECK"              = '[{"applicationId":"254207e6-8e18-45f5-8f99-922c67c2319a","recipients":"email.01@example.org"},{"applicationId":"a556add5-62a7-441b-9b65-877224ab3735","recipients":"email.02@example.org"}]'
  }

  site_config {
    #   application_insights_key = azurerm_application_insights.ai-secret-rotation.instrumentation_key
    application_stack {
      java_version = 17
    }
    cors {
      allowed_origins = ["https://portal.azure.com"]
    }
  }

  tags = local.common_tags
}

#resource "azurerm_monitor_diagnostic_setting" "ds-func-enterprise-app-secret-check" {
#  name               = "ds-func-enterprise-app-secret-check"
#  target_resource_id = azurerm_linux_function_app.func-enterprise-app-secret-check.id
#  log_analytics_workspace_id = azurerm_log_analytics_workspace.law-secret-rotation.id
#
#
#  enabled_log {
#    category = "FunctionAppLogs"
#
#    retention_policy {
#      enabled = false
#    }
#  }
#
#  metric {
#    category = "AllMetrics"
#    enabled  = false
#
#    retention_policy {
#      days    = 0
#      enabled = false
#    }
#  }
#}
#
#
#resource "null_resource" "deploy-func-enterprise-app-secret-check" {
#  provisioner "local-exec" {
#     command = "mvn -V clean package azure-functions:deploy -f ../java/enterprise-app-secret/enterprise-app-secret-check"
#  }
#  depends_on = [
#    azurerm_monitor_diagnostic_setting.ds-func-enterprise-app-secret-check
#  ]
#}


resource "azurerm_linux_function_app" "func-enterprise-app-secret-rotation" {
  name                = "func-enterprise-app-secret-rotation"
  resource_group_name = azurerm_resource_group.rg-secret-rotation.name
  location            = azurerm_resource_group.rg-secret-rotation.location
  service_plan_id     = azurerm_service_plan.service-plan.id

  storage_account_name       = azurerm_storage_account.st-func-enterprise-app-secret-rotation.name
  storage_account_access_key = azurerm_storage_account.st-func-enterprise-app-secret-rotation.primary_access_key

  identity {
    type         = "UserAssigned"
    identity_ids = [azurerm_user_assigned_identity.mi-secret-rotation-enterprise-app-rotate.id]
  }

  app_settings = {
    "FUNCTIONS_WORKER_RUNTIME"                                = "java"
    "AzureWebJobsDisableHomepage"                             = "true"
    "AZURE_CLIENT_ID"                                         = azurerm_user_assigned_identity.mi-secret-rotation-enterprise-app-rotate.client_id
    "EVENT_GRID_LOGIC_APP_MAIL_SEND_ENDPOINT"                 = azurerm_eventgrid_topic.evt-grid-logicapp-sendmail.endpoint
    "ENTERPRISE_APPS_ROTATION_VALID_FOR_DAYS"                 = 180
    "ENTERPRISE_APPS_ROTATION_STORAGE_ACCOUNT_SUBSCRIPTION"   = data.azurerm_client_config.current.subscription_id
    "ENTERPRISE_APPS_ROTATION_STORAGE_ACCOUNT_RESOURCE_GROUP" = azurerm_storage_account.st-client-secrets.resource_group_name
    "ENTERPRISE_APPS_ROTATION_STORAGE_ACCOUNT_NAME"           = azurerm_storage_account.st-client-secrets.name
    #   "ENTERPRISE_APPS_ROTATION_SCHEDULE"                       = "0 0 */1 * * *"
  }

  site_config {
    application_insights_key = azurerm_application_insights.ai-secret-rotation.instrumentation_key
    application_stack {
      java_version = 17
    }
    cors {
      allowed_origins = ["https://portal.azure.com"]
    }
  }

  tags = local.common_tags

  lifecycle {
    ignore_changes = [tags]
  }
}

#------------------------------------------------------------------------------

