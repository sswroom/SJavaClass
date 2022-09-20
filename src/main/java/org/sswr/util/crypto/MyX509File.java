package org.sswr.util.crypto;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.sswr.util.data.LineBreakType;
import org.sswr.util.data.StringUtil;
import org.sswr.util.net.ASN1Data;
import org.sswr.util.net.ASN1Item;
import org.sswr.util.net.ASN1OIDDB;
import org.sswr.util.net.ASN1OIDInfo;
import org.sswr.util.net.ASN1Type;
import org.sswr.util.net.ASN1Util;
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
		CRL,
		FileList
	}

	public enum KeyType
	{
		Unknown,
		RSA,
		DSA,
		ECDSA,
		ED25519,
		RSAPublic,
		ECPublic
	}
	
	public enum ValidStatus
	{
		Valid,
		SelfSigned,
		SignatureInvalid,
		Revoked,
		FileFormatInvalid,
		UnknownIssuer,
		Expired,
		UnsupportedAlgorithm,
		CRLNotFound
	}

	public enum AlgType
	{
		Unknown,
		MD2WithRSAEncryption,
		MD5WithRSAEncryption,
		SHA1WithRSAEncryption,
		SHA256WithRSAEncryption,
		SHA384WithRSAEncryption,
		SHA512WithRSAEncryption,
		SHA224WithRSAEncryption,
		ECDSAWithSHA256,
		ECDSAWithSHA384
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

	protected static boolean isTBSCertList(byte[] pdu, int beginOfst, int endOfst, String path)
	{
		int cnt = ASN1Util.pduCountItem(pdu, beginOfst, endOfst, path);
		if (cnt < 4)
		{
			return false;
		}
		int i = 1;
		if (ASN1Util.pduGetItemType(pdu, beginOfst, endOfst, path+"."+i) == ASN1Util.IT_INTEGER)
		{
			i++;
		}
		if (ASN1Util.pduGetItemType(pdu, beginOfst, endOfst, path+"."+i) != ASN1Util.IT_SEQUENCE)
		{
			return false;
		}
		i++;
		if (ASN1Util.pduGetItemType(pdu, beginOfst, endOfst, path+"."+i) != ASN1Util.IT_SEQUENCE)
		{
			return false;
		}
		i++;
		if (ASN1Util.pduGetItemType(pdu, beginOfst, endOfst, path+"."+i) != ASN1Util.IT_UTCTIME)
		{
			return false;
		}
		i++;
		if (ASN1Util.pduGetItemType(pdu, beginOfst, endOfst, path+"."+i) == ASN1Util.IT_UTCTIME)
		{
			i++;
		}
		if (ASN1Util.pduGetItemType(pdu, beginOfst, endOfst, path+"."+i) != ASN1Util.IT_SEQUENCE)
		{
			return false;
		}
		i++;
		int itemType = ASN1Util.pduGetItemType(pdu, beginOfst, endOfst, path+"."+i);
		if (itemType != ASN1Util.IT_CONTEXT_SPECIFIC_0 && itemType != ASN1Util.IT_UNKNOWN)
		{
			return false;
		}
		return true;
	}

	protected static void appendTBSCertList(byte[] pdu, int beginOfst, int endOfst, String path, StringBuilder sb, String varName)
	{
		ZonedDateTime dt;
		String name;
		int i = 1;
		ASN1Item itemPDU;
		ASN1Item subitemPDU;
		ASN1Item subsubitemPDU;
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, path + "." + i)) != null)
		{
			if (itemPDU.itemType == ASN1Util.IT_INTEGER)
			{
				if (varName != null)
				{
					sb.append(varName);
					sb.append('.');
				}
				sb.append("version = ");
				appendVersion(pdu, beginOfst, endOfst, path + "." + i, sb);
				sb.append("\r\n");
				i++;
			}
		}
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, path + "." + i)) != null)
		{
			if (itemPDU.itemType == ASN1Util.IT_SEQUENCE)
			{
				name = "signature";
				if (varName != null)
				{
					name = varName + "." + name;
				}
				appendAlgorithmIdentifier(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, sb, name, false);
			}
		}
		i++;
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, path + "." + i)) != null)
		{
			if (itemPDU.itemType == ASN1Util.IT_SEQUENCE)
			{
				name = "issuer";
				if (varName != null)
				{
					name = varName + "." + name;
				}
				appendName(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, sb, name);
			}
		}
		i++;
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, path + "." + i)) != null)
		{
			if (itemPDU.itemType == ASN1Util.IT_UTCTIME && (dt = ASN1Util.pduParseUTCTimeCont(pdu, itemPDU.ofst, itemPDU.len)) != null)
			{
				if (varName != null)
				{
					sb.append(varName);
					sb.append('.');
				}
				sb.append("thisUpdate = ");
				sb.append(dt.toString());
				sb.append("\r\n");
			}
		}
		i++;
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, path + "." + i)) != null && itemPDU.itemType == ASN1Util.IT_UTCTIME)
		{
			if ((dt = ASN1Util.pduParseUTCTimeCont(pdu, itemPDU.ofst, itemPDU.len)) != null)
			{
				if (varName != null)
				{
					sb.append(varName);
					sb.append('.');
				}
				sb.append("nextUpdate = ");
				sb.append(dt.toString());
				sb.append("\r\n");
			}
			i++;
		}
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, path + "." + i)) != null && itemPDU.itemType == ASN1Util.IT_SEQUENCE)
		{
			int j = 1;
			while (true)
			{
				if ((subitemPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, String.valueOf(j))) == null || subitemPDU.itemType != ASN1Util.IT_SEQUENCE)
				{
					break;
				}

				if ((subsubitemPDU = ASN1Util.pduGetItem(pdu, subitemPDU.ofst, subitemPDU.ofst + subitemPDU.len, "1")) != null && subsubitemPDU.itemType == ASN1Util.IT_INTEGER)
				{
					if (varName != null)
					{
						sb.append(varName);
						sb.append('.');
					}
					sb.append("revokedCertificates[");
					sb.append(j);
					sb.append("].userCertificate = ");
					StringUtil.appendHex(sb, pdu, subsubitemPDU.ofst, subsubitemPDU.len, ':', LineBreakType.NONE);
					sb.append("\r\n");
				}
				if ((subsubitemPDU = ASN1Util.pduGetItem(pdu, subitemPDU.ofst, subitemPDU.ofst + subitemPDU.len, "2")) != null && subsubitemPDU.itemType == ASN1Util.IT_UTCTIME && (dt = ASN1Util.pduParseUTCTimeCont(pdu, subsubitemPDU.ofst, subsubitemPDU.len)) != null)
				{
					if (varName != null)
					{
						sb.append(varName);
						sb.append('.');
					}
					sb.append("revokedCertificates[");
					sb.append(j);
					sb.append("].revocationDate = ");
					sb.append(dt.toString());
					sb.append("\r\n");
				}
				if ((subsubitemPDU = ASN1Util.pduGetItem(pdu, subitemPDU.ofst, subitemPDU.ofst + subitemPDU.len, "3")) != null && subsubitemPDU.itemType == ASN1Util.IT_SEQUENCE)
				{
					name = "revokedCertificates[" + j + "].crlEntryExtensions";
					if (varName != null)
					{
						name = varName + "." + name;
					}
					appendCRLExtensions(pdu, subsubitemPDU.pduBegin, subsubitemPDU.ofst + subsubitemPDU.len, sb, name);
				}
				j++;
			}
		}
		i++;
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, path + "." + i)) != null)
		{
			if (itemPDU.itemType == ASN1Util.IT_CONTEXT_SPECIFIC_0)
			{
				name = "crlExtensions";
				if (varName != null)
				{
					name = varName + "." + name;
				}
				appendCRLExtensions(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, sb, name);
			}
		}
	}

	protected static boolean isCertificateList(byte[] pdu, int beginOfst, int endOfst, String path)
	{
		return isSigned(pdu, beginOfst, endOfst, path) && isTBSCertList(pdu, beginOfst, endOfst, path + ".1");
	}

	protected static void appendCertificateList(byte[] pdu, int beginOfst, int endOfst, String path, StringBuilder sb, String varName)
	{
		appendTBSCertList(pdu, beginOfst, endOfst, path + ".1", sb, varName);
		appendSigned(pdu, beginOfst, endOfst, path, sb, varName);
	}

	protected static boolean isPrivateKeyInfo(byte[] pdu, int beginOfst, int endOfst, String path)
	{
		int cnt = ASN1Util.pduCountItem(pdu, beginOfst, endOfst, path);
		if (cnt != 3 && cnt != 4)
		{
			return false;
		}
		if (ASN1Util.pduGetItemType(pdu, beginOfst, endOfst, path + ".1") != ASN1Util.IT_INTEGER)
		{
			return false;
		}
		if (ASN1Util.pduGetItemType(pdu, beginOfst, endOfst, path + ".2") != ASN1Util.IT_SEQUENCE)
		{
			return false;
		}
		if (ASN1Util.pduGetItemType(pdu, beginOfst, endOfst, path + ".3") != ASN1Util.IT_OCTET_STRING)
		{
			return false;
		}
		if (cnt == 4)
		{
			if (ASN1Util.pduGetItemType(pdu, beginOfst, endOfst, path + ".4") != ASN1Util.IT_SET)
			{
				return false;
			}
		}
		return true;
	}

	protected static void appendPrivateKeyInfo(byte[] pdu, int beginOfst, int endOfst, String path, StringBuilder sb)
	{
		ASN1Item itemPDU;
		KeyType keyType = KeyType.Unknown;
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, path + ".1")) != null)
		{
			if (itemPDU.itemType == ASN1Util.IT_INTEGER)
			{
				sb.append("version = ");
				appendVersion(pdu, beginOfst, endOfst, path + ".1", sb);
				sb.append("\r\n");
			}
		}
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, path + ".2")) != null)
		{
			if (itemPDU.itemType == ASN1Util.IT_SEQUENCE)
			{
				keyType = appendAlgorithmIdentifier(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, sb, "privateKeyAlgorithm", false);
			}
		}
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, path + ".3")) != null)
		{
			if (itemPDU.itemType == ASN1Util.IT_OCTET_STRING)
			{
				sb.append("privateKey = ");
				sb.append("\r\n");
				if (keyType != KeyType.Unknown)
				{
					MyX509Key privkey = new MyX509Key("PrivKey", pdu, itemPDU.ofst, itemPDU.len, keyType);
					sb.append(privkey.toString());
				}
			}
		}
	}

	protected static boolean isCertificateRequestInfo(byte[] pdu, int beginOfst, int endOfst, String path)
	{
		int cnt = ASN1Util.pduCountItem(pdu, beginOfst, endOfst, path);
		if (cnt < 4)
		{
			return false;
		}
		int i = 1;
		if (ASN1Util.pduGetItemType(pdu, beginOfst, endOfst, path + "." + i) != ASN1Util.IT_INTEGER)
		{
			return false;
		}
		i++;
		if (ASN1Util.pduGetItemType(pdu, beginOfst, endOfst, path + "." + i) != ASN1Util.IT_SEQUENCE)
		{
			return false;
		}
		i++;
		if (ASN1Util.pduGetItemType(pdu, beginOfst, endOfst, path + "." + i) != ASN1Util.IT_SEQUENCE)
		{
			return false;
		}
		i++;
		if (ASN1Util.pduGetItemType(pdu, beginOfst, endOfst, path + "." + i) != ASN1Util.IT_CONTEXT_SPECIFIC_0)
		{
			return false;
		}
		return true;
	}

	protected static void appendCertificateRequestInfo(byte[] pdu, int beginOfst, int endOfst, String path, StringBuilder sb)
	{
		int i = 1;
		ASN1Item itemPDU;
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, path + "." + i)) != null)
		{
			if (itemPDU.itemType == ASN1Util.IT_INTEGER)
			{
				sb.append("serialNumber = ");
				appendVersion(pdu, beginOfst, endOfst, path + "." + i, sb);
				sb.append("\r\n");
			}
		}
		i++;
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, path + "." + i)) != null)
		{
			if (itemPDU.itemType == ASN1Util.IT_SEQUENCE)
			{
				appendName(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, sb, "subject");
			}
		}
		i++;
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, path + "." + i)) != null)
		{
			if (itemPDU.itemType == ASN1Util.IT_SEQUENCE)
			{
				appendSubjectPublicKeyInfo(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, sb, "subjectPublicKeyInfo");
				MyX509PubKey pubKey;
				MyX509Key key;
				pubKey = new MyX509PubKey("PubKey", pdu, itemPDU.pduBegin, itemPDU.len + itemPDU.ofst - itemPDU.pduBegin);
				key = pubKey.createKey();
				if (key != null)
				{
					sb.append(key.toString());
					sb.append("\r\n");
				}
			}
		}
		i++;
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, path + "." + i)) != null && itemPDU.itemType == ASN1Util.IT_CONTEXT_SPECIFIC_0)
		{
			if ((itemPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "1")) != null && itemPDU.itemType == ASN1Util.IT_SEQUENCE)
			{
				ASN1Item extOID;
				ASN1Item ext;
				if ((extOID = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "1")) != null && extOID.itemType == ASN1Util.IT_OID &&
					(ext = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "2")) != null && ext.itemType == ASN1Util.IT_SET)
				{
					if (ASN1Util.oidEqualsText(pdu, extOID.ofst, extOID.len, "1.2.840.113549.1.9.14"))
					{
						appendCRLExtensions(pdu, ext.ofst, ext.ofst + ext.len, sb, "extensionRequest");
					}
				}
			}
		}
	}

	protected static boolean isCertificateRequest(byte[] pdu, int beginOfst, int endOfst, String path)
	{
		return isSigned(pdu, beginOfst, endOfst, path) && isCertificateRequestInfo(pdu, beginOfst, endOfst, path + ".1");
	}

	protected static void appendCertificateRequest(byte[] pdu, int beginOfst, int endOfst, String path, StringBuilder sb)
	{
		appendCertificateRequestInfo(pdu, beginOfst, endOfst, path + ".1", sb);
		appendSigned(pdu, beginOfst, endOfst, path, sb, null);
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
				ASN1OIDInfo oid = ASN1OIDDB.oidGetEntry(pdu, contentType.ofst, contentType.len);
				if (oid != null)
				{
					sb.append(" (");
					sb.append(oid.getName());
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
			ASN1OIDInfo oid = ASN1OIDDB.oidGetEntry(pdu, algorithm.ofst, algorithm.len);
			if (oid != null)
			{
				sb.append(" (");
				sb.append(oid.getName());
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
				ASN1OIDInfo oid = ASN1OIDDB.oidGetEntry(pdu, extension.ofst, extension.len);
				if (oid != null)
				{
					sb.append(" (");
					sb.append(oid.getName());
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
/*					Net.SocketUtil.AddressInfo addr;
					Net.SocketUtil.SetAddrInfoV6(&addr, subItemPDU, 0);
					sptr = Net.SocketUtil.GetAddrName(sbuff, &addr);
					sb.appendP(sbuff, sptr);*/
				}
				sb.append("\r\n");
				return true;
			case 0x88:
				sb.append(varName);
				sb.append(".registeredID = ");
				ASN1Util.oidToString(pdu, subItemPDU.ofst, subItemPDU.len, sb);
				{
					ASN1OIDInfo ent = ASN1OIDDB.oidGetEntry(pdu, subItemPDU.ofst, subItemPDU.len);
					if (ent != null)
					{
						sb.append(" (");
						sb.append(ent.getName());
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
				//AppendCertificate(subItemPDU, subItemPDU + subItemLen, "1", sb, "signedData.crls"));
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
					ASN1OIDInfo oid = ASN1OIDDB.oidGetEntry(pdu, oidPDU.ofst, oidPDU.len);
					if (oid != null)
					{
						sb.append(" (");
						sb.append(oid.getName());
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
							ASN1OIDInfo oid = ASN1OIDDB.oidGetEntry(pdu, itemPDU.ofst, itemPDU.len);
							if (oid != null)
							{
								sb.append(" (");
								sb.append(oid.getName());
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
			ASN1OIDInfo oid = ASN1OIDDB.oidGetEntry(pdu, itemPDU.ofst, itemPDU.len);
			if (oid != null)
			{
				sb.append(" (");
				sb.append(oid.getName());
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

	protected static String nameGetByOID(byte[] pdu, int beginOfst, int endOfst, String oidText)
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
	
	protected static String nameGetCN(byte[] pdu, int beginOfst, int endOfst)
	{
		return nameGetByOID(pdu, beginOfst, endOfst, "2.5.4.3");
	}
	
	protected static CertNames namesGet(byte[] pdu, int beginOfst, int endOfst)
	{
		String path;
		ASN1Item itemPDU;
		ASN1Item oidPDU;
		ASN1Item strPDU;
		int cnt = ASN1Util.pduCountItem(pdu, beginOfst, endOfst, null);
		int i = 0;
		CertNames names = new CertNames();
		while (i < cnt)
		{
			i++;
	
			path = i + ".1";
			if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, path)) != null)
			{
				if (itemPDU.itemType == ASN1Util.IT_SEQUENCE)
				{
					oidPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "1");
					if (oidPDU != null && oidPDU.itemType == ASN1Util.IT_OID)
					{
						strPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "2");
						if (strPDU != null)
						{
							if (ASN1Util.oidEqualsText(pdu, oidPDU.ofst, oidPDU.len, "2.5.4.6"))
							{
								names.countryName = new String(pdu, strPDU.ofst, strPDU.len);
							}
							else if (ASN1Util.oidEqualsText(pdu, oidPDU.ofst, oidPDU.len, "2.5.4.8"))
							{
								names.stateOrProvinceName = new String(pdu, strPDU.ofst, strPDU.len);
							}
							else if (ASN1Util.oidEqualsText(pdu, oidPDU.ofst, oidPDU.len, "2.5.4.7"))
							{
								names.localityName = new String(pdu, strPDU.ofst, strPDU.len);
							}
							else if (ASN1Util.oidEqualsText(pdu, oidPDU.ofst, oidPDU.len, "2.5.4.10"))
							{
								names.organizationName = new String(pdu, strPDU.ofst, strPDU.len);
							}
							else if (ASN1Util.oidEqualsText(pdu, oidPDU.ofst, oidPDU.len, "2.5.4.11"))
							{
								names.organizationUnitName = new String(pdu, strPDU.ofst, strPDU.len);
							}
							else if (ASN1Util.oidEqualsText(pdu, oidPDU.ofst, oidPDU.len, "2.5.4.3"))
							{
								names.commonName = new String(pdu, strPDU.ofst, strPDU.len);
							}
							else if (ASN1Util.oidEqualsText(pdu, oidPDU.ofst, oidPDU.len, "1.2.840.113549.1.9.1"))
							{
								names.emailAddress = new String(pdu, strPDU.ofst, strPDU.len);
							}
						}
					}
				}
			}
		}
		if (names != null && names.commonName != null)
		{
			return names;
		}
		else
		{
			return null;
		}
	}

	protected static List<String> extensionsGetCRLDistributionPoints(byte[] pdu, int beginOfst, int endOfst)
	{
		String path;
		ASN1Item itemPDU;
		ASN1Item oidPDU;
		ASN1Item strPDU;
		ASN1Item subItemPDU;
		List<String> crlDistributionPoints = new ArrayList<String>();
		int cnt = ASN1Util.pduCountItem(pdu, beginOfst, endOfst, null);
		int i = 0;
		while (i < cnt)
		{
			i++;
	
			path = String.valueOf(i);
			if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, path)) != null)
			{
				if (itemPDU.itemType == ASN1Util.IT_SEQUENCE)
				{
					oidPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "1");
					if (oidPDU != null && oidPDU.itemType == ASN1Util.IT_OID)
					{
						strPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "2");
						if (strPDU != null && strPDU.itemType == ASN1Util.IT_BOOLEAN)
						{
							strPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "3");
						}
						if (strPDU != null && strPDU.itemType == ASN1Util.IT_OCTET_STRING)
						{
							if (ASN1Util.oidEqualsText(pdu, oidPDU.ofst, oidPDU.len,"2.5.29.31")) //id-ce-cRLDistributionPoints
							{
								int j = 0;
								int k = ASN1Util.pduCountItem(pdu, strPDU.ofst, strPDU.ofst + strPDU.len, "1");
								while (j < k)
								{
									j++;
									path = "1."+j;
									subItemPDU = ASN1Util.pduGetItem(pdu, strPDU.ofst, strPDU.ofst + strPDU.len, path);
									if (subItemPDU != null && subItemPDU.itemType == ASN1Util.IT_SEQUENCE)
									{
										distributionPointAdd(pdu, subItemPDU.ofst, subItemPDU.ofst + subItemPDU.len, crlDistributionPoints);
									}
								}
							}
						}
					}
				}
			}
		}
		return crlDistributionPoints;
	}

	protected static int distributionPointAdd(byte[] pdu, int beginOfst, int endOfst, List<String> crlDistributionPoints)
	{
		ASN1Item itemPDU;
		if ((itemPDU = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, "1")) != null && itemPDU.itemType == ASN1Util.IT_CONTEXT_SPECIFIC_0)
		{
			if ((itemPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "1")) != null && itemPDU.itemType == ASN1Util.IT_CONTEXT_SPECIFIC_0)
			{
				if ((itemPDU = ASN1Util.pduGetItem(pdu, itemPDU.ofst, itemPDU.ofst + itemPDU.len, "1")) != null && itemPDU.itemType == ASN1Util.IT_CHOICE_6)
				{
					crlDistributionPoints.add(new String(pdu, itemPDU.ofst, itemPDU.len));
					return 1;
				}
			}
		}
		return 0;
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

	protected static ECName ecNameFromOID(byte[] oid, int ofst, int oidLen)
	{
		if (ASN1Util.oidEqualsText(oid, ofst, oidLen, "1.2.840.10045.3.1.7"))
		{
			return ECName.secp256r1;
		}
		else if (ASN1Util.oidEqualsText(oid, ofst, oidLen, "1.3.132.0.34"))
		{
			return ECName.secp384r1;
		}
		else if (ASN1Util.oidEqualsText(oid, ofst, oidLen, "1.3.132.0.35"))
		{
			return ECName.secp521r1;
		}
		return ECName.Unknown;
	}

	protected static AlgType algorithmIdentifierGet(byte[] pdu, int beginOfst, int endOfst)
	{
		int cnt = ASN1Util.pduCountItem(pdu, beginOfst, endOfst, null);
		if (cnt != 2 && cnt != 1)
		{
			return AlgType.Unknown;
		}
		ASN1Item item = ASN1Util.pduGetItem(pdu, beginOfst, endOfst, "1");
		if (item == null || item.itemType != ASN1Util.IT_OID)
		{
			return AlgType.Unknown;
		}
		if (ASN1Util.oidEqualsText(pdu, item.ofst, item.len,"1.2.840.113549.1.1.2")) //md2WithRSAEncryption
		{
			return AlgType.MD2WithRSAEncryption;
		}
		else if (ASN1Util.oidEqualsText(pdu, item.ofst, item.len,"1.2.840.113549.1.1.4")) //md5WithRSAEncryption
		{
			return AlgType.MD5WithRSAEncryption;
		}
		else if (ASN1Util.oidEqualsText(pdu, item.ofst, item.len,"1.2.840.113549.1.1.5")) //sha1WithRSAEncryption
		{
			return AlgType.SHA1WithRSAEncryption;
		}
		else if (ASN1Util.oidEqualsText(pdu, item.ofst, item.len,"1.2.840.113549.1.1.11")) //sha256WithRSAEncryption
		{
			return AlgType.SHA256WithRSAEncryption;
		}
		else if (ASN1Util.oidEqualsText(pdu, item.ofst, item.len,"1.2.840.113549.1.1.12")) //sha384WithRSAEncryption
		{
			return AlgType.SHA384WithRSAEncryption;
		}
		else if (ASN1Util.oidEqualsText(pdu, item.ofst, item.len,"1.2.840.113549.1.1.13")) //sha512WithRSAEncryption
		{
			return AlgType.SHA512WithRSAEncryption;
		}
		else if (ASN1Util.oidEqualsText(pdu, item.ofst, item.len,"1.2.840.113549.1.1.14")) //sha224WithRSAEncryption
		{
			return AlgType.SHA224WithRSAEncryption;
		}
		else if (ASN1Util.oidEqualsText(pdu, item.ofst, item.len,"1.2.840.10045.4.3.2")) //ecdsa-with-SHA256
		{
			return AlgType.ECDSAWithSHA256;
		}
		else if (ASN1Util.oidEqualsText(pdu, item.ofst, item.len,"1.2.840.10045.4.3.3")) //ecdsa-with-SHA384
		{
			return AlgType.ECDSAWithSHA384;
		}
		else
		{
			return AlgType.Unknown;
		}
	}

	public SignedInfo getSignedInfo()
	{
		SignedInfo signedInfo = new SignedInfo();
		ASN1Item item;
		item = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.1");
		if (item == null)
		{
			System.out.println("SignedInfo: error 1");
			return null;
		}
		signedInfo.payload = this.buff;
		signedInfo.payloadOfst = item.pduBegin;
		signedInfo.payloadSize = item.ofst + item.len - item.pduBegin;
		item = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.2");
		if (item == null)
		{
			System.out.println("SignedInfo: error 2a");
			return null;
		}
		else if (item.itemType != ASN1Util.IT_SEQUENCE)
		{
			System.out.println("SignedInfo: error 2b");
			return null;
		}
		else if ((signedInfo.algType = algorithmIdentifierGet(this.buff, item.ofst, item.ofst + item.len)) == AlgType.Unknown)
		{
			System.out.println("SignedInfo: error 2c");
			return null;
		}
		if ((item = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.3")) == null || item.itemType != ASN1Util.IT_BIT_STRING)
		{
			System.out.println("SignedInfo: error 3");
			return null;
		}
		signedInfo.signature = this.buff;
		signedInfo.signOfst = item.ofst + 1;
		signedInfo.signSize = item.len - 1;
		return signedInfo;
	
	}

	public static HashType getAlgHash(AlgType algType)
	{
		switch (algType)
		{
		case SHA1WithRSAEncryption:
			return HashType.SHA1;
		case SHA256WithRSAEncryption:
			return HashType.SHA256;
		case SHA512WithRSAEncryption:
			return HashType.SHA512;
		case SHA384WithRSAEncryption:
			return HashType.SHA384;
		case SHA224WithRSAEncryption:
			return HashType.SHA224;
		case MD2WithRSAEncryption:
			return HashType.Unknown;
		case MD5WithRSAEncryption:
			return HashType.MD5;
		case ECDSAWithSHA256:
			return HashType.SHA256;
		case ECDSAWithSHA384:
			return HashType.SHA384;
		case Unknown:
		default:
			return HashType.Unknown;
		}
	}

	public static String getAlgName(AlgType algType)
	{
		switch (algType)
		{
		case SHA1WithRSAEncryption:
			return "SHA1withRSA";
		case SHA256WithRSAEncryption:
			return "SHA256withRSA";
		case SHA512WithRSAEncryption:
			return "SHA512withRSA";
		case SHA384WithRSAEncryption:
			return "SHA384withRSA";
		case SHA224WithRSAEncryption:
			return "SHA224withRSA";
		case MD2WithRSAEncryption:
			return "MD2withRSA";
		case MD5WithRSAEncryption:
			return "MD5withRSA";
		case ECDSAWithSHA256:
			return "ECDSAwithSHA256";
		case ECDSAWithSHA384:
			return "ECDSAWithSHA384";
		case Unknown:
		default:
			return null;
		}
	}
	public static String keyTypeGetName(KeyType keyType)
	{
		switch (keyType)
		{
		case RSA:
			return "RSA";
		case DSA:
			return "DSA";
		case ECDSA:
			return "ECDSA";
		case ED25519:
			return "ED25519";
		case RSAPublic:
			return "RSAPublic";
		case ECPublic:
			return "ECPublic";
		case Unknown:
		default:
			return "Unknown";
		}
	}

	public static String keyTypeGetOID(KeyType keyType)
	{
		switch (keyType)
		{
		case RSA:
			return "1.2.840.113549.1.1.1";
		case DSA:
			return "1.2.840.10040.4.1";
		case ECDSA:
			return "1.2.840.10045.2.1";
		case ED25519:
			return "1.3.101.112";
		case ECPublic:
			return "1.2.840.10045.2.1";
		case RSAPublic:
		case Unknown:
		default:
			return "1.2.840.113549.1.1.1";
		}
	}
	public static String ecNameGetName(ECName ecName)
	{
		switch (ecName)
		{
		case secp256r1:
			return "secp256r1";
		case secp384r1:
			return "secp384r1";
		case secp521r1:
			return "secp521r1";
		case Unknown:
		default:
			return "Unknown";
		}
	}

	public static String ecNameGetOID(ECName ecName)
	{
		switch (ecName)
		{
		case secp256r1:
			return "1.2.840.10045.3.1.7";
		case secp384r1:
			return "1.3.132.0.34";
		case secp521r1:
			return "1.3.132.0.35";
		case Unknown:
		default:
			return "1.3.132.0.34";
		}
	}

	public static boolean verifySignedInfo(SignedInfo signedInfo, PublicKey key)
	{
		try
		{
			Signature sign = Signature.getInstance(getAlgName(signedInfo.algType));
			sign.initVerify(key);
			sign.update(signedInfo.payload, signedInfo.payloadOfst, signedInfo.payloadSize);
			return sign.verify(signedInfo.signature, signedInfo.signOfst, signedInfo.signSize);
		}
		catch (NoSuchAlgorithmException ex)
		{
			ex.printStackTrace();
			return false;
		}
		catch (InvalidKeyException ex)
		{
			ex.printStackTrace();
			return false;
		}
		catch (SignatureException ex)
		{
			ex.printStackTrace();
			return false;
		}
	}

	public static HashType signAlgGetHashType(String signAlg)
	{
		switch (signAlg)
		{
		case "SHA1withRSA":
			return HashType.SHA1;
		case "SHA256withRSA":
			return HashType.SHA256;
		case "SHA512withRSA":
			return HashType.SHA512;
		case "SHA384withRSA":
			return HashType.SHA384;
		case "SHA224withRSA":
			return HashType.SHA224;
		case "MD5withRSA":
			return HashType.MD5;
		case "ECDSAwithSHA256":
			return HashType.SHA256;
		case "ECDSAWithSHA384":
			return HashType.SHA384;
		case "MD2withRSA":
		default:
			return HashType.Unknown;
		}
	}

	public static HashType hashTypeFromOID(byte[] oid, int oidOfst, int oidLen)
	{
		if (ASN1Util.oidEqualsText(oid, oidOfst, oidLen, "2.16.840.1.101.3.4.2.1"))
		{
			return HashType.SHA256;
		}
		else if (ASN1Util.oidEqualsText(oid, oidOfst, oidLen, "2.16.840.1.101.3.4.2.2"))
		{
			return HashType.SHA384;
		}
		else if (ASN1Util.oidEqualsText(oid, oidOfst, oidLen, "2.16.840.1.101.3.4.2.3"))
		{
			return HashType.SHA512;
		}
		else if (ASN1Util.oidEqualsText(oid, oidOfst, oidLen, "2.16.840.1.101.3.4.2.4"))
		{
			return HashType.SHA224;
		}
		else if (ASN1Util.oidEqualsText(oid, oidOfst, oidLen, "1.3.14.3.2.26"))
		{
			return HashType.SHA1;
		}
		return HashType.Unknown;
	}
}
