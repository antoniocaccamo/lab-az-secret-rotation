data "azurerm_client_config" "current" {}



locals {
  base_name     = "secret-rotation"
  location      = "northeurope"
  tenant_id     = data.azurerm_client_config.current.tenant_id
  support_name  = "antonio caccamo"
  support_email = "caccamo.antonio.@gmail.com"
  common_tags = {
    env       = "development"
    container = "cloud-security"
    project   = "secret-rotation"
    source    = "terraform"
  }
}



