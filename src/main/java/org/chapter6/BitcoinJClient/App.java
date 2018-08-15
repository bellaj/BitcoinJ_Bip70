package org.chapter6.BitcoinJClient;
 

/**
 * Hello world!
 *
 */
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;

public class App {

    /**
     * @param args
     */
    public static void main(String[] args) {
        App tester = new App();
        try {
            tester.testConnectionTo("https://bip70.com:8883");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public App() {
        super();
    }

    public void testConnectionTo(String aURL) throws Exception {
        URL destinationURL = new URL(aURL);
        HttpsURLConnection conn = (HttpsURLConnection) destinationURL.openConnection();
        conn.connect();
      System.out.println("tfoo");

        Certificate[] certs = conn.getServerCertificates();
        for (Certificate cert : certs) {
            System.out.println("Certificate is: " + cert);
            if(cert instanceof X509Certificate) {
                System.out.println("rjoola");

                try {
                    ( (X509Certificate) cert).checkValidity();
                    System.out.println("Certificate is active for current date");
                } catch(CertificateExpiredException cee) {
                    System.out.println("Certificate is expired");
                }
            }
        } 
    }
}