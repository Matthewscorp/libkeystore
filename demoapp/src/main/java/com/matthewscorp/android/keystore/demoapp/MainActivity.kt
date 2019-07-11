package com.matthewscorp.android.keystore.demoapp

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.matthewscorp.android.keystore.LibKeyStore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main_header.*


/**
 * This demo app uses the UI from https://github.com/obaro/SimpleKeystoreApp
 *
 * Chages from the original include:
 * A quick conversion to kotlin
 * Crypto operations are provided by the LibKeyStore class
 * from the library that accompanies this demo app
 */
class MainActivity : AppCompatActivity() {

    private var keyAliases: MutableList<String> = ArrayList()
    private var listAdapter: KeyRecyclerAdapter? = null
    private var IV: ByteArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        refreshKeys()
        val listHeader = View.inflate(this, R.layout.activity_main_header, null)
        listView.addHeaderView(listHeader)
        listAdapter = KeyRecyclerAdapter(this, R.id.keyAlias)
        listView.adapter = listAdapter
    }

    private fun refreshKeys() {
        keyAliases = ArrayList()
        val aliases = LibKeyStore.keyStore.aliases()
        while (aliases.hasMoreElements()) {
            keyAliases.add(aliases.nextElement())
        }
        if (listAdapter != null) {
            listAdapter!!.notifyDataSetChanged()
        }
    }

    fun createNewKeys(view: View) {
        val alias = aliasText.text.toString()
        LibKeyStore.createKey(this, alias)
        refreshKeys()
    }

    fun deleteKey(alias: String) {
        val alertDialog = AlertDialog.Builder(this)
                .setTitle("Delete Key")
                .setMessage("Do you want to delete the key \"$alias\" from the keystore?")
                .setPositiveButton("Yes") { dialog, which ->
                    LibKeyStore.deleteKey(alias)
                    refreshKeys()
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, which -> dialog.dismiss() }
                .create()
        alertDialog.show()
    }

    fun encryptString(alias: String) {
        val initialText = startText.text.toString()
        if (initialText.isEmpty()) {
            Toast.makeText(this, "Enter text in the 'Initial Text' widget", Toast.LENGTH_LONG).show()
            return
        }
        val (first, second) = LibKeyStore.encryptString(alias, initialText)
        encryptedText.setText(first)
        IV = second
    }

    fun decryptString(alias: String) {
        decryptedText.setText(LibKeyStore.decryptString(alias, encryptedText.text.toString(), IV))
    }

    fun removeCredentials() {
        (application as MainApplication).bearerToke = null
        Credentials.removeCredentials(applicationContext)
    }

    fun saveCredentials(username: String, pwd: String) {
        //(application as MainApplication).bearerToke = some token
        Credentials.saveCredentials(applicationContext, username, pwd, Credentials.ACCESS_TYPE_EMAIL)
    }

    inner class KeyRecyclerAdapter internal constructor(context: Context, textView: Int) : ArrayAdapter<String>(context, textView) {

        override fun getCount(): Int {
            return keyAliases.size
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

            var itemView = convertView
            if (itemView == null) {
                itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
            }

            val keyAlias = itemView?.findViewById<TextView>(R.id.keyAlias)
            keyAlias?.text = keyAliases[position]

            val encryptButton = itemView?.findViewById<Button>(R.id.encryptButton)
            encryptButton?.setOnClickListener { encryptString(keyAlias?.text.toString()) }

            val decryptButton = itemView?.findViewById<Button>(R.id.decryptButton)
            decryptButton?.setOnClickListener { decryptString(keyAlias?.text.toString()) }

            val deleteButton = itemView?.findViewById<Button>(R.id.deleteButton)
            deleteButton?.setOnClickListener { deleteKey(keyAlias?.text.toString()) }

            return itemView!!
        }

        override fun getItem(position: Int): String? {
            return keyAliases[position]
        }

    }



}
