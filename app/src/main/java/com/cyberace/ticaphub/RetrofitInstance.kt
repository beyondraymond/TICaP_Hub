package com.cyberace.ticaphub

import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.*

object RetrofitInstance: AppCompatActivity() {
    val api: TaskApi by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl) //add the specific url for this
            .client(getUnsafeOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(TaskApi::class.java)
    }

    private var gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    private fun getUnsafeOkHttpClient(): OkHttpClient? {
        return try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
                    @Throws(CertificateException::class)
                    override fun checkClientTrusted(
                        chain: Array<X509Certificate?>?,
                        authType: String?
                    ) {
                    }

                    @Throws(CertificateException::class)
                    override fun checkServerTrusted(
                        chain: Array<X509Certificate?>?,
                        authType: String?
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate?>? {
                        return arrayOf()
                    }
                }
            )

            // Install the all-trusting trust manager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            // Create an ssl socket factory with our all-trusting manager
            val sslSocketFactory = sslContext.socketFactory
            val trustManagerFactory: TrustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(null as KeyStore?)
            val trustManagers: Array<TrustManager> =
                trustManagerFactory.trustManagers
            check(!(trustManagers.size != 1 || trustManagers[0] !is X509TrustManager)) {
                "Unexpected default trust managers:" + trustManagers.contentToString()
            }

            val trustManager =
                trustManagers[0] as X509TrustManager


            val builder = OkHttpClient.Builder()
            builder.sslSocketFactory(sslSocketFactory, trustManager)
            builder.hostnameVerifier(HostnameVerifier { _, _ -> true })
            builder.build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

//    private fun provideOkHttpClient(): OkHttpClient {
//        val okhttpClientBuilder = OkHttpClient.Builder()
//        okhttpClientBuilder.connectTimeout(30, TimeUnit.SECONDS)
//        okhttpClientBuilder.readTimeout(30, TimeUnit.SECONDS)
//        okhttpClientBuilder.writeTimeout(30, TimeUnit.SECONDS)
//
////        okhttpClientBuilder.addInterceptor{
////            val response: Response = it.proceed(it.request())
////
////            // if 'x-auth-token' is available into the response header
////            // save the new token into session.The header key can be
////            // different upon implementation of backend.
////            val newToken: String? = response.header("x-auth-token")
////            if (newToken != null) {
////                this@RetrofitInstance.getSharedPreferences("loginCredential", Context.MODE_PRIVATE).getString("userToken", null)
////            }
////            return@addInterceptor response
////        }
//        okhttpClientBuilder.addInterceptor{
//            val sharedPref = this@RetrofitInstance.getSharedPreferences("loginCredential", Context.MODE_PRIVATE)
//            val editor = sharedPref.edit()
//            val token = sharedPref.getString("userToken", "no-token")
//            val request = it.request().newBuilder().addHeader("Authorization", "Bearer " + token).build()
//            val response: Response = it.proceed(request)
//
//            if (response.code == 401 || response.code == 401) {
//                editor.clear()
//                editor.apply()
//                Intent(this@RetrofitInstance, LoginActivity::class.java).apply {
//                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                    startActivity(this)
//                }
//            }
//            return@addInterceptor response
//        }
//        return okhttpClientBuilder.build()
//    }

}

