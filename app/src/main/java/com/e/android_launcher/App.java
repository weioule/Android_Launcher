package com.e.android_launcher;

import android.app.Application;

import com.e.crashhandler.CrashHandlerJar;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by weioule
 * on 2020/11/21
 */
public class App extends Application {

    public static final String DIRECTORY = "android_launcher";

    private static App instance;

    public static synchronized App getInstance() {
        if (null == instance) {
            instance = new App();
        }
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        handleSSLHandshake();

        //初始化崩溃日志
        CrashHandlerJar.init(this, MainActivity.class);
    }

    //忽略https的证书校验
    public static void handleSSLHandshake() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};

            SSLContext sc = SSLContext.getInstance("TLS");
            // trustAllCerts信任所有的证书
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        } catch (Exception ignored) {
        }
    }

}
