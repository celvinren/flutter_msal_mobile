package com.example.flutter_msal_mobile

import androidx.annotation.NonNull

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import java.io.File

/** FlutterMsalMobilePlugin */
class FlutterMsalMobilePlugin: FlutterPlugin, ActivityAware, MethodChannel.MethodCallHandler {
  private lateinit var channel: MethodChannel
  private var msalBase: MsalBase? = null
  private var applicationContext: Context? = null
  private var activity: FlutterActivity? = null

  override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
      channel = MethodChannel(binding.binaryMessenger, "flutter_msal_mobile")
      channel.setMethodCallHandler(this)
      applicationContext = binding.applicationContext
  }

  override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
      when (call.method) {
          "initialize" -> {
              val configFilePath: String? = call.argument("configFilePath")
              if (configFilePath != null) {
                  // Make sure context is initialized
                  if (applicationContext != null) {
                      initializeMsal(applicationContext!!, File(configFilePath), result)
                  } else {
                      result.error(
                              "CONTEXT_ERROR",
                              "Application context is not initialized",
                              null
                      )
                  }
              } else {
                  result.error("CONFIG_ERROR", "Config file path is missing", null)
              }
          }
          "acquireToken" -> {
              val scopes: ArrayList<String>? = call.argument("scopes")
              if (scopes != null) {
                  msalBase?.acquireToken(scopes.toTypedArray(), result) // Convert to Array<String>
              } else {
                  result.error("SCOPE_ERROR", "Scopes must be provided", null)
              }
          }
          "acquireTokenSilent" -> {
              val scopes: ArrayList<String>? = call.argument("scopes")
              if (scopes != null) {
                  msalBase?.acquireTokenSilent(scopes.toTypedArray(), result) // Convert to Array<String>
              } else {
                  result.error("SCOPE_ERROR", "Scopes must be provided", null)
              }
          }
          "loadAccounts" -> msalBase?.loadAccounts(result)
          "logout" -> msalBase?.signOut(result)
          else -> result.notImplemented()
      }
  }

  private fun initializeMsal(context: Context, configFile: File, result: MethodChannel.Result) {
      // According to account_mode in config file to init Single or Multiple Account
      val accountMode = readAccountMode(configFile)

      msalBase =
              if (accountMode == "MULTIPLE") {
                  MsalMultiple(context, activity)
              } else {
                  MsalSingle(context, activity)
              }

      msalBase?.initialize(configFile, result)
  }

  private fun readAccountMode(configFile: File): String {
      // Read config file to chekc account_mode is MULTIPLE or SINGLE
      val json = configFile.readText()
      val mapType = object : TypeToken<Map<String, Any>>() {}.type
      val configMap: Map<String, Any> = Gson().fromJson(json, mapType)
      return (configMap["account_mode"] as String?)?.uppercase() ?: "SINGLE" // Default to SINGLE
    }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
      channel.setMethodCallHandler(null)
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
      activity = binding.activity as? FlutterActivity
      msalBase?.setFlutterActivity(activity) // set FlutterActivity to msalBase
  }

  override fun onDetachedFromActivityForConfigChanges() {
      onDetachedFromActivity()
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
      onAttachedToActivity(binding)
  }

  override fun onDetachedFromActivity() {
      activity = null
      msalBase?.setFlutterActivity(null) // when activity detached，clean Activity
  }
}
