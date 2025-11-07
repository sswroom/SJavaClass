package org.sswr.util.net;

import java.security.PrivateKey;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.sswr.util.crypto.cert.CertUtil;
import org.sswr.util.crypto.cert.CipherPadding;
import org.sswr.util.crypto.encrypt.AES256;
import org.sswr.util.crypto.encrypt.BlockCipher;
import org.sswr.util.crypto.encrypt.EncryptionException;
import org.sswr.util.crypto.encrypt.BlockCipher.ChainMode;
import org.sswr.util.data.ByteTool;
import org.sswr.util.data.EncodingFactory;
import org.sswr.util.data.SharedObject;
import org.sswr.util.data.StringBuilderUTF8;
import org.sswr.util.data.StringUtil;
import org.sswr.util.data.XMLAttrib;
import org.sswr.util.data.XMLReader;
import org.sswr.util.data.XMLReader.ParseMode;
import org.sswr.util.data.textbinenc.Base64Enc;
import org.sswr.util.io.MemoryReadingStream;
import org.sswr.util.io.MemoryStream;
import org.sswr.util.net.SAMLHandler.SAMLStatusCode;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class SAMLUtil {
	private static int decryptEncryptedKey(@Nonnull PrivateKey key, @Nonnull XMLReader reader, @Nonnull StringBuilderUTF8 sbResult, @Nonnull byte[] keyBuff)
	{
		String nodeName;
		CipherPadding rsaPadding = CipherPadding.PKCS1;
		boolean algFound = false;
		XMLAttrib attr;
		String avalue;
		int keySize = 0;
		while ((nodeName = reader.nextElementName()) != null)
		{
			if (nodeName.equals("e:EncryptionMethod"))
			{
				int i = reader.getAttribCount();
				while (i-- > 0)
				{
					attr = reader.getAttribNoCheck(i);
					if (StringUtil.orEmpty(attr.name).equals("Algorithm") && (avalue = attr.value) != null)
					{
						if (avalue.equals("http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p"))
						{
							algFound = true;
							rsaPadding = CipherPadding.OAEP;
							break;
						}
					}
				}
				reader.skipElement();
			}
			else if (nodeName.equals("KeyInfo"))
			{
				reader.skipElement();
			}
			else if (nodeName.equals("e:CipherData"))
			{
				if (!algFound)
				{
					sbResult.append("Algorithm not found in EncryptedKey");
					return 0;
				}
				StringBuilderUTF8 sb = new StringBuilderUTF8();
				reader.readNodeText(sb);
				Base64Enc b64 = new Base64Enc();
				byte[] data = b64.decodeBin(sb.toString());
				int blockSize = CertUtil.getDataBlockSize(key);
				if (blockSize != 0 && data.length != blockSize)
				{
					sbResult.append("Length of e:CipherData not valid in EncryptedKey");
					return 0;
				}
				keySize = CertUtil.decrypt(key, keyBuff, 0, data, 0, data.length, rsaPadding);
				if (keySize == 0)
				{
					sbResult.append("Error in decrypting the EncryptedKey");
					return 0;
				}
			}
			else
			{
				reader.skipElement();
			}
		}
		if (reader.getErrorCode() != 0)
		{
			sbResult.append("End of EncryptedKey not found");
			return 0;
		}
		if (keySize == 0)
		{
			sbResult.append("e:CipherData not found in EncryptedKey");
			return 0;
		}
		return keySize;
	}

	private static int parseKeyInfo(@Nonnull PrivateKey key, @Nonnull XMLReader reader, @Nonnull StringBuilderUTF8 sbResult, @Nonnull byte[] keyBuff)
	{
		String nodeName;
		int keySize = 0;
		while ((nodeName = reader.nextElementName()) != null)
		{
			if (nodeName.equals("e:EncryptedKey"))
			{
				keySize = decryptEncryptedKey(key, reader, sbResult, keyBuff);
				if (keySize == 0)
				{
					return 0;
				}
			}
			else
			{
				reader.skipElement();
			}
		}
		if (reader.getErrorCode() != 0)
		{
			sbResult.append("End of EncryptedKey not found");
			return 0;
		}
		if (keySize == 0)
		{
			sbResult.append("e:CipherData not found in EncryptedKey");
			return 0;
		}
		return keySize;
	}

	private static boolean decryptEncryptedData(@Nonnull PrivateKey key, @Nonnull XMLReader reader, @Nonnull StringBuilderUTF8 sbResult)
	{
		byte[] keyBuff = new byte[128];
		int keySize = 0;
		int algKeySize = 0;
		boolean headingIV = false;
		ByteTool.clearArray(keyBuff, 0, keyBuff.length);
		BlockCipher cipher = null;
		XMLAttrib attr;
		String avalue;
		String nodeName;
		while ((nodeName = reader.nextElementName()) != null)
		{
			if (nodeName.equals("xenc:EncryptionMethod"))
			{
				if (cipher != null)
				{
					sbResult.append("xenc:EncryptionMethod already exists");
					return false;
				}
				int i = reader.getAttribCount();
				while (i-- > 0)
				{
					attr = reader.getAttribNoCheck(i);
					if (StringUtil.orEmpty(attr.name).equals("Algorithm") && (avalue = attr.value) != null)
					{
						if (avalue.equals("http://www.w3.org/2001/04/xmlenc#aes256-cbc"))
						{
							cipher = new AES256(keyBuff);
							cipher.setChainMode(ChainMode.CBC);
							algKeySize = 32;
							headingIV = true;
						}
						else
						{
							sbResult.append("Algorithm not supported: ");
							sbResult.append(avalue);
							break;
						}
					}
				}
				if (cipher == null)
				{
					sbResult.append("Algorithm not found in xenc:EncryptionMethod");
					return false;
				}
				reader.skipElement();
			}
			else if (nodeName.equals("KeyInfo"))
			{
				keySize = parseKeyInfo(key, reader, sbResult, keyBuff);
				if (keySize == 0)
				{
					return false;
				}
			}
			else if (nodeName.equals("xenc:CipherData"))
			{
				if (cipher == null)
				{
					sbResult.append("xenc:EncryptionMethod not found before xenc:CipherData");
					return false;
				}
				else if (keySize != algKeySize)
				{
					sbResult.append("Key size invalid");
					return false;
				}
				((AES256)cipher).setKey(keyBuff);
				while ((nodeName = reader.nextElementName()) != null)
				{
					if (nodeName.equals("xenc:CipherValue"))
					{
						StringBuilderUTF8 sb = new StringBuilderUTF8();
						reader.readNodeText(sb);
						Base64Enc b64 = new Base64Enc();
						byte[] data = b64.decodeBin(sb.toString());
						if (headingIV)
						{
							if (data.length < cipher.getDecBlockSize())
							{
								sbResult.append("xenc:CipherValue is too short to decrypt");
								return false;
							}
						}
						int blkSize = cipher.getDecBlockSize();
						byte[] decData;
						int decSize;
						try
						{
							if (headingIV)
							{
								cipher.setIV(data);
								decData = cipher.decrypt(data, blkSize, data.length - blkSize);
							}
							else
							{
								decData = cipher.decrypt(data, 0, data.length);
							}
						}
						catch (EncryptionException ex)
						{
							ex.printStackTrace();
							sbResult.append("Error in decrypting content: "+ex.getMessage());
							return false;
						}
						decSize = decData.length;
						if (decSize > 0 && (decData[decSize - 1] & 255) <= blkSize)
						{
							decSize -= decData[decSize - 1];
						}
						sbResult.appendC(decData, 0, decSize);
						return true;
					}
					else
					{
						reader.skipElement();
					}
				}
				sbResult.append("xenc:CipherData not found in EncryptedData");
				return false;
			}
			else
			{
				reader.skipElement();
			}
		}
		if (reader.getErrorCode() != 0)
		{
			sbResult.append("End of EncryptedData not found");
			return false;
		}
		sbResult.append("xenc:CipherData not found in EncryptedData");
		return false;
	}

	private static boolean decryptAssertion(@Nonnull PrivateKey key, @Nonnull XMLReader reader, @Nonnull StringBuilderUTF8 sbResult, @Nonnull SharedObject<SAMLStatusCode> statusCode)
	{
		String nodeName;
		while ((nodeName = reader.nextElementName()) != null)
		{
			if (nodeName.equals("xenc:EncryptedData"))
			{
				return decryptEncryptedData(key, reader, sbResult);
			}
			else
			{
				reader.skipElement();
			}
		}
		if (reader.getErrorCode() != 0)
		{
			sbResult.append("End of EncryptedAssertion not found");
			return false;
		}
		sbResult.append("xenc:EncryptedData not found in EncryptedAssertion");
		return false;
	}

	private static boolean decryptResponse(@Nonnull PrivateKey key, @Nonnull XMLReader reader, @Nonnull StringBuilderUTF8 sbResult, @Nonnull SharedObject<SAMLStatusCode> statusCode)
	{
		String nodeName;
		boolean success = false;
		boolean found = false;
		while ((nodeName = reader.nextElementName2()) != null)
		{
			if (nodeName.equals("EncryptedAssertion"))
			{
				success = decryptAssertion(key, reader, sbResult, statusCode);
				found = true;
			}
			else if (nodeName.equals("Status"))
			{
				while ((nodeName = reader.nextElementName2()) != null)
				{
					if (nodeName.equals("StatusCode"))
					{
						int i = reader.getAttribCount();
						XMLAttrib attr;
						while (i-- > 0)
						{
							attr = reader.getAttribNoCheck(i);
							if ((nodeName = attr.name) != null && nodeName.equals("Value") && (nodeName = attr.value) != null)
							{
								statusCode.value = SAMLStatusCode.fromString(nodeName);
								break;
							}
						}
					}
					reader.skipElement();
				}
			}
			else
			{
				reader.skipElement();
			}
		}
		if (found)
		{
			return success;
		}
		if (reader.getErrorCode() != 0)
		{
			sbResult.append("End of Response not found");
			return false;
		}
		sbResult.append("Assertion not found in response");
		return false;
	}

	public static boolean decryptResponse(@Nullable EncodingFactory encFact, @Nonnull PrivateKey key, @Nonnull byte[] responseXML, @Nonnull StringBuilderUTF8 sbResult, @Nonnull SharedObject<SAMLStatusCode> statusCode)
	{
		MemoryReadingStream mstm = new MemoryReadingStream(responseXML, 0, responseXML.length);
		XMLReader reader = new XMLReader(encFact, mstm, ParseMode.XML);
		String nodeText;
		if ((nodeText = reader.nextElementName()) != null)
		{
			if (nodeText.equals("samlp:Response"))
			{
				return decryptResponse(key, reader, sbResult, statusCode);
			}
			else
			{
				sbResult.append("Root node is not SAML Response");
				return false;
			}
		}
		sbResult.append("File is not valid XML");
		return false;
	}

	public static boolean decodeRequest(@Nonnull String requestB64, @Nonnull StringBuilderUTF8 sbResult)
	{
		Base64Enc b64 = new Base64Enc();
		byte[] decBuff = b64.decodeBin(requestB64);
		if (decBuff == null || decBuff.length == 0)
			return false;
		MemoryStream mstm = new MemoryStream();
		Inflater inflater = new Inflater();
		inflater.setInput(decBuff);
		byte[] buff = new byte[4096];
		int buffSize;
		try
		{
			while ((buffSize = inflater.inflate(buff)) > 0)
			{
				mstm.write(buff, 0, buffSize);
			}
		}
		catch (DataFormatException ex)
		{
			ex.printStackTrace();
			return false;
		}
		
		boolean succ = inflater.getTotalIn() == decBuff.length;
		if (succ)
		{
			sbResult.appendC(mstm.getBuff(), 0, (int)mstm.getLength());
		}
		return succ;
	}
}
