package com.myproject.safealarm.util

import android.content.Context
import android.location.Geocoder
import com.google.android.gms.common.util.Base64Utils
import com.myproject.safealarm.App
import com.myproject.safealarm.R
import com.naver.maps.geometry.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.sdbk.domain.model.Gender
import java.io.*
import java.security.*
import java.security.cert.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher

fun getSign(text: String): String {
    val hash = getHash(text)
    return "SiGn" + rsaEncrypt(Base64Utils.encode(hash), getPrivateKey())
}

fun getHash(text: String): ByteArray {
    val hash: ByteArray
    try {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(text.toByteArray())
        hash = md.digest()
    } catch (e: CloneNotSupportedException) {
        throw DigestException("couldn't make digest of patial content")
    }
    return hash
}

fun checkSign(msg: String, publicKey: PublicKey): Boolean {
    val msgArr = msg.split("SiGn")
    val originText = msgArr[0]
    val sign = msgArr[1]
    val textSign = Base64Utils.encode(getHash(originText))
    val originSign = rsaDecrypt(sign, publicKey)

    return textSign == originSign
}

fun checkCASign(msg: ByteArray, sign: ByteArray, publicKey: PublicKey): Boolean {
    val signature = Signature.getInstance("SHA256withRSA")
    signature.initVerify(publicKey)
    signature.update(msg)
    return signature.verify(sign)
}

fun rsaEncrypt(input: String, key: PrivateKey): String {
    try {
        val cipher = Cipher.getInstance("RSA")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encrypt = cipher.doFinal(input.toByteArray())
        return Base64Utils.encode(encrypt)
    } catch (e: Exception) {
        throw RuntimeException(e)
    }
}

fun rsaDecrypt(input: String, key: PublicKey): String {
    try {
        var byteEncrypt: ByteArray = Base64Utils.decode(input)
        val cipher = Cipher.getInstance("RSA")
        cipher.init(Cipher.DECRYPT_MODE, key)
        val decrypt = cipher.doFinal(byteEncrypt)
        return String(decrypt)
    } catch (e: Exception) {
        throw RuntimeException(e)
    }
}

fun getPrivateKey(): PrivateKey {
    var kf = KeyFactory.getInstance("RSA")
    var private = kf.generatePrivate(PKCS8EncodedKeySpec(Base64Utils.decode(App.prefs.privateKey)))
    return private
}

fun getPublicKey(): PublicKey {
    var kf = KeyFactory.getInstance("RSA")
    var public = kf.generatePublic(X509EncodedKeySpec(Base64Utils.decode(App.prefs.publicKey)))
    return public
}

suspend fun locationToText(context: Context, latLng: LatLng): String {      // 주소 텍스트 -> 위도, 경도
    val mGeocoder = Geocoder(context, Locale.KOREAN)

    return withContext(Dispatchers.IO) {
        try {
            val res = mGeocoder.getFromLocation(
                latLng.latitude,
                latLng.longitude,
                1
            )
            res?.run { res[0].getAddressLine(0) } ?: ""
        } catch (e: IOException) {
            ""
        }
    }
}

suspend fun textToLocation(text: String, context: Context): LatLng? {        // 위도, 경도 -> 주소 텍스트
    val address = text.replace("\\s".toRegex(), "")
    val mGeocoder = Geocoder(context, Locale.KOREAN)

    return withContext(Dispatchers.IO) {
        try {
            mGeocoder.getFromLocationName(address, 10)?.first()?.run {
                LatLng(latitude, longitude)
            }
        } catch (e: IOException) {
            null
        }
    }
}

fun getGenderText(gender: Gender) = when (gender) {
    Gender.MALE -> R.string.male
    Gender.FEMALE -> R.string.female
}