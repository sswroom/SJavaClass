package org.sswr.util.crypto;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;

import org.sswr.util.data.LineBreakType;
import org.sswr.util.data.StringUtil;
import org.sswr.util.net.SocketUtil;

public abstract class MyX509File extends ASN1Data
{
	public enum FileType
	{
		Cert,
		Key,
		CertRequest,
		PrivateKey,
		Jks,
		PublicKey,
		PKCS7,
		PKCS12,
		CRL
	}

	public enum KeyType
	{
		Unknown,
		RSA,
		DSA,
		ECDSA,
		ED25519,
		RSAPublic
	}

	public enum ContentDataType
	{
		Unknown,
		AuthenticatedSafe
	}

	protected MyX509File(String sourceName, byte[] buff, int ofst, int size)
	{
		super(sourceName, buff, ofst, size);
	}

	public ASN1Type getASN1Type()
	{
		return ASN1Type.X509;
	}

	public abstract FileType getFileType();

	protected static boolean isSigned(byte[] pdu, int beginOfst, int endOfst, String path)
	{
		int cnt = ASN1Util.pduCountItem(pdu, beginOfst, endOfst, path);
		if (cnt < 3)
		{
			return false;
		}
		if (ASN1Util.pduGetItemType(pdu, beginOfst, endOfst, path+".2") != ASN1Util.IT_SEQUENCE)
		{
			return false;
		}
		if (ASN1Util.pduGetItemType(pdu, beginOfst, endOfst, path+".3") != ASN1Util.IT_BIT_STRING)
		{
			return false;
		}
		return true;
	
	}

	protected static void appendSigned(byte[] pdu, int beginOfst, int endOfst, String path, StringBuilder sb, String varName)
	{
		String name;
		ASN1Item itemPDU;
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, path+".2")) != null)
		{
			if (itemPDU.itemType == ASN1Util.IT_SEQUENCE)
			{
				name = "algorithmIdentifier";
				if (varName != null)
				{
					name = varName + "." + name;
				}
				appendAlgorithmIdentifier(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, sb, name, false);
			}
		}
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, path+".3")) != null)
		{
			if (itemPDU.itemType == ASN1Util.IT_BIT_STRING)
			{
				if (varName != null)
				{
					sb.append(varName);
					sb.append(".");
				}
				sb.append("signature = ");
				StringUtil.appendHex(sb, pdu, itemPDU.ofst + 1, itemPDU.len - 1, ':', LineBreakType.NONE);
				sb.append("\r\n");
			}
		}
	}

	protected static boolean isTBSCertificate(byte[] pdu, int beginOfst, int endOfst, String path)
	{
		int cnt = ASN1Util.pduCountItem(pdu, beginOfst, endOfst, path);
		if (cnt < 6)
		{
			return false;
		}
		int i = 1;
		String ipath = path+"."+(i++);
		if (ASN1Util.pduGetItemType(pdu, beginOfst, endOfst, ipath) == ASN1Util.IT_CONTEXT_SPECIFIC_0)
		{
			ipath = path+"."+(i++);
		}
		if (ASN1Util.pduGetItemType(pdu, beginOfst, endOfst, ipath) != ASN1Util.IT_INTEGER)
		{
			return false;
		}
		ipath = path+"."+(i++);
		if (ASN1Util.pduGetItemType(pdu, beginOfst, endOfst, ipath) != ASN1Util.IT_SEQUENCE)
		{
			return false;
		}
		ipath = path+"."+(i++);
		if (ASN1Util.pduGetItemType(pdu, beginOfst, endOfst, ipath) != ASN1Util.IT_SEQUENCE)
		{
			return false;
		}
		ipath = path+"."+(i++);
		if (ASN1Util.pduGetItemType(pdu, beginOfst, endOfst, ipath) != ASN1Util.IT_SEQUENCE)
		{
			return false;
		}
		ipath = path+"."+(i++);
		if (ASN1Util.pduGetItemType(pdu, beginOfst, endOfst, ipath) != ASN1Util.IT_SEQUENCE)
		{
			return false;
		}
		ipath = path+"."+(i++);
		if (ASN1Util.pduGetItemType(pdu, beginOfst, endOfst, ipath) != ASN1Util.IT_SEQUENCE)
		{
			return false;
		}
		return true;
	}

	protected static void appendTBSCertificate(byte[] pdu, int beginOfst, int endOfst, String path, StringBuilder sb, String varName)
	{
		String name;
		int i = 1;
		ASN1Item itemPDU;
		String ipath;
		ipath = path+"."+(i++);
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, ipath)) != null)
		{
			if (itemPDU.itemType == ASN1Util.IT_CONTEXT_SPECIFIC_0)
			{
				if (varName != null)
				{
					sb.append(varName);
					sb.append('.');
				}
				sb.append("version = ");
				appendVersion(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "1", sb);
				sb.append("\r\n");
				ipath = path+"."+(i++);
			}
		}
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, ipath)) != null)
		{
			if (itemPDU.itemType == ASN1Util.IT_INTEGER)
			{
				if (varName != null)
				{
					sb.append(varName);
					sb.append('.');
				}
				sb.append("serialNumber = ");
				StringUtil.appendHex(sb, pdu, itemPDU.ofst, itemPDU.len, ':', LineBreakType.NONE);
				sb.append("\r\n");
			}
		}
		ipath = path+"."+(i++);
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, ipath)) != null)
		{
			if (itemPDU.itemType == ASN1Util.IT_SEQUENCE)
			{
				name = "signature";
				if (varName != null)
				{
					name = varName + '.' + name;
				}
				appendAlgorithmIdentifier(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, sb, name, false);
			}
		}
		ipath = path+"."+(i++);
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, ipath)) != null)
		{
			if (itemPDU.itemType == ASN1Util.IT_SEQUENCE)
			{
				name = "issuer";
				if (varName != null)
				{
					name = varName + '.' + name;
				}
				appendName(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, sb, name);
			}
		}
		ipath = path+"."+(i++);
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, ipath)) != null)
		{
			if (itemPDU.itemType == ASN1Util.IT_SEQUENCE)
			{
				name = "validity";
				if (varName != null)
				{
					name = varName + '.' + name;
				}
				appendValidity(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, sb, name);
			}
		}
		ipath = path+"."+(i++);
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, ipath)) != null)
		{
			if (itemPDU.itemType == ASN1Util.IT_SEQUENCE)
			{
				name = "subject";
				if (varName != null)
				{
					name = varName + '.' + name;
				}
				appendName(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, sb, name);
			}
		}
		ipath = path+"."+(i++);
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, ipath)) != null)
		{
			if (itemPDU.itemType == ASN1Util.IT_SEQUENCE)
			{
				name = "subjectPublicKeyInfo";
				if (varName != null)
				{
					name = varName + '.' + name;
				}
				appendSubjectPublicKeyInfo(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, sb, name);
				MyX509Key key;
				MyX509PubKey pubKey = new MyX509PubKey(name, pdu, itemPDU.pduBegin, itemPDU.ofst + itemPDU.len);
				key = pubKey.createKey();
				if (key != null)
				{
					sb.append(key.toString());
					sb.append("\r\n");
				}
			}
		}
		ipath = path+"."+(i++);
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, ipath)) != null)
		{
			if (itemPDU.itemType == ASN1Util.IT_CONTEXT_SPECIFIC_3)
			{
				name = "extensions";
				if (varName != null)
				{
					name = varName + '.' + name;
				}
				appendCRLExtensions(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, sb, name);
			}
		}
	}

	protected static boolean isCertificate(byte[] pdu, int beginOfst, int endOfst, String path)
	{
		return isSigned(pdu, beginOfst, endOfst, path) && isTBSCertificate(pdu, beginOfst, endOfst, path + ".1");
	}

	protected static void appendCertificate(byte[] pdu, int beginOfst, int endOfst, String path, StringBuilder sb, String varName)
	{
		appendTBSCertificate(pdu, beginOfst, endOfst, path + ".1", sb, varName);
		appendSigned(pdu, beginOfst, endOfst, path, sb, varName);
	}

	protected static boolean isPublicKeyInfo(byte[] pdu, int beginOfst, int endOfst, String path)
	{
		int cnt = ASN1Util.pduCountItem(pdu, beginOfst, endOfst, path);
		if (cnt < 2)
		{
			return false;
		}
		if (ASN1Util.pduGetItemType(pdu, beginOfst, endOfst, path + ".1") != ASN1Util.IT_SEQUENCE)
		{
			return false;
		}
		if (ASN1Util.pduGetItemType(pdu, beginOfst, endOfst, path + ".2") != ASN1Util.IT_BIT_STRING)
		{
			return false;
		}
		return true;
	}

	protected static void appendPublicKeyInfo(byte[] pdu, int beginOfst, int endOfst, String path, StringBuilder sb)
	{
		ASN1Item item = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, path);
		if (item != null && item.itemType == ASN1Util.IT_SEQUENCE)
		{
			appendSubjectPublicKeyInfo(pdu, item.ofst, item.ofst + item.len, sb, "PubKey");
			MyX509PubKey pubKey;
			MyX509Key key;
			pubKey = new MyX509PubKey("PubKey", pdu, item.pduBegin, item.len + item.ofst - item.pduBegin);
			key = pubKey.createKey();
			if (key != null)
			{
				sb.append(key.toString());
				sb.append("\r\n");
			}
		}
	}

	protected static boolean isContentInfo(byte[] pdu, int beginOfst, int endOfst, String path)
	{
		if (ASN1Util.pduGetItemType(pdu, beginOfst, endOfst, path) != ASN1Util.IT_SEQUENCE)
			return false;
		int cnt = ASN1Util.pduCountItem(pdu, beginOfst, endOfst, path);
		if (cnt != 2)
		{
			return false;
		}
		if (ASN1Util.pduGetItemType(pdu, beginOfst, endOfst, path + ".1") != ASN1Util.IT_OID)
		{
			return false;
		}
		if (ASN1Util.pduGetItemType(pdu, beginOfst, endOfst, path + ".2") != ASN1Util.IT_CONTEXT_SPECIFIC_0)
		{
			return false;
		}
		return true;
	}

	protected static void appendContentInfo(byte[] pdu, int beginOfst, int endOfst, String path, StringBuilder sb, String varName, ContentDataType dataType)
	{
		ASN1Item item = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, path);
		if (item != null && item.itemType == ASN1Util.IT_SEQUENCE)
		{
			ASN1Item contentType = ASN1Util.pduGetItem(pdu, item.ofst, item.ofst + item.len, "1");
			ASN1Item content = ASN1Util.pduGetItem(pdu, item.ofst, item.ofst + item.len, "2");
	
			if (contentType != null)
			{
				if (varName != null)
				{
					sb.append(varName);
					sb.append('.');
				}
				sb.append("content-type = ");
				ASN1Util.oidToString(pdu, contentType.ofst, contentType.len, sb);
				OIDInfo oid = ASN1OIDDB.oidGetEntry(pdu, contentType.ofst, contentType.len);
				if (oid != null)
				{
					sb.append(" (");
					sb.append(oid.name);
					sb.append(')');
				}
				sb.append("\r\n");
			}
			if (contentType != null && content != null)
			{
				if (ASN1Util.oidEqualsText(pdu, contentType.ofst, contentType.len, "1.2.840.113549.1.7.1")) //data
				{
					item = ASN1Util.pduGetItem(pdu, content.ofst, content.ofst + content.len, "1");
					if (item != null && item.itemType == ASN1Util.IT_OCTET_STRING)
					{
						appendData(pdu, item.ofst, item.ofst + item.len, sb, varName + ".pkcs7-content", dataType);
					}
				}
				else if (ASN1Util.oidEqualsText(pdu, contentType.ofst, contentType.len, "1.2.840.113549.1.7.2")) //signedData
				{
					appendPKCS7SignedData(pdu, content.ofst, content.ofst + content.len, sb, varName + ".pkcs7-content");
				}
				else if (ASN1Util.oidEqualsText(pdu, contentType.ofst, contentType.len, "1.2.840.113549.1.7.3")) //envelopedData
				{
	
				}
				else if (ASN1Util.oidEqualsText(pdu, contentType.ofst, contentType.len, "1.2.840.113549.1.7.4")) //signedAndEnvelopedData
				{
	
				}
				else if (ASN1Util.oidEqualsText(pdu, contentType.ofst, contentType.len, "1.2.840.113549.1.7.5")) //digestedData
				{
	
				}
				else if (ASN1Util.oidEqualsText(pdu, contentType.ofst, contentType.len, "1.2.840.113549.1.7.6")) //encryptedData
				{
					appendEncryptedData(pdu, content.ofst, content.ofst + content.len, sb, varName + ".pkcs7-content", dataType);
				}
			}
		}
	}

	protected static boolean isPFX(byte[] pdu, int beginOfst, int endOfst, String path)
	{
		if (ASN1Util.pduGetItemType(pdu, beginOfst, endOfst, path) != ASN1Util.IT_SEQUENCE)
			return false;
		int cnt = ASN1Util.pduCountItem(pdu, beginOfst, endOfst, path);
		if (cnt != 2 && cnt != 3)
		{
			return false;
		}
		if (ASN1Util.pduGetItemType(pdu, beginOfst, endOfst, path + ".1") != ASN1Util.IT_INTEGER)
		{
			return false;
		}
		if (!isContentInfo(pdu, beginOfst, endOfst, path + ".2"))
		{
			return false;
		}
		if (cnt == 3)
		{
			if (ASN1Util.pduGetItemType(pdu, beginOfst, endOfst, path + ".3") != ASN1Util.IT_SEQUENCE)
			{
				return false;
			}
		}
		return true;
	}

	protected static void appendPFX(byte[] pdu, int beginOfst, int endOfst, String path, StringBuilder sb, String varName)
	{
		ASN1Item item = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, path);
		if (item != null && item.itemType == ASN1Util.IT_SEQUENCE)
		{
			String name;
			int cnt = ASN1Util.pduCountItem(pdu, item.ofst, item.ofst + item.len, null);
			ASN1Item version = ASN1Util.pduGetItem(pdu, item.ofst, item.ofst + item.len, "1");
			if (version != null && version.itemType == ASN1Util.IT_INTEGER)
			{
				if (varName != null)
				{
					sb.append(varName);
					sb.append('.');
				}
				sb.append("version = ");
				ASN1Util.integerToString(pdu, version.ofst, version.len, sb);
				sb.append("\r\n");
			}
			name = "authSafe";
			if (varName != null)
			{
				name = varName + "." + name;
			}
			appendContentInfo(pdu, item.ofst, item.ofst + item.len, "2", sb, name, ContentDataType.AuthenticatedSafe);
			if (cnt == 3)
			{
				name = "macData";
				if (varName != null)
				{
					name = varName + "." + name;
				}
				appendMacData(pdu, item.ofst, item.ofst + item.len, "3", sb, name);
			}
		}
	}

	protected static void appendVersion(byte[] pdu, int beginOfst, int endOfst, String path, StringBuilder sb)
	{
		ASN1Item itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, path);
		if (itemPDU != null && itemPDU.itemType == ASN1Util.IT_INTEGER)
		{
			if (itemPDU.len == 1)
			{
				switch (pdu[itemPDU.ofst])
				{
				case 0:
					sb.append("v1");
					break;
				case 1:
					sb.append("v2");
					break;
				case 2:
					sb.append("v3");
					break;
				}
			}
		}
	}

	protected static KeyType appendAlgorithmIdentifier(byte[] pdu, int beginOfst, int endOfst, StringBuilder sb, String varName, boolean pubKey)
	{
		KeyType keyType = KeyType.Unknown;
		ASN1Item algorithm = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, "1");
		ASN1Item parameters = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, "2");
		if (algorithm != null && algorithm.itemType == ASN1Util.IT_OID)
		{
			sb.append(varName);
			sb.append('.');
			sb.append("algorithm = ");
			ASN1Util.oidToString(pdu, algorithm.ofst, algorithm.len, sb);
			keyType = keyTypeFromOID(pdu, algorithm.ofst, algorithm.len, pubKey);
			OIDInfo oid = ASN1OIDDB.oidGetEntry(pdu, algorithm.ofst, algorithm.len);
			if (oid != null)
			{
				sb.append(" (");
				sb.append(oid.name);
				sb.append(')');
			}
			sb.append("\r\n");
		}
		if (parameters != null)
		{
			sb.append(varName);
			sb.append('.');
			sb.append("parameters = ");
			if (parameters.itemType == ASN1Util.IT_NULL)
			{
				sb.append("NULL");
			}
			sb.append("\r\n");
		}
		return keyType;
	}

	protected static void appendValidity(byte[] pdu, int beginOfst, int endOfst, StringBuilder sb, String varName)
	{
		ZonedDateTime dt;
		ASN1Item itemPDU;
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, "1")) != null)
		{
			if ((itemPDU.itemType == ASN1Util.IT_UTCTIME || itemPDU.itemType == ASN1Util.IT_GENERALIZEDTIME) && (dt = ASN1Util.pduParseUTCTimeCont(pdu, itemPDU.ofst, itemPDU.len)) != null)
			{
				sb.append(varName);
				sb.append('.');
				sb.append("notBefore = ");
				sb.append(dt.toString());
				sb.append("\r\n");
			}
		}
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, "2")) != null)
		{
			if ((itemPDU.itemType == ASN1Util.IT_UTCTIME || itemPDU.itemType == ASN1Util.IT_GENERALIZEDTIME) && (dt = ASN1Util.pduParseUTCTimeCont(pdu, itemPDU.ofst, itemPDU.len)) != null)
			{
				sb.append(varName);
				sb.append('.');
				sb.append("notAfter = ");
				sb.append(dt.toString());
				sb.append("\r\n");
			}
		}
	}

	protected static void appendSubjectPublicKeyInfo(byte[] pdu, int beginOfst, int endOfst, StringBuilder sb, String varName)
	{
		ASN1Item itemPDU;
		KeyType keyType = KeyType.Unknown;
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, "1")) != null)
		{
			if (itemPDU.itemType == ASN1Util.IT_SEQUENCE)
			{
				keyType = appendAlgorithmIdentifier(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, sb, varName + ".algorithm", true);
			}
		}
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, "2")) != null)
		{
			if (itemPDU.itemType == ASN1Util.IT_BIT_STRING)
			{
				sb.append(varName);
				sb.append(".subjectPublicKey = ");
				StringUtil.appendHex(sb, pdu, itemPDU.ofst + 1, itemPDU.len - 1, ':', LineBreakType.NONE);
				sb.append("\r\n");
				if (keyType != KeyType.Unknown)
				{
					MyX509Key pkey = new MyX509Key(varName + ".subjectPublicKey", pdu, itemPDU.ofst + 1, itemPDU.len - 1, keyType);
					sb.append(pkey.toString());
					sb.append("\r\n");
				}
			}
		}
	}

	protected static void appendName(byte[] pdu, int beginOfst, int endOfst, StringBuilder sb, String varName)
	{
		ASN1Item itemPDU;
		int cnt = ASN1Util.pduCountItem(pdu, beginOfst, endOfst, null);
		int i = 0;
		while (i < cnt)
		{
			i++;
	
			if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, String.valueOf(i))) != null)
			{
				if (itemPDU.itemType == ASN1Util.IT_SET)
				{
					appendRelativeDistinguishedName(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, sb, varName);
				}
			}
		}
	}

	protected static void appendRelativeDistinguishedName(byte[] pdu, int beginOfst, int endOfst, StringBuilder sb, String varName)
	{
		ASN1Item itemPDU;
		int cnt = ASN1Util.pduCountItem(pdu, beginOfst, endOfst, null);
		int i = 0;
		while (i < cnt)
		{
			i++;

			if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, String.valueOf(i))) != null)
			{
				if (itemPDU.itemType == ASN1Util.IT_SEQUENCE)
				{
					appendAttributeTypeAndDistinguishedValue(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, sb, varName);
				}
			}
		}
	}

	protected static void appendAttributeTypeAndDistinguishedValue(byte[] pdu, int beginOfst, int endOfst, StringBuilder sb, String varName)
	{
		ASN1Item typePDU;
		ASN1Item valuePDU;
		typePDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, "1");
		valuePDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, "2");
		if (typePDU != null && valuePDU != null && typePDU.itemType == ASN1Util.IT_OID)
		{
			sb.append(varName);
			sb.append('.');
			if (ASN1Util.oidEqualsText(pdu, typePDU.ofst, typePDU.len, "2.5.4.3"))
			{
				sb.append("commonName");
			}
			else if (ASN1Util.oidEqualsText(pdu, typePDU.ofst, typePDU.len, "2.5.4.6"))
			{
				sb.append("countryName");
			}
			else if (ASN1Util.oidEqualsText(pdu, typePDU.ofst, typePDU.len, "2.5.4.7"))
			{
				sb.append("localityName");
			}
			else if (ASN1Util.oidEqualsText(pdu, typePDU.ofst, typePDU.len, "2.5.4.8"))
			{
				sb.append("stateOrProvinceName");
			}
			else if (ASN1Util.oidEqualsText(pdu, typePDU.ofst, typePDU.len, "2.5.4.10"))
			{
				sb.append("organizationName");
			}
			else if (ASN1Util.oidEqualsText(pdu, typePDU.ofst, typePDU.len, "2.5.4.11"))
			{
				sb.append("organizationalUnitName");
			}
			else if (ASN1Util.oidEqualsText(pdu, typePDU.ofst, typePDU.len, "1.2.840.113549.1.9.1"))
			{
				sb.append("emailAddress");
			}
			else
			{
				ASN1Util.oidToString(pdu, typePDU.ofst, typePDU.len, sb);
			}
			sb.append(" = ");
			sb.append(new String(pdu, valuePDU.ofst, valuePDU.len, StandardCharsets.UTF_8));
			sb.append("\r\n");
		}
	}

	protected static void appendCRLExtensions(byte[] pdu, int beginOfst, int endOfst, StringBuilder sb, String varName)
	{
		ASN1Item itemPDU;
		ASN1Item subItemPDU;
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, "1")) != null)
		{
			if (itemPDU.itemType == ASN1Util.IT_SEQUENCE)
			{
				int i = 1;
				while ((subItemPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, String.valueOf(i))) != null)
				{
					appendCRLExtension(pdu, subItemPDU.ofst, subItemPDU.ofst + subItemPDU.len, sb, varName);
					i++;
				}
			}
		}
	}
	
	protected static void appendCRLExtension(byte[] pdu, int beginOfst, int endOfst, StringBuilder sb, String varName)
	{
		ASN1Item extension = null;
		ASN1Item itemPDU;
		ASN1Item subItemPDU;
		if ((extension = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, "1")) != null)
		{
			if (extension.itemType == ASN1Util.IT_OID)
			{
				sb.append(varName);
				sb.append('.');
				sb.append("extensionType = ");
				ASN1Util.oidToString(pdu, extension.ofst, extension.len, sb);
				OIDInfo oid = ASN1OIDDB.oidGetEntry(pdu, extension.ofst, extension.len);
				if (oid != null)
				{
					sb.append(" (");
					sb.append(oid.name);
					sb.append(')');
				}
				sb.append("\r\n");
			}
		}
		else
		{
			return;
		}
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, "2")) != null)
		{
			if (itemPDU.itemType == ASN1Util.IT_BOOLEAN)
			{
				sb.append(varName);
				sb.append('.');
				sb.append("critical = ");
				ASN1Util.booleanToString(pdu, itemPDU.ofst, itemPDU.len, sb);
				sb.append("\r\n");
				if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, "3")) == null)
				{
					return;
				}
			}
			if (itemPDU.itemType == ASN1Util.IT_OCTET_STRING)
			{
				if (ASN1Util.oidEqualsText(pdu, extension.ofst, extension.len, "1.3.6.1.5.5.7.1.1")) //id-pe-authorityInfoAccess
				{
					if ((itemPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "1")) != null && itemPDU.itemType == ASN1Util.IT_SEQUENCE)
					{
						int i = 1;
						while ((subItemPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, String.valueOf(i))) != null && subItemPDU.itemType == ASN1Util.IT_SEQUENCE)
						{
							ASN1Item descPDU;
							if ((descPDU = ASN1Util.pduGetItem(pdu, subItemPDU.ofst, subItemPDU.ofst + subItemPDU.len, "1")) != null && descPDU.itemType == ASN1Util.IT_OID)
							{
								sb.append(varName);
								sb.append(".authorityInfoAccess[");
								sb.append(i);
								sb.append("].accessMethod = ");
								ASN1Util.oidToString(pdu, descPDU.ofst, descPDU.len, sb);
								sb.append(" (");
								ASN1OIDDB.oidToNameString(pdu, descPDU.ofst, descPDU.len, sb);
								sb.append(")\r\n");
							}
							appendGeneralName(pdu, subItemPDU.ofst, subItemPDU.ofst + subItemPDU.len, "2", sb, varName+".authorityInfoAccess["+i+"].accessLocation");
							i++;
						}
					}
				}
				else if (ASN1Util.oidEqualsText(pdu, extension.ofst, extension.len, "2.5.29.14")) //id-ce-subjectKeyIdentifier
				{
					sb.append(varName);
					sb.append('.');
					sb.append("subjectKeyId = ");
					if (itemPDU.len == 22 && pdu[itemPDU.ofst + 1] == 20)
					{
						StringUtil.appendHex(sb, pdu, itemPDU.ofst + 2, itemPDU.len - 2, ':', LineBreakType.NONE);
					}
					else
					{
						StringUtil.appendHex(sb, pdu, itemPDU.ofst, itemPDU.len, ':', LineBreakType.NONE);
					}
					sb.append("\r\n");
				}
				else if (ASN1Util.oidEqualsText(pdu, extension.ofst, extension.len, "2.5.29.15")) //id-ce-keyUsage
				{
					if ((subItemPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "1")) != null && subItemPDU.itemType == ASN1Util.IT_BIT_STRING)
					{
						sb.append(varName);
						sb.append('.');
						sb.append("keyUsage =");
						if (subItemPDU.len >= 2)
						{
							if ((pdu[subItemPDU.ofst + 1] & 0x80) != 0) sb.append(" digitalSignature");
							if ((pdu[subItemPDU.ofst + 1] & 0x40) != 0) sb.append(" nonRepudiation");
							if ((pdu[subItemPDU.ofst + 1] & 0x20) != 0) sb.append(" keyEncipherment");
							if ((pdu[subItemPDU.ofst + 1] & 0x10) != 0) sb.append(" dataEncipherment");
							if ((pdu[subItemPDU.ofst + 1] & 0x8) != 0) sb.append(" keyAgreement");
							if ((pdu[subItemPDU.ofst + 1] & 0x4) != 0) sb.append(" keyCertSign");
							if ((pdu[subItemPDU.ofst + 1] & 0x2) != 0) sb.append(" cRLSign");
							if ((pdu[subItemPDU.ofst + 1] & 0x1) != 0) sb.append(" encipherOnly");
						}
						if (subItemPDU.len >= 3)
						{
							if ((pdu[subItemPDU.ofst + 2] & 0x80) != 0) sb.append(" decipherOnly");
						}
						sb.append("\r\n");
					}
				}
				else if (ASN1Util.oidEqualsText(pdu, extension.ofst, extension.len, "2.5.29.17")) //id-ce-subjectAltName
				{
					appendGeneralNames(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, sb, varName + ".subjectAltName");
				}
				else if (ASN1Util.oidEqualsText(pdu, extension.ofst, extension.len, "2.5.29.19")) //id-ce-basicConstraints
				{
					if ((itemPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "1")) != null && itemPDU.itemType == ASN1Util.IT_SEQUENCE)
					{
						if ((subItemPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "1")) != null && subItemPDU.itemType == ASN1Util.IT_BOOLEAN)
						{
							sb.append(varName);
							sb.append('.');
							sb.append("basicConstraints.cA = ");
							ASN1Util.booleanToString(pdu, subItemPDU.ofst, subItemPDU.len, sb);
							sb.append("\r\n");
						}
						if ((subItemPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "2")) != null && subItemPDU.itemType == ASN1Util.IT_INTEGER)
						{
							sb.append(varName);
							sb.append('.');
							sb.append("basicConstraints.pathLenConstraint = ");
							ASN1Util.integerToString(pdu, subItemPDU.ofst, subItemPDU.len, sb);
							sb.append("\r\n");
						}
					}
				}
				else if (ASN1Util.oidEqualsText(pdu, extension.ofst, extension.len, "2.5.29.20")) //id-ce-cRLNumber
				{
					if ((subItemPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "1")) != null && subItemPDU.itemType == ASN1Util.IT_INTEGER)
					{
						sb.append(varName);
						sb.append('.');
						sb.append("cRLNumber = ");
						ASN1Util.integerToString(pdu, subItemPDU.ofst, subItemPDU.len, sb);
						sb.append("\r\n");
					}
				}
				else if (ASN1Util.oidEqualsText(pdu, extension.ofst, extension.len, "2.5.29.21")) //id-ce-cRLReasons
				{
					if ((subItemPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "1")) != null && subItemPDU.itemType == ASN1Util.IT_ENUMERATED && subItemPDU.len == 1)
					{
						sb.append(varName);
						sb.append('.');
						sb.append("cRLReasons = ");
						switch (pdu[subItemPDU.ofst])
						{
						case 0:
							sb.append("unspecified");
							break;
						case 1:
							sb.append("keyCompromise");
							break;
						case 2:
							sb.append("cACompromise");
							break;
						case 3:
							sb.append("affiliationChanged");
							break;
						case 4:
							sb.append("superseded");
							break;
						case 5:
							sb.append("cessationOfOperation");
							break;
						case 6:
							sb.append("certificateHold");
							break;
						case 8:
							sb.append("removeFromCRL");
							break;
						case 9:
							sb.append("privilegeWithdrawn");
							break;
						case 10:
							sb.append("aACompromise");
							break;
						default:
							sb.append("unknown");
							break;
						}
						sb.append("\r\n");
					}
				}
				else if (ASN1Util.oidEqualsText(pdu, extension.ofst, extension.len, "2.5.29.31")) //id-ce-cRLDistributionPoints
				{
					if ((itemPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "1")) != null && itemPDU.itemType == ASN1Util.IT_SEQUENCE)
					{
						int i = 1;
						while (appendDistributionPoint(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, String.valueOf(i), sb, varName))
						{
							i++;
						}
					}
				}
				else if (ASN1Util.oidEqualsText(pdu, extension.ofst, extension.len, "2.5.29.32")) //id-ce-certificatePolicies
				{
					if ((itemPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "1")) != null && itemPDU.itemType == ASN1Util.IT_SEQUENCE)
					{
						int i = 1;
						while (appendPolicyInformation(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, String.valueOf(i), sb, varName))
						{
							i++;
						}
					}
				}
				else if (ASN1Util.oidEqualsText(pdu, extension.ofst, extension.len, "2.5.29.35")) //id-ce-authorityKeyIdentifier
				{
					if ((itemPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "1")) != null && itemPDU.itemType == ASN1Util.IT_SEQUENCE)
					{
						int i = 1;
						while ((subItemPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, String.valueOf(i))) != null)
						{
							if (subItemPDU.itemType == 0x80)
							{
								sb.append(varName);
								sb.append('.');
								sb.append("authorityKey.keyId = ");
								StringUtil.appendHex(sb, pdu, subItemPDU.ofst, subItemPDU.len, ':', LineBreakType.NONE);
								sb.append("\r\n");
							}
							else if (subItemPDU.itemType == 0x81 || subItemPDU.itemType == 0xa1)
							{
								appendGeneralName(pdu, subItemPDU.ofst, subItemPDU.ofst + subItemPDU.len, "1", sb, varName + ".authorityKey.authorityCertIssuer");
							}
							else if (subItemPDU.itemType == 0x82)
							{
								sb.append(varName);
								sb.append('.');
								sb.append("authorityKey.authorityCertSerialNumber = ");
								StringUtil.appendHex(sb, pdu, subItemPDU.ofst, subItemPDU.len, ':', LineBreakType.NONE);
								sb.append("\r\n");
							}
							i++;
						}
					}
				}
				else if (ASN1Util.oidEqualsText(pdu, extension.ofst, extension.len, "2.5.29.37")) //id-ce-extKeyUsage
				{
					if ((itemPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "1")) != null && itemPDU.itemType == ASN1Util.IT_SEQUENCE)
					{
						int i = 1;
	
						sb.append(varName);
						sb.append('.');
						sb.append("extKeyUsage =");
	
						while ((subItemPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, String.valueOf(i))) != null)
						{
							if (subItemPDU.itemType == ASN1Util.IT_OID)
							{
								if (ASN1Util.oidEqualsText(pdu, subItemPDU.ofst, subItemPDU.len, "1.3.6.1.5.5.7.3.1"))
								{
									sb.append(" serverAuth");
								}
								else if (ASN1Util.oidEqualsText(pdu, subItemPDU.ofst, subItemPDU.len, "1.3.6.1.5.5.7.3.2"))
								{
									sb.append(" clientAuth");
								}
								else if (ASN1Util.oidEqualsText(pdu, subItemPDU.ofst, subItemPDU.len, "1.3.6.1.5.5.7.3.3"))
								{
									sb.append(" codeSigning");
								}
								else if (ASN1Util.oidEqualsText(pdu, subItemPDU.ofst, subItemPDU.len, "1.3.6.1.5.5.7.3.4"))
								{
									sb.append(" emailProtection");
								}
								else if (ASN1Util.oidEqualsText(pdu, subItemPDU.ofst, subItemPDU.len, "1.3.6.1.5.5.7.3.8"))
								{
									sb.append(" timeStamping");
								}
								else if (ASN1Util.oidEqualsText(pdu, subItemPDU.ofst, subItemPDU.len, "1.3.6.1.5.5.7.3.9"))
								{
									sb.append(" OCSPSigning");
								}
							}
							i++;
						}
						sb.append("\r\n");
					}
				}
			}
		}
	}
		
	protected static void appendGeneralNames(byte[] pdu, int beginOfst, int endOfst, StringBuilder sb, String varName)
	{
		ASN1Item itemPDU;
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, "1")) != null)
		{
			if (itemPDU.itemType == ASN1Util.IT_SEQUENCE)
			{
				int i = 1;
				while (appendGeneralName(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, String.valueOf(i), sb, varName))
				{
					i++;
				}
			}
		}
	}

	protected static boolean appendGeneralName(byte[] pdu, int beginOfst, int endOfst, String path, StringBuilder sb, String varName)
	{
		ASN1Item subItemPDU;
		if ((subItemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, path)) != null)
		{
			switch (0x8F & subItemPDU.itemType)
			{
			case 0x80:
				sb.append(varName);
				sb.append(".otherName = ");
				sb.append(new String(pdu, subItemPDU.ofst, subItemPDU.len, StandardCharsets.UTF_8));
				sb.append("\r\n");
				return true;
			case 0x81:
				sb.append(varName);
				sb.append(".rfc822Name = ");
				sb.append(new String(pdu, subItemPDU.ofst, subItemPDU.len, StandardCharsets.UTF_8));
				sb.append("\r\n");
				return true;
			case 0x82:
				sb.append(varName);
				sb.append(".dNSName = ");
				sb.append(new String(pdu, subItemPDU.ofst, subItemPDU.len, StandardCharsets.UTF_8));
				sb.append("\r\n");
				return true;
			case 0x83:
				sb.append(varName);
				sb.append(".x400Address = ");
				sb.append(new String(pdu, subItemPDU.ofst, subItemPDU.len, StandardCharsets.UTF_8));
				sb.append("\r\n");
				return true;
			case 0x84:
				if ((subItemPDU = ASN1Util.pduGetItem(pdu, subItemPDU.ofst, subItemPDU.ofst + subItemPDU.len, path)) != null && subItemPDU.itemType == ASN1Util.IT_SEQUENCE)
				{
					appendName(pdu, subItemPDU.ofst, subItemPDU.ofst + subItemPDU.len, sb, varName + ".directoryName");
				}
				return true;
			case 0x85:
				sb.append(varName);
				sb.append(".ediPartyName = ");
				sb.append(new String(pdu, subItemPDU.ofst, subItemPDU.len, StandardCharsets.UTF_8));
				sb.append("\r\n");
				return true;
			case 0x86:
				sb.append(varName);
				sb.append(".uniformResourceIdentifier = ");
				sb.append(new String(pdu, subItemPDU.ofst, subItemPDU.len, StandardCharsets.UTF_8));
				sb.append("\r\n");
				return true;
			case 0x87:
				sb.append(varName);
				sb.append(".iPAddress = ");
				if (subItemPDU.len == 4)
				{
					sb.append(SocketUtil.getIPv4Name(pdu, subItemPDU.ofst));
				}
				else if (subItemPDU.len == 16)
				{
					///////////////////////////////////////
/*					Net::SocketUtil::AddressInfo addr;
					Net::SocketUtil::SetAddrInfoV6(&addr, subItemPDU, 0);
					sptr = Net::SocketUtil::GetAddrName(sbuff, &addr);
					sb.appendP(sbuff, sptr);*/
				}
				sb.append("\r\n");
				return true;
			case 0x88:
				sb.append(varName);
				sb.append(".registeredID = ");
				ASN1Util.oidToString(pdu, subItemPDU.ofst, subItemPDU.len, sb);
				{
					OIDInfo ent = ASN1OIDDB.oidGetEntry(pdu, subItemPDU.ofst, subItemPDU.len);
					if (ent != null)
					{
						sb.append(" (");
						sb.append(ent.name);
						sb.append(')');
					}
				}
				sb.append("\r\n");
				return true;
			}
		}
		return false;
	}

	protected static boolean appendDistributionPoint(byte[] pdu, int beginOfst, int endOfst, String path, StringBuilder sb, String varName)
	{
		ASN1Item itemPDU;
		ASN1Item subItemPDU;
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, path)) != null)
		{
			if (itemPDU.itemType == ASN1Util.IT_SEQUENCE)
			{
				int i = 1;
				while ((subItemPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, String.valueOf(i))) != null)
				{
					switch (subItemPDU.itemType)
					{
					case ASN1Util.IT_CONTEXT_SPECIFIC_0:
						appendDistributionPointName(pdu, subItemPDU.ofst, subItemPDU.ofst + subItemPDU.len, sb, varName + ".distributionPoint");
						break;
					case ASN1Util.IT_CONTEXT_SPECIFIC_1:
						if ((subItemPDU = ASN1Util.pduGetItem(pdu, subItemPDU.ofst, subItemPDU.ofst + subItemPDU.len, "1")) != null && subItemPDU.itemType == ASN1Util.IT_BIT_STRING)
						{
							sb.append(varName);
							sb.append(".reasons =");
							if (subItemPDU.len >= 2)
							{
								if ((pdu[subItemPDU.ofst + 1] & 0x80) != 0) sb.append("unused");
								if ((pdu[subItemPDU.ofst + 1] & 0x40) != 0) sb.append("keyCompromise");
								if ((pdu[subItemPDU.ofst + 1] & 0x20) != 0) sb.append("cACompromise");
								if ((pdu[subItemPDU.ofst + 1] & 0x10) != 0) sb.append("affiliationChanged");
								if ((pdu[subItemPDU.ofst + 1] & 0x8) != 0) sb.append("superseded");
								if ((pdu[subItemPDU.ofst + 1] & 0x4) != 0) sb.append("cessationOfOperation");
								if ((pdu[subItemPDU.ofst + 1] & 0x2) != 0) sb.append("certificateHold");
								if ((pdu[subItemPDU.ofst + 1] & 0x1) != 0) sb.append("privilegeWithdrawn");
							}
							if (subItemPDU.len >= 3)
							{
								if ((pdu[subItemPDU.ofst + 2] & 0x80) != 0) sb.append("aACompromise");
							}
							sb.append("\r\n");
						}
						break;
					case ASN1Util.IT_CONTEXT_SPECIFIC_2:
						appendGeneralNames(pdu, subItemPDU.ofst, subItemPDU.ofst + subItemPDU.len, sb, varName + ".cRLIssuer");
						break;
					}
					i++;
				}
				return true;
			}
		}
		return false;
	}

	protected static void appendDistributionPointName(byte[] pdu, int beginOfst, int endOfst, StringBuilder sb, String varName)
	{
		int i;
		ASN1Item itemPDU;
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, "1")) != null)
		{
			if (itemPDU.itemType == ASN1Util.IT_CONTEXT_SPECIFIC_0)
			{
				i = 1;
				while (appendGeneralName(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, String.valueOf(i), sb, varName + ".fullName"))
				{
					i++;
				}
			}
			else if (itemPDU.itemType == ASN1Util.IT_CONTEXT_SPECIFIC_1)
			{
				appendRelativeDistinguishedName(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, sb, varName + ".nameRelativeToCRLIssuer");
			}
		}
	}

	protected static boolean appendPolicyInformation(byte[] pdu, int beginOfst, int endOfst, String path, StringBuilder sb, String varName)
	{
		ASN1Item itemPDU;
		ASN1Item subItemPDU;
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, path)) != null && itemPDU.itemType == ASN1Util.IT_SEQUENCE)
		{
			subItemPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "1");
			if (subItemPDU != null && subItemPDU.itemType == ASN1Util.IT_OID)
			{
				sb.append(varName);
				sb.append('.');
				sb.append("policyIdentifier = ");
				ASN1Util.oidToString(pdu, subItemPDU.ofst, subItemPDU.len, sb);
				sb.append(" (");
				ASN1OIDDB.oidToNameString(pdu, subItemPDU.ofst, subItemPDU.len, sb);
				sb.append(')');
				sb.append("\r\n");
			}
			subItemPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "2");
			if (subItemPDU != null && subItemPDU.itemType == ASN1Util.IT_SEQUENCE)
			{
				ASN1Item policyQualifierInfoPDU;
				int i = 1;
				while ((policyQualifierInfoPDU = ASN1Util.pduGetItem(pdu, subItemPDU.ofst, subItemPDU.ofst + subItemPDU.len, String.valueOf(i))) != null && policyQualifierInfoPDU.itemType == ASN1Util.IT_SEQUENCE)
				{
					if ((itemPDU = ASN1Util.pduGetItem(pdu, policyQualifierInfoPDU.ofst, policyQualifierInfoPDU.ofst + policyQualifierInfoPDU.len, "1")) != null && itemPDU.itemType == ASN1Util.IT_OID)
					{
						sb.append(varName);
						sb.append('.');
						sb.append("policyQualifiers[");
						sb.append(i);
						sb.append("].policyQualifierId = ");
						ASN1Util.oidToString(pdu, itemPDU.ofst, itemPDU.len, sb);
						sb.append(" (");
						ASN1OIDDB.oidToNameString(pdu, itemPDU.ofst, itemPDU.len, sb);
						sb.append(')');
						sb.append("\r\n");
					}
					if ((itemPDU = ASN1Util.pduGetItem(pdu, policyQualifierInfoPDU.ofst, policyQualifierInfoPDU.ofst + policyQualifierInfoPDU.len, "2")) != null)
					{
						if (policyQualifierInfoPDU.itemType == ASN1Util.IT_IA5STRING)
						{
							sb.append(varName);
							sb.append('.');
							sb.append("policyQualifiers[");
							sb.append(i);
							sb.append("].qualifier = ");
							sb.append(new String(pdu, itemPDU.ofst, itemPDU.len, StandardCharsets.UTF_8));
							sb.append("\r\n");
						}
						else if (policyQualifierInfoPDU.itemType == ASN1Util.IT_SEQUENCE)
						{
							/////////////////////////////////// UserNotice
						}
					}
					i++;
				}
			}
			return true;
		}
		return false;
	}

	protected static void appendPKCS7SignedData(byte[] pdu, int beginOfst, int endOfst, StringBuilder sb, String varName)
	{
		ASN1Item itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, "1");
		if (itemPDU != null && itemPDU.itemType == ASN1Util.IT_SEQUENCE)
		{
			int i;
			ASN1Item subItemPDU;
			if ((subItemPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "1")) != null && subItemPDU.itemType == ASN1Util.IT_INTEGER)
			{
				sb.append("signedData.version = ");
				ASN1Util.integerToString(pdu, subItemPDU.ofst, subItemPDU.len, sb);
				sb.append("\r\n");
			}
			if ((subItemPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "2")) != null && subItemPDU.itemType == ASN1Util.IT_SET)
			{
				appendPKCS7DigestAlgorithmIdentifiers(pdu, subItemPDU.ofst, subItemPDU.ofst + subItemPDU.len, sb, "signedData.digestAlgorithms");
			}
			if ((subItemPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "3")) != null && subItemPDU.itemType == ASN1Util.IT_SEQUENCE)
			{
				appendContentInfo(pdu, subItemPDU.pduBegin, subItemPDU.ofst + subItemPDU.len, "1", sb, "signedData.contentInfo", ContentDataType.Unknown);
			}
			i = 4;
			subItemPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "4");
			if (subItemPDU != null && subItemPDU.itemType == ASN1Util.IT_CONTEXT_SPECIFIC_0)
			{
				appendCertificate(pdu, subItemPDU.ofst, subItemPDU.ofst + subItemPDU.len, "1", sb, "signedData.certificates");
				i++;
				subItemPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, String.valueOf(i));
			}
			if (subItemPDU != null && subItemPDU.itemType == ASN1Util.IT_CONTEXT_SPECIFIC_1)
			{
				//AppendCertificate(subItemPDU, subItemPDU + subItemLen, "1", sb, CSTR("signedData.crls"));
				i++;
				subItemPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, String.valueOf(i));
			}
			if (subItemPDU != null && subItemPDU.itemType == ASN1Util.IT_SET)
			{
				appendPKCS7SignerInfos(pdu, subItemPDU.ofst, subItemPDU.ofst + subItemPDU.len, sb, "signedData.signerInfos");
			}
		}
	}

	protected static void appendPKCS7DigestAlgorithmIdentifiers(byte[] pdu, int beginOfst, int endOfst, StringBuilder sb, String varName)
	{
		int i;
		ASN1Item itemPDU;
		i = 1;
		while (true)
		{
			if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, String.valueOf(i))) == null)
			{
				return;
			}
			if (itemPDU.itemType == ASN1Util.IT_SEQUENCE)
			{
				appendAlgorithmIdentifier(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, sb, varName, false);
			}
			i++;
		}
	}

	protected static void appendPKCS7SignerInfos(byte[] pdu, int beginOfst, int endOfst, StringBuilder sb, String varName)
	{
		int i;
		ASN1Item itemPDU;
		i = 1;
		while (true)
		{
			itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, String.valueOf(i));
			if (itemPDU == null)
			{
				break;
			}
			if (itemPDU.itemType == ASN1Util.IT_SEQUENCE)
			{
				appendPKCS7SignerInfo(pdu, itemPDU.pduBegin, itemPDU.ofst + itemPDU.len, sb, varName);
			}
			i++;
		}
	}

	protected static void appendPKCS7SignerInfo(byte[] pdu, int beginOfst, int endOfst, StringBuilder sb, String varName)
	{
		int i;
		ASN1Item itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, "1");
		if (itemPDU == null || itemPDU.itemType != ASN1Util.IT_SEQUENCE)
		{
			return;
		}
		ASN1Item subItemPDU;
		if ((subItemPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "1")) != null && subItemPDU.itemType == ASN1Util.IT_INTEGER)
		{
			sb.append(varName);
			sb.append('.');
			sb.append("version = ");
			ASN1Util.integerToString(pdu, subItemPDU.ofst, subItemPDU.len, sb);
			sb.append("\r\n");
		}
		if ((subItemPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "2")) != null && subItemPDU.itemType == ASN1Util.IT_SEQUENCE)
		{
			appendIssuerAndSerialNumber(pdu, subItemPDU.ofst, subItemPDU.ofst + subItemPDU.len, sb, varName + ".issuerAndSerialNumber");
		}
		if ((subItemPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "3")) != null && subItemPDU.itemType == ASN1Util.IT_SEQUENCE)
		{
			appendAlgorithmIdentifier(pdu, subItemPDU.ofst, subItemPDU.ofst + subItemPDU.len, sb, varName + ".digestAlgorithm", false);
		}
		i = 4;
		if ((subItemPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "4")) != null && subItemPDU.itemType == ASN1Util.IT_CONTEXT_SPECIFIC_0)
		{
			appendPKCS7Attributes(pdu, subItemPDU.ofst, subItemPDU.ofst + subItemPDU.len, sb, varName + ".authenticatedAttributes");
			i++;
			subItemPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, String.valueOf(i));
		}
		if (subItemPDU != null && subItemPDU.itemType == ASN1Util.IT_SEQUENCE)
		{
			appendAlgorithmIdentifier(pdu, subItemPDU.ofst, subItemPDU.ofst + subItemPDU.len, sb, varName + ".digestEncryptionAlgorithm", false);
		}
		i++;
		if ((subItemPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, String.valueOf(i))) != null && subItemPDU.itemType == ASN1Util.IT_OCTET_STRING)
		{
			sb.append(varName);
			sb.append('.');
			sb.append("encryptedDigest = ");
			StringUtil.appendHex(sb, pdu, subItemPDU.ofst, subItemPDU.len, ':', LineBreakType.NONE);
			sb.append("\r\n");
		}
		i++;
		if ((subItemPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, String.valueOf(i))) != null && subItemPDU.itemType == ASN1Util.IT_CONTEXT_SPECIFIC_1)
		{
			appendPKCS7Attributes(pdu, subItemPDU.ofst, subItemPDU.ofst + subItemPDU.len, sb, varName + ".unauthenticatedAttributes");
		}
	}

	protected static void appendIssuerAndSerialNumber(byte[] pdu, int beginOfst, int endOfst, StringBuilder sb, String varName)
	{
		ASN1Item itemPDU;
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, "1")) != null && itemPDU.itemType == ASN1Util.IT_SEQUENCE)
		{
			appendName(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, sb, varName + ".issuer");
		}
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, "2")) != null && itemPDU.itemType == ASN1Util.IT_INTEGER)
		{
			sb.append(varName);
			sb.append('.');
			sb.append("serialNumber = ");
			StringUtil.appendHex(sb, pdu, itemPDU.ofst, itemPDU.len, ':', LineBreakType.NONE);
			sb.append("\r\n");
		}
	}

	protected static void appendPKCS7Attributes(byte[] pdu, int beginOfst, int endOfst, StringBuilder sb, String varName)
	{
		ASN1Item itemPDU;
		ASN1Item oidPDU;
		ASN1Item valuePDU;
		int i = 1;
		while (true)
		{
			if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, String.valueOf(i))) == null)
			{
				return;
			}
			if (itemPDU.itemType == ASN1Util.IT_SEQUENCE)
			{
				oidPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "1");
				valuePDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "2");
				if (oidPDU != null && oidPDU.itemType == ASN1Util.IT_OID)
				{
					sb.append(varName);
					sb.append('.');
					sb.append("attributeType = ");
					ASN1Util.oidToString(pdu, oidPDU.ofst, oidPDU.len, sb);
					OIDInfo oid = ASN1OIDDB.oidGetEntry(pdu, oidPDU.ofst, oidPDU.len);
					if (oid != null)
					{
						sb.append(" (");
						sb.append(oid.name);
						sb.append(')');
					}
					sb.append("\r\n");
				}
				if (valuePDU != null && valuePDU.itemType == ASN1Util.IT_SET)
				{
					if (ASN1Util.oidEqualsText(pdu, oidPDU.ofst, oidPDU.len, "1.2.840.113549.1.9.3")) //contentType
					{
						if ((itemPDU = ASN1Util.pduGetItem(pdu, valuePDU.ofst, valuePDU.ofst + valuePDU.len, "1")) != null && itemPDU.itemType == ASN1Util.IT_OID)
						{
							sb.append(varName);
							sb.append('.');
							sb.append("contentType = ");
							ASN1Util.oidToString(pdu, itemPDU.ofst, itemPDU.len, sb);
							OIDInfo oid = ASN1OIDDB.oidGetEntry(pdu, itemPDU.ofst, itemPDU.len);
							if (oid != null)
							{
								sb.append(" (");
								sb.append(oid.name);
								sb.append(')');
							}
							sb.append("\r\n");
						}
					}
					else if (ASN1Util.oidEqualsText(pdu, oidPDU.ofst, oidPDU.len, "1.2.840.113549.1.9.4")) //messageDigest
					{
						if ((itemPDU = ASN1Util.pduGetItem(pdu, valuePDU.ofst, valuePDU.ofst + valuePDU.len, "1")) != null && itemPDU.itemType == ASN1Util.IT_OCTET_STRING)
						{
							sb.append(varName);
							sb.append('.');
							sb.append("messageDigest = ");
							StringUtil.appendHex(sb, pdu, itemPDU.ofst, itemPDU.len, ':', LineBreakType.NONE);
							sb.append("\r\n");
						}
					}
					else if (ASN1Util.oidEqualsText(pdu, oidPDU.ofst, oidPDU.len, "1.2.840.113549.1.9.5")) //signing-time
					{
						if ((itemPDU = ASN1Util.pduGetItem(pdu, valuePDU.ofst, valuePDU.ofst + valuePDU.len, "1")) != null && itemPDU.itemType == ASN1Util.IT_UTCTIME)
						{
							sb.append(varName);
							sb.append('.');
							sb.append("signing-time = ");
							ASN1Util.utcTimeToString(pdu, itemPDU.ofst, itemPDU.len, sb);
							sb.append("\r\n");
						}
					}
					else if (ASN1Util.oidEqualsText(pdu, oidPDU.ofst, oidPDU.len, "1.2.840.113549.1.9.15")) //smimeCapabilities
					{
						/////////////////////////////////////
					}
					else if (ASN1Util.oidEqualsText(pdu, oidPDU.ofst, oidPDU.len, "1.2.840.113549.1.9.16.2.11")) //id-aa-encrypKeyPref
					{
						if ((itemPDU = ASN1Util.pduGetItem(pdu, valuePDU.ofst, valuePDU.ofst + valuePDU.len, "1")) != null && itemPDU.itemType == ASN1Util.IT_CONTEXT_SPECIFIC_0)
						{
							appendIssuerAndSerialNumber(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, sb, varName + ".encrypKeyPref");
						}
					}
					else if (ASN1Util.oidEqualsText(pdu, oidPDU.ofst, oidPDU.len, "1.3.6.1.4.1.311.16.4")) //outlookExpress
					{
						if ((itemPDU = ASN1Util.pduGetItem(pdu, valuePDU.ofst, valuePDU.ofst + valuePDU.len, "1")) != null && itemPDU.itemType == ASN1Util.IT_SEQUENCE)
						{
							appendIssuerAndSerialNumber(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, sb, varName + ".outlookExpress");
						}
					}
					
				}
			}
			i++;
		}
	}

	protected static boolean appendMacData(byte[] pdu, int beginOfst, int endOfst, String path, StringBuilder sb, String varName)
	{
		ASN1Item itemPDU;
		ASN1Item subItemPDU;
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, path)) != null && itemPDU.itemType == ASN1Util.IT_SEQUENCE)
		{
			if ((subItemPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "1")) != null && subItemPDU.itemType == ASN1Util.IT_SEQUENCE)
			{
				appendDigestInfo(pdu, subItemPDU.ofst, subItemPDU.ofst + subItemPDU.len, sb, varName + ".mac");
			}
			if ((subItemPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "2")) != null && subItemPDU.itemType == ASN1Util.IT_OCTET_STRING)
			{
				sb.append(varName);
				sb.append('.');
				sb.append("macSalt = ");
				StringUtil.appendHex(sb, pdu, subItemPDU.ofst, subItemPDU.len, ' ', LineBreakType.NONE);
				sb.append("\r\n");
			}
			if ((subItemPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "3")) != null && subItemPDU.itemType == ASN1Util.IT_INTEGER)
			{
				sb.append(varName);
				sb.append('.');
				sb.append("iterations = ");
				ASN1Util.integerToString(pdu, subItemPDU.ofst, subItemPDU.len, sb);
				sb.append("\r\n");
			}
			return true;
		}
		return false;
	}

	protected static void appendDigestInfo(byte[] pdu, int beginOfst, int endOfst, StringBuilder sb, String varName)
	{
		ASN1Item itemPDU;
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, "1")) != null && itemPDU.itemType == ASN1Util.IT_SEQUENCE)
		{
			appendAlgorithmIdentifier(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, sb, varName + ".digestAlgorithm", false);
		}
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, "2")) != null && itemPDU.itemType == ASN1Util.IT_OCTET_STRING)
		{
			sb.append(varName);
			sb.append(".digest = ");
			StringUtil.appendHex(sb, pdu, itemPDU.ofst, itemPDU.len, ' ', LineBreakType.NONE);
			sb.append("\r\n");
		}
	}

	protected static void appendData(byte[] pdu, int beginOfst, int endOfst, StringBuilder sb, String varName, ContentDataType dataType)
	{
		switch (dataType)
		{
		case AuthenticatedSafe:
			appendAuthenticatedSafe(pdu, beginOfst, endOfst, sb, varName);
			break;
		case Unknown:
		default:
			break;
		}
	}

	protected static void appendEncryptedData(byte[] pdu, int beginOfst, int endOfst, StringBuilder sb, String varName, ContentDataType dataType)
	{
		ASN1Item itemPDU;
		ASN1Item subitemPDU;
		String name;
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, "1")) != null && itemPDU.itemType == ASN1Util.IT_SEQUENCE)
		{
			if (varName != null)
			{
				sb.append(varName);
				sb.append('.');
			}
			sb.append("version = ");
			appendVersion(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "1", sb);
			sb.append("\r\n");

			if ((subitemPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "2")) != null && subitemPDU.itemType == ASN1Util.IT_SEQUENCE)
			{
				name = "encryptedContentInfo";
				if (varName != null)
				{
					name = varName + "." + name;
				}
				appendEncryptedContentInfo(pdu, subitemPDU.ofst, subitemPDU.ofst + subitemPDU.len, sb, name, dataType);
			}
			if ((subitemPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "3")) != null && subitemPDU.itemType == ASN1Util.IT_CONTEXT_SPECIFIC_0)
			{
				name = "unprotectedAttributes";
				if (varName != null)
				{
					name = varName + "." + name;
				}
				appendPKCS7Attributes(pdu, subitemPDU.ofst, subitemPDU.ofst + subitemPDU.len, sb, name);
			}
		}
	}

	protected static void appendAuthenticatedSafe(byte[] pdu, int beginOfst, int endOfst, StringBuilder sb, String varName)
	{
		ASN1Item itemPDU;
		int i;
		int j;
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, "1")) != null && itemPDU.itemType == ASN1Util.IT_SEQUENCE)
		{
			i = 0;
			j = ASN1Util.pduCountItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, null);
			while (i < j)
			{
				i++;
				appendContentInfo(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, String.valueOf(i), sb, varName + "[" + i + "]", ContentDataType.Unknown);
			}
		}
	}

	protected static void appendEncryptedContentInfo(byte[] pdu, int beginOfst, int endOfst, StringBuilder sb, String varName, ContentDataType dataType)
	{
		String name;
		ASN1Item itemPDU;
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, "1")) != null && itemPDU.itemType == ASN1Util.IT_OID)
		{
			sb.append(varName);
			sb.append(".contentType = ");
			ASN1Util.oidToString(pdu, itemPDU.ofst, itemPDU.len, sb);
			OIDInfo oid = ASN1OIDDB.oidGetEntry(pdu, itemPDU.ofst, itemPDU.len);
			if (oid != null)
			{
				sb.append(" (");
				sb.append(oid.name);
				sb.append(')');
			}
			sb.append("\r\n");
		}
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, "2")) != null && itemPDU.itemType == ASN1Util.IT_SEQUENCE)
		{
			name = "contentEncryptionAlgorithm";
			if (varName != null)
			{
				name = varName + "." + name;
			}
			appendAlgorithmIdentifier(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, sb, name, false);
		}
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, "3")) != null && (itemPDU.itemType & 0x8F) == 0x80)
		{
			sb.append(varName);
			sb.append(".encryptedContent = ");
			StringUtil.appendHex(sb, pdu, itemPDU.ofst, itemPDU.len, ' ', LineBreakType.NONE);
			sb.append("\r\n");
		}
	}

	protected String nameGetByOID(byte[] pdu, int beginOfst, int endOfst, String oidText)
	{
		ASN1Item itemPDU;
		ASN1Item oidPDU;
		ASN1Item strPDU;
		int cnt = ASN1Util.pduCountItem(pdu, beginOfst, endOfst, null);
		int i = 0;
		while (i < cnt)
		{
			i++;
	
			if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, i + ".1")) != null)
			{
				if (itemPDU.itemType == ASN1Util.IT_SEQUENCE)
				{
					oidPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "1");
					if (oidPDU != null && oidPDU.itemType == ASN1Util.IT_OID && ASN1Util.oidEqualsText(pdu, oidPDU.ofst, oidPDU.len, oidText))
					{
						strPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "2");
						if (strPDU != null)
						{
							return new String(pdu, strPDU.ofst, strPDU.len, StandardCharsets.UTF_8);
						}
					}
				}
			}
		}
		return null;
	}
	
	protected String nameGetCN(byte[] pdu, int beginOfst, int endOfst)
	{
		return nameGetByOID(pdu, beginOfst, endOfst, "2.5.4.3");
	}
	
	protected static KeyType keyTypeFromOID(byte[] oid, int ofst, int oidLen, boolean pubKey)
	{
		if (ASN1Util.oidEqualsText(oid, ofst, oidLen, "1.2.840.113549.1.1.1"))
		{
			if (pubKey)
			{
				return KeyType.RSAPublic;
			}
			else
			{
				return KeyType.RSA;
			}
		}
		return KeyType.Unknown;
	}
}
