package org.sswr.util.parser;

import org.sswr.util.crypto.MyX509CRL;
import org.sswr.util.crypto.MyX509Cert;
import org.sswr.util.crypto.MyX509CertReq;
import org.sswr.util.crypto.MyX509File;
import org.sswr.util.crypto.MyX509Key;
import org.sswr.util.crypto.MyX509PKCS12;
import org.sswr.util.crypto.MyX509PKCS7;
import org.sswr.util.crypto.MyX509PrivKey;
import org.sswr.util.crypto.MyX509PubKey;
import org.sswr.util.crypto.MyX509File.KeyType;
import org.sswr.util.data.StringUtil;
import org.sswr.util.data.textbinenc.Base64Enc;
import org.sswr.util.io.FileParser;
import org.sswr.util.io.FileSelector;
import org.sswr.util.io.PackageFile;
import org.sswr.util.io.ParsedObject;
import org.sswr.util.io.ParserType;
import org.sswr.util.io.StreamData;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class X509Parser extends FileParser
{
	@Nullable
	public static MyX509File parseBuff(@Nonnull byte[] buff, int ofst, int buffSize, @Nonnull String fileName)
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

	@Override
	@Nullable
	public ParsedObject parseFileHdr(@Nonnull StreamData fd, @Nullable PackageFile pkgFile, @Nonnull ParserType targetType, @Nonnull byte[] hdr, int hdrOfst, int hdrSize) {
		byte[] buff;
		long len = fd.getDataSize();
		if (targetType != ParserType.Unknown && targetType != ParserType.ASN1Data)
		{
			return null;
		}
		String fileName = fd.getFullFileName();
		if (len > 10240)
		{
			if (fileName.toUpperCase().endsWith(".CRL") && len <= 10485760)
			{
				buff = new byte[(int)len];
				if (fd.getRealData(0, (int)len, buff, 0) != len)
					return null;
				return parseBuff(buff, 0, buff.length, fileName);
			}
			else
			{
				return null;
			}
		}
		buff = new byte[(int)len];
		fd.getRealData(0, (int)len, buff, 0);
		return parseBuff(buff, 0, buff.length, fileName);
	}

	@Override
	@Nonnull
	public String getName() {
		return "X509";
	}

	@Override
	public void prepareSelector(@Nonnull FileSelector selector, @Nonnull ParserType t) {
		if (t == ParserType.Unknown || t == ParserType.ASN1Data)
		{
			selector.addFilter("*.crt", "X.509 Certification File");
			selector.addFilter("*.csr", "X.509 Certification Request");
			selector.addFilter("*.p7b", "PKCS 7 Certification File");
			selector.addFilter("*.p7s", "PKCS 7 Signature File");
			selector.addFilter("*.p12", "PKCS 12 KeyStore File");
			selector.addFilter("*.pfx", "PKCS 12 KeyStore File");
			selector.addFilter("*.pem", "PEM File");
			selector.addFilter("*.der", "DER File");
			selector.addFilter("*.cer", "CER File");
			selector.addFilter("*.req", "PKCS 10 Request File");
			selector.addFilter("*.crl", "Certification Revocation List");
		}
	}

	@Override
	@Nonnull
	public ParserType getParserType() {
		return ParserType.ASN1Data;
	}
}
