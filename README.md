# Project Requirements

* Flutter version greater than 3.7.0
* Android minSdkVersion of at least 24
* iOS version of at least 14.0

# Installation

Add the following to your pubspec.yaml or run 'dart pub add flutter_msal_mobile'
```yaml
dependencies:
  flutter_msal_mobile: ^[version]
```

# Android Setup

This package supports both Single and Multiple Account modes, automatically selecting based on user configuration. Single Account mode is ideal for apps needing one active user, while Multiple Account mode enables seamless switching between accounts if tokens are valid. Re-authentication is required only if both access and refresh tokens have expired.

To optimize Multiple Account mode:

- Use Refresh Tokens: Ensure token renewal to avoid disruptions.
- Adjust Token Lifetimes: Configure longer lifetimes in Azure AD for easier account switching.

The following is the android setup steps:

1. Open **android > app > src > main** > **AndroidManifest.xml**.
2. Add BrowserTabActivity to your AndroidManifest.xml file.
    * `[your-base64-signature-hash]` should be replaced with the base64 signature hash you generated while setting up your Android app in the Azure app registration.
    * `[your-package-name]` should be replaced with the Android package name you entered when setting up the Android app in your Azure app registration.
```xml
<activity android:name="com.microsoft.identity.client.BrowserTabActivity">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data
            android:host="[your-package-name]"
            android:path="/[your-base64-signature-hash]"
            android:scheme="msauth" />
    </intent-filter>
</activity>
```
3. Make sure your Android app has internet access by adding the following just above **\<application\>**
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```
4. Create a new JSON file in your Flutter project and populate it with the configuration that was generated for you by Azure when you added the Android platform to your app registration.  The file can be called anything and placed anywhere in your project.  This was added as **assets/auth_config.json** in the example project.  The default configuration provided by Azure should look something like this:
```json
{
  "client_id" : "00000000-0000-0000-0000-000000000000",
  "authorization_user_agent" : "DEFAULT",
  "redirect_uri" : "msauth://[your-package-name]/[my-url-encoded-base64-signature-hash]",
  "authorities" : [
    {
      "type": "AAD",
      "audience": {
       "type": "AzureADMultipleOrgs",
       "tenant_id": "organizations"
      }
    }
  ]
}
```
5. Add the JSON asset file to the pubspec.yaml file.
```yaml
assets
    - assets/auth_config.json
```

# iOS Setup
1. Add `"ios_redirect_uri"` value to the auth_config.json file created during the Android setup.
```json
{
  "client_id" : "<app-registration-client-id>",
  "authorization_user_agent" : "DEFAULT",
  "redirect_uri" : "msauth://<your-package-name>/<url-encoded-package-signature-hash>",
  "ios_redirect_uri": "msauth.<your-ios-bundle-identifier>://auth",
  "account_mode": "SINGLE",
  "authorities" : [
    {
      "type": "AAD",
      "audience": {
       "type": "AzureADMyOrg",
       "tenant_id": "organizations"
      }
    }
  ],
  "logging": {
    "pii_enabled": false
  }
}
```
2. Set the iOS platform target version to a version >= 14.0 by opening the properties window of the Runner.xcodeproj file in Xcode.
3. In the Signing and Capabilities section, add the Keychain Sharing capability.
4. Add com.microsoft.adalcache as a keychain group.
5. Add the following to the Info.plist file in **\<dict\>** by right clicking the file and opening as source. Replace `[your-bundle-identifier]` with the iOS bundle identifier identified during the iOS platform setup portion of the app registration setup.
```xml
<key>CFBundleURLTypes</key>
<array>
    <dict>
        <key>CFBundleURLSchemes</key>
        <array>
            <string>msauth.[your-bundle-identifier]</string>
        </array>
    </dict>
</array>
```
6. Add the following to the Info.plist in **\<dict\>** to enable the use of Microsoft Authenticator if available:
```xml
<array>
    <string>msauthv2</string>
    <string>msauthv3</string>
</array>
```

# Usage

Import flutter_msal_mobile package
```dart
import 'package:flutter_msal_mobile/flutter_msal_mobile.dart';
```

Initialize MSAL, replace [clientId], [scopes], [tenantId], [authority] with yours.
```dart
    FlutterMsal.init(
      clientId: [clientId],
      scopes: [scopes],
      androidConfig: AndroidConfig(
        configFilePath: 'assets/msal_config.json',
        tenantId: [tenantId],
      ),
      iosConfig: IosConfig(authority: [authority]),
    )
```

## APIs

Sign in and get token
```dart
await msal.acquireToken().then((MsalUser result) {
    print('access token (truncated): ${result.accessToken}');
})
```

Get token silently
```dart
await msal.acquireTokenSilent().then((MsalUser result) {
    print('access token (truncated): ${result.accessToken}');
})
```

Sign out
```dart
await msal.signOut()
```

## Error Handling
This package provides a set of custom exceptions to simplify error handling when working with MSAL authentication. The main exception types you may encounter are:

1. MsalException: A general exception for authentication errors.
2. MsalUserCanceledException: Thrown when the user cancels the authentication process.
3. MsalUiRequiredException: Indicates that a UI prompt is needed to complete authentication.

To handle these exceptions effectively, you can use a catchError block in your code as shown below:
```dart
await msal.acquireToken().then((MsalUser user) {
    if (user != null) {
        print('current account id: ${result.oid}');
    }
}).catchError((exception) {
    if (exception is MsalUserCanceledException) {
        print('User cancelled the request: ${exception.errorMessage}');
    } else if (exception is MsalUiRequiredException) {
        print('UI prompt required to acquire token: ${exception.errorMessage}');
    } else if (exception is MsalException) {
        print('General MSAL error: ${exception.errorMessage}');
    } else {
        print('An unexpected exception occurred.');
    }
});
```
This approach enables you to provide tailored responses based on the specific exception type, making it easier to handle user cancellations, required UI prompts, and other authentication errors.