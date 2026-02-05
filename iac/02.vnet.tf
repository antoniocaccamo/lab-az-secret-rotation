

resource "azurerm_network_security_group" "nsg-secret-rotation" {
  name                = "nsg-secret-rotation"
  location            = azurerm_resource_group.rg-secret-rotation.location
  resource_group_name = azurerm_resource_group.rg-secret-rotation.name

}


resource "azurerm_virtual_network" "vnet-secret-rotation" {
  name                = "vnet-ne-0-x-001-secret"
  location            = azurerm_resource_group.rg-secret-rotation.location
  resource_group_name = azurerm_resource_group.rg-secret-rotation.name
  address_space       = ["10.181.54.0/26"]



  tags = local.common_tags
}

resource "azurerm_subnet" "sbn-ne-0-x-001-secret" {
  name                 = "sbn-ne-0-x-001-secret"
  resource_group_name  = azurerm_resource_group.rg-secret-rotation.name
  virtual_network_name = azurerm_virtual_network.vnet-secret-rotation.name
  address_prefixes     = ["10.181.54.0/28"]




#  delegation {
#    name = "sbn-ne-0-x-001-secret-delegation"
#
#    service_delegation {
#      name    = "Microsoft.Web/serverFarms"
#      actions = ["Microsoft.Network/virtualNetworks/subnets/action"]
#    }
#  }


}


resource "azurerm_subnet_network_security_group_association" "sbn-ne-0-x-001-secret-nsg-ass" {
  subnet_id                 = azurerm_subnet.sbn-ne-0-x-001-secret.id
  network_security_group_id = azurerm_network_security_group.nsg-secret-rotation.id
}