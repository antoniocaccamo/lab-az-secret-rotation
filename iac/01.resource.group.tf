resource "random_string" "unique" {
  length  = 6
  special = false
  upper   = false
}


resource "azurerm_resource_group" "rg-secret-rotation" {
  name     = "rg-ne-0-x-002-secret"
  location = local.location
  tags     = local.common_tags
}

resource "azurerm_resource_group" "rg-secret-rotation-ext" {
  name     = "rg-ne-0-x-002-secret-ext"
  location = local.location
  tags     = local.common_tags
}

#resource "azurerm_resource_group" "rg-secret-logs" {
#  name     = "rg-secret-logs"
#  location =  local.location
#  tags = local.common_tags
#}
#
##------------------------------------------------------------------------------
#
#resource "azurerm_resource_group" "rg-secret-rotation-keyvault" {
#  name     = "rg-secret-rotation-keyvault"
#  location = local.location
#  tags = local.common_tags
#}
#

resource "azurerm_user_assigned_identity" "mi-secret-rotation-keyvault" {
  name                = "mi-secret-rotation-keyvault"
  location            = azurerm_resource_group.rg-secret-rotation.location
  resource_group_name = azurerm_resource_group.rg-secret-rotation.name
}

#------------------------------------------------------------------------------




resource "azurerm_user_assigned_identity" "mi-secret-rotation-enterprise-app-check" {
  name                = "mi-secret-rotation-enterprise-app-check"
  location            = azurerm_resource_group.rg-secret-rotation.location
  resource_group_name = azurerm_resource_group.rg-secret-rotation.name
}


resource "azurerm_user_assigned_identity" "mi-secret-rotation-enterprise-app-rotate" {
  name                = "mi-secret-rotation-enterprise-app-rotate"
  location            = azurerm_resource_group.rg-secret-rotation.location
  resource_group_name = azurerm_resource_group.rg-secret-rotation.name
}


#
##------------------------------------------------------------------------------
#
#resource "azurerm_resource_group" "rg-secret-logicapp-sendmail" {
#  name     = "rg-secret-logicapp-sendmail"
#  location = local.location
#  tags = local.common_tags
#}

#resource "azurerm_user_assigned_identity" "mi-secret-logicapp-sendmail" {
#  name                = "mi-secret-rotation-enterprise-app-rotate"
#  location            = azurerm_resource_group.rg-secret-logicapp-sendmail.location
#  resource_group_name = azurerm_resource_group.rg-secret-logicapp-sendmail.name
#}