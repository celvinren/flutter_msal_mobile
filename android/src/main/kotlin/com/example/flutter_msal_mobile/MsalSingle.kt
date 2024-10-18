package com.example.flutter_msal_mobile

import android.content.Context
import com.microsoft.identity.client.AcquireTokenParameters
import com.microsoft.identity.client.AcquireTokenSilentParameters
import com.microsoft.identity.client.IAccount
import com.microsoft.identity.client.IPublicClientApplication
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.Prompt
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.exception.MsalException
import io.flutter.embedding.android.FlutterActivity
import io.flutter.plugin.common.MethodChannel
import java.io.File

class MsalSingle(context: Context, activity: FlutterActivity?) : MsalBase(context, activity) {

    private fun getApplicationCreatedListener(
            result: MethodChannel.Result
    ): IPublicClientApplication.ISingleAccountApplicationCreatedListener {
        return object : IPublicClientApplication.ISingleAccountApplicationCreatedListener {
            override fun onCreated(application: ISingleAccountPublicClientApplication) {
                clientApplication = application
                result.success(true)
            }

            override fun onError(exception: MsalException?) {
                result.error("AUTH_ERROR", exception?.message, null)
            }
        }
    }

    override fun initialize(configFilePath: File, result: MethodChannel.Result) {
        if (isClientInitialized()) {
            result.success(true)
            return
        }

        PublicClientApplication.createSingleAccountPublicClientApplication(
            context,
            configFilePath,
            getApplicationCreatedListener(result)
        )
    }

    override fun loadAccounts(result: MethodChannel.Result) {
        val singleAccountApp = clientApplication as ISingleAccountPublicClientApplication
        singleAccountApp.getCurrentAccountAsync(
            object : ISingleAccountPublicClientApplication.CurrentAccountCallback {
                override fun onAccountLoaded(account: IAccount?) {
                    if (account != null) {
                        result.success(account.username)
                    }
                    else {
                        result.error("AUTH_ERROR", "Failed to load account: No signed in account", null)
                    }
                }

                override fun onAccountChanged(
                    priorAccount: IAccount?,
                    currentAccount: IAccount?
                ) {
                    result.success(currentAccount?.username)
                }

                override fun onError(exception: MsalException) {
                    result.error(
                        "AUTH_ERROR",
                        "Failed to load account: ${exception.message}",
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

        val singleAccountApp = clientApplication as ISingleAccountPublicClientApplication
        singleAccountApp.signOut(
            object : ISingleAccountPublicClientApplication.SignOutCallback {
                override fun onSignOut() {
                    result.success(true)
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

        val singleAccountApp = clientApplication as ISingleAccountPublicClientApplication

        // Async function to get the current account
        singleAccountApp.getCurrentAccountAsync(object : ISingleAccountPublicClientApplication.CurrentAccountCallback {
            override fun onAccountLoaded(account: IAccount?) {
                if (account == null) {
                    result.error("AUTH_ERROR", "No account available for silent authentication", null)
                    return
                }

                val builder = AcquireTokenSilentParameters.Builder()
                builder.withScopes(scopes.toList())
                    .forAccount(account) // Use the account object to acquire token silently
                    .fromAuthority(account.authority)
                    .withCallback(getAuthSilentCallback(result))

                clientApplication.acquireTokenSilentAsync(builder.build())
            }

            override fun onAccountChanged(priorAccount: IAccount?, currentAccount: IAccount?) {
                // If account changes, try to acquire token silently again
            }

            override fun onError(exception: MsalException) {
                result.error("AUTH_ERROR", "Error fetching current account: ${exception.message}", null)
            }
        })
    }
}
