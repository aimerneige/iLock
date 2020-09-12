package com.aimerneige.lab.ilock.activity


import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.aimerneige.lab.ilock.R
import com.aimerneige.lab.ilock.util.paste2ClipBoard
import com.aimerneige.lab.ilock.util.KeyRSAUtil


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        private val rsaUtil: KeyRSAUtil = KeyRSAUtil()
        private val KEY_ALIAS = "com_aimerneige_lab_ilock_rsa_key"

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            findPreference<Preference>("public_key")?.setOnPreferenceClickListener {
                if (rsaUtil.isHaveKeyStore(KEY_ALIAS)) {
                    showDialogPublicKey(getPublicKeyData())
                }
                else {
                    showDialogNoPublicKeyFound()
                }
                true
            }
        }


        private fun getPublicKeyData(): String {
            val publicKey = rsaUtil.getLocalPublicKey(KEY_ALIAS)
            return if (publicKey != null) {
                rsaUtil.publicKey2String(publicKey)
            } else {
                getString(R.string.read_public_key_failed)
            }
        }


        private fun showDialogNoPublicKeyFound() {
            AlertDialog.Builder(requireActivity()).apply {
                setTitle(getString(R.string.dialog_no_public_key_found_title))
                setMessage(getString(R.string.dialog_no_public_key_found_message))
                setCancelable(true)
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
                setCancelable(true)
                setPositiveButton(R.string.dialog_copy) { dialog, which ->
                    paste2ClipBoard("Key", publicKeyStringData, requireContext())
                    Toast.makeText(context, getString(R.string.public_key_copy_success), Toast.LENGTH_SHORT).show()
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
