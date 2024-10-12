// You have generated a new plugin project without specifying the `--platforms`
// flag. A plugin project with no platform support was generated. To add a
// platform, run `flutter create -t plugin --platforms <platforms> .` under the
// same directory. You can also find a detailed instruction on how to add
// platforms in the `pubspec.yaml` at
// https://flutter.dev/docs/development/packages-and-plugins/developing-packages#plugin-platforms.

import 'flutter_msal_mobile_platform_interface.dart';
import 'models/models.dart';

class MsalMobileClient {
  final List<String> _scopes;

  MsalMobileClient._create({required List<String> scopes}) : _scopes = scopes;

  /// Initializes MSAL with required data.
  static Future<MsalMobileClient> init({
    required String clientId,
    required List<String> scopes,
    String? loginHint,
    AndroidConfig? androidConfig,
    IosConfig? iosConfig,
  }) async {
    await FlutterMsalMobilePlatform.instance.init(
      clientId: clientId,
      loginHint: loginHint,
      androidConfig: androidConfig,
      iosConfig: iosConfig,
    );
    return MsalMobileClient._create(scopes: scopes);
  }

  /// Acquire a token interactively for the given [scopes]
  /// return [UserAdModel] contains user information but token and expiration date
  Future<MsalUser?> acquireToken() async =>
      FlutterMsalMobilePlatform.instance.acquireToken(scopes: _scopes);

  /// Acquire a token silently, with no user interaction, for the given [scopes]
  /// return [UserAdModel] contains user information but token and expiration date
  Future<MsalUser?> acquireTokenSilent() async =>
      FlutterMsalMobilePlatform.instance.acquireTokenSilent(scopes: _scopes);

  /// Logout user from Microsoft account.
  Future<void> logout() async => FlutterMsalMobilePlatform.instance.logout();
}
