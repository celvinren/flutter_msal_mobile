package com.example.flutter_msal_mobile

import android.content.Context
import com.google.gson.Gson
import com.microsoft.identity.client.AuthenticationCallback
import com.microsoft.identity.client.IAccount
import com.microsoft.identity.client.IAuthenticationResult
import com.microsoft.identity.client.IPublicClientApplication
import com.microsoft.identity.client.SilentAuthenticationCallback
import com.microsoft.identity.client.exception.MsalException
import com.microsoft.identity.client.exception.MsalUiRequiredException
import io.flutter.embedding.android.FlutterActivity
import io.flutter.plugin.common.MethodChannel
import java.io.File

abstract class MsalBase(protected val context: Context, protected var activity: FlutterActivity?) {

    internal lateinit var clientApplication: IPublicClientApplication
    internal lateinit var accountList: List<IAccount>

    fun setFlutterActivity(activity: FlutterActivity?) {
        this.activity = activity
    }

    internal fun isClientInitialized(): Boolean = ::clientApplication.isInitialized

    internal fun getAuthCallback(result: MethodChannel.Result): AuthenticationCallback {
        return object : AuthenticationCallback {
            override fun onSuccess(authenticationResult: IAuthenticationResult) {
                val accountMap = mutableMapOf<String, Any?>()
                authenticationResult.account.claims?.let { accountMap.putAll(it) }
                accountMap["access_token"] = authenticationResult.accessToken
                accountMap["id_token"] = authenticationResult.account.idToken
                accountMap["exp"] = authenticationResult.expiresOn.time
                result.success(Gson().toJson(accountMap))
            }

            override fun onError(exception: MsalException) {
                result.error("AUTH_ERROR", "Authentication failed ${exception.message}", null)
            }

            override fun onCancel() {
                result.error("USER_CANCELED", "User has cancelled the login process", null)
            }
        }
    }

    internal fun getAuthSilentCallback(result: MethodChannel.Result): SilentAuthenticationCallback {
        return object : SilentAuthenticationCallback {
            override fun onSuccess(authenticationResult: IAuthenticationResult) {
                val accountMap = mutableMapOf<String, Any?>()
                authenticationResult.account.claims?.let { accountMap.putAll(it) }
                accountMap["access_token"] = authenticationResult.accessToken
                accountMap["id_token"] = authenticationResult.account.idToken
                accountMap["exp"] = authenticationResult.expiresOn.time
                result.success(Gson().toJson(accountMap))
            }

            override fun onError(exception: MsalException) {
                when (exception) {
                    is MsalUiRequiredException ->
                        result.error("UI_REQUIRED", exception.message, null)

                    else -> result.error("AUTH_ERROR", exception.message, null)
                }
            }
        }
    }

    internal abstract fun initialize(configFilePath: File, result: MethodChannel.Result) // 子类实现

    internal abstract fun signOut(result: MethodChannel.Result)

    internal abstract fun acquireToken(scopes: Array<String>?, result: MethodChannel.Result)

    internal abstract fun acquireTokenSilent(scopes: Array<String>?, result: MethodChannel.Result)

    internal abstract fun loadAccounts(result: MethodChannel.Result)
}
