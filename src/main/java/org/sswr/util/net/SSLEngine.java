package org.sswr.util.net;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SSLEngine
{
	SSLSocketFactory factory;
	public SSLEngine()
	{
		this.factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
	}

	public SocketFactory getSocketFactory()
	{
		return this.factory;
	}

	public static void ignoreCertCheck()
	{
		TrustManager[] trustAllCerts = new TrustManager[] {
			new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}
				@Override
				public void checkClientTrusted(X509Certificate[] arg0, String arg1)	throws CertificateException
				{
				}
				

				@Override
				public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException
				{

				}
			}
		};
	
		SSLContext sc=null;
		try
		{
			sc = SSLContext.getInstance("SSL");
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		try
		{
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
		}
		catch (KeyManagementException e)
		{
			e.printStackTrace();
		}
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		// Create all-trusting host name verifier
		HostnameVerifier validHosts = new HostnameVerifier() {
			@Override
			public boolean verify(String arg0, SSLSession arg1) {
				return true;
			}
		};
		// All hosts will be valid
		HttpsURLConnection.setDefaultHostnameVerifier(validHosts);
	}

	public static void setTrustStore(String path, String password)
	{
		System.setProperty("javax.net.ssl.trustStore", path);
		System.setProperty("javax.net.ssl.trustStorePassword", password);
	}
}
