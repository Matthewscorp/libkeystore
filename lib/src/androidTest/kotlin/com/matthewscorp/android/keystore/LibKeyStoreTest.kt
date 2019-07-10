package com.matthewscorp.android.keystore

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.util.Log
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*

/**
 * Created by jmatthews on 11/7/2017.
 */

class LibKeyStoreTest {

    var instrumentationContext: Context? = null
    val aliasName: String = "testAlias"

    @Before
    fun setUp() {
        instrumentationContext = InstrumentationRegistry.getContext();
        LibKeyStore.createKey(this!!.instrumentationContext!!, aliasName)
    }

    @After
    fun tearDown() {
        LibKeyStore.deleteKey(aliasName)
    }

    @Test
    fun testGetKeyStore() {
        assertNotNull(LibKeyStore.keyStore);
    }

    @Test
    fun testCreateKey() {
        var keys = LibKeyStore.keyStore.aliases()
        keys.iterator().forEach {
            Log.d("TEST-Create", it)
            assertEquals(it, aliasName)
        }
    }

    @Test
    fun testDeleteKey() {
        var keys = LibKeyStore.keyStore.aliases()
        keys.iterator().forEach {
            Log.d("TEST-Delete", it)
            assertEquals(it, aliasName)
            LibKeyStore.deleteKey(aliasName)
        }
        assertFalse(keys.hasMoreElements())
        // test error handling on key not there
        LibKeyStore.deleteKey(aliasName)
    }

    @Test
    fun testEncryptString() {
        val pair = LibKeyStore.encryptString(aliasName, "myPassword")
        assertNotNull(pair)
    }

    @Test
    fun testEncryptBlankString() {
        val pair2 = LibKeyStore.encryptString(aliasName, "")
        assertNotNull(pair2)
        val pwd2 = LibKeyStore.decryptString(aliasName, pair2.first, pair2.second)
        assertTrue(pwd2.equals(""))
    }

    @Test
    fun testDecryptString() {
        val pair = LibKeyStore.encryptString(aliasName, "myPassword")
        assertNotNull(pair)
        val pwd = LibKeyStore.decryptString(aliasName, pair.first, pair.second)
        assertTrue(pwd.equals("myPassword"))
    }

}