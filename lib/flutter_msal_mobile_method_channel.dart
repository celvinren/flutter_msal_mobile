import 'dart:convert';
import 'dart:developer';
import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter_msal_mobile/core/extensions.dart';
import 'package:flutter_msal_mobile/models/models.dart';
import 'package:path_provider/path_provider.dart';

import 'flutter_msal_mobile_platform_interface.dart';

/// An implementation of [FlutterMsalMobilePlatform] that uses method channels.
class MethodChannelFlutterMsalMobile extends FlutterMsalMobilePlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('flutter_msal_mobile');

  @override
  Future<void> init({
    required String clientId,
    String? loginHint,
    AndroidConfig? androidConfig,
    IosConfig? iosConfig,
  }) async {
    await _ErrorHandler.guard(() async {
      late final Map<String, dynamic> arguments;

      if (Platform.isAndroid) {
        assert(androidConfig != null, 'Android config can not be null');
        final config =
            await rootBundle.loadString(androidConfig!.configFilePath);
        final map = json.decode(config) as Map<String, dynamic>;
        map['client_id'] = clientId;
        if (androidConfig.tenantId != null) {
          map['authorities'][0]['audience']['tenant_id'] =
              androidConfig.tenantId;
        }

        final directory = await getApplicationDocumentsDirectory();
        final file = File('${directory.path}/msal_auth_config.json');
        await file.writeAsBytes(utf8.encode(json.encode(map)));

        arguments = {'configFilePath': file.path};
      } else if (Platform.isIOS) {
        assert(iosConfig != null, 'iOS config can not be null');
        arguments = <String, dynamic>{
          'clientId': clientId,
          'authority': iosConfig!.authority,
          'authMiddleware': iosConfig.authMiddleware.name,
          'tenantType': iosConfig.tenantType.name,
          'loginHint': loginHint,
        };
      }

      await methodChannel.invokeMethod('initialize', arguments);
    });
  }

  @override
  Future<MsalUser?> acquireToken({required List<String> scopes}) async {
    return _ErrorHandler.guard<MsalUser?>(() async {
      assert(scopes.isNotEmpty, 'Scopes can not be empty');
      final arguments = <String, dynamic>{'scopes': scopes};
      final json = await methodChannel.invokeMethod('acquireToken', arguments);
      if (json != null) {
        return MsalUser.fromJson(jsonDecode(json));
      }
      return null;
    });
  }

  @override
  Future<MsalUser?> acquireTokenSilent({required List<String> scopes}) async {
    return _ErrorHandler.guard<MsalUser?>(() async {
      assert(scopes.isNotEmpty, 'Scopes can not be empty');
      final arguments = <String, dynamic>{'scopes': scopes};
      final json =
          await methodChannel.invokeMethod('acquireTokenSilent', arguments);
      if (json != null) {
        return MsalUser.fromJson(jsonDecode(json));
      }
      return null;
    });
  }

  @override
  Future<void> logout() async {
    await _ErrorHandler.guard(() async {
      if (Platform.isAndroid) {
        await methodChannel.invokeMethod('loadAccounts');
      }
      await methodChannel.invokeMethod('logout', <String, dynamic>{});
    });
  }
}

class _ErrorHandler {
  static Future<T> guard<T>(Future<T> Function() function) async {
    try {
      final result = await function();
      return result;
    } on PlatformException catch (e) {
      if (!kReleaseMode) {
        log('PlatformException error: $e', name: 'flutter_msal_mobile');
      }
      throw e.msalException;
    } catch (e) {
      if (!kReleaseMode) {
        log('Generate error: $e', name: 'flutter_msal_mobile');
      }
      rethrow;
    }
  }
}
