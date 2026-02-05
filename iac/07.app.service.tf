
resource "azurerm_service_plan" "service-plan" {
  name                = "service-plan-secret-rotation"
  resource_group_name = azurerm_resource_group.rg-secret-rotation.name
  location            = azurerm_resource_group.rg-secret-rotation.location
  os_type             = "Linux"
  sku_name            = "B1"

  tags = local.common_tags
}

resource "azurerm_application_insights" "ai-secret-rotation" {
  name                = "ai-secret-rotation"
  location            = azurerm_resource_group.rg-secret-rotation.location
  resource_group_name = azurerm_resource_group.rg-secret-rotation.name
  application_type    = "java"

  #  workspace_id = azurerm_log_analytics_workspace.law-secret-rotation.id

  tags = local.common_tags
}

