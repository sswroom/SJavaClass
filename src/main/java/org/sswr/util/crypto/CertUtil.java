package org.sswr.util.crypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.Security;
import java.security.cert.CRLException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.sswr.util.crypto.MyX509File.FileType;

public class CertUtil
{
	public enum CertValidStatus
	{
		Valid,
		SelfSigned,
		SignatureInvalid,
		Revoked,
		FileFormatInvalid,
		UnknownIssuer,
		Expired,
		UnsupportedAlgorithm
	}

	private static KeyStore trustStore = null;
	private static Map<String, Certificate> trustStoreMap = null;

	public static KeyStore loadDefaultTrustStore()
	{
        Path location = null;
        String type = null;
        String password = null;

        String locationProperty = System.getProperty("javax.net.ssl.trustStore");
        if ((null != locationProperty) && (locationProperty.length() > 0)) {
            Path p = Paths.get(locationProperty);
            File f = p.toFile();
            if (f.exists() && f.isFile() && f.canRead()) {
                location = p;
            }
        } else {
            String javaHome = System.getProperty("java.home");
            location = Paths.get(javaHome, "lib", "security", "jssecacerts");
            if (!location.toFile().exists()) {
                location = Paths.get(javaHome, "lib", "security", "cacerts");
            }
        }

        String passwordProperty = System.getProperty("javax.net.ssl.trustStorePassword");
        if ((null != passwordProperty) && (passwordProperty.length() > 0)) {
            password = passwordProperty;
        } else {
            password = "changeit";
        }

        String typeProperty = System.getProperty("javax.net.ssl.trustStoreType");
        if ((null != typeProperty) && (typeProperty.length() > 0)) {
            type = passwordProperty;
        } else {
            type = KeyStore.getDefaultType();
        }

        KeyStore trustStore = null;
        try
		{
            trustStore = KeyStore.getInstance(type, Security.getProvider("SUN"));
        }
		catch (KeyStoreException e)
		{
			e.printStackTrace();
			return null;
        }

        try (InputStream is = Files.newInputStream(location))
		{
            trustStore.load(is, password.toCharArray());
        }
		catch (IOException | CertificateException | NoSuchAlgorithmException e)
		{
			e.printStackTrace();
			return null;
        }
        return trustStore;
    }

	public static Map<String, Certificate> buildCNMap(KeyStore keystore)
	{
		try
		{
			Map<String, Certificate> certMap = new HashMap<String, Certificate>();
			Enumeration<String> aliases = keystore.aliases();
			while (aliases.hasMoreElements())
			{
				String alias = aliases.nextElement();
				Certificate cert = keystore.getCertificate(alias);
				byte[] asn1 = cert.getEncoded();
				MyX509File x509 = X509Parser.parseBuff(asn1, 0, asn1.length, "cert.crt");
				if (x509 != null)
				{
					if (x509.getFileType() == FileType.Cert)
					{
						MyX509Cert mycert = (MyX509Cert)x509;
						String subject = mycert.getSubjectCN();
						certMap.put(subject, cert);
					}
					/////////////////////
				}
			}
			return certMap;
		}
		catch (KeyStoreException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (CertificateEncodingException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

	public static X509CRL loadCRL(String filePath)
	{
		try
		{
			FileInputStream fis = new FileInputStream(filePath);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			return (X509CRL)cf.generateCRL(fis);
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (CRLException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (CertificateException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

	public static String nameGetCN(Principal name)
	{
		String str = name.getName();
		String strs[] = str.split(",");
		int i = 0;
		int j = strs.length;
		while (i < j)
		{
			if (strs[i].startsWith("CN="))
			{
				return strs[i].substring(3);
			}
			i++;
		}
		return null;
	}

	public static String getIssuerCN(X509CRL crl)
	{
		return nameGetCN(crl.getIssuerDN());
	}

	public static CertValidStatus isValid(X509CRL crl)
	{
		String algName = crl.getSigAlgName();
		byte[] signature = crl.getSignature();
		if (trustStore == null)
		{
			trustStore = loadDefaultTrustStore();
			if (trustStore != null)
			{
				trustStoreMap = buildCNMap(trustStore);
			}
		}
		String issuer = getIssuerCN(crl);
		if (trustStoreMap == null)
		{
			return CertValidStatus.UnknownIssuer;
		}
		System.out.println("algName = "+algName);
		System.out.println("Issuer = "+issuer);
		return CertValidStatus.SignatureInvalid;
	}
}
