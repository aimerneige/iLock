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

//        val myPref = findPreference("myKey") as Preference
//        myPref.setOnPreferenceClickListener(object : OnPreferenceClickListener() {
//            fun onPreferenceClick(preference: Preference?): Boolean {
//                //open browser or intent here
//                return true
//            }
//        })




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
                setPositiveButton("") { dialog, which ->

                }
                setNegativeButton("") { dialog, which ->

                }
                show()
            }
        }

        private fun showDialogPublicKey(publicKeyStringData: String) {
//            val sharedPref = requireActivity().getSharedPreferences("key", Context.MODE_PRIVATE)
//            val publicKeyStringData: String = sharedPref.getString("public_key", "__EMPTY_KEY_VALUE__").toString()
            AlertDialog.Builder(requireActivity()).apply {
                setTitle("公钥信息")
                setMessage(publicKeyStringData)
                setCancelable(false)
                setPositiveButton("复制") { dialog, which ->
                    paste2ClipBoard("Key", publicKeyStringData, requireContext())
                    dialog.cancel()
                }
                setNegativeButton("取消") { dialog, which ->
                    dialog.cancel()
                }
                show()
            }
        }

    }


}
