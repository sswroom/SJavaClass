package org.sswr.util.exporter;

import java.nio.charset.StandardCharsets;

import org.sswr.util.crypto.cert.MyX509File;
import org.sswr.util.crypto.cert.MyX509FileList;
import org.sswr.util.crypto.cert.MyX509Key;
import org.sswr.util.data.LineBreakType;
import org.sswr.util.data.StringBuilderUTF8;
import org.sswr.util.data.textbinenc.Base64Enc;
import org.sswr.util.io.FileStream;
import org.sswr.util.io.ParsedObject;
import org.sswr.util.io.SeekableStream;
import org.sswr.util.io.FileStream.BufferType;
import org.sswr.util.io.FileStream.FileMode;
import org.sswr.util.io.FileStream.FileShare;
import org.sswr.util.net.ASN1Data;
import org.sswr.util.net.ASN1Type;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class PEMExporter
{
	public PEMExporter()
	{
	}

	public boolean exportFile(@Nonnull SeekableStream stm, @Nonnull String fileName, @Nonnull ParsedObject pobj, @Nullable Object param)
	{
		if (!(pobj instanceof ASN1Data))
		{
			return false;
		}
		ASN1Data asn1 = (ASN1Data)pobj;
		if (asn1.getASN1Type() != ASN1Type.X509)
		{
			return false;
		}
		return exportStream(stm, (MyX509File)asn1);
	}

	public static boolean exportStream(@Nonnull SeekableStream stm, @Nonnull MyX509File x509)
	{
		Base64Enc b64 = new Base64Enc();
		StringBuilderUTF8 sb = new StringBuilderUTF8();
		byte[] sbuff;
		switch (x509.getFileType())
		{
		case Cert:
			sb.append("-----BEGIN CERTIFICATE-----\n");
			b64.encodeBin(sb, x509.getASN1Buff(), 0, x509.getASN1BuffSize(), LineBreakType.LF, 64);
			sb.append("\n-----END CERTIFICATE-----\n");
			sbuff = sb.toString().getBytes(StandardCharsets.UTF_8);
			return stm.write(sbuff, 0, sbuff.length) == sbuff.length;
		case CertRequest:
			sb.append("-----BEGIN CERTIFICATE REQUEST-----\n");
			b64.encodeBin(sb, x509.getASN1Buff(), 0, x509.getASN1BuffSize(), LineBreakType.LF, 64);
			sb.append("\n-----END CERTIFICATE REQUEST-----\n");
			sbuff = sb.toString().getBytes(StandardCharsets.UTF_8);
			return stm.write(sbuff, 0, sbuff.length) == sbuff.length;
		case Key:
			{
				MyX509Key key = (MyX509Key)x509;
				switch (key.getKeyType())
				{
				case RSA:
					sb.append("-----BEGIN RSA PRIVATE KEY-----\n");
					b64.encodeBin(sb, x509.getASN1Buff(), 0, x509.getASN1BuffSize(), LineBreakType.LF, 64);
					sb.append("\n-----END RSA PRIVATE KEY-----\n");
					sbuff = sb.toString().getBytes(StandardCharsets.UTF_8);
					return stm.write(sbuff, 0, sbuff.length) == sbuff.length;
				case DSA:
					sb.append("-----BEGIN DSA PRIVATE KEY-----\n");
					b64.encodeBin(sb, x509.getASN1Buff(), 0, x509.getASN1BuffSize(), LineBreakType.LF, 64);
					sb.append("\n-----END DSA PRIVATE KEY-----\n");
					sbuff = sb.toString().getBytes(StandardCharsets.UTF_8);
					return stm.write(sbuff, 0, sbuff.length) == sbuff.length;
				case ECDSA:
					sb.append("-----BEGIN EC PRIVATE KEY-----\n");
					b64.encodeBin(sb, x509.getASN1Buff(), 0, x509.getASN1BuffSize(), LineBreakType.LF, 64);
					sb.append("\n-----END EC PRIVATE KEY-----\n");
					sbuff = sb.toString().getBytes(StandardCharsets.UTF_8);
					return stm.write(sbuff, 0, sbuff.length) == sbuff.length;
				case ECPublic:
				case RSAPublic:
				case ED25519:
				case Unknown:
				default:
					return false;
				}
			}
		case PrivateKey:
			sb.append("-----BEGIN PRIVATE KEY-----\n");
			b64.encodeBin(sb, x509.getASN1Buff(), 0, x509.getASN1BuffSize(), LineBreakType.LF, 64);
			sb.append("\n-----END PRIVATE KEY-----\n");
			sbuff = sb.toString().getBytes(StandardCharsets.UTF_8);
			return stm.write(sbuff, 0, sbuff.length) == sbuff.length;
		case PublicKey:
			sb.append("-----BEGIN PUBLIC KEY-----\n");
			b64.encodeBin(sb, x509.getASN1Buff(), 0, x509.getASN1BuffSize(), LineBreakType.LF, 64);
			sb.append("\n-----END PUBLIC KEY-----\n");
			sbuff = sb.toString().getBytes(StandardCharsets.UTF_8);
			return stm.write(sbuff, 0, sbuff.length) == sbuff.length;
		case PKCS7:
			sb.append("-----BEGIN PKCS7-----\n");
			b64.encodeBin(sb, x509.getASN1Buff(), 0, x509.getASN1BuffSize(), LineBreakType.LF, 64);
			sb.append("\n-----END PKCS7-----\n");
			sbuff = sb.toString().getBytes(StandardCharsets.UTF_8);
			return stm.write(sbuff, 0, sbuff.length) == sbuff.length;
		case FileList:
		{
			MyX509FileList fileList = (MyX509FileList)x509;
			MyX509File file;
			int i = 0;
			int j = fileList.getFileCount();
			while (i < j)
			{
				if ((file = fileList.getFile(i)) == null || !exportStream(stm, file))
					return false;
				i++;
			}
			return true;
		}
		case PKCS12:
		case CRL:
		case Jks:
			break;
		}
		return false;
	}

	public static boolean exportFile(@Nonnull String fileName, @Nonnull MyX509File x509)
	{
		FileStream fs = new FileStream(fileName, FileMode.Create, FileShare.DenyNone, BufferType.Normal);
		if (fs.isError())
		{
			return false;
		}
		boolean succ = exportStream(fs, x509);
		fs.close();
		return succ;
	}
}
