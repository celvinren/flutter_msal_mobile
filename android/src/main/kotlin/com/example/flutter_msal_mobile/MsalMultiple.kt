package com.example.flutter_msal_mobile

import android.content.Context
import com.microsoft.identity.client.AcquireTokenParameters
import com.microsoft.identity.client.AcquireTokenSilentParameters
import com.microsoft.identity.client.IAccount
import com.microsoft.identity.client.IMultipleAccountPublicClientApplication
import com.microsoft.identity.client.IPublicClientApplication
import com.microsoft.identity.client.IPublicClientApplication.IMultipleAccountApplicationCreatedListener
import com.microsoft.identity.client.Prompt
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.exception.MsalException
import io.flutter.embedding.android.FlutterActivity
import io.flutter.plugin.common.MethodChannel
import java.io.File

class MsalMultiple(context: Context, activity: FlutterActivity?) : MsalBase(context, activity) {

    private fun getApplicationCreatedListener(
            result: MethodChannel.Result
    ): IMultipleAccountApplicationCreatedListener {

        return object : IMultipleAccountApplicationCreatedListener {
            override fun onCreated(application: IMultipleAccountPublicClientApplication) {
                clientApplication = application
                result.success(true)
            }

            override fun onError(exception: MsalException?) {
                result.error("AUTH_ERROR", exception?.message, null)
            }
        }
    }

    override fun initialize(configFilePath: File, result: MethodChannel.Result) {
        PublicClientApplication.createMultipleAccountPublicClientApplication(
                context,
                configFilePath,
                getApplicationCreatedListener(result)
        )
    }

    override fun loadAccounts(result: MethodChannel.Result) {
        val multipleAccountApp = clientApplication as IMultipleAccountPublicClientApplication
        multipleAccountApp.getAccounts(
                object : IPublicClientApplication.LoadAccountsCallback {
                    override fun onTaskCompleted(accounts: List<IAccount>) {
                        accountList = accounts
                        result.success(accounts.map { it.username }) // Return a list of usernames
                    }

                    override fun onError(exception: MsalException?) {
                        result.error(
                                "AUTH_ERROR",
                                "Failed to load accounts: ${exception?.message}",
                                null
                        )
                    }
                }
        )
    }

    override fun signOut(result: MethodChannel.Result) {
        if (!isClientInitialized()) {
            result.error(
                    "AUTH_ERROR",
                    "Client must be initialized before attempting to sign out",
                    null
            )
            return
        }

        if (accountList.isEmpty()) {
            result.error("AUTH_ERROR", "No account to sign out", null)
            return
        }

        val multipleAccountApp = clientApplication as IMultipleAccountPublicClientApplication
        multipleAccountApp.removeAccount(
                accountList.first(),
                object : IMultipleAccountPublicClientApplication.RemoveAccountCallback {
                    override fun onRemoved() {
                        loadAccounts(result)
                    }

                    override fun onError(exception: MsalException) {
                        result.error("AUTH_ERROR", exception.message, null)
                    }
                }
        )
    }

    override fun acquireToken(scopes: Array<String>?, result: MethodChannel.Result) {
        if (!isClientInitialized()) {
            result.error(
                    "AUTH_ERROR",
                    "Client must be initialized before attempting to acquire a token.",
                    null
            )
            return
        }

        if (scopes == null) {
            result.error("AUTH_ERROR", "Scopes must be provided", null)
            return
        }

        val builder = AcquireTokenParameters.Builder()
        builder.withScopes(scopes.toList())
                .withPrompt(Prompt.LOGIN)
                .withCallback(getAuthCallback(result))
                .startAuthorizationFromActivity(activity)

        clientApplication.acquireToken(builder.build())
    }

    override fun acquireTokenSilent(scopes: Array<String>?, result: MethodChannel.Result) {
        if (!isClientInitialized()) {
            result.error(
                    "AUTH_ERROR",
                    "Client must be initialized before attempting to acquire a silent token.",
                    null
            )
            return
        }

        if (scopes == null) {
            result.error("AUTH_ERROR", "Scopes must be provided", null)
            return
        }

        if (accountList.isEmpty()) {
            result.error("AUTH_ERROR", "No account available for silent authentication", null)
            return
        }

        val selectedAccount = accountList.first()
        val builder = AcquireTokenSilentParameters.Builder()
        builder.withScopes(scopes.toList())
                .forAccount(selectedAccount)
                .fromAuthority(selectedAccount.authority)
                .withCallback(getAuthSilentCallback(result))

        clientApplication.acquireTokenSilentAsync(builder.build())
    }
}
