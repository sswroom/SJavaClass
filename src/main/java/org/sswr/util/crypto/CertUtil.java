package org.sswr.util.crypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CRLException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.sswr.util.crypto.MyX509File.FileType;
import org.sswr.util.crypto.MyX509File.KeyType;
import org.sswr.util.data.ByteTool;
import org.sswr.util.net.ASN1Item;
import org.sswr.util.net.ASN1Util;
import org.sswr.util.parser.X509Parser;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class CertUtil
{
	private static final boolean DEBUG = false;

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

	@Nullable
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

	@Nullable
	public static KeyStore loadKeyStore(@Nonnull String fileName, @Nonnull String password)
	{
		FileInputStream fis = null;
		KeyStoreType type;
		try
		{
			fis = new FileInputStream(fileName);
			type = parseKeyStoreType(fis);
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			if (fis != null)
			{
				try
				{
					fis.close();
				}
				catch (IOException ex2)
				{
					
				}
			}
			return null;
		}
        KeyStore trustStore = null;
        try
		{
            trustStore = KeyStore.getInstance(getKeyStoreTypeName(type), Security.getProvider("SUN"));
        }
		catch (KeyStoreException e)
		{
			e.printStackTrace();
			try
			{
				fis.close();
			}
			catch (IOException ex2)
			{
				
			}
			return null;
        }

        try
		{
            trustStore.load(fis, password.toCharArray());
        }
		catch (IOException | CertificateException | NoSuchAlgorithmException e)
		{
			e.printStackTrace();
			try
			{
				fis.close();
			}
			catch (IOException ex2)
			{
				
			}
			return null;
        }
		try
		{
			fis.close();
		}
		catch (IOException ex2)
		{
			
		}
		return trustStore;
	}

	public static boolean isKeyStoreSingleCertWithKey(@Nonnull KeyStore ks, @Nonnull String password)
	{
		try
		{
			if (ks.size() != 1)
				return false;
			String alias = ks.aliases().nextElement();
			Certificate cert = ks.getCertificate(alias);
			Key key = ks.getKey(alias, password.toCharArray());
			return cert != null && key != null;
		}
		catch (KeyStoreException ex)
		{
			ex.printStackTrace();
			return false;
		}
		catch (NoSuchAlgorithmException ex)
		{
			ex.printStackTrace();
			return false;
		}
		catch (UnrecoverableKeyException ex)
		{
			ex.printStackTrace();
			return false;
		}
	}

	@Nonnull
	public static KeyStoreType parseKeyStoreType(@Nonnull FileInputStream is) throws IOException
	{
		byte[] buff = new byte[12];
		long pos = is.getChannel().position();
		if (is.read(buff, 0, 12) != 12)
		{
			is.getChannel().position(pos);
			return KeyStoreType.Unknown;
		}
		is.getChannel().position(pos);
		if (ByteTool.readMInt32(buff, 0) == 0xFEEDFEED && ByteTool.readMInt32(buff, 4) <= 2)
		{
			return KeyStoreType.JKS;
		}
		if (buff[0] == 0x30)
		{
			return KeyStoreType.PKCS12;
		}
		return KeyStoreType.Unknown;
	}

	public static boolean loadMyTrusts(@Nonnull Map<String, Certificate> certMap)
	{
		File trustPath = new File("trustcerts");
		if (DEBUG)
		{
			System.out.println("Cert Path = "+trustPath.getAbsoluteFile());
		}
		return loadTrustsFromDir(certMap, trustPath);
	}

	public static boolean loadTrustsFromDir(@Nonnull Map<String, Certificate> certMap, @Nonnull File trustPath)
	{
		if (trustPath.isDirectory())
		{
			File[] files = trustPath.listFiles();
			int i = 0;
			int j = files.length;
			while (i < j)
			{
				Certificate cert = loadCertificate(files[i]);
				if (cert != null)
				{
					MyX509Cert mycert = toMyCert(cert);
					if (mycert != null)
					{
						String subject = mycert.getSubjectCN();
						if (DEBUG)
						{
							System.out.println("Loaded "+files[i].getAbsolutePath()+": "+subject);
						}
						certMap.put(subject, cert);
					}
				}
				i++;
			}
			return true;
		}
		return false;
	}

	@Nullable
	public static Map<String, Certificate> buildCNMap(@Nonnull KeyStore keystore)
	{
		try
		{
			Map<String, Certificate> certMap = new HashMap<String, Certificate>();
			Enumeration<String> aliases = keystore.aliases();
			while (aliases.hasMoreElements())
			{
				String alias = aliases.nextElement();
				Certificate cert = keystore.getCertificate(alias);
				MyX509Cert mycert = toMyCert(cert);
				if (mycert != null)
				{
					String subject = mycert.getSubjectCN();
					certMap.put(subject, cert);
				}
			}
			return certMap;
		}
		catch (KeyStoreException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

	@Nullable
	public static MyX509Cert toMyCert(@Nonnull Certificate cert)
	{
		try
		{
			byte[] asn1 = cert.getEncoded();
			return new MyX509Cert("cert.cer", asn1, 0, asn1.length);
		}
		catch (CertificateEncodingException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

	@Nullable
	public static X509CRL loadCRL(@Nonnull String filePath)
	{
		FileInputStream fis = null;
		try
		{
			fis = new FileInputStream(filePath);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509CRL crl = (X509CRL)cf.generateCRL(fis);
			try
			{
				fis.close();
			}
			catch (IOException ex2)
			{

			}
			return crl;
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (CRLException ex)
		{
			ex.printStackTrace();
			if (fis != null)
			{
				try
				{
					fis.close();
				}
				catch (IOException ex2)
				{
					
				}
			}
			return null;
		}
		catch (CertificateException ex)
		{
			ex.printStackTrace();
			if (fis != null)
			{
				try
				{
					fis.close();
				}
				catch (IOException ex2)
				{
					
				}
			}
			return null;
		}
	}

	@Nullable
	public static X509Certificate loadCertificate(@Nonnull File file)
	{
		FileInputStream fis = null;
		try
		{
			fis = new FileInputStream(file);
			X509Certificate cert = loadCertificate(fis);
			try
			{
				fis.close();
			}
			catch (IOException ex2)
			{
			}
			return cert;
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

	@Nullable
	public static X509Certificate loadCertificate(@Nonnull InputStream stm)
	{
		try
		{
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			return (X509Certificate)cf.generateCertificate(stm);
		}
		catch (CertificateException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

	@Nullable
	public static PrivateKey loadPrivateKey(@Nonnull File file, @Nullable String password)
	{

		byte[] fileData;
		FileInputStream fis = null;
		try
		{
			fis = new FileInputStream(file);
			fileData = fis.readAllBytes();
			fis.close();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			if (fis != null)
			{
				try
				{
					fis.close();
				}
				catch (IOException ex2)
				{
	
				}
			}
			return null;
		}
		MyX509File x509 = X509Parser.parseBuff(fileData, 0, fileData.length, file.getName());
		if (x509 == null)
		{
			return null;
		}
		if (x509.getFileType() != FileType.Key)
		{
			return null;
		}
		MyX509Key key = (MyX509Key)x509;
		if (!key.isPrivateKey())
		{
			return null;
		}
		MyX509PrivKey privKey = MyX509PrivKey.createFromKey(key);
		if (privKey == null)
		{
			return null;
		}
		try
		{
			PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privKey.getASN1Buff());
			KeyFactory kf;
			KeyType keyType = key.getKeyType();
			if (keyType == KeyType.RSA)
			{
				kf = KeyFactory.getInstance("RSA");
			}
			else if (keyType == KeyType.DSA)
			{
				kf = KeyFactory.getInstance("DSA");
			}
			else if (keyType == KeyType.ECDSA)
			{
				kf = KeyFactory.getInstance("ECDSA");
			}
			else if (keyType == KeyType.ED25519)
			{
				kf = KeyFactory.getInstance("ED25519");
			}
			else
			{
				return null;
			}
			return kf.generatePrivate(spec);
		}
		catch (NoSuchAlgorithmException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (InvalidKeySpecException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

	@Nullable
	public static String nameGetCN(@Nonnull Principal name)
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

	@Nullable
	public static String getIssuerCN(@Nonnull X509CRL crl)
	{
		return nameGetCN(crl.getIssuerX500Principal());
	}

	@Nonnull
	public static CertValidStatus isValid(@Nonnull X509CRL crl)
	{
//		String algName = crl.getSigAlgName();
//		byte[] signature = crl.getSignature();
		if (trustStore == null)
		{
			trustStore = loadDefaultTrustStore();
			if (trustStore != null)
			{
				trustStoreMap = buildCNMap(trustStore);
			}
			if (trustStoreMap != null)
			{
				loadMyTrusts(trustStoreMap);
			}
		}
		String issuer = getIssuerCN(crl);
		if (trustStoreMap == null)
		{
			if (DEBUG)
			{
				System.out.println("trustStoreMap = null");
			}
			return CertValidStatus.UnknownIssuer;
		}
		if (issuer == null)
		{
			if (DEBUG)
			{
				System.out.println("issuer = "+issuer);
			}
			return CertValidStatus.UnknownIssuer;
		}
		Certificate issuerCert = trustStoreMap.get(issuer);
		if (issuerCert == null)
		{
			if (DEBUG)
			{
				System.out.println("issuer = "+issuer+", not found");
			}
			return CertValidStatus.UnknownIssuer;
		}
		try
		{
			crl.verify(issuerCert.getPublicKey());
			return CertValidStatus.Valid;
		}
		catch (SignatureException ex)
		{
			if (DEBUG)
			{
				ex.printStackTrace();
			}
			return CertValidStatus.SignatureInvalid;
		}
		catch (CRLException ex)
		{
			if (DEBUG)
			{
				ex.printStackTrace();
			}
			return CertValidStatus.FileFormatInvalid;
		}
		catch (NoSuchAlgorithmException ex)
		{
			if (DEBUG)
			{
				ex.printStackTrace();
			}
			return CertValidStatus.UnsupportedAlgorithm;
		}
		catch (InvalidKeyException ex)
		{
			if (DEBUG)
			{
				ex.printStackTrace();
			}
			return CertValidStatus.SignatureInvalid;
		}
		catch (NoSuchProviderException ex)
		{
			if (DEBUG)
			{
				ex.printStackTrace();
			}
			return CertValidStatus.UnsupportedAlgorithm;
		}
	}

	@Nullable
	public static byte[] rsaSignDecrypt(@Nonnull byte[] sign, int ofst, int len, @Nonnull Key key)
	{
		if (len != 256)
		{
			return null;
		}
		try
		{
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.DECRYPT_MODE, key);
			byte[] decryptedMessageHash = cipher.doFinal(sign, ofst, len);
			return decryptedMessageHash;
		}
		catch (NoSuchAlgorithmException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (NoSuchPaddingException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (InvalidKeyException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (BadPaddingException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (IllegalBlockSizeException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

	public static boolean verifySign(@Nonnull byte[] buff, int ofst, int buffSize, @Nonnull byte[] signature, int signOfst, int signLen, @Nonnull PublicKey key, @Nonnull HashType hashType, @Nullable StringBuilder sbError, @Nullable String dataName)
	{
		byte[] digestInfo = rsaSignDecrypt(signature, signOfst, signLen, key);
		if (digestInfo == null)
		{
			if (sbError != null) sbError.append(dataName+": Signature is not a valid RSA Signature.\r\n");
			return false;
		}
		ASN1Item item = ASN1Util.pduGetItem(digestInfo, 0, digestInfo.length, "1.1.1");
		if (item != null && item.itemType == ASN1Util.IT_OID)
		{
			HashType digestHash = MyX509File.hashTypeFromOID(digestInfo, item.ofst, item.len);
			if (digestHash != hashType)
			{
				if (sbError != null) sbError.append(dataName+": Hash Type mismatch, requested hash type = "+hashType+", hash type in signature = "+digestHash+"\r\n");
				if (digestHash != HashType.Unknown)
				{
					hashType = digestHash;
				}
			}
		}
		Hash hash = HashCreator.createHash(hashType);
		if (hash == null)
		{
			if (sbError != null) sbError.append(dataName+": Hash Type is not supported: "+hashType+"\r\n");
			return false;
		}
		item = ASN1Util.pduGetItem(digestInfo, 0, digestInfo.length, "1.2");
		if (item == null || item.itemType != ASN1Util.IT_OCTET_STRING)
		{
			if (sbError != null) sbError.append(dataName+": Signature format is not correct\r\n");
			return false;
		}
		hash.calc(buff, ofst, buffSize);
		byte[] hashVal = hash.getValue();
		if (hashVal.length != item.len)
		{
			if (sbError != null) sbError.append(dataName+": Hash length does not match: size in signature = "+item.len+", calculated hash = "+hashVal.length+"\r\n");
			return false;
		}
		if (ByteTool.byteEquals(hashVal, 0, digestInfo, item.ofst, item.len))
			return true;
		else
		{
			if (sbError != null) sbError.append(dataName+": Hash value not matched\r\n");
			return false;
		}
	}

	@Nullable
	public static byte[] signature(@Nonnull byte[] buff, int ofst, int buffSize, @Nonnull HashType hashType, @Nonnull PrivateKey key)
	{
		if (key.getAlgorithm().equals("RSA"))
		{
			Signature sig;
			try
			{
				switch (hashType)
				{
				case SHA1:
					sig = Signature.getInstance("SHA1withRSA");
					break;
				case SHA224:
					sig = Signature.getInstance("SHA224withRSA");
					break;
				case SHA256:
					sig = Signature.getInstance("SHA256withRSA");
					break;
				case SHA384:
					sig = Signature.getInstance("SHA384withRSA");
					break;
				case SHA512:
					sig = Signature.getInstance("SHA512withRSA");
					break;
				default:
					return null;
				}
				sig.initSign(key);
				sig.update(buff, ofst, buffSize);
				return sig.sign();
			}
			catch (NoSuchAlgorithmException ex)
			{
				ex.printStackTrace();
				return null;
			}
			catch (InvalidKeyException ex)
			{
				ex.printStackTrace();
				return null;
			}
			catch (SignatureException ex)
			{
				ex.printStackTrace();
				return null;
			}
		}
		return null;
	}

	@Nonnull
	public static String getKeyStoreTypeName(@Nonnull KeyStoreType type)
	{
		switch (type)
		{
		case Unknown:
		case PKCS12:
		default:
			return "pkcs12";
		case JKS:
			return "jks";
		case JCEKS:
			return "jceks";
		}
	}
}
