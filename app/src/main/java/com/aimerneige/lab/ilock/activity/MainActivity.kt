package com.aimerneige.lab.ilock.activity

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.aimerneige.lab.ilock.R
import com.aimerneige.lab.ilock.util.getHour24
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /**
         * 指纹验证相关
         */

        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationError(errorCode: Int,
                                                   errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)

                    Toast.makeText(applicationContext, "认证错误: $errString", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    // TODO 发送开门请求

                    Toast.makeText(applicationContext, "认证成功", Toast.LENGTH_SHORT).show()
                    showDialogSendRequest()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()

                    Toast.makeText(applicationContext, "认证失败", Toast.LENGTH_SHORT).show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("需要进行身份认证")
            .setSubtitle("请进行身份认证以确认操作。您的操作会被记录在服务器，恶意发送开门请求可能会被暴打。")
            .setConfirmationRequired(false)
            .setDeviceCredentialAllowed(true)
            .build()


        /**
         * 开门按钮的交互逻辑
         */

        button_open_door.setOnClickListener {
            if (!hasKeyCheck()) {
                showDialogWithoutKey()
            }
            else {
                if (getHour24() >= 23 || getHour24() <= 4) {
                    showDialogTimeError()
                }
                else {
                    biometricPrompt.authenticate(promptInfo)
                }
            }
        }

    }

    // TODO 将以下代码移动到 util 包内

    /**
     * 用于调试的 Toast
     */

    private fun debug_toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }


    /**
     * 是否生成过密钥的验证
     */

    private fun hasKeyCheck(): Boolean {
        val sharedPref = getSharedPreferences("hasKeyCheck", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("hasKey", false)
    }

    private fun onKeyGenerated() {
        val sharedPref = getSharedPreferences("hasKeyCheck", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("hasKey", true)
        editor.apply()
    }


    /**
     * 下面的代码是各种对话框
     */

    private fun showDialogWithoutKey() {
        // TODO 生成密钥，弹出对话框，提供公钥，提示用户在服务器注册账户
        // 可以在设置中重新查看公钥，但是用户无法获得私钥

        AlertDialog.Builder(this).apply {
            setTitle("本地不存在密钥")
            setMessage("本地不存在密钥，请点击下方生成来生成一个密钥，生成密钥后请联系管理员进行注册，在右上角设置中可再次查看")
            setCancelable(false)
            setPositiveButton("生成") { dialog, which ->
                dialog.cancel()





                // TODO 生成密钥



                onKeyGenerated()
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
            setMessage("深夜请使用钥匙或一卡通开门")
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
            setMessage("已经向服务器发送了开门请求，您的操作会被记录在服务器，请勿重复提交请求。如果门没有开，请确认您的密钥是否被登记。")
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
            setMessage("发送开门请求失败，请检查您的网络设置。")
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
}