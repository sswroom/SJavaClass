package org.sswr.util.crypto;

import org.sswr.util.crypto.MyX509File.KeyType;
import org.sswr.util.data.StringUtil;
import org.sswr.util.data.textbinenc.Base64Enc;

public class X509Parser
{
	public static MyX509File parseBuff(byte[] buff, int ofst, int buffSize, String fileName)
	{
		MyX509File ret = null;
		int lbSize;
		if (buff[ofst + buffSize - 2] == 13 && buff[ofst + buffSize - 1] == 10)
		{
			lbSize = 2;
		}
		else if (buff[ofst + buffSize - 1] == 10 && buff[0] != 48)
		{
			lbSize = 1;
		}
		else if (buff[ofst] == 0x30)
		{
			lbSize = 0;
		}
		else
		{
			return null;
		}
		if (buff[ofst + 0] == 0xEF && buff[ofst + 1] == 0xBB && buff[ofst + 2] == 0xBF)
		{
			ofst += 3;
			buffSize -= 3;
		}
		if (lbSize != 0)
		{
			byte[] dataBuff;
			if (StringUtil.startsWithC(buff, ofst, buffSize, "-----BEGIN CERTIFICATE-----") && StringUtil.startsWithC(buff, ofst + buffSize - 25 - lbSize, 25 + lbSize, "-----END CERTIFICATE-----"))
			{
				Base64Enc b64 = new Base64Enc();
				dataBuff = b64.decodeBin(buff, ofst + 27, buffSize - 52 - lbSize);
				ret = new MyX509Cert(fileName, dataBuff, 0, dataBuff.length);
			}
			else if (StringUtil.startsWithC(buff, ofst, buffSize, "-----BEGIN RSA PRIVATE KEY-----") && StringUtil.startsWithC(buff, ofst + buffSize - 29 - lbSize, 29 + lbSize, "-----END RSA PRIVATE KEY-----"))
			{
				if (StringUtil.startsWithC(buff, ofst + 31 + lbSize, buffSize - 31 - lbSize, "Proc-Type:"))
				{
	
				}
				else
				{
					Base64Enc b64 = new Base64Enc();
					dataBuff = b64.decodeBin(buff, ofst + 31, buffSize - 60 - lbSize);
					ret = new MyX509Key(fileName, dataBuff, 0, dataBuff.length, KeyType.RSA);
				}
			}
			else if (StringUtil.startsWithC(buff, ofst, buffSize, "-----BEGIN DSA PRIVATE KEY-----") && StringUtil.startsWithC(buff, ofst + buffSize - 29 - lbSize, 29 + lbSize, "-----END DSA PRIVATE KEY-----"))
			{
				Base64Enc b64 = new Base64Enc();
				dataBuff = b64.decodeBin(buff, ofst + 31, buffSize - 60 - lbSize);
				ret = new MyX509Key(fileName, dataBuff, 0, dataBuff.length, KeyType.DSA);
			}
			else if (StringUtil.startsWithC(buff, ofst, buffSize, "-----BEGIN EC PRIVATE KEY-----") && StringUtil.startsWithC(buff, ofst + buffSize - 28 - lbSize, 28 + lbSize, "-----END EC PRIVATE KEY-----"))
			{
				Base64Enc b64 = new Base64Enc();
				dataBuff = b64.decodeBin(buff, ofst + 30, buffSize - 58 - lbSize);
				ret = new  MyX509Key(fileName, dataBuff, 0, dataBuff.length, KeyType.ECDSA);
			}
			else if (StringUtil.startsWithC(buff, ofst, buffSize, "-----BEGIN PRIVATE KEY-----") && StringUtil.startsWithC(buff, ofst + buffSize - 25 - lbSize, 25 + lbSize, "-----END PRIVATE KEY-----"))
			{
				Base64Enc b64 = new Base64Enc();
				dataBuff = b64.decodeBin(buff, ofst + 27, buffSize - 52 - lbSize);
				ret = new MyX509PrivKey(fileName, dataBuff, 0, dataBuff.length);
			}
			else if (StringUtil.startsWithC(buff, ofst, buffSize, "-----BEGIN PUBLIC KEY-----") && StringUtil.startsWithC(buff, ofst + buffSize - 24 - lbSize, 24 + lbSize, "-----END PUBLIC KEY-----"))
			{
				Base64Enc b64 = new Base64Enc();
				dataBuff = b64.decodeBin(buff, ofst + 26, buffSize - 50 - lbSize);
				ret = new MyX509PubKey(fileName, dataBuff, 0, dataBuff.length);
			}
			else if (StringUtil.startsWithC(buff, ofst, buffSize, "-----BEGIN CERTIFICATE REQUEST-----") && StringUtil.startsWithC(buff, ofst + buffSize - 33 - lbSize, 33 + lbSize, "-----END CERTIFICATE REQUEST-----"))
			{
				Base64Enc b64 = new Base64Enc();
				dataBuff = b64.decodeBin(buff, ofst + 35, buffSize - 68 - lbSize);
				ret = new MyX509CertReq(fileName, dataBuff, 0, dataBuff.length);
			}
			else if (StringUtil.startsWithC(buff, ofst, buffSize, "-----BEGIN PKCS7-----") && StringUtil.startsWithC(buff, ofst + buffSize - 19 - lbSize, 19 + lbSize, "-----END PKCS7-----"))
			{
				Base64Enc b64 = new Base64Enc();
				dataBuff = b64.decodeBin(buff, ofst + 21, buffSize - 40 - lbSize);
				ret = new MyX509PKCS7(fileName, dataBuff, 0, dataBuff.length);
			}
		}
		else
		{
			if (fileName.toUpperCase().endsWith(".P12"))
			{
				ret = new MyX509PKCS12(fileName, buff, ofst, buffSize);
			}
			else if (fileName.toUpperCase().endsWith(".PFX"))
			{
				ret = new MyX509PKCS12(fileName, buff, ofst, buffSize);
			}
			else if (fileName.toUpperCase().endsWith(".DER"))
			{
				ret = new MyX509Cert(fileName, buff, ofst, buffSize);
			}
			else if (fileName.toUpperCase().endsWith(".CER"))
			{
				ret = new MyX509Cert(fileName, buff, ofst, buffSize);
			}
			else if (fileName.toUpperCase().endsWith(".P7B"))
			{
				ret = new MyX509PKCS7(fileName, buff, ofst, buffSize);
			}
			else if (fileName.toUpperCase().endsWith(".P7S"))
			{
				ret = new MyX509PKCS7(fileName, buff, ofst, buffSize);
			}
			else if (fileName.toUpperCase().endsWith(".CRL"))
			{
				ret = new MyX509CRL(fileName, buff, ofst, buffSize);
			}
			else
			{
				return null;
			}
		}
		return ret;
	}
}
