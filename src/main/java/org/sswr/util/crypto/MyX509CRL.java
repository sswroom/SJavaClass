package org.sswr.util.crypto;

import java.security.PublicKey;
import java.security.cert.Certificate;
import java.time.ZonedDateTime;
import java.util.Map;

import org.sswr.util.data.ByteTool;
import org.sswr.util.net.ASN1Data;
import org.sswr.util.net.ASN1Item;
import org.sswr.util.net.ASN1Util;

public class MyX509CRL extends MyX509File
{
	public MyX509CRL(String sourceName, byte[] buff, int ofst, int size)
	{
		super(sourceName, buff, ofst, size);
	}

	@Override
	public FileType getFileType()
	{
		return FileType.CRL;
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
		ZonedDateTime t = this.getThisUpdate();
		ZonedDateTime currTime = ZonedDateTime.now();
		if (t == null)
		{
			return ValidStatus.FileFormatInvalid;
		}
		if (t.compareTo(currTime) > 0)
		{
			return ValidStatus.Expired;
		}
		t = this.getNextUpdate();
		if (t != null)
		{
			if (t.compareTo(currTime) < 0)
			{
				return ValidStatus.Expired;
			}
		}
		SignedInfo signedInfo = this.getSignedInfo();
		if (signedInfo == null)
		{
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
			return ValidStatus.UnknownIssuer;
		}
		PublicKey key = issuer.getPublicKey();
		if (key == null)
		{
			return ValidStatus.FileFormatInvalid;
		}
		boolean signValid = verifySignedInfo(signedInfo, key);
		if (!signValid)
		{
			return ValidStatus.SignatureInvalid;
		}
		return ValidStatus.Valid;
	}

	public boolean timeValid(ZonedDateTime time)
	{
		ZonedDateTime t = this.getThisUpdate();
		if (t == null)
		{
			return false;
		}
		if (t.compareTo(time) > 0)
		{
			return false;
		}
		t = this.getNextUpdate();
		if (t != null)
		{
			if (t.compareTo(time) < 0)
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public ASN1Data clone()
	{
		return new MyX509CRL(this.sourceName, this.buff, 0, this.buff.length);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		if (isCertificateList(this.buff, 0, this.buff.length, "1"))
		{
			appendCertificateList(this.buff, 0, this.buff.length, "1", sb, null);
		}
		return sb.toString();
	}

	public boolean hasVersion()
	{
		ASN1Item item;
		if ((item = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.1.1")) != null && item.itemType == ASN1Util.IT_INTEGER)
		{
			return true;
		}
		return false;
	}

	public String getIssuerCN()
	{
		ASN1Item tmpBuff;
		if (this.hasVersion())
		{
			tmpBuff = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.1.3");
		}
		else
		{
			tmpBuff = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.1.2");
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

	public ZonedDateTime getThisUpdate()
	{
		ASN1Item itemPDU;
		if (this.hasVersion())
		{
			itemPDU = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.1.4");
		}
		else
		{
			itemPDU = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.1.3");
		}
		if (itemPDU != null && (itemPDU.itemType == ASN1Util.IT_UTCTIME || itemPDU.itemType == ASN1Util.IT_GENERALIZEDTIME))
		{
			return ASN1Util.pduParseUTCTimeCont(this.buff, itemPDU.ofst, itemPDU.len);
		}
		else
		{
			return null;
		}
	}

	public ZonedDateTime getNextUpdate()
	{
		ASN1Item itemPDU;
		if (this.hasVersion())
		{
			itemPDU = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.1.5");
		}
		else
		{
			itemPDU = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.1.4");
		}
		if (itemPDU != null && (itemPDU.itemType == ASN1Util.IT_UTCTIME || itemPDU.itemType == ASN1Util.IT_GENERALIZEDTIME))
		{
			return ASN1Util.pduParseUTCTimeCont(this.buff, itemPDU.ofst, itemPDU.len);
		}
		else
		{
			return null;
		}
	}

	public boolean isRevoked(MyX509Cert cert)
	{
		byte[] sn = cert.getSerialNumber();
		if (sn == null)
			return true;
		String path;
		ASN1Item itemPDU;
		int i;
		if (this.hasVersion())
		{
			itemPDU = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.1.5");
			i = 5;
		}
		else
		{
			itemPDU = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.1.4");
			i = 4;
		}
		if (itemPDU == null)
		{
			return false;
		}
		if (itemPDU.itemType != ASN1Util.IT_SEQUENCE)
		{
			i++;
			path = "1.1." + i;
			itemPDU = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, path);
			if (itemPDU == null || itemPDU.itemType != ASN1Util.IT_SEQUENCE)
			{
				return false;
			}
		}
		i = 0;
		while (true)
		{
			ASN1Item subitemPDU;
			path = (++i) + ".1";
			subitemPDU = ASN1Util.pduGetItem(this.buff, itemPDU.ofst, itemPDU.ofst + itemPDU.len, path);
			if (subitemPDU == null)
				break;
			if (subitemPDU.itemType == ASN1Util.IT_INTEGER && sn.length == subitemPDU.len && ByteTool.byteEquals(this.buff, subitemPDU.ofst, sn, 0, sn.length))
			{
				return true;
			}
		}
		return false;
	}
}
