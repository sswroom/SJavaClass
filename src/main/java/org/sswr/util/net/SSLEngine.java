package org.sswr.util.net;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;

import org.sswr.util.crypto.CertUtil;
import org.sswr.util.crypto.MyX509Cert;
import org.sswr.util.crypto.MyX509File;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class SSLEngine
{
	private SSLContext sc;
	private boolean noCertCheck;
	private List<X509Certificate> certList;

	class SSLTrustManager implements X509TrustManager
	{
		private TrustManager[] tms;
		public SSLTrustManager(@Nonnull TrustManager[] tms)
		{
			this.tms = tms;
		}

		public @Nullable java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		private boolean certValid(@Nonnull X509Certificate cert) throws CertificateException
		{
			cert.checkValidity();
			X500Principal principal = cert.getIssuerX500Principal();
			int i = certList.size();
			while (i-- > 0)
			{
				if (principal.equals(certList.get(i).getSubjectX500Principal()))
					return true;
			}
			return false;
		}

		@Override
		public void checkClientTrusted(X509Certificate[] arg0, String arg1)	throws CertificateException
		{
			if (noCertCheck)
				return;
			
			boolean valid = false;
			int i = 0;
			int j = arg0.length;
			while (i < j)
			{
				valid = valid || certValid(arg0[i]);
				i++;
			}
			if (!valid)
			{
				i = 0;
				j = tms.length;
				while (i < j)
				{
					if (tms[i] instanceof X509TrustManager)
					{
						((X509TrustManager)tms[i]).checkClientTrusted(arg0, arg1);
						valid = true;
					}
					i++;
				}
				if (!valid)
					throw new CertificateException("Cert not found");
			}
		}

		@Override
		public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException
		{
			if (noCertCheck)
				return;
			
			boolean valid = false;
			int i = 0;
			int j = arg0.length;
			while (i < j)
			{
				valid = valid || certValid(arg0[i]);
				i++;
			}
			if (!valid)
			{
				i = 0;
				j = tms.length;
				while (i < j)
				{
					if (tms[i] instanceof X509TrustManager)
					{
						((X509TrustManager)tms[i]).checkServerTrusted(arg0, arg1);
						valid = true;
					}
					i++;
				}
				if (!valid)
					throw new CertificateException("Cert not found");
			}
		}
	};

	public SSLEngine(boolean noCertCheck) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException
	{
		this.noCertCheck = noCertCheck;
		this.certList = new ArrayList<X509Certificate>();
		this.sc = SSLContext.getInstance("SSL");
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init((KeyStore)null);
		TrustManager[] newTMS;
		newTMS = new TrustManager[1];
		newTMS[0] = new SSLTrustManager(tmf.getTrustManagers());
		this.sc.init(null, newTMS, new java.security.SecureRandom());
	}

	@Nonnull
	public SocketFactory getSocketFactory()
	{
		return this.sc.getSocketFactory();
	}

	public boolean isNoCertCheck()
	{
		return this.isNoCertCheck();
	}

	public void setNoCertCheck(boolean noCertCheck)
	{
		this.noCertCheck = noCertCheck;
	}

	public boolean addCert(@Nonnull String fileName)
	{
		X509Certificate cert = CertUtil.loadCertificate(new File(fileName));
		if (cert != null)
		{
			this.certList.add(cert);
			return true;
		}
		return false;
	}

	public boolean clientSetCertASN1(@Nonnull MyX509Cert cert, @Nonnull MyX509File key)
	{
		//this.sc.getDefaultSSLParameters().
		return false;
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
			return;
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

	@Nullable
	public TCPClient clientConnect(@Nonnull TCPClientFactory clif, @Nonnull String host, int port, @Nonnull Duration timeout)
	{
		TCPClient cli = clif.create(host, port, timeout);
		if (cli.isConnectError())
		{
			cli.dispose();
			return null;
		}
		try
		{
			SSLSocketFactory factory = this.sc.getSocketFactory();
			SSLSocket soc = (SSLSocket)factory.createSocket(cli.getSocket(), null, false);
			cli.replaceSocket(soc);
			soc.setSoTimeout(5000);
			soc.setUseClientMode(true);
			soc.startHandshake();
			return cli;
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			cli.dispose();
			return null;
		}
		
	}

	public static void setTrustStore(@Nonnull String path, @Nonnull String password)
	{
		System.setProperty("javax.net.ssl.trustStore", path);
		System.setProperty("javax.net.ssl.trustStorePassword", password);
	}
}
