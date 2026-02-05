#!/bin/bash

set -xe

resourceGroup=rg-secret-rotation
funcName=func-enterprise-app-secret-check
userAssignedIdentity=mi-func-enterprise-app-secret-check

subscriptionId=$(az account show --query id -o tsv)
funcId=$(az webapp show -g ${resourceGroup} -n ${funcName}  --query id  -o tsv)

# build patch body
echo "{ \"properties\": { \"keyVaultReferenceIdentity\": \"/subscriptions/${subscriptionId}/resourcegroups/${resourceGroup}/providers/Microsoft.ManagedIdentity/userAssignedIdentities/${userAssignedIdentity}\"} }" > func.patch.json


# call
az rest --method PATCH --uri "${funcId}?api-version=2021-01-01" --body @func.patch.json
