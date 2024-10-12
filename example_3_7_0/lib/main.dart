import 'dart:developer';

import 'package:flutter/material.dart';
import 'package:flutter_msal_mobile/flutter_msal_mobile.dart';
import 'package:flutter_msal_mobile/models/models.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final _clientId = '<CLIENT_ID>';
  final _tenantId = '<TENANT_ID>';
  late final _authority =
      'https://login.microsoftonline.com/$_tenantId/oauth2/v2.0/authorize';
  final _scopes = <String>[
    'https://graph.microsoft.com/user.read',
    // Add other scopes here if required.
  ];

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('MSAL example app'),
        ),
        body: Center(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              ElevatedButton(
                onPressed: getToken,
                child: const Text('Get Token'),
              ),
              const SizedBox(height: 24),
              ElevatedButton(
                onPressed: getTokenSilently,
                child: const Text('Get Token silent'),
              ),
              const SizedBox(height: 24),
              ElevatedButton(
                onPressed: logout,
                child: const Text('Logout'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Future<MsalMobileClient> getMsalAuth() async {
    return MsalMobileClient.init(
      clientId: _clientId,
      scopes: _scopes,
      androidConfig: AndroidConfig(
        configFilePath: 'assets/msal_config.json',
        tenantId: _tenantId,
      ),
      iosConfig: IosConfig(authority: _authority),
    );
  }

  Future<void> getToken() async {
    try {
      final msalAuth = await getMsalAuth();
      final user = await msalAuth.acquireToken();
      log('User data: ${user?.toJson()}');
    } on MsalException catch (e) {
      log('Msal exception with error: ${e.errorMessage}');
    } catch (e) {
      log(e.toString());
    }
  }

  Future<void> getTokenSilently() async {
    try {
      final msalAuth = await getMsalAuth();
      final user = await msalAuth.acquireTokenSilent();
      log('User data: ${user?.toJson()}');
    } on MsalException catch (e) {
      log('Msal exception with error: ${e.errorMessage}');
    } catch (e) {
      log(e.toString());
    }
  }

  Future<void> logout() async {
    try {
      final msalAuth = await getMsalAuth();
      await msalAuth.logout();
    } on MsalException catch (e) {
      log('Msal exception with error: ${e.errorMessage}');
    } catch (e) {
      log(e.toString());
    }
  }
}
