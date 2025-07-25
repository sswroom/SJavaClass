package org.sswr.util.net.email;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.sswr.util.crypto.hash.CRC32R;
import org.sswr.util.crypto.hash.SHA1;
import org.sswr.util.crypto.hash.SHA256;
import org.sswr.util.data.ByteTool;
import org.sswr.util.data.StringUtil;
import org.sswr.util.data.textbinenc.Base64Enc;
import org.sswr.util.io.MemoryStream;
import org.sswr.util.net.ASN1PDUBuilder;
import org.sswr.util.net.MIME;
import org.sswr.util.net.WebUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class SMTPMessage
{
	public static final int LINECHARCNT = 77;

	private EmailAddress fromAddr;
	private List<EmailAddress> recpList;
	private List<String> headerList;
	private String contentType;
	private byte[] content;
	private List<EmailAttachment> attachments;

	private X509Certificate signCert;
	private PrivateKey signKey;

	private int getHeaderIndex(@Nonnull String name)
	{
		String header;
		int i = 0;
		int j = this.headerList.size();
		while (i < j)
		{
			header = this.headerList.get(i);
			if (header.length() + 2 >= name.length() && header.startsWith(name) && header.charAt(name.length()) == ':' && header.charAt(name.length() + 1) == ' ')
			{
				return i;
			}
			i++;
		}
		return -1;
	}
		
	private boolean setHeader(@Nonnull String name, @Nonnull String val)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		sb.append(':');
		sb.append(' ');
		sb.append(val);
		int i = this.getHeaderIndex(name);
		if (i == -1)
		{
			this.headerList.add(sb.toString());
		}
		else
		{
			this.headerList.set(i, sb.toString());
		}
		return true;
	}
	private boolean appendUTF8Header(@Nonnull StringBuilder sb, @Nonnull String val)
	{
		sb.append(EmailTools.toUTF8Header(val));
		return true;
	}

	private void genMultipart(@Nonnull OutputStream stm, @Nonnull String boundary) throws IOException
	{
		stm.write("--".getBytes(StandardCharsets.UTF_8));
		stm.write(boundary.getBytes(StandardCharsets.UTF_8));
		stm.write("\r\nContent-Type: ".getBytes(StandardCharsets.UTF_8));
		stm.write(this.contentType.getBytes(StandardCharsets.UTF_8));
		stm.write("\r\nContent-Transfer-Encoding: base64\r\n\r\n".getBytes(StandardCharsets.UTF_8));
		writeB64Data(stm, this.content);
	
		byte[] strBuff;
		EmailAttachment att;
		int k;
		int i = 0;
		int j = this.attachments.size();
		while (i < j)
		{
			att = this.attachments.get(i);
			byte[] fileNameBuff = null;
			if (att.fileName != null)
				fileNameBuff = att.fileName.getBytes(StandardCharsets.UTF_8);
			stm.write("--".getBytes(StandardCharsets.UTF_8));
			stm.write(boundary.getBytes(StandardCharsets.UTF_8));
			stm.write("\r\nContent-Type: ".getBytes(StandardCharsets.UTF_8));
			stm.write(att.contentType.getBytes(StandardCharsets.UTF_8));
			if (fileNameBuff != null)
			{
				stm.write("; name=\"".getBytes(StandardCharsets.UTF_8));
				stm.write(fileNameBuff);
				if (att.isInline)
				{
					stm.write("\"".getBytes(StandardCharsets.UTF_8));
				}
				else
				{
					stm.write("\"\r\nContent-Description: ".getBytes(StandardCharsets.UTF_8));
					stm.write(fileNameBuff);
				}
			}
			stm.write("\r\nContent-Disposition: ".getBytes(StandardCharsets.UTF_8));
			if (att.isInline)
			{
				stm.write("inline".getBytes(StandardCharsets.UTF_8));
				k = 21 + 6;
			}
			else
			{
				stm.write("attachment".getBytes(StandardCharsets.UTF_8));
				k = 21 + 10;
			}
			if (fileNameBuff != null)
			{
				if (k + 14 + fileNameBuff.length > LINECHARCNT)
				{
					stm.write(";\r\n\tfilename=\"".getBytes(StandardCharsets.UTF_8));
					stm.write(fileNameBuff);
					stm.write("\"".getBytes(StandardCharsets.UTF_8));
					k = 15 + fileNameBuff.length;
				}
				else
				{
					stm.write("; filename=\"".getBytes(StandardCharsets.UTF_8));
					stm.write(fileNameBuff);
					stm.write("\"".getBytes(StandardCharsets.UTF_8));
					k += 13 + fileNameBuff.length;
				}
			}
			strBuff = String.valueOf(att.content.length).getBytes(StandardCharsets.UTF_8);
			if (k + 8 + strBuff.length > LINECHARCNT)
			{
				stm.write(";\r\n\tsize=".getBytes(StandardCharsets.UTF_8));
				stm.write(strBuff);
				k = 9 + strBuff.length;
			}
			else
			{
				stm.write("; size=".getBytes(StandardCharsets.UTF_8));
				stm.write(strBuff);
				k += 7 + strBuff.length;
			}
			if (att.createTime != null)
			{
				if (k + 48 > LINECHARCNT)
				{
					stm.write(";\r\n\tcreation-date=\"".getBytes(StandardCharsets.UTF_8));
					strBuff = WebUtil.date2Str(att.createTime).getBytes(StandardCharsets.UTF_8);
					stm.write(strBuff);
					stm.write("\"".getBytes(StandardCharsets.UTF_8));
					k = 20 + strBuff.length;
				}
				else
				{
					stm.write("; creation-date=\"".getBytes(StandardCharsets.UTF_8));
					strBuff = WebUtil.date2Str(att.createTime).getBytes(StandardCharsets.UTF_8);
					stm.write(strBuff);
					stm.write("\"".getBytes(StandardCharsets.UTF_8));
					k += 18 + strBuff.length;
				}
			}
			if (att.modifyTime != null)
			{
				if (k + 51 > LINECHARCNT)
				{
					stm.write(";\r\n\tmodification-date=\"".getBytes(StandardCharsets.UTF_8));
					strBuff = WebUtil.date2Str(att.modifyTime).getBytes(StandardCharsets.UTF_8);
					stm.write(strBuff);
					stm.write("\"".getBytes(StandardCharsets.UTF_8));
				}
				else
				{
					stm.write("; modification-date=\"".getBytes(StandardCharsets.UTF_8));
					strBuff = WebUtil.date2Str(att.modifyTime).getBytes(StandardCharsets.UTF_8);
					stm.write(strBuff);
					stm.write("\"".getBytes(StandardCharsets.UTF_8));
					k = 21 + strBuff.length;
				}
			}
			if (att.contentId != null)
			{
				stm.write("\r\nContent-Id: <".getBytes(StandardCharsets.UTF_8));
				stm.write(att.contentId.getBytes(StandardCharsets.UTF_8));
				stm.write(">".getBytes(StandardCharsets.UTF_8));
			}
			stm.write("\r\nContent-Transfer-Encoding: base64\r\n\r\n".getBytes(StandardCharsets.UTF_8));
			writeB64Data(stm, att.content);
	
			i++;
		}
		stm.write("--".getBytes(StandardCharsets.UTF_8));
		stm.write(boundary.getBytes(StandardCharsets.UTF_8));
		stm.write("--\r\n".getBytes(StandardCharsets.UTF_8));
	}

	private void writeHeaders(@Nonnull OutputStream stm) throws IOException
	{
		String header;
		int i = 0;
		int j = this.headerList.size();
		while (i < j)
		{
			header = this.headerList.get(i);
			stm.write(header.getBytes(StandardCharsets.UTF_8));
			stm.write("\r\n".getBytes(StandardCharsets.UTF_8));
			i++;
		}
	}

	private void writeContents(@Nonnull OutputStream stm) throws IOException
	{
		byte[] sbuff;
		if (this.attachments.size() > 0)
		{
			sbuff = genBoundary(this.content);
			stm.write("Content-Type: multipart/mixed;\r\n\tboundary=\"".getBytes(StandardCharsets.UTF_8));
			stm.write(sbuff);
			stm.write("\"\r\n".getBytes(StandardCharsets.UTF_8));
			MemoryStream mstm = new MemoryStream();
			genMultipart(mstm.createOutputStream(), new String(sbuff, StandardCharsets.UTF_8));
			stm.write("Content-Length: ".getBytes(StandardCharsets.UTF_8));
			stm.write(String.valueOf(mstm.getLength()).getBytes(StandardCharsets.UTF_8));
			stm.write("\r\n".getBytes(StandardCharsets.UTF_8));
			stm.write("\r\n".getBytes(StandardCharsets.UTF_8));
			stm.write(mstm.getBuff(), 0, (int)mstm.getLength());
		}
		else
		{
			stm.write("Content-Type: ".getBytes(StandardCharsets.UTF_8));
			stm.write(this.contentType.getBytes(StandardCharsets.UTF_8));
			stm.write("\r\n".getBytes(StandardCharsets.UTF_8));
			stm.write("Content-Length: ".getBytes(StandardCharsets.UTF_8));
			stm.write(String.valueOf(this.content.length).getBytes(StandardCharsets.UTF_8));
			stm.write("\r\n".getBytes(StandardCharsets.UTF_8));
			stm.write("\r\n".getBytes(StandardCharsets.UTF_8));
			stm.write(this.content);
		}
	}
	
	@Nonnull
	private byte[] genBoundary(@Nonnull byte[] data)
	{
		long ts = System.currentTimeMillis();
		SHA1 sha1 = new SHA1();
		byte sha1Val[] = new byte[20];
		ByteTool.writeInt64(sha1Val, 0, ts);
		sha1.calc(sha1Val, 0, 8);
		sha1.calc(data, 0, data.length);
		sha1Val = sha1.getValue();
		return new Base64Enc(Base64Enc.B64Charset.URL, true).encodeBin(sha1Val, 0, sha1Val.length).getBytes(StandardCharsets.UTF_8);
	}

	private void writeB64Data(@Nonnull OutputStream stm, @Nonnull byte[] data) throws IOException
	{
		writeB64Data(stm, data, 0, data.length);
	}

	private void writeB64Data(@Nonnull OutputStream stm, @Nonnull byte[] data, int dataOfst, int dataSize) throws IOException
	{
		Base64Enc b64 = new Base64Enc(Base64Enc.B64Charset.NORMAL, false);
		byte[] sbuff;
		byte[] crlf = {13, 10};
		while (dataSize > 57)
		{
			sbuff = b64.encodeBin(data, dataOfst, 57).getBytes(StandardCharsets.UTF_8);
			stm.write(sbuff, 0, sbuff.length);
			stm.write(crlf, 0, 2);
			dataOfst += 57;
			dataSize -= 57;
		}
		sbuff = b64.encodeBin(data, dataOfst, dataSize).getBytes(StandardCharsets.UTF_8);
		stm.write(sbuff, 0, sbuff.length);
		stm.write(crlf, 0, 2);
	}

	public SMTPMessage()
	{
		this.fromAddr = null;
		this.recpList = new ArrayList<EmailAddress>();
		this.headerList = new ArrayList<String>();
		this.content = null;
		this.attachments = new ArrayList<EmailAttachment>();
	}

	public boolean setSubject(@Nonnull String subject)
	{
		if (StringUtil.isNonASCII(subject))
		{
			StringBuilder sb = new StringBuilder();
			this.appendUTF8Header(sb, subject);
			this.setHeader("Subject", sb.toString());
		}
		else
		{
			this.setHeader("Subject", subject);
		}
		return true;
	}

	public boolean setContent(@Nonnull String content, @Nonnull String contentType)
	{
		this.contentType = contentType;
		this.content = content.getBytes(StandardCharsets.UTF_8);
		return true;
	
	}

	public boolean setSentDate(@Nonnull ZonedDateTime dt)
	{
		return this.setHeader("Date", WebUtil.date2Str(dt));
	}

	public boolean setMessageId(@Nonnull String msgId)
	{
		StringBuilder sb = new StringBuilder();
		sb.append('<');
		sb.append(msgId);
		sb.append('>');
		return this.setHeader("Message-ID", sb.toString());
	}

	public boolean setFrom(@Nonnull EmailAddress addr)
	{
		this.setHeader("From", addr.toString());
		this.fromAddr = addr;
		return true;
	}

	public boolean addTo(@Nonnull EmailAddress addr)
	{
		int i = this.getHeaderIndex("To");
		if (i != -1)
		{
			StringBuilder sb = new StringBuilder();
			sb.append(this.headerList.get(i).substring(4));
			sb.append(", ");
			sb.append(addr.toString());
			this.setHeader("To", sb.toString());
		}
		else
		{
			this.setHeader("To", addr.toString());
		}
		this.recpList.add(addr);
		return true;		
	}

	public boolean addToList(@Nonnull String addrs)
	{
		List<EmailAddress> addrList = EmailAddress.parseList(addrs);
		if (addrList == null)
			return false;
		int i = 0;
		int j = addrList.size();
		while (i < j)
		{
			EmailAddress addr = addrList.get(i);
			this.addTo(addr);
			i++;
		}
		return true;
	}

	public boolean addCcList(@Nonnull String addrs)
	{
		List<EmailAddress> addrList = EmailAddress.parseList(addrs);
		if (addrList == null)
			return false;
		int i = 0;
		int j = addrList.size();
		while (i < j)
		{
			EmailAddress addr = addrList.get(i);
			this.addCc(addr);
			i++;
		}
		return true;
	}

	public boolean addCc(@Nonnull EmailAddress addr)
	{
		int i = this.getHeaderIndex("Cc");
		if (i != -1)
		{
			StringBuilder sb = new StringBuilder();
			String s = this.headerList.get(i);
			sb.append(s.substring(4));
			sb.append(", ");
			sb.append(addr.toString());
			this.setHeader("Cc", sb.toString());
		}
		else
		{
			this.setHeader("Cc", addr.toString());
		}
		this.recpList.add(addr);
		return true;
	}

	public boolean addBcc(@Nonnull EmailAddress addr)
	{
		this.recpList.add(addr);
		return true;
	}

	public boolean addBccList(@Nonnull String addrs)
	{
		List<EmailAddress> addrList = EmailAddress.parseList(addrs);
		if (addrList == null)
			return false;
		int i = 0;
		int j = addrList.size();
		while (i < j)
		{
			EmailAddress addr = addrList.get(i);
			this.addBcc(addr);
			i++;
		}
		return true;
	}

	@Nullable
	public EmailAttachment addAttachment(@Nonnull String fileName)
	{
		EmailAttachment att = EmailAttachment.createFromFile(fileName, "attach"+(this.attachments.size() + 1));
		if (att != null)
			this.attachments.add(att);
		return att;
	}

	@Nonnull
	public EmailAttachment addAttachment(@Nonnull byte[] content, @Nonnull String fileName)
	{
		EmailAttachment attachment = new EmailAttachment();
		attachment.content = content;
		attachment.createTime = ZonedDateTime.now();
		attachment.modifyTime = attachment.createTime;
		attachment.fileName = fileName.substring(fileName.lastIndexOf(File.separator) + 1);
		attachment.contentId = "attach" + (this.attachments.size() + 1);
		attachment.isInline = false;
		attachment.contentType = MIME.getMIMEFromFileName(attachment.fileName);
		this.attachments.add(attachment);
		return attachment;
	}

	public void addAttachment(@Nonnull EmailAttachment att)
	{
		this.attachments.add(att.clone());
	}

	public boolean addSignature(@Nullable X509Certificate cert, @Nullable PrivateKey key)
	{
		this.signCert = cert;
		this.signKey = key;
		return cert != null && key != null;
	}


	public boolean canSend()
	{
		if (this.fromAddr == null || this.recpList.size() == 0 || this.content == null)
		{
			return false;
		}
		return true;
	}

	@Nullable
	public String getFromAddr()
	{
		return (this.fromAddr != null)?this.fromAddr.getAddress():null;
	}

	@Nonnull
	public List<EmailAddress> getRecpList()
	{
		return this.recpList;
	}

	public boolean writeToSend(@Nonnull OutputStream stm)
	{
		if (!this.canSend())
		{
			return false;
		}
		return this.writeMessage(stm);
	}
	public boolean writeMessage(@Nonnull OutputStream stm)
	{
		try
		{
			this.writeHeaders(stm);
			if (this.signCert != null && this.signKey != null)
			{
				MemoryStream mstm = new MemoryStream();
				this.writeContents(mstm.createOutputStream());
				mstm.write("\r\n".getBytes(StandardCharsets.UTF_8));
		
				byte[] signData;
				byte[] sbuff = genBoundary(mstm.getBuff());
				stm.write("Content-Type: multipart/signed; protocol=\"application/pkcs7-signature\";\r\n micalg=sha-256; boundary=\"".getBytes(StandardCharsets.UTF_8));
				stm.write(sbuff);
				stm.write("\"\r\n\r\n".getBytes(StandardCharsets.UTF_8));
				stm.write("This is a cryptographically signed message in MIME format.\r\n".getBytes(StandardCharsets.UTF_8));
				stm.write("\r\n\r\n--".getBytes(StandardCharsets.UTF_8));
				stm.write(sbuff);
				stm.write("\r\n".getBytes(StandardCharsets.UTF_8));
				stm.write(mstm.getBuff(), 0, (int)mstm.getLength());
				stm.write("\r\n\r\n--".getBytes(StandardCharsets.UTF_8));
				stm.write(sbuff);
				stm.write("\r\n".getBytes(StandardCharsets.UTF_8));
				stm.write("Content-Type: application/pkcs7-signature; name=\"smime.p7s\"\r\n".getBytes(StandardCharsets.UTF_8));
				stm.write("Content-Transfer-Encoding: base64\r\n".getBytes(StandardCharsets.UTF_8));
				stm.write("Content-Disposition: attachment; filename=\"smime.p7s\"\r\n".getBytes(StandardCharsets.UTF_8));
				stm.write("Content-Description: S/MIME Cryptographic Signature\r\n\r\n".getBytes(StandardCharsets.UTF_8));
		
				byte[] data;
				ZonedDateTime dt = ZonedDateTime.now();
				ASN1PDUBuilder builder = new ASN1PDUBuilder();
				builder.beginSequence();
					builder.appendOIDString("1.2.840.113549.1.7.2"); //signedData
					builder.beginContentSpecific(0);
						builder.beginSequence();
							builder.appendInt32(1);
							builder.beginSet();
								builder.beginSequence();
									builder.appendOIDString("2.16.840.1.101.3.4.2.1"); //id-sha256
									builder.appendNull();
								builder.endLevel();
							builder.endLevel();
							builder.beginSequence();
								builder.appendOIDString("1.2.840.113549.1.7.1"); //data
							builder.endLevel();
							builder.appendContentSpecific((byte)0, this.signCert.getEncoded());
							builder.beginSet();
								builder.beginSequence();
									builder.appendInt32(1);
									builder.beginSequence();
										data = this.signCert.getIssuerX500Principal().getEncoded();
										builder.appendSequence(data);
										data = this.signCert.getSerialNumber().toByteArray();
										builder.appendInteger(data);
									builder.endLevel();
									builder.beginSequence();
										builder.appendOIDString("2.16.840.1.101.3.4.2.1"); //id-sha256
										builder.appendNull();
									builder.endLevel();
									builder.beginContentSpecific(0);
										builder.beginSequence();
											builder.appendOIDString("1.2.840.113549.1.9.3"); //contentType
											builder.beginSet();
												builder.appendOIDString("1.2.840.113549.1.7.1"); //data
											builder.endLevel();
										builder.endLevel();
										builder.beginSequence();
											builder.appendOIDString("1.2.840.113549.1.9.5"); //signing-time
											builder.beginSet();
												builder.appendUTCTime(dt);
											builder.endLevel();
										builder.endLevel();
										{
											SHA256 sha = new SHA256();
											sha.calc(mstm.getBuff(), 0, (int)mstm.getLength());
											signData = sha.getValue();
		
											builder.beginSequence();
												builder.appendOIDString("1.2.840.113549.1.9.4"); //messageDigest
												builder.beginSet();
													builder.appendOctetString(signData, 0, signData.length);
												builder.endLevel();
											builder.endLevel();
										}
										builder.beginSequence();
											builder.appendOIDString("1.2.840.113549.1.9.15"); //smimeCapabilities
											builder.beginSet();
												builder.beginSequence();
													builder.beginSequence();
														builder.appendOIDString("2.16.840.1.101.3.4.1.42"); //aes256-CBC
													builder.endLevel();
													builder.beginSequence();
														builder.appendOIDString("2.16.840.1.101.3.4.1.2"); //aes128-CBC
													builder.endLevel();
													builder.beginSequence();
														builder.appendOIDString("1.2.840.113549.3.7"); //des-ede3-cbc
													builder.endLevel();
													builder.beginSequence();
														builder.appendOIDString("1.2.840.113549.3.2"); //rc2CBC
														builder.appendInt32(128);
													builder.endLevel();
													builder.beginSequence();
														builder.appendOIDString("1.2.840.113549.3.2"); //rc2CBC
														builder.appendInt32(64);
													builder.endLevel();
													builder.beginSequence();
														builder.appendOIDString("1.3.14.3.2.7"); //desCBC
													builder.endLevel();
													builder.beginSequence();
														builder.appendOIDString("1.2.840.113549.3.2"); //rc2CBC
														builder.appendInt32(40);
													builder.endLevel();
												builder.endLevel();
											builder.endLevel();
										builder.endLevel();
										builder.beginSequence();
											builder.appendOIDString("1.3.6.1.4.1.311.16.4"); //outlookExpress
											builder.beginSet();
												builder.beginSequence();
													data = this.signCert.getIssuerX500Principal().getEncoded();
													builder.appendSequence(data);
													data = this.signCert.getSerialNumber().toByteArray();
													builder.appendInteger(data);
												builder.endLevel();
											builder.endLevel();
										builder.endLevel();
										builder.beginSequence();
											builder.appendOIDString("1.2.840.113549.1.9.16.2.11"); //id-aa-encrypKeyPref
											builder.beginSet();
												builder.beginContentSpecific(0);
													data = this.signCert.getIssuerX500Principal().getEncoded();
													builder.appendSequence(data);
													data = this.signCert.getSerialNumber().toByteArray();
													builder.appendInteger(data);
												builder.endLevel();
											builder.endLevel();
										builder.endLevel();
									builder.endLevel();
									builder.beginSequence();
										builder.appendOIDString("1.2.840.113549.1.1.1"); //rsaEncryption
										builder.appendNull();
									builder.endLevel();
									///////////////////////////////////////
									//this.signCert.Signature(this.signKey, Crypto::Hash::HT_SHA256, mstm.getBuff(), 0, mstm.getLength(), signData, &signLen);
									//builder.appendOctetString(signData, 0, signData.length);
								builder.endLevel();
							builder.endLevel();
						builder.endLevel();
					builder.endLevel();
				builder.endLevel();
				
				writeB64Data(stm, builder.getBuff(null), 0, builder.getBuffSize());
				stm.write("\r\n--".getBytes(StandardCharsets.UTF_8));
				stm.write(sbuff);
				stm.write("--".getBytes(StandardCharsets.UTF_8));
			}
			else
			{
				this.writeContents(stm);
			}
			return true;
		}
		catch (CertificateEncodingException ex)
		{
			ex.printStackTrace();
			return false;
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			return false;
		}
	}

	public static boolean generateMessageID(@Nonnull StringBuilder sb, @Nonnull String mailFrom)
	{
		sb.append(StringUtil.toHex64(System.currentTimeMillis()));
		sb.append('.');
		CRC32R crc = new CRC32R();
		int i;
		i = mailFrom.indexOf('@');
		crc.calc(mailFrom.getBytes(StandardCharsets.UTF_8), 0, i);
		;
		sb.append(StringUtil.toHex32(ByteTool.readMInt32(crc.getValue(), 0)));
		sb.append(mailFrom.substring(i));
		return true;
	}
}
