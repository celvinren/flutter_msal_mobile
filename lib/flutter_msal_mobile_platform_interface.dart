import 'package:flutter_msal_mobile/models/models.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'flutter_msal_mobile_method_channel.dart';

abstract class FlutterMsalMobilePlatform extends PlatformInterface {
  /// Constructs a FlutterMsalMobilePlatform.
  FlutterMsalMobilePlatform() : super(token: _token);

  static final Object _token = Object();

  static FlutterMsalMobilePlatform _instance = MethodChannelFlutterMsalMobile();

  /// The default instance of [FlutterMsalMobilePlatform] to use.
  ///
  /// Defaults to [MethodChannelFlutterMsalMobile].
  static FlutterMsalMobilePlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FlutterMsalMobilePlatform] when
  /// they register themselves.
  static set instance(FlutterMsalMobilePlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  /// Initializes MSAL with required data.
  Future<void> init({
    required String clientId,
    String? loginHint,
    AndroidConfig? androidConfig,
    IosConfig? iosConfig,
  });

  /// Acquire a token interactively for the given [scopes]
  /// return [UserAdModel] contains user information but token and expiration date
  Future<MsalUser?> acquireToken({required List<String> scopes});

  /// Acquire a token silently, with no user interaction, for the given [scopes]
  /// return [UserAdModel] contains user information but token and expiration date
  Future<MsalUser?> acquireTokenSilent({required List<String> scopes});

  /// Logout user from Microsoft account.
  Future<void> logout();
}
