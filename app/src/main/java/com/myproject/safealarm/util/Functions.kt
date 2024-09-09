package com.myproject.safealarm.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.util.Log
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

fun checkValidify(certificate: X509Certificate, crl: X509CRL): Boolean {
    try {
        val sig = Signature.getInstance("SHA256withRSA").provider
        certificate.verify(getCAPublicKey(), sig)
        certificate.checkValidity()
        if (crl.getRevokedCertificate(certificate) == null) {
            return true
        } else {
            Log.e("인증서 유효성 검사", "CRL에 포함된 인증서")
            return false
        }
    } catch (e: Exception) {
        Log.e("인증서 유효성 검사", e.toString())
        throw e
    }
}

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

fun getCAPublicKey(): PublicKey {
    var kf = KeyFactory.getInstance("RSA")
    var public = kf.generatePublic(X509EncodedKeySpec(Base64Utils.decode(App.prefs.CAPublic)))
    return public
}

fun saveCertificate(certificate: String, path: File?, crl: X509CRL) {
    if (path != null) {
        var tempFile = File(path, "certificate.crt")
        try {
            val writer = FileWriter(tempFile)
            val buffer = BufferedWriter(writer)
            buffer.write(certificate)
            buffer.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        var cf = CertificateFactory.getInstance("X.509")
        var caIn = BufferedInputStream(FileInputStream(tempFile))
        var ca = caIn.use {
            cf.generateCertificate(it) as X509Certificate
        }

        var kf = KeyFactory.getInstance("RSA")
        var public = kf.generatePublic(X509EncodedKeySpec(ca.publicKey.encoded))

        try {
            if (checkValidify(ca, crl)) {
                App.prefs.publicKey = Base64Utils.encode(public.encoded)
            }
        } catch (e: Exception) {
            Log.e("CRL유효성 검사", e.toString())
            e.printStackTrace()
        }
    }
}

fun loadCRL(response: String?, path: File?): X509CRL {
    var tempFile = File(path, "CRL.crl")
    try {
        val writer = FileWriter(tempFile)
        val buffer = BufferedWriter(writer)
        buffer.write(response)
        buffer.close()
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }

    val cf = CertificateFactory.getInstance("X.509")
    val crlIn = BufferedInputStream(FileInputStream(tempFile))
    val crl = crlIn.use {
        cf.generateCRL(it) as X509CRL
    }

    try {
        crl.verify(getCAPublicKey())
    } catch (e: Exception) {
        Log.e("CRL 서명 검증", "실패")
    }

    return crl
}

suspend fun locationToText(context: Context, latLng: LatLng): String {
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

suspend fun textToLocation(
    text: String,
    context: Context
): Pair<Double, Double> {           //위도, 경도를 주소로 변경
    val address = text.replace("\\s".toRegex(), "")
    val mGeocoder = Geocoder(context, Locale.KOREAN)
    var list: List<Address>? = null
    try {
        list = mGeocoder.getFromLocationName(address, 10)
    } catch (e: IOException) {
        e.printStackTrace()
    }
    if (list == null || list.size <= 0) {
        return Pair(0.0, 0.0)
    } else {
        return Pair(list.get(0).latitude, list.get(0).longitude)
    }
}

fun arrayListToByteArray(arrayList: ArrayList<Double>): ByteArray {
    var tmpList: MutableList<Byte> = mutableListOf()
    for (i in 0..arrayList.size - 1) {
        tmpList.add(arrayList[i].toInt().toByte())
    }
    var byteArr = tmpList.toByteArray()
    return byteArr
}

fun arrayListToBitmap(input: ArrayList<Double>): Bitmap {
    var byteArr = arrayListToByteArray(input)

    try {
        var bitmap = BitmapFactory.decodeByteArray(byteArr, 0, byteArr.size)
        return bitmap
    } catch (e: java.lang.Exception) {
        throw RuntimeException(e)
    }
}

fun getGenderText(gender: Gender) = when (gender) {
    Gender.MALE -> R.string.male
    Gender.FEMALE -> R.string.female
}