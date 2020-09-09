package com.aimerneige.lab.ilock.activity

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.aimerneige.lab.ilock.R
import com.aimerneige.lab.ilock.util.KeyRSAUtil
import com.aimerneige.lab.ilock.util.getHour24
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
//        return super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return super.onOptionsItemSelected(item)

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

                    Toast.makeText(applicationContext, "认证错误: $errString", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(applicationContext, "认证成功", Toast.LENGTH_SHORT).show()
                    // TODO 发送开门请求

                    showDialogSendRequest()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()

                    Toast.makeText(applicationContext, "认证失败", Toast.LENGTH_SHORT).show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("需要进行身份认证")
            .setSubtitle("请进行身份认证以确认操作。您的操作会被记录在服务器，请勿恶意发送开门请求。")
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
        savePublicKeyData2SharedPreferences(publicKeyStringData)
        showDialogKeyGenerated(publicKeyStringData)
    }


    /**
     * 复制内容到剪贴板
     */
    private fun paste2ClipBoard(lable: String, data: String) {
        val clipData: ClipData = ClipData.newPlainText(lable, data)
        val clipboard = mContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(clipData)
        Toast.makeText(this, "已将公钥复制到剪切板", Toast.LENGTH_SHORT).show()
    }


    /**
     * 公钥的读取
     */
    private fun getPublicKeyData2SharedPreferences(): String? {
        val sharedPref = getSharedPreferences("key", Context.MODE_PRIVATE)
        return sharedPref.getString("public_key", "__EMPTY_KEY_VALUE__")
    }


    /**
     * 公钥的保存
     */
    private fun savePublicKeyData2SharedPreferences(publicKeyData: String) {
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
            setTitle("本地不存在密钥")
            setMessage("本地不存在密钥，请点击下方“生成”来生成一个密钥，生成密钥后请联系管理员进行注册。" +
                    "在右上角设置中可再次查看密钥。\n" +
                    "生成密钥需要一定时间，软件并没有卡住，具体时间取决于你的CPU。")
            setCancelable(false)
            setPositiveButton("生成") { dialog, which ->
                genKey()
                dialog.cancel()
            }
            setNegativeButton("取消") { dialog, which ->
                dialog.cancel()
            }
            show()
        }
    }

    private fun showDialogKeyGenerated(keyData: String) {
        AlertDialog.Builder(this).apply {
            setTitle("生成密钥成功")
            setMessage("生成密钥成功！请复制密钥后联系管理员在服务器注册密钥信息。\n" +
                    keyData + "\n" +
                    "密钥有效期为6个月，到期请重新注册。"
            )
            setCancelable(false)
            setPositiveButton("复制") { dialog, which ->
                paste2ClipBoard("Key", keyData)
                dialog.cancel()
            }
            setNegativeButton("取消") { dialog, which ->
                dialog.cancel()
            }
            show()
        }
    }

    private fun showDialogTimeError() {
        AlertDialog.Builder(this).apply {
            setTitle("深夜禁止开门")
            setMessage("深夜请使用其他方式开门。您的操作会被记录在服务器，请勿恶意提交请求。")
            setCancelable(false)
            setPositiveButton("我知道了") { dialog, which ->
                dialog.cancel()
            }
            setNegativeButton("退出") { dialog, which ->
                finish()
            }
            show()
        }
    }

    private fun showDialogSendRequest() {
        AlertDialog.Builder(this).apply {
            setTitle("已发送开门请求")
            setMessage("已经向服务器发送了开门请求，您的操作会被记录在服务器，请勿重复提交请求。如果门没有开，请确认您的密钥是否被登记或联系管理员。")
            setCancelable(false)
            setPositiveButton("我知道了") { dialog, which ->
                dialog.cancel()
            }
            setNegativeButton("退出") { dialog, which ->
                finish()
            }
            show()
        }
    }

    private fun showDialogSendRequestFailed() {
        AlertDialog.Builder(this).apply {
            setTitle("发送开门请求失败")
            setMessage("发送开门请求失败，请检查您的网络设置或者联系管理员。")
            setCancelable(false)
            setPositiveButton("确认") { dialog, which ->
                dialog.cancel()
            }
            setNegativeButton("取消"){ dialog, which ->
                dialog.cancel()
            }
            show()
        }
    }

//    private fun showDialogUseTooMuch() {
//        AlertDialog.Builder(this).apply {
//            setTitle("当日客户端开门次数已达上限")
//            setMessage("您今天开门次数过多了！请使用其他方式开门。您的操作会被记录在服务器，请勿恶意提交请求。")
//            setCancelable(false)
//            setPositiveButton("确认") { dialog, which ->
//                dialog.cancel()
//            }
//            setNegativeButton("取消"){ dialog, which ->
//                dialog.cancel()
//            }
//            show()
//        }
//    }

}