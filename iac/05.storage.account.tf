


resource "azurerm_storage_account" "st-client-secrets" {
  name                     = "stclientsecrets${random_string.unique.result}"
  resource_group_name      = azurerm_resource_group.rg-secret-rotation.name
  location                 = azurerm_resource_group.rg-secret-rotation.location
  account_tier             = "Standard"
  account_replication_type = "LRS"

  public_network_access_enabled   = true
  enable_https_traffic_only       = true
  default_to_oauth_authentication = true
  shared_access_key_enabled       = true
  allow_nested_items_to_be_public = false


  tags = local.common_tags
}


#------------------------------------------------------------------------------

resource "azurerm_storage_account" "st-func-kv-secret-rotation" {
  name                     = "stfunckvsecrot${random_string.unique.result}"
  resource_group_name      = azurerm_resource_group.rg-secret-rotation.name
  location                 = azurerm_resource_group.rg-secret-rotation.location
  account_tier             = "Standard"
  account_replication_type = "LRS"

  tags = local.common_tags
}


#------------------------------------------------------------------------------

resource "azurerm_storage_account" "st-func-enterprise-app-secret-check" {
  name                     = "stfunceasecchk${random_string.unique.result}"
  resource_group_name      = azurerm_resource_group.rg-secret-rotation.name
  location                 = azurerm_resource_group.rg-secret-rotation.location
  account_tier             = "Standard"
  account_replication_type = "LRS"

  tags = local.common_tags
}

resource "azurerm_storage_account" "st-func-enterprise-app-secret-rotation" {
  name                     = "stfunceasecrot${random_string.unique.result}"
  resource_group_name      = azurerm_resource_group.rg-secret-rotation.name
  location                 = azurerm_resource_group.rg-secret-rotation.location
  account_tier             = "Standard"
  account_replication_type = "LRS"

  tags = local.common_tags
}


#------------------------------------------------------------------------------

resource "azurerm_role_assignment" "st-client-secrets-role-assignment-enterprise-app-rotate-00" {
  principal_id         = azurerm_user_assigned_identity.mi-secret-rotation-enterprise-app-rotate.principal_id
  scope                = azurerm_storage_account.st-client-secrets.id
  role_definition_name = "Owner"
}

resource "azurerm_role_assignment" "st-client-secrets-role-assignment-enterprise-app-rotate-01" {
  principal_id         = azurerm_user_assigned_identity.mi-secret-rotation-enterprise-app-rotate.principal_id
  scope                = azurerm_storage_account.st-client-secrets.id
  role_definition_name = "Storage Blob Data Contributor"
}