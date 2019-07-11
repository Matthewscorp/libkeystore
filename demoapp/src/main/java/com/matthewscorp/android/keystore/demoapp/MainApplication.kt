package com.matthewscorp.android.keystore.demoapp

import android.app.Application
import com.matthewscorp.android.keystore.LibKeyStore
import com.matthewscorp.android.keystore.demoapp.Credentials.ALIAS_KEY

class MainApplication : Application() {

    var isLoggedIn: Boolean = false
    var bearerToke: String? = null
        get() = field
        set(value) {
            field = value
        }

    override fun onCreate() {
        super.onCreate()

        LibKeyStore.createKey(applicationContext, ALIAS_KEY)

        getCredsToken()
    }

    fun getCredsToken() {
        val pair = Credentials.retrieveCredentials(applicationContext)
        if (pair != null) {
            callCredentialMediator(pair)
        } else {
            // pair equals null so log the user out and forcing them to log in.  This code could probably be
            // removed once all old version without credentials are updated.
            if (isLoggedIn) {
                //logOut()
                bearerToke = null
            }
        }
    }

    fun callCredentialMediator(pair: Pair<String?, String?>) {
        // make call to login webservice and if using a token then save that login token to use for future logins
        // bearerToken = tokenResponse.getAccessToken()
    }

}