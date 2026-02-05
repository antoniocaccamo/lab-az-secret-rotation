

resource "azurerm_eventgrid_system_topic" "evt-grid-keyvault-ext-system-topic" {
  name                   = "evt-grid-keyvault-ext-system-topic"
  resource_group_name    = azurerm_key_vault.kv-secret-rotation-ext.resource_group_name
  location               = azurerm_key_vault.kv-secret-rotation-ext.location
  source_arm_resource_id = azurerm_key_vault.kv-secret-rotation-ext.id
  topic_type             = "Microsoft.KeyVault.vaults"
  tags                   = local.common_tags

  depends_on = [
    azurerm_linux_function_app.func-keyvault-secret-rotation
  ]
}


#------------------------------------------------------------------------------

resource "azurerm_eventgrid_topic" "evt-grid-enterprice-app" {
  name                = "evt-grid-enterprise-app-topic"
  resource_group_name = azurerm_resource_group.rg-secret-rotation.name
  location            = azurerm_resource_group.rg-secret-rotation.location
  input_schema        = "EventGridSchema"
  tags                = local.common_tags

  depends_on = [
    azurerm_linux_function_app.func-enterprise-app-secret-rotation
  ]
}


resource "azurerm_eventgrid_topic" "evt-grid-logicapp-sendmail" {
  name                = "evt-grid-sendmail-topic"
  resource_group_name = azurerm_resource_group.rg-secret-rotation.name
  location            = azurerm_resource_group.rg-secret-rotation.location
  input_schema        = "EventGridSchema"
  tags                = local.common_tags
}


#------------------------------------------------------------------------------

resource "azurerm_role_assignment" "evt-grid-enterprice-app-role-assignment-00" {
  principal_id         = azurerm_user_assigned_identity.mi-secret-rotation-enterprise-app-check.principal_id
  scope                = azurerm_eventgrid_topic.evt-grid-enterprice-app.id
  role_definition_name = "EventGrid Data Sender"

}

#------------------------------------------------------------------------------

resource "azurerm_role_assignment" "evt-grid-logicapp-sendmail-role-assignment-00" {
  principal_id         = azurerm_user_assigned_identity.mi-secret-rotation-keyvault.principal_id
  scope                = azurerm_eventgrid_topic.evt-grid-logicapp-sendmail.id
  role_definition_name = "EventGrid Data Sender"
}

resource "azurerm_role_assignment" "evt-grid-logicapp-sendmail-role-assignment-01" {
  principal_id         = azurerm_user_assigned_identity.mi-secret-rotation-enterprise-app-rotate.principal_id
  scope                = azurerm_eventgrid_topic.evt-grid-logicapp-sendmail.id
  role_definition_name = "EventGrid Data Sender"
}