
#------------------------------------------------------------------------------

output "FUNC_KEYVAULT_SECRET_ROTATION_RG" {
  value = azurerm_linux_function_app.func-keyvault-secret-rotation.resource_group_name
}

output "FUNC_KEYVAULT_SECRET_ROTATION_LOCATION" {
  value = azurerm_linux_function_app.func-keyvault-secret-rotation.location
}

output "FUNC_KEYVAULT_SECRET_ROTATION_APP_SERVICE_PLAN" {
  value = azurerm_linux_function_app.func-keyvault-secret-rotation.service_plan_id
}

output "FUNC_KEYVAULT_SECRET_ROTATION_NAME" {
  value = azurerm_linux_function_app.func-keyvault-secret-rotation.name
}


#------------------------------------------------------------------------------