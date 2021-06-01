package com.myproject.safealarm

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.util.Log
import com.google.android.gms.common.util.Base64Utils
import okhttp3.OkHttpClient
import java.io.*
import java.lang.RuntimeException
import java.security.*
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

fun getSign(text: String): String{
    val hash = getHash(text)
    return "SiGn"+ rsaEncrypt(Base64Utils.encode(hash), getPrivateKey())
}

fun getHash(text: String): ByteArray{
    val hash: ByteArray
    try{
        val md = MessageDigest.getInstance("SHA-256")
        md.update(text.toByteArray())
        hash = md.digest()
    }catch (e: CloneNotSupportedException){
        throw DigestException("couldn't make digest of patial content")
    }
    return hash
}

fun checkSign(msg: String, publicKey: PublicKey): Boolean{
    val msgArr = msg.split("SiGn")
    val originText = msgArr[0]
    val sign = msgArr[1]
    val textSign = Base64Utils.encode(getHash(originText))
    val originSign = rsaDecrypt(sign, publicKey)

    Log.e("서명1", textSign)
    Log.e("서명2", originSign)

    return textSign == originSign
}

fun checkCASign(msg: ByteArray, sign: ByteArray, publicKey: PublicKey): Boolean{
    val signature = Signature.getInstance("SHA256withRSA")
    signature.initVerify(publicKey)
    signature.update(msg)
    return signature.verify(sign)
}

fun rsaEncrypt(input: String, key: PrivateKey): String{
    try {
        val cipher = Cipher.getInstance("RSA")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encrypt = cipher.doFinal(input.toByteArray())
        return Base64Utils.encode(encrypt)
    }catch (e: Exception){
        throw RuntimeException(e)
    }
}

fun rsaDecrypt(input: String, key: PublicKey): String{
    try {
        var byteEncrypt: ByteArray = Base64Utils.decode(input)
        val cipher = Cipher.getInstance("RSA")
        cipher.init(Cipher.DECRYPT_MODE, key)
        val decrypt = cipher.doFinal(byteEncrypt)
        return String(decrypt)
    }catch (e: Exception){
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

fun getCAPublicKey(): PublicKey{
    var kf = KeyFactory.getInstance("RSA")
    var public = kf.generatePublic(X509EncodedKeySpec(Base64Utils.decode(App.prefs.CAPublic)))
    return public
}

fun saveCertificate(response: String, path: File?){
    if(path != null){
        var tempFile = File(path, "certificate.crt")
        try{
            val writer = FileWriter(tempFile)
            val buffer = BufferedWriter(writer)
            buffer.write(response)
            buffer.close()
        }catch(e: java.lang.Exception){
            e.printStackTrace()
        }

        var cf = CertificateFactory.getInstance("X.509")
        var caIn = BufferedInputStream(FileInputStream(tempFile))
        var ca = caIn.use{
            cf.generateCertificate(it) as X509Certificate
        }
        var kf = KeyFactory.getInstance("RSA")
        var public = kf.generatePublic(X509EncodedKeySpec(ca.publicKey.encoded))

        if(checkCASign(ca.tbsCertificate, ca.signature, getCAPublicKey())){
            App.prefs.publicKey = Base64Utils.encode(public.encoded)
        }else{
            Log.e("CA서명 검증", "실패")
        }
    }
}

fun locationToText(context: Context): String{           //위도, 경도를 주소로 변경
    val mGeocoder = Geocoder(context, Locale.KOREAN)
    var mResultList: List<Address>? = null
    var currentLocation = ""
    try{
        mResultList = mGeocoder.getFromLocation(
            App.prefs.saveLat.toDouble(), App.prefs.saveLng.toDouble(), 1
        )
    }catch(e: IOException){
        e.printStackTrace()
    }
    if(mResultList != null){
        currentLocation = mResultList[0].getAddressLine(0)
        currentLocation = currentLocation.substring(5)
    }
    return currentLocation
}

fun textToLocation(text: String, context: Context) : Pair<Double, Double>{           //위도, 경도를 주소로 변경
    val address = text.replace("\\s".toRegex(), "")
    val mGeocoder = Geocoder(context, Locale.KOREAN)
    var list: List<Address>? = null
    try {
        list = mGeocoder.getFromLocationName(address, 10)
    } catch (e: IOException) {
        e.printStackTrace()
    }
    if(list == null || list.size <= 0){
        return Pair(0.0, 0.0)
    }else{
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