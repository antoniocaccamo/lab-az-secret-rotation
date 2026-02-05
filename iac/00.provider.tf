

terraform {
  required_providers {
    azurerm = {
      version = "~> 3.66.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.4.3"
    }
  }

  backend "azurerm" {
    resource_group_name  = "rg-terraform-state"
    storage_account_name = "st0terraform0state0000"
    container_name       = "terraform-state-secret-rotation"
    key                  = "terraform.tfstate"
  }

}


