package com.aimerneige.lab.ilock.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.aimerneige.lab.ilock.R
import com.aimerneige.lab.ilock.util.KeyRSAUtil
import com.aimerneige.lab.ilock.util.getHour24
import com.aimerneige.lab.ilock.util.paste2ClipBoard
import kotlinx.android.synthetic.main.activity_main.*
import java.security.PublicKey
import java.util.concurrent.Executor


class MainActivity : AppCompatActivity() {

    private val KEY_ALIAS = "com_aimerneige_lab_ilock_rsa_key"
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private lateinit var mContext: Context
    private lateinit var mActivity: Activity
    private lateinit var publicKey: PublicKey


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        fun callSettingsActivity() {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        when (item.itemId) {
            R.id.toolbar_settings -> callSettingsActivity()
        }
        return true
    }

    private val rsaUtil: KeyRSAUtil = KeyRSAUtil()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        mActivity = this
        mContext = this

        /**
         * 指纹验证相关
         */
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)

                    Toast.makeText(applicationContext, "Error: $errString", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(applicationContext, "Success", Toast.LENGTH_SHORT).show()
                    // TODO 发送开门请求

                    showDialogSendRequest()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()

                    Toast.makeText(applicationContext, "Failed", Toast.LENGTH_SHORT).show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.biometric_prompt_title))
            .setSubtitle(getString(R.string.biometric_prompt_message))
            .setConfirmationRequired(false)
            .setDeviceCredentialAllowed(true)
            .build()


        /**
         * 开门按钮的交互逻辑
         */

        button_open_door.setOnClickListener {
            if (!rsaUtil.isHaveKeyStore(KEY_ALIAS)) {
                // 本地不存在密钥，提示用户生成密钥
                showDialogWithoutKey()
            }
            else {
                if (getHour24() >= 23 || getHour24() <= 4) {
                    // 检查时间是否合法
                    showDialogTimeError()
                }
                else {
                    // 通过指纹验证确认操作
                    biometricPrompt.authenticate(promptInfo)
                }
            }
        }

    }


    /**
     * 用于调试的 Toast
     */
    private fun debug_toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }


    /**
     * 生成密钥
     */
    private fun genKey() {
        val kp = rsaUtil.generateRSAKeyPair(mContext, KEY_ALIAS)
        publicKey = kp.public
        val publicKeyStringData: String = rsaUtil.publicKey2String(publicKey)
        savePublicKeyDataToSharedPreferences(publicKeyStringData)
        showDialogKeyGenerated(publicKeyStringData)
    }


    /**
     * 公钥的读取
     */
    private fun getPublicKeyDataFromSharedPreferences(): String? {
        val sharedPref = getSharedPreferences("key", Context.MODE_PRIVATE)
        return sharedPref.getString("public_key", "__EMPTY_KEY_VALUE__")
    }


    /**
     * 公钥的保存
     */
    private fun savePublicKeyDataToSharedPreferences(publicKeyData: String) {
        val sharedPref = getSharedPreferences("key", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("public_key", publicKeyData)
        editor.apply()
    }


    /**
     * 对话框
     */
    private fun showDialogWithoutKey() {
        AlertDialog.Builder(this).apply {
            setTitle(getString(R.string.dialog_without_key_title))
            setMessage(getString(R.string.dialog_without_key_message))
            setCancelable(false)
            setPositiveButton(getString(R.string.dialog_generated)) { dialog, which ->
                genKey()
                dialog.cancel()
            }
            setNegativeButton(R.string.dialog_cancel) { dialog, which ->
                dialog.cancel()
            }
            show()
        }
    }

    private fun showDialogKeyGenerated(keyData: String) {
        AlertDialog.Builder(this).apply {
            setTitle(getString(R.string.dialog_key_generated_title))
            setMessage(getString(R.string.dialog_key_generated_message_part1) + "\n" +
                    keyData + "\n" +
                    getString(R.string.dialog_key_generated_message_part2)
            )
            setCancelable(false)
            setPositiveButton(getString(R.string.dialog_copy)) { dialog, which ->
                paste2ClipBoard("Key", keyData, mContext)
                dialog.cancel()
            }
            setNegativeButton(R.string.dialog_cancel) { dialog, which ->
                dialog.cancel()
            }
            show()
        }
    }

    private fun showDialogTimeError() {
        AlertDialog.Builder(this).apply {
            setTitle(getString(R.string.dialog_time_error_title))
            setMessage(getString(R.string.dialog_time_error_message))
            setCancelable(false)
            setPositiveButton(R.string.dialog_i_know) { dialog, which ->
                dialog.cancel()
            }
            setNegativeButton(R.string.dialog_exit) { dialog, which ->
                finish()
            }
            show()
        }
    }

    private fun showDialogSendRequest() {
        AlertDialog.Builder(this).apply {
            setTitle(getString(R.string.dialog_send_request_title))
            setMessage(getString(R.string.dialog_send_request_message))
            setCancelable(false)
            setPositiveButton(getString(R.string.dialog_i_know)) { dialog, which ->
                dialog.cancel()
            }
            setNegativeButton(getString(R.string.dialog_exit)) { dialog, which ->
                finish()
            }
            show()
        }
    }

    private fun showDialogSendRequestFailed() {
        AlertDialog.Builder(this).apply {
            setTitle(getString(R.string.dialog_send_request_failed_title))
            setMessage(getString(R.string.dialog_send_request_failed_message))
            setCancelable(false)
            setPositiveButton(getString(R.string.dialog_ok)) { dialog, which ->
                dialog.cancel()
            }
            setNegativeButton(getString(R.string.dialog_cancel)){ dialog, which ->
                dialog.cancel()
            }
            show()
        }
    }
}