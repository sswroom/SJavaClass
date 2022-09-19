package org.sswr.util.crypto;

import java.security.PublicKey;
import java.security.cert.Certificate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.sswr.util.data.DateTimeUtil;
import org.sswr.util.net.ASN1Data;
import org.sswr.util.net.ASN1Item;
import org.sswr.util.net.ASN1Util;

public class MyX509Cert extends MyX509File
{
	public MyX509Cert(String sourceName, byte[] buff, int ofst, int size)
	{
		super(sourceName, buff, ofst, size);
	}

	public String getSubjectCN()
	{
		ASN1Item tmpBuff;
		if (ASN1Util.pduGetItemType(this.buff, 0, this.buff.length, "1.1.1") == ASN1Util.IT_CONTEXT_SPECIFIC_0)
		{
			tmpBuff = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.1.6");
		}
		else
		{
			tmpBuff = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.1.5");
		}
		if (tmpBuff != null && tmpBuff.itemType == ASN1Util.IT_SEQUENCE)
		{
			return nameGetCN(this.buff, tmpBuff.ofst, tmpBuff.ofst + tmpBuff.len);
		}
		else
		{
			return null;
		}
	}

	public String getIssuerCN()
	{
		ASN1Item tmpBuff;
		if (ASN1Util.pduGetItemType(this.buff, 0, this.buff.length, "1.1.1") == ASN1Util.IT_CONTEXT_SPECIFIC_0)
		{
			tmpBuff = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.1.4");
		}
		else
		{
			tmpBuff = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.1.3");
		}
		if (tmpBuff != null && tmpBuff.itemType == ASN1Util.IT_SEQUENCE)
		{
			return nameGetCN(this.buff, tmpBuff.ofst, tmpBuff.ofst + tmpBuff.len);
		}
		else
		{
			return null;
		}
	}

	@Override
	public FileType getFileType()
	{
		return FileType.Cert;
	}

	public ValidStatus isValid(MyX509Env ssl, Map<String, Certificate> trustStoreMap)
	{
		if (trustStoreMap == null)
		{
			trustStoreMap = ssl.getTrustStore();
		}
		String issuerCN = this.getIssuerCN();
		if (issuerCN == null)
		{
			return ValidStatus.FileFormatInvalid;
		}
		long currTime = System.currentTimeMillis();
		ZonedDateTime dt = this.getNotBefore();
		if (dt == null)
		{
			System.out.println("getNotBefore = null");
			return ValidStatus.FileFormatInvalid;
		}
		if (DateTimeUtil.getTimeMillis(dt) > currTime)
		{
			return ValidStatus.Expired;
		}
		dt = this.getNotAfter();
		if (dt == null)
		{
			System.out.println("getNotAfter = null");
			return ValidStatus.FileFormatInvalid;
		}
		if (DateTimeUtil.getTimeMillis(dt) < currTime)
		{
			return ValidStatus.Expired;
		}
		SignedInfo signedInfo = this.getSignedInfo();
		if (signedInfo == null)
		{
			System.out.println("signedInfo = null");
			return ValidStatus.FileFormatInvalid;
		}
		HashType hashType = getAlgHash(signedInfo.algType);
		if (hashType == HashType.Unknown)
		{
			return ValidStatus.UnsupportedAlgorithm;
		}
	
		Certificate issuer = trustStoreMap.get(issuerCN);
		if (issuer == null)
		{
			if (!this.isSelfSigned())
			{
				System.out.println("UnknownIssuer: cn = "+issuerCN);
				return ValidStatus.UnknownIssuer;
			}
/*			Crypto.Cert.X509Key *key = this.GetNewPublicKey();
			if (key == 0)
			{
				return ValidStatus.FileFormatInvalid;
			}
			Bool signValid = ssl.SignatureVerify(key, hashType, signedInfo.payload, signedInfo.payloadSize, signedInfo.signature, signedInfo.signSize);
			DEL_CLASS(key);
			*/
			boolean signValid = false;
			if (signValid)
			{
				return ValidStatus.SelfSigned;
			}
			else
			{
				return ValidStatus.SignatureInvalid;
			}
		}
	
		PublicKey key = issuer.getPublicKey();
		if (key == null)
		{
			System.out.println("key = null");
			return ValidStatus.FileFormatInvalid;
		}
		boolean signValid = verifySignedInfo(signedInfo, key);
		if (!signValid)
		{
			return ValidStatus.SignatureInvalid;
		}
	
		List<String> crlDistributionPoints = this.getCRLDistributionPoints();
		if (crlDistributionPoints == null)
		{
			System.out.println("crlDistributionPoints = null");
			return ValidStatus.FileFormatInvalid;
		}
		int i = 0;
		int j = crlDistributionPoints.size();
		while (i < j)
		{
			MyX509CRL crl = ssl.getCRL(crlDistributionPoints.get(i));
			if (crl == null || crl.isValid(ssl, trustStoreMap) != ValidStatus.Valid)
			{
				return ValidStatus.CRLNotFound;
			}
			if (crl.isRevoked(this))
			{
				return ValidStatus.Revoked;
			}
			i++;
		}
		return ValidStatus.Valid;
	}

	@Override
	public ASN1Data clone()
	{
		return new MyX509Cert(this.sourceName, this.buff, 0, this.buff.length);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		if (isCertificate(this.buff, 0, this.buff.length, "1"))
		{
			appendCertificate(this.buff, 0, this.buff.length, "1", sb, null);
		}
		return sb.toString();
	}

	public CertNames getIssuerNames()
	{
		ASN1Item item = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.1.1");
		if (item == null)
		{
			return null;
		}
		if (item.itemType == ASN1Util.IT_CONTEXT_SPECIFIC_0)
		{
			item = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.1.4");
			if (item != null)
			{
				return namesGet(this.buff, item.ofst, item.ofst + item.len);
			}
		}
		else
		{
			item = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.1.3");
			if (item != null)
			{
				return namesGet(this.buff, item.ofst, item.ofst + item.len);
			}
		}
		return null;
	}

	public CertNames getSubjNames()
	{
		ASN1Item item = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.1.1");
		if (item == null)
		{
			return null;
		}
		if (item.itemType == ASN1Util.IT_CONTEXT_SPECIFIC_0)
		{
			item = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.1.6");
			if (item != null)
			{
				return namesGet(this.buff, item.ofst, item.ofst + item.len);
			}
		}
		else
		{
			item = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.1.5");
			if (item != null)
			{
				return namesGet(this.buff, item.ofst, item.ofst + item.len);
			}
		}
		return null;
	}

	public ZonedDateTime getNotBefore()
	{
		ASN1Item tmpBuff;
		if (ASN1Util.pduGetItemType(this.buff, 0, this.buff.length, "1.1.1") == ASN1Util.IT_CONTEXT_SPECIFIC_0)
		{
			tmpBuff = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.1.5.1");
		}
		else
		{
			tmpBuff = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.1.4.1");
		}
		if (tmpBuff != null && (tmpBuff.itemType == ASN1Util.IT_UTCTIME || tmpBuff.itemType == ASN1Util.IT_GENERALIZEDTIME))
		{
			return ASN1Util.pduParseUTCTimeCont(this.buff, tmpBuff.ofst, tmpBuff.len);
		}
		return null;
	}

	public ZonedDateTime getNotAfter()
	{
		ASN1Item tmpBuff;
		if (ASN1Util.pduGetItemType(this.buff, 0, this.buff.length, "1.1.1") == ASN1Util.IT_CONTEXT_SPECIFIC_0)
		{
			tmpBuff = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.1.5.2");
		}
		else
		{
			tmpBuff = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.1.4.2");
		}
		if (tmpBuff != null && (tmpBuff.itemType == ASN1Util.IT_UTCTIME || tmpBuff.itemType == ASN1Util.IT_GENERALIZEDTIME))
		{
			return ASN1Util.pduParseUTCTimeCont(this.buff, tmpBuff.ofst, tmpBuff.len);
		}
		return null;
	}

	public boolean isSelfSigned()
	{
		CertNames subjNames;
		CertNames issueNames;
		boolean ret = false;
		if ((issueNames = this.getIssuerNames()) != null && (subjNames = this.getSubjNames()) != null)
		{
			ret = issueNames.commonName.equals(subjNames.commonName);
		}
		return ret;
	
	}

	public List<String> getCRLDistributionPoints()
	{
		ASN1Item pdu = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.1.1");
		if (pdu == null)
		{
			return null;
		}
		if (pdu.itemType == ASN1Util.IT_CONTEXT_SPECIFIC_0)
		{
			pdu = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.1.8.1");
			if (pdu != null)
			{
				return extensionsGetCRLDistributionPoints(this.buff, pdu.ofst, pdu.ofst + pdu.len);
			}
		}
		else
		{
			pdu = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.1.7.1");
			if (pdu != null)
			{
				return extensionsGetCRLDistributionPoints(this.buff, pdu.ofst, pdu.ofst + pdu.len);
			}
		}
		return null;
	}

	public byte[] getSerialNumber()
	{
		ASN1Item pdu = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.1.1");
		if (pdu == null)
		{
			return null;
		}
		if (pdu.itemType == ASN1Util.IT_CONTEXT_SPECIFIC_0)
		{
			pdu = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.1.2");
			if (pdu != null && pdu.itemType == ASN1Util.IT_INTEGER)
			{
				return Arrays.copyOfRange(this.buff, pdu.ofst, pdu.ofst + pdu.len);
			}
		}
		else
		{
			pdu = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.1.1");
			if (pdu != null && pdu.itemType == ASN1Util.IT_INTEGER)
			{
				return Arrays.copyOfRange(this.buff, pdu.ofst, pdu.ofst + pdu.len);
			}
		}
		return null;
	}
}
