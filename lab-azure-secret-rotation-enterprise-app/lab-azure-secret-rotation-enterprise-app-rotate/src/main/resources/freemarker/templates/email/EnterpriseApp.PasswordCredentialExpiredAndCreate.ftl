<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
</head>
<body>
<p>
    The application <a href="https://portal.azure.com/#view/Microsoft_AAD_RegisteredApps/ApplicationMenuBlade/~/Credentials/appId/${ApplicationAppId}"><b>${ApplicationDisplayName}</b><a/>
    <br/>
    has password credential <b>${PasswordCredentialDisplayName}</b> that is expired on ${EXP}.
</p>

<p>
    A new one called <b>${NewPasswordCredentialDisplayName}</b> has been created on and will expire on ${NewEXP},<br/>
    and its value could be found at </p>
<p>
<dl>
    <dt>storageAccount</dt>
    <dd>${storageAccount}</dd>
    <dt>containerName</dt>
    <dd>${containerName}</dd>
    <dt>blob</dt>
    <dd>${blobName}</dd>
</dl>

</p>
</body>
</html>