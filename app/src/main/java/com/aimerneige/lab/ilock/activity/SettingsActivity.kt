package com.aimerneige.lab.ilock.activity

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.aimerneige.lab.ilock.R
import com.aimerneige.lab.ilock.util.paste2ClipBoard

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)


        }

        override fun onPreferenceTreeClick(preference: Preference?): Boolean {
            return if (preference != null) {
                when (preference.key) {
                    "public_key" -> {
                        // do something

                        true
                    }
                    else -> {
                        super.onPreferenceTreeClick(preference)
                    }
                }
            } else {
                super.onPreferenceTreeClick(preference)
            }
        }


        /**
         * 公钥的读取
         */
        private fun getPublicKeyDataFromSharedPreferences(): String {
            val sharedPref = requireActivity().getSharedPreferences("key", Context.MODE_PRIVATE)
            return sharedPref.getString("public_key", "__EMPTY_KEY_VALUE__").toString()
        }

//        private fun getPublicKeyData(): String {
////            !rsaUtil.isHaveKeyStore(KEY_ALIAS)
////            val publicKeyStringData: String = rsaUtil.publicKey2String(publicKey)
//        }

        private fun showDialogNoPublicKeyFound() {
            AlertDialog.Builder(requireActivity()).apply {
                setTitle("未发现公钥")
                setMessage("在设备内未发现公钥，请返回主界面点击开门生成一个密钥。")
                setCancelable(false)
                setPositiveButton(R.string.dialog_ok) { dialog, which ->
                    dialog.cancel()
                }
                setNegativeButton(R.string.dialog_cancel) { dialog, which ->
                    dialog.cancel()
                }
                show()
            }
        }

        private fun showDialogPublicKey(publicKeyStringData: String) {
            AlertDialog.Builder(requireActivity()).apply {
                setTitle(getString(R.string.dialog_publickey_title))
                setMessage(publicKeyStringData)
                setCancelable(false)
                setPositiveButton(R.string.dialog_copy) { dialog, which ->
                    paste2ClipBoard("Key", publicKeyStringData, requireContext())
                    dialog.cancel()
                }
                setNegativeButton(R.string.dialog_cancel) { dialog, which ->
                    dialog.cancel()
                }
                show()
            }
        }

    }


}
