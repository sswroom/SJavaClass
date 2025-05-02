package org.sswr.util.net;

import org.sswr.util.crypto.MyX509Cert;
import org.sswr.util.crypto.MyX509Key;
import org.sswr.util.data.LineBreakType;
import org.sswr.util.data.textbinenc.Base64Enc;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class SAMLHandler {
	private String host;
	private String loginPath;
	private String logoutPath;
	private String ssoPath;
	private String metadataPath;
	private String allowOrigin;
	private String contentSecurityPolicy;
	private MyX509Cert cert;
	private MyX509Key key;

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
	}

	public void setAllowOrigin(String allowOrigin)
	{
		this.allowOrigin = allowOrigin;
	}

	public void setContentSecurityPolicy(String contentSecurityPolicy)
	{
		this.contentSecurityPolicy = contentSecurityPolicy;
	}

	public void doMetadataGet(HttpServletRequest req, HttpServletResponse resp)
	{
		if (cert == null)
		{
			throw new IllegalArgumentException("Signature cert is not found");
		}
		Base64Enc b64 = new Base64Enc();
		StringBuilder sb = new StringBuilder();
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
		b64.encodeBin(sb, cert.getASN1Buff(), 0, cert.getASN1BuffSize(), LineBreakType.CRLF, 76);
		sb.append("</ds:X509Certificate>");
		sb.append("</ds:X509Data>");
		sb.append("</ds:KeyInfo>");
		sb.append("</md:KeyDescriptor>");
		sb.append("<md:KeyDescriptor use=\"encryption\">");
		sb.append("<ds:KeyInfo xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\">");
		sb.append("<ds:X509Data>");
		sb.append("<ds:X509Certificate>");
		b64.encodeBin(sb, cert.getASN1Buff(), 0, cert.getASN1BuffSize(), LineBreakType.CRLF, 76);
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
}
