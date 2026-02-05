

resource "azurerm_log_analytics_workspace" "law-secret-rotation" {
  name                = "law-secret-rotation"
  location            = azurerm_resource_group.rg-secret-rotation.location
  resource_group_name = azurerm_resource_group.rg-secret-rotation.name
  tags                = local.common_tags
}