package com.aimerneige.lab.ilock.util

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyGenParameterSpec.Builder
import android.security.keystore.KeyProperties
import android.util.Base64
import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher



/**
 * 生成 RSA 密钥的工具类
 */

class KeyRSAUtil {

    val ECB_PKCS1_PADDING = "RSA/ECB/PKCS1Padding"
    val DEFAULT_KEY_SIZE = 4096

    /**
     * 生成密钥对，并将其保存在 AndroidKeyStore 中
     */
    fun generateRSAKeyPair(context: Context?, alias: String): KeyPair {

        val start: Calendar = GregorianCalendar()
        val end: Calendar = GregorianCalendar()
        end.add(Calendar.MONTH, 6) // 密钥有效期为6个月

        val parameterSpec: KeyGenParameterSpec = Builder(
            alias,
            KeyProperties.PURPOSE_SIGN
                    or KeyProperties.PURPOSE_ENCRYPT
                    or KeyProperties.PURPOSE_DECRYPT
        ).run {
            setKeySize(DEFAULT_KEY_SIZE)
            setUserAuthenticationRequired(true)
            setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA1)
            setCertificateNotBefore(start.time)
            setCertificateNotAfter(end.time)
            setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
            setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
            build()
        }

        val keyPairGenerator: KeyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA,
            "AndroidKeyStore"
        )

        keyPairGenerator.initialize(parameterSpec)

        return keyPairGenerator.genKeyPair()
    }


    /**
     * 使用公钥进行加密
     */
    fun encryptByPublicKey(data: ByteArray?, publicKey: ByteArray?): ByteArray? {
        // 得到公钥
        val keySpec = X509EncodedKeySpec(publicKey)
        val kf: KeyFactory = KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_RSA)
        val keyPublic: PublicKey = kf.generatePublic(keySpec)
        // 加密数据
        val cp: Cipher = Cipher.getInstance(ECB_PKCS1_PADDING)
        cp.init(Cipher.ENCRYPT_MODE, keyPublic)
        return cp.doFinal(data)
    }


    /**
     * 使用私钥进行加密
     */
    fun encryptByPrivateKey(data: ByteArray?, privateKey: ByteArray?): ByteArray? {
        // 得到私钥
        val keySpec = PKCS8EncodedKeySpec(privateKey)
        val kf: KeyFactory = KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_RSA)
        val keyPrivate: PrivateKey = kf.generatePrivate(keySpec)
        // 加密数据
        val cp: Cipher = Cipher.getInstance(ECB_PKCS1_PADDING)
        cp.init(Cipher.ENCRYPT_MODE, keyPrivate)
        return cp.doFinal(data)
    }


    /**
     * 使用公钥进行解密
     */
    fun decryptByPublicKey(data: ByteArray?, publicKey: ByteArray?): ByteArray? {
        // 得到公钥
        val keySpec = X509EncodedKeySpec(publicKey)
        val kf = KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_RSA)
        val keyPublic = kf.generatePublic(keySpec)
        // 数据解密
        val cipher = Cipher.getInstance(ECB_PKCS1_PADDING)
        cipher.init(Cipher.DECRYPT_MODE, keyPublic)
        return cipher.doFinal(data)
    }


    /**
     * 使用私钥进行解密
     */
    fun decryptByPrivateKey(encrypted: ByteArray?, alias: String): ByteArray? {
        val ks = KeyStore.getInstance("AndroidKeyStore")
        ks.load(null)
        // 从Android对密钥存储库中加载密钥
        val entry =
            (ks.getEntry(alias, null) ?: return null) as? KeyStore.PrivateKeyEntry ?: return null
        val keyPrivate = entry.privateKey
        // 解密数据
        val cp = Cipher.getInstance(ECB_PKCS1_PADDING)
        cp.init(Cipher.DECRYPT_MODE, keyPrivate)
        return cp.doFinal(encrypted)
    }


    /**
     * 通过字符串生成私钥
     */
    fun string2PrivateKey(privateKeyData: String?): PrivateKey? {
        var privateKey: PrivateKey? = null
        try {
            val decodeKey: ByteArray = Base64Decoder.decodeToBytes(privateKeyData)
            val x509 = PKCS8EncodedKeySpec(decodeKey) // 创建x509证书封装类
            val keyFactory = KeyFactory.getInstance("RSA") // 指定RSA
            privateKey = keyFactory.generatePrivate(x509) // 生成私钥
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: InvalidKeySpecException) {
            e.printStackTrace()
        }
        return privateKey
    }


    /**
     * 通过字符串生成公钥
     */
    fun string2PublicKey(publicKeyData: String?): PublicKey? {
        var publicKey: PublicKey? = null
        try {
            val decodeKey: ByteArray = Base64Decoder.decodeToBytes(publicKeyData)
            val x509 = X509EncodedKeySpec(decodeKey)
            val keyFactory = KeyFactory.getInstance("RSA")
            publicKey = keyFactory.generatePublic(x509)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: InvalidKeySpecException) {
            e.printStackTrace()
        }
        return publicKey
    }


    /**
     * 转换公钥为字符串
     */
    fun publicKey2String(publicKey: PublicKey): String {
        return Base64.encodeToString(publicKey.encoded, 2)
    }


    /**
     * 判断是否生成过密钥
     */
    fun isHaveKeyStore(alias: String): Boolean {
        try {
            val ks = KeyStore.getInstance("AndroidKeyStore")
            ks.load(null)
            // 从Android加载密钥对密钥存储库中
            val entry = ks.getEntry(alias, null) ?: return false
        } catch (e: KeyStoreException) {
            e.printStackTrace()
            return false
        } catch (e: CertificateException) {
            e.printStackTrace()
            return false
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            return false
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        } catch (e: UnrecoverableEntryException) {
            e.printStackTrace()
            return false
        }
        return true
    }


    /**
     * 获得本地 AndroidKeyStore 中的公钥
     */
    fun getLocalPublicKey(alias: String): PublicKey? {
        return try {
            val ks = KeyStore.getInstance("AndroidKeyStore")
            ks.load(null)
            //从Android加载密钥对密钥存储库中
            val entry = (ks.getEntry(alias, null) ?: return null) as? KeyStore.PrivateKeyEntry ?: return null
            (entry as KeyStore.PrivateKeyEntry).certificate.publicKey
        } catch (e: KeyStoreException) {
            e.printStackTrace()
            null
        } catch (e: CertificateException) {
            e.printStackTrace()
            null
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } catch (e: UnrecoverableEntryException) {
            e.printStackTrace()
            null
        }
    }

}