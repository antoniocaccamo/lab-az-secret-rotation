

resource "azurerm_key_vault" "kv-secret-rotation-ext" {
  resource_group_name        = azurerm_resource_group.rg-secret-rotation-ext.name
  location                   = azurerm_resource_group.rg-secret-rotation-ext.location
  name                       = "kvsecretrotation${random_string.unique.result}"
  sku_name                   = "standard"
  tenant_id                  = local.tenant_id
  soft_delete_retention_days = 7
  purge_protection_enabled   = false

  enable_rbac_authorization = true

  lifecycle {
    ignore_changes = [contact]
  }

  tags = local.common_tags
}


resource "azurerm_key_vault_secret" "secret-near-expiry" {
  key_vault_id    = azurerm_key_vault.kv-secret-rotation-ext.id
  name            = "secret-near-expiry"
  value           = "whaaaaat,secret-near-expiry"
  expiration_date = "2023-10-30T11:00:00Z"
  tags = merge(
    local.common_tags,
    tomap({
      "endusers-recipients" = "antonio.caccamo@outlook.com;caccamo.antonio@gmail.com"
    })
  )

  depends_on = [
    azurerm_role_assignment.kv-secret-rotation-role-assignment-00
  ]
}

resource "azurerm_key_vault_secret" "secret-expired" {
  key_vault_id    = azurerm_key_vault.kv-secret-rotation-ext.id
  name            = "secret-expired"
  value           = "whaaaaat,secret-expired"
  expiration_date = "2023-10-02T11:00:00Z"
  tags = merge(
    local.common_tags,
    tomap({
      "endusers-recipients" = "antonio.caccamo@outlook.com;caccamo.antonio@gmail.com"
    })
  )

  depends_on = [
    azurerm_role_assignment.kv-secret-rotation-role-assignment-00
  ]
}

#------------------------------------------------------------------------------

resource "azurerm_role_assignment" "kv-rbac_role-00" {
  scope              = azurerm_key_vault.kv-secret-rotation-ext.id
  role_definition_id = "/subscriptions/${data.azurerm_client_config.current.subscription_id}/providers/Microsoft.Authorization/roleDefinitions/a4417e6f-fecd-4de8-b567-7b0420556985"
  principal_id       = data.azurerm_client_config.current.object_id
}


resource "azurerm_role_assignment" "kv-secret-rotation-role-assignment-00" {
  principal_id         = data.azurerm_client_config.current.object_id
  scope                = azurerm_key_vault.kv-secret-rotation-ext.id
  role_definition_name = "Key Vault Secrets Officer"
}

resource "azurerm_role_assignment" "kv-secret-rotation-role-assignment-01" {
  principal_id         = azurerm_user_assigned_identity.mi-secret-rotation-keyvault.principal_id
  scope                = azurerm_key_vault.kv-secret-rotation-ext.id
  role_definition_name = "Key Vault Secrets Officer"
}

#------------------------------------------------------------------------------

resource "null_resource" "add_contacts" {
  provisioner "local-exec" {
    // add az cli command to add certificate contact to keyvault when it does not exists already
    command = "az keyvault certificate contact list --vault-name kvsecretrotation${random_string.unique.result} | jq -r '.[].emailAddress' | grep -q ${local.support_email} || az keyvault certificate contact add --vault-name kvsecretrotation${random_string.unique.result} --email \"${local.support_email}\" --name \"${local.support_name}\""
    when    = create
  }
  depends_on = [
    azurerm_role_assignment.kv-rbac_role-00
  ]
}