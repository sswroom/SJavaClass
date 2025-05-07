package org.sswr.util.net;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.zip.Deflater;

import org.sswr.util.crypto.cert.CertUtil;
import org.sswr.util.crypto.cert.MyX509Cert;
import org.sswr.util.crypto.cert.MyX509File;
import org.sswr.util.crypto.cert.MyX509Key;
import org.sswr.util.crypto.cert.MyX509PrivKey;
import org.sswr.util.crypto.cert.MyX509File.FileType;
import org.sswr.util.crypto.hash.HashType;
import org.sswr.util.data.DateTimeUtil;
import org.sswr.util.data.EncodingFactory;
import org.sswr.util.data.LineBreakType;
import org.sswr.util.data.StringBuilderUTF8;
import org.sswr.util.data.XmlUtil;
import org.sswr.util.data.textbinenc.Base64Enc;
import org.sswr.util.data.textenc.FormEncoding;
import org.sswr.util.io.ParsedObject;
import org.sswr.util.io.ParserType;
import org.sswr.util.parser.X509Parser;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class SAMLHandler {
	public static class SAMLSSOResponse
	{
		@Nonnull
		public String status;
		@Nullable
		public String username;
		public SAMLSSOResponse(@Nonnull String status, @Nullable String username)
		{
			this.status = status;
			this.username = username;
		}
	}
	private String host;
	private String loginPath;
	private String logoutPath;
	private String ssoPath;
	private String metadataPath;
	private String allowOrigin;
	private String contentSecurityPolicy;
	private MyX509Cert signCert;
	private MyX509Key signKey;
	private SAMLIdpConfig idp;
	private HashType hashType;
	private EncodingFactory encFact;

	private void addResponseHeaders(HttpServletRequest req, HttpServletResponse resp)
	{
		String s;
		HTTPServerUtil.addDefHeaders(resp, req);
		if ((s = this.allowOrigin) != null)
		{
			resp.addHeader("Access-Control-Allow-Origin", s);
		}
		if ((s = this.contentSecurityPolicy) != null)
		{
			resp.addHeader("Content-Security-Policy", s);
		}
	}
	
	public SAMLHandler(@Nonnull String host, @Nonnull String loginPath, @Nonnull String logoutPath, @Nonnull String ssoPath, @Nonnull String metadataPath)
	{
		this.host = host;
		this.loginPath = loginPath;
		this.logoutPath = logoutPath;
		this.ssoPath = ssoPath;
		this.metadataPath = metadataPath;
		this.hashType = HashType.SHA1;
		this.encFact = new EncodingFactory();
	}

	public void setAllowOrigin(@Nullable String allowOrigin)
	{
		this.allowOrigin = allowOrigin;
	}

	public void setContentSecurityPolicy(@Nullable String contentSecurityPolicy)
	{
		this.contentSecurityPolicy = contentSecurityPolicy;
	}

	public void setIdp(@Nonnull SAMLIdpConfig idp)
	{
		this.idp = idp;
	}

	public void setHashType(@Nonnull HashType hashType)
	{
		this.hashType = hashType;
	}

	public boolean loadSignCertKeyFiles(@Nonnull String certPath, @Nonnull String keyPath)
	{
		X509Parser parser = new X509Parser();
		ParsedObject pobj;
		MyX509File x509;
		pobj = parser.parseFilePath(certPath);
		if (pobj == null || pobj.getParserType() != ParserType.ASN1Data)
			return false;
		x509 = X509Parser.toType(pobj, FileType.Cert);
		if (x509 == null)
			return false;
		this.signCert = (MyX509Cert)x509;
		pobj = parser.parseFilePath(keyPath);
		if (pobj == null || pobj.getParserType() != ParserType.ASN1Data)
			return false;
		x509 = X509Parser.toType(pobj, FileType.PrivateKey);
		if (x509 == null)
			return false;
		this.signKey = ((MyX509PrivKey)x509).createKey();
		if (this.signKey == null)
			return false;
		return true;
	}

	public boolean loadSignCertKeyPKCS12(@Nonnull String pfxPath, @Nonnull String name, @Nonnull String password)
	{
		KeyStore ks = CertUtil.loadKeyStore(pfxPath, password);
		if (ks == null)
			return false;
		try
		{
			if (ks.size() == 1)
			{
				name = ks.aliases().nextElement();
			}
			Certificate cert = ks.getCertificate(name);
			Key key = ks.getKey(name, password.toCharArray());
			if (cert != null && key != null)
			{
				this.signCert = CertUtil.toMyCert(cert);
				MyX509PrivKey privKey = CertUtil.toMyPrivKey(key);
				this.signKey = null;
				if (privKey != null)
				{
					this.signKey = privKey.createKey();
				}
				return this.signCert != null && this.signKey != null;
			}
			return false;
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

	public boolean hasError()
	{
		if (this.host == null || this.host.length() == 0)
			return true;
		if (this.metadataPath == null || !this.metadataPath.startsWith("/"))
			return true;
		if (this.loginPath == null || !this.loginPath.startsWith("/"))
			return true;
		if (this.logoutPath == null || !this.logoutPath.startsWith("/"))
			return true;
		if (this.ssoPath == null || !this.ssoPath.startsWith("/"))
			return true;
		if (this.signCert == null)
			return true;
		if (this.signKey == null)
			return false;
		return false;
	}

	private void sendRedirect(@Nonnull HttpServletRequest req, @Nonnull HttpServletResponse resp, @Nonnull String url, @Nonnull String reqContent, @Nonnull HashType hashType) throws IOException
	{
		byte[] contentBuff = reqContent.getBytes(StandardCharsets.UTF_8);
		byte[] buff = new byte[contentBuff.length + 16];
		int buffSize;
		byte[] signBuff = new byte[512];
		MyX509Key key;
		if ((key = this.signKey) == null)
		{
			resp.sendError(StatusCode.NOT_FOUND);
			return;
		}
		Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
		deflater.setInput(contentBuff);
		deflater.finish();
		buffSize = deflater.deflate(buff);
		
		if (!deflater.finished())
		{
			System.out.println("SAMLHandler: Error in compressing content");
			resp.sendError(StatusCode.INTERNAL_SERVER_ERROR);
			return;
		}
		deflater.end();
		Base64Enc b64 = new Base64Enc();
		StringBuilderUTF8 sb = new StringBuilderUTF8();
		StringBuilderUTF8 sb2 = new StringBuilderUTF8();
	
		sb.append("SAMLRequest=");
		b64.encodeBin(sb2, buff, 0, buffSize);
		sb.append(FormEncoding.formEncode(sb2.toString()));
	
		if (hashType == HashType.SHA256)
		{
			sb.append("&SigAlg=http%3A%2F%2Fwww.w3.org%2F2001%2F04%2Fxmldsig-more%23rsa-sha256");
		}
		else if (hashType == HashType.SHA384)
		{
			sb.append("&SigAlg=http%3A%2F%2Fwww.w3.org%2F2001%2F04%2Fxmldsig-more%23rsa-sha384");
		}
		else if (hashType == HashType.SHA512)
		{
			sb.append("&SigAlg=http%3A%2F%2Fwww.w3.org%2F2001%2F04%2Fxmldsig-more%23rsa-sha512");
		}
		else
		{
			hashType = HashType.SHA1;
			sb.append("&SigAlg=http%3A%2F%2Fwww.w3.org%2F2000%2F09%2Fxmldsig%23rsa-sha1");
		}

		PrivateKey privKey = CertUtil.createPrivateKey(key);
		if (privKey == null)
		{
			System.out.println("SAMLHandler: Signature Key is not valid");
			resp.sendError(StatusCode.INTERNAL_SERVER_ERROR);
			return;
		}

		signBuff = CertUtil.signature(sb.getBytes(), 0, sb.getLength(), hashType, privKey);
		if (signBuff != null)
		{
			sb.append("&Signature=");
			sb2.clearStr();
			b64.encodeBin(sb2, signBuff, 0, signBuff.length);
			sb.append(FormEncoding.formEncode(sb2.toString()));
	
			sb2.clearStr();
			sb2.append(url);
			sb2.appendUTF8Char((byte)'?');
			sb2.append(sb.toString());
			HTTPServerUtil.redirectURL(resp, req, sb2.toString(), 0);
		}
		else
		{
			System.out.println("SAMLHandler: Error in Signature");
			resp.sendError(StatusCode.INTERNAL_SERVER_ERROR);
		}
	}

	public void doLoginGet(@Nonnull HttpServletRequest req, @Nonnull HttpServletResponse resp) throws IOException
	{
		SAMLIdpConfig idp;
		String metadataPath;
		String ssoPath;
		String s;
		if ((idp = this.idp) != null && (metadataPath = this.metadataPath) != null && (ssoPath = this.ssoPath) != null)
		{
			Timestamp currTime = DateTimeUtil.timestampNow();
			StringBuilderUTF8 sb = new StringBuilderUTF8();
			sb.append("<samlp:AuthnRequest xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\"");
			sb.append(" ID=\"SAML_");
			sb.appendI64(currTime.getTime() / 1000);
			sb.appendI32(currTime.getNanos());
			sb.appendUTF8Char((byte)'"');
			sb.append(" Version=\"2.0\"");
			sb.append(" ProviderName=");
			s = XmlUtil.toAttr(idp.getServiceDispName());
			sb.append(s);
			sb.append(" IssueInstant=\"");
			sb.append(DateTimeUtil.toStringISO8601(DateTimeUtil.clearTime(DateTimeUtil.newZonedDateTime(currTime)).withZoneSameInstant(ZoneOffset.UTC)));
			sb.appendUTF8Char((byte)'"');
			sb.append(" Destination=\"");
			sb.append(idp.getSignOnLocation());
			sb.appendUTF8Char((byte)'"');
			sb.append(" ProtocolBinding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\"");
			sb.append(" AssertionConsumerServiceURL=\"https://");
			sb.append(this.host);
			sb.append(ssoPath);
			sb.append("\">");
			sb.append("<saml:Issuer>https://");
			sb.append(this.host);
			sb.append(metadataPath);
			sb.append("</saml:Issuer>");
			sb.append("<samlp:NameIDPolicy Format=\"urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified\" AllowCreate=\"true\"/>");
			sb.append("<samlp:RequestedAuthnContext Comparison=\"exact\">");
			sb.append("<saml:AuthnContextClassRef>urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport</saml:AuthnContextClassRef>");
			sb.append("</samlp:RequestedAuthnContext>");
			sb.append("</samlp:AuthnRequest>");

			this.sendRedirect(req, resp, idp.getSignOnLocation(), sb.toString(), this.hashType);
		}
		else
		{
			resp.sendError(StatusCode.NOT_FOUND);
		}
	}

	public void doMetadataGet(HttpServletRequest req, HttpServletResponse resp)
	{
		if (this.signCert == null)
		{
			throw new IllegalArgumentException("Signature cert is not found");
		}
		Base64Enc b64 = new Base64Enc();
		StringBuilderUTF8 sb = new StringBuilderUTF8();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		sb.append("<md:EntityDescriptor xmlns:md=\"urn:oasis:names:tc:SAML:2.0:metadata\" ID=\"");
		sb.append(("https://"+this.host+this.metadataPath).replace(':', '_').replace('/', '_'));
		sb.append("\" entityID=\"");
		sb.append(("https://"+this.host+this.metadataPath));
		sb.append("\">");
		sb.append("<md:SPSSODescriptor AuthnRequestsSigned=\"true\" WantAssertionsSigned=\"true\" protocolSupportEnumeration=\"urn:oasis:names:tc:SAML:2.0:protocol\">");
		sb.append("<md:KeyDescriptor use=\"signing\">");
		sb.append("<ds:KeyInfo xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\">");
		sb.append("<ds:X509Data>");
		sb.append("<ds:X509Certificate>");
		b64.encodeBin(sb, this.signCert.getASN1Buff(), 0, this.signCert.getASN1BuffSize(), LineBreakType.CRLF, 76);
		sb.append("</ds:X509Certificate>");
		sb.append("</ds:X509Data>");
		sb.append("</ds:KeyInfo>");
		sb.append("</md:KeyDescriptor>");
		sb.append("<md:KeyDescriptor use=\"encryption\">");
		sb.append("<ds:KeyInfo xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\">");
		sb.append("<ds:X509Data>");
		sb.append("<ds:X509Certificate>");
		b64.encodeBin(sb, this.signCert.getASN1Buff(), 0, this.signCert.getASN1BuffSize(), LineBreakType.CRLF, 76);
		sb.append("</ds:X509Certificate>");
		sb.append("</ds:X509Data>");
		sb.append("</ds:KeyInfo>");
		sb.append("</md:KeyDescriptor>");
		sb.append("<md:SingleLogoutService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\" Location=\"https://"+this.host+this.logoutPath+"\"/>");
		sb.append("<md:SingleLogoutService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect\" Location=\"https://"+this.host+this.logoutPath+"\"/>");
		sb.append("<md:NameIDFormat>");
		sb.append("urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress");
		sb.append("</md:NameIDFormat>");
		sb.append("<md:NameIDFormat>");
		sb.append("urn:oasis:names:tc:SAML:2.0:nameid-format:transient");
		sb.append("</md:NameIDFormat>");
		sb.append("<md:NameIDFormat>");
		sb.append("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent");
		sb.append("</md:NameIDFormat>");
		sb.append("<md:NameIDFormat>");
		sb.append("urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified");
		sb.append("</md:NameIDFormat>");
		sb.append("<md:NameIDFormat>");
		sb.append("urn:oasis:names:tc:SAML:1.1:nameid-format:X509SubjectName");
		sb.append("</md:NameIDFormat>");
		sb.append("<md:AssertionConsumerService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\" Location=\"https://"+this.host+this.ssoPath+"\" index=\"0\" isDefault=\"true\"/>");
		sb.append("<md:AssertionConsumerService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect\" Location=\"https://"+this.host+this.ssoPath+"\" index=\"1\"/>");
		sb.append("</md:SPSSODescriptor>");
		sb.append("</md:EntityDescriptor>");

		this.addResponseHeaders(req, resp);
		resp.addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		resp.addHeader("Content-Type", "application/samlmetadata+xml");
		HTTPServerUtil.sendContent(req, resp, "application/samlmetadata+xml", sb.toString().getBytes());
	}

	@Nonnull
	public SAMLSSOResponse doSSOPost(HttpServletRequest req, HttpServletResponse resp)
	{
		String s = req.getParameter("SAMLResponse");
		if (s != null)
		{
			Base64Enc b64 = new Base64Enc();
			byte[] buff = b64.decodeBin(s);

			String decMsg = null;
			PrivateKey key;
			if (this.signKey == null)
			{
				return new SAMLSSOResponse("Sign Key not exists", null);
			}
			if ((key = CertUtil.createPrivateKey(this.signKey)) == null)
			{
				return new SAMLSSOResponse("Sign Key is not Private Key", null);
			}
			StringBuilderUTF8 sb = new StringBuilderUTF8();
			if (SAMLUtil.decryptResponse(this.encFact, key, buff, sb))
			{
				return new SAMLSSOResponse(sb.toString(), null);
			}
			else
			{
				return new SAMLSSOResponse("Failed in decrypting response message", null);
			}
		}
		else
		{
			return new SAMLSSOResponse("SAMLResponse not found", null);
		}
	}
}
