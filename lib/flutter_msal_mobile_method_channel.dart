import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'flutter_msal_mobile_platform_interface.dart';

/// An implementation of [FlutterMsalMobilePlatform] that uses method channels.
class MethodChannelFlutterMsalMobile extends FlutterMsalMobilePlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('flutter_msal_mobile');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
