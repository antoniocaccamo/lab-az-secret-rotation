#!/bin/bash

set -xe

# get id for  User Assigned Identity principal
userAssignedIdentity="mi-secret-rotation-enterprise-app-check"
principalId=$(az ad sp list --display-name $userAssignedIdentity  --query "[0].id" -o tsv)

# get id for
# graphId=$(az ad sp list --query "[?appDisplayName=='Microsoft Graph'].id | [0]" -o tsv --all)
graphId=$(az ad sp list --display-name 'Microsoft Graph' --query  "[0].id" -o tsv --all)

# retrieve id for require Microsoft Graph permissions
appRoles=( "User.Read.All" "Application.Read.All")
allowedMemberTypes=Application

# appRole loop
for appRole in "${appRoles[@]}"; do
  echo "assign \"$appRole\" to user assigned managed identity \"$userAssignedIdentity\""
  appRoleId=$(az ad sp list --display-name "Microsoft Graph" --query "[0].appRoles[?value=='$appRole' && contains(allowedMemberTypes, '$allowedMemberTypes')].id" -o tsv)
  echo "{ \"principalId\" : \"$principalId\", \"resourceId\"  : \"$graphId\", \"appRoleId\"   : \"$appRoleId\" }" > assign."$appRole".json
  cat << EOF > assign.${appRole}.json
  {
    "principalId" : "$principalId", "resourceId"  : "$graphId", "appRoleId"   : "$appRoleId"
  }
  EOF
  az rest --method post \
    --uri https://graph.microsoft.com/v1.0/servicePrincipals/$principalId/appRoleAssignments \
    --body @assign.$appRole.json \
    --headers Content-Type=application/json
done