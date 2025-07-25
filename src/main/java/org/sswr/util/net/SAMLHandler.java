package org.sswr.util.net;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.sswr.util.crypto.cert.CertUtil;
import org.sswr.util.crypto.cert.MyX509Cert;
import org.sswr.util.crypto.cert.MyX509File;
import org.sswr.util.crypto.cert.MyX509Key;
import org.sswr.util.crypto.cert.MyX509PrivKey;
import org.sswr.util.crypto.cert.MyX509File.FileType;
import org.sswr.util.crypto.hash.HashType;
import org.sswr.util.data.ByteArray;
import org.sswr.util.data.ByteTool;
import org.sswr.util.data.DateTimeUtil;
import org.sswr.util.data.EncodingFactory;
import org.sswr.util.data.LineBreakType;
import org.sswr.util.data.SharedObject;
import org.sswr.util.data.StaticByteArray;
import org.sswr.util.data.StringBuilderUTF8;
import org.sswr.util.data.StringUtil;
import org.sswr.util.data.XMLAttrib;
import org.sswr.util.data.XMLReader;
import org.sswr.util.data.XmlUtil;
import org.sswr.util.data.XMLReader.ParseMode;
import org.sswr.util.data.textbinenc.Base64Enc;
import org.sswr.util.data.textenc.FormEncoding;
import org.sswr.util.io.IOStream;
import org.sswr.util.io.MemoryReadingStream;
import org.sswr.util.io.MemoryStream;
import org.sswr.util.io.ParsedObject;
import org.sswr.util.io.ParserType;
import org.sswr.util.parser.X509Parser;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class SAMLHandler {
	public static enum ResponseError
	{
		Success,
		ResponseNotFound,
		IDPMissing,
		SignKeyMissing,
		SignKeyInvalid,
		ResponseFormatError,
		DecryptFailed,
		UnexpectedIssuer,
		TimeOutOfRange,
		InvalidAudience,
		UsernameMissing,
		InvalidSignature
	}

	public static enum RespProcessError
	{
		Success,
		IDPMissing,
		SSLMissing,
		ParamMissing,
		SigAlgNotSupported,
		QueryStringError,
		KeyError,
		SignatureInvalid,
		MessageInvalid,
		StatusError,
		DataError
	}

	public static enum ReqProcessError
	{
		Success,
		IDPMissing,
		SSLMissing,
		ParamMissing,
		SigAlgNotSupported,
		QueryStringError,
		KeyError,
		SignatureInvalid,
		MessageInvalid,
		DestinationInvalid,
		IssuerInvalid,
		TimeInvalid
	}

	public static enum SAMLSignError
	{
		Valid,
		SignatureInvalid,
		CertMissing,
		SignatureMissing,
		SigAlgMissing,
		SigAlgNotSupported,
		QueryStringGetError,
		QueryStringSearchError,
		KeyError
	}

	public static enum SAMLAuthMethod
	{
		Unknown,
		Password,
		PasswordProtectedTransport,
		TLSClient,
		X509,
		WindowsAuth,
		Kerberos,
		PGP,
		SecureRemotePassword,
		XMLDSig,
		SPKI,
		Smartcard,
		SmartcardPKI;

		@Nonnull
		public static String getString(@Nonnull SAMLAuthMethod authMethod)
		{
			switch (authMethod)
			{
				case Kerberos:
					return "urn:oasis:names:tc:SAML:2.0:ac:classes:Kerberos";
				case Password:
					return "urn:oasis:names:tc:SAML:2.0:ac:classes:Password";
				case PasswordProtectedTransport:
					return "urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport";
				case TLSClient:
					return "urn:oasis:names:tc:SAML:2.0:ac:classes:TLSClient";
				case WindowsAuth:
					return "urn:federation:authentication:windows";
				case X509:
					return "urn:oasis:names:tc:SAML:2.0:ac:classes:X509";
				case PGP:
					return "urn:oasis:names:tc:SAML:2.0:ac:classes:PGP";
				case SecureRemotePassword:
					return "urn:oasis:names:tc:SAML:2.0:ac:classes:SecureRemotePassword";
				case XMLDSig:
					return "urn:oasis:names:tc:SAML:2.0:ac:classes:XMLDSig";
				case SPKI:
					return "urn:oasis:names:tc:SAML:2.0:ac:classes:SPKI";
				case Smartcard:
					return "urn:oasis:names:tc:SAML:2.0:ac:classes:Smartcard";
				case SmartcardPKI:
					return "urn:oasis:names:tc:SAML:2.0:ac:classes:SmartcardPKI";
		
				case Unknown:
				default:
					return "urn:oasis:names:tc:SAML:2.0:ac:classes:unspecified";
			}
		}

		@Nonnull
		public static SAMLAuthMethod fromString(@Nonnull String val)
		{
			switch (val)
			{
			case "urn:oasis:names:tc:SAML:2.0:ac:classes:Password":
				return SAMLAuthMethod.Password;
			case "urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport":
				return SAMLAuthMethod.PasswordProtectedTransport;
			case "urn:oasis:names:tc:SAML:2.0:ac:classes:TLSClient":
				return SAMLAuthMethod.TLSClient;
			case "urn:oasis:names:tc:SAML:2.0:ac:classes:X509":
				return SAMLAuthMethod.X509;
			case "urn:federation:authentication:windows":
				return SAMLAuthMethod.WindowsAuth;
			case "urn:oasis:names:tc:SAML:2.0:ac:classes:Kerberos":
				return SAMLAuthMethod.Kerberos;
			case "urn:oasis:names:tc:SAML:2.0:ac:classes:PGP":
				return SAMLAuthMethod.PGP;
			case "urn:oasis:names:tc:SAML:2.0:ac:classes:SecureRemotePassword":
				return SAMLAuthMethod.SecureRemotePassword;
			case "urn:oasis:names:tc:SAML:2.0:ac:classes:XMLDSig":
				return SAMLAuthMethod.XMLDSig;
			case "urn:oasis:names:tc:SAML:2.0:ac:classes:SPKI":
				return SAMLAuthMethod.SPKI;
			case "urn:oasis:names:tc:SAML:2.0:ac:classes:Smartcard":
				return SAMLAuthMethod.Smartcard;
			case "urn:oasis:names:tc:SAML:2.0:ac:classes:SmartcardPKI":
				return SAMLAuthMethod.SmartcardPKI;
			default:
				return SAMLAuthMethod.Unknown;
			}
		}
	}

	public static enum SAMLStatusCode
	{
		Unknown,
		Success,
		Requester,
		Responder,
		VersionMismatch,
		AuthnFailed,
		InvalidAttrNameOrValue,
		InvalidNameIDPolicy,
		NoAuthnContext,
		NoAvailableIDP,
		NoPassive,
		NoSupportedIDP,
		PartialLogout,
		ProxyCountExceeded,
		RequestDenied,
		RequestUnsupported,
		RequestVersionDeprecated,
		RequestVersionTooHigh,
		RequestVersionTooLow,
		ResourceNotRecognized,
		TooManyResponses,
		UnknownAttrProfile,
		UnknownPrincipal,
		UnsupportedBinding;

		public static SAMLStatusCode fromString(@Nonnull String s)
		{
			switch (s)
			{
			case "urn:oasis:names:tc:SAML:2.0:status:Success":
				return SAMLStatusCode.Success;
			case "urn:oasis:names:tc:SAML:2.0:status:Requester":
				return SAMLStatusCode.Requester;
			case "urn:oasis:names:tc:SAML:2.0:status:Responder":
				return SAMLStatusCode.Responder;
			case "urn:oasis:names:tc:SAML:2.0:status:VersionMismatch":
				return SAMLStatusCode.VersionMismatch;
			case "urn:oasis:names:tc:SAML:2.0:status:AuthnFailed":
				return SAMLStatusCode.AuthnFailed;
			case "urn:oasis:names:tc:SAML:2.0:status:InvalidAttrNameOrValue":
				return SAMLStatusCode.InvalidAttrNameOrValue;
			case "urn:oasis:names:tc:SAML:2.0:status:InvalidNameIDPolicy":
				return SAMLStatusCode.InvalidNameIDPolicy;
			case "urn:oasis:names:tc:SAML:2.0:status:NoAuthnContext":
				return SAMLStatusCode.NoAuthnContext;
			case "urn:oasis:names:tc:SAML:2.0:status:NoAvailableIDP":
				return SAMLStatusCode.NoAvailableIDP;
			case "urn:oasis:names:tc:SAML:2.0:status:NoPassive":
				return SAMLStatusCode.NoPassive;
			case "urn:oasis:names:tc:SAML:2.0:status:NoSupportedIDP":
				return SAMLStatusCode.NoSupportedIDP;
			case "urn:oasis:names:tc:SAML:2.0:status:PartialLogout":
				return SAMLStatusCode.PartialLogout;
			case "urn:oasis:names:tc:SAML:2.0:status:ProxyCountExceeded":
				return SAMLStatusCode.ProxyCountExceeded;
			case "urn:oasis:names:tc:SAML:2.0:status:RequestDenied":
				return SAMLStatusCode.RequestDenied;
			case "urn:oasis:names:tc:SAML:2.0:status:RequestUnsupported":
				return SAMLStatusCode.RequestUnsupported;
			case "urn:oasis:names:tc:SAML:2.0:status:RequestVersionDeprecated":
				return SAMLStatusCode.RequestVersionDeprecated;
			case "urn:oasis:names:tc:SAML:2.0:status:RequestVersionTooHigh":
				return SAMLStatusCode.RequestVersionTooHigh;
			case "urn:oasis:names:tc:SAML:2.0:status:RequestVersionTooLow":
				return SAMLStatusCode.RequestVersionTooLow;
			case "urn:oasis:names:tc:SAML:2.0:status:ResourceNotRecognized":
				return SAMLStatusCode.ResourceNotRecognized;
			case "urn:oasis:names:tc:SAML:2.0:status:TooManyResponses":
				return SAMLStatusCode.TooManyResponses;
			case "urn:oasis:names:tc:SAML:2.0:status:UnknownAttrProfile":
				return SAMLStatusCode.UnknownAttrProfile;
			case "urn:oasis:names:tc:SAML:2.0:status:UnknownPrincipal":
				return SAMLStatusCode.UnknownPrincipal;
			case "urn:oasis:names:tc:SAML:2.0:status:UnsupportedBinding":
				return SAMLStatusCode.UnsupportedBinding;
			default:
				return SAMLStatusCode.Unknown;
			}
		}

		@Nonnull
		public static String getString(@Nonnull SAMLStatusCode status)
		{
			return "urn:oasis:names:tc:SAML:2.0:status:"+status.toString();
		}
	}

	public static class SAMLSSOResponse
	{
		@Nonnull
		public ResponseError error;
		@Nonnull
		public SAMLStatusCode status;
		@Nonnull
		public String errorMessage;
		@Nullable
		public String rawResponse;
		@Nullable
		public String decResponse;
		@Nullable
		public String id;
		@Nullable
		public String sessionIndex;
		@Nullable
		public String issuer;
		@Nullable
		public String audience;
		@Nullable
		public Timestamp notBefore;
		@Nullable
		public Timestamp notOnOrAfter;
		@Nullable
		public String nameID;
		@Nullable
		public String name;
		@Nullable
		public String givenname;
		@Nullable
		public String surname;
		@Nullable
		public String emailAddress;

		public SAMLSSOResponse(@Nonnull ResponseError error, @Nonnull String errorMessage)
		{
			this.error = error;
			this.errorMessage = errorMessage;
			this.status = SAMLStatusCode.Unknown;
		}
	}

	public static class SAMLLogoutResponse
	{
		@Nonnull
		public RespProcessError error;
		@Nonnull 
		public String errorMessage;
		@Nonnull
		public SAMLStatusCode status;
		@Nullable
		public String rawResponse;

		public SAMLLogoutResponse(@Nonnull RespProcessError error, @Nonnull String errorMessage)
		{
			this.error = error;
			this.errorMessage = errorMessage;
			this.status = SAMLStatusCode.Unknown;
		}
	}

	public static class SAMLLogoutRequest
	{
		@Nonnull
		public ReqProcessError error;
		@Nonnull 
		public String errorMessage;
		@Nullable
		public String rawResponse;
		@Nullable
		public String id;
		@Nullable
		public String nameId;
		@Nonnull 
		public ArrayList<String> sessionIndex;

		public SAMLLogoutRequest(@Nonnull ReqProcessError error, @Nonnull String errorMessage)
		{
			this.error = error;
			this.errorMessage = errorMessage;
			this.sessionIndex = new ArrayList<String>();
		}
	}

	private SAMLAuthMethod[] authMethods;
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

	@Nullable
	private String buildRedirectUrl(@Nonnull String url, @Nonnull ByteArray reqContent, @Nonnull HashType hashType, boolean response)
	{
		byte[] buff = new byte[reqContent.getBytesLength() + 16];
		int buffSize;
		byte[] signBuff = new byte[512];
		MyX509Key key;
		if ((key = this.signKey) == null)
		{
			System.out.println("SAMLHandler: signKey is null");
			return null;
		}
		Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION, true);
		deflater.setInput(reqContent.getBytes(), reqContent.getBytesOffset(), reqContent.getBytesLength());
		deflater.finish();
		buffSize = deflater.deflate(buff);
		
		if (!deflater.finished())
		{
			System.out.println("SAMLHandler: Error in compressing content");
			return null;
		}
		deflater.end();
		Base64Enc b64 = new Base64Enc();
		StringBuilderUTF8 sb = new StringBuilderUTF8();
		StringBuilderUTF8 sb2 = new StringBuilderUTF8();
	
		if (response)
		{
			sb.append("SAMLResponse=");
		}
		else
		{
			sb.append("SAMLRequest=");
		}
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
			return null;
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
			return sb2.toString();
		}
		else
		{
			System.out.println("SAMLHandler: Error in Signature");
			return null;
		}
	}

	@Nonnull
	private static SAMLSignError verifyHTTPRedirect(@Nonnull SAMLIdpConfig idp, @Nonnull HttpServletRequest req)
	{
		byte[] sbuff;
		String signature;
		String sigAlg;
		MyX509Cert signCert;

		if ((signCert = idp.getSigningCert()) == null)
		{
			return SAMLSignError.CertMissing;
		}
		if ((signature = req.getParameter("Signature")) == null)
		{
			return SAMLSignError.SignatureMissing;
		}
		if ((sigAlg = req.getParameter("SigAlg")) == null)
		{
			return SAMLSignError.SigAlgMissing;
		}
		HashType hashType = HashType.Unknown;
		if (sigAlg.equals("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256"))
		{
			hashType = HashType.SHA256;
		}
		else if (sigAlg.equals("http://www.w3.org/2001/04/xmldsig-more#rsa-sha384"))
		{
			hashType = HashType.SHA384;
		}
		else if (sigAlg.equals("http://www.w3.org/2001/04/xmldsig-more#rsa-sha512"))
		{
			hashType = HashType.SHA512;
		}
		else if (sigAlg.equals("http://www.w3.org/2000/09/xmldsig#rsa-sha1"))
		{
			hashType = HashType.SHA1;
		}
		else
		{
			return SAMLSignError.SigAlgNotSupported;
		}
		String qs = req.getQueryString();
		sbuff = qs.getBytes(StandardCharsets.UTF_8);
		int i = StringUtil.indexOfUTF8(sbuff, 0, sbuff.length, "&Signature=");
		if (i == -1)
		{
			return SAMLSignError.QueryStringSearchError;
		}
		int j = StringUtil.indexOfUTF8(sbuff, 0, sbuff.length, "&SigAlg=");
		if (j != -1 && j > i)
		{
			ByteTool.copyArray(sbuff, i, sbuff, j, sbuff.length - j);
			i += sbuff.length - j;
		}
		Base64Enc b64 = new Base64Enc();
		byte[] signBuff = b64.decodeBin(signature);
		MyX509Key key;
		if ((key = signCert.getNewPublicKey()) == null)
		{
			return SAMLSignError.KeyError;
		}
		PublicKey pubKey = key.createJPublicKey();
		if (pubKey == null)
		{
			return SAMLSignError.KeyError;
		}
		StringBuilderUTF8 sbError = new StringBuilderUTF8();
		if (!CertUtil.verifySign(sbuff, 0, i, signBuff, 0, signBuff.length, pubKey, hashType, sbError, null))
		{
			System.out.println(sbError.toString());
			return SAMLSignError.SignatureInvalid;
		}
		return SAMLSignError.Valid;
	}

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

	private void parseSAMLLogoutResponse(@Nonnull SAMLLogoutResponse saml, @Nonnull IOStream stm)
	{
		XMLAttrib attr;
		String cs;
		String s;
		int i;
		XMLReader reader = new XMLReader(this.encFact, stm, ParseMode.XML);
		if ((cs = reader.nextElementName2()) != null && cs.equals("LogoutResponse"))
		{
			while ((cs = reader.nextElementName2()) != null)
			{
				if (cs.equals("Status"))
				{
					while ((cs = reader.nextElementName2()) != null)
					{
						if (cs.equals("StatusCode"))
						{
							i = reader.getAttribCount();
							while (i-- > 0)
							{
								attr = reader.getAttribNoCheck(i);
								if ((s = attr.name) != null && s.equals("Value"))
								{
									if ((s = attr.value) != null)
									{
										saml.status = SAMLStatusCode.fromString(s);
									}
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
		}
	}
	
	private void parseSAMLLogoutRequest(@Nonnull SAMLLogoutRequest saml, @Nonnull IOStream stm)
	{
		XMLAttrib attr;
		String cs;
		String s;
		int i;
		XMLReader reader = new XMLReader(this.encFact, stm, ParseMode.XML);
		if ((cs = reader.nextElementName2()) != null && cs.equals("LogoutRequest"))
		{
			StringBuilderUTF8 sb = new StringBuilderUTF8();
			i = reader.getAttribCount();
			while (i-- > 0)
			{
				attr = reader.getAttribNoCheck(i);
				if ((s = attr.name) != null)
				{
					if (s.equals("ID"))
					{
						saml.id = attr.value;
					}
					else if (s.equals("Destination"))
					{
						if ((s = attr.value) != null)
						{
							sb.clearStr();
							sb.append("https://");
							sb.appendOpt(this.host);
							sb.appendOpt(this.logoutPath);
							if (!sb.toString().equals(s))
							{
								saml.error = ReqProcessError.DestinationInvalid;
								saml.errorMessage = "Destination is not valid: " + s;
								return;
							}
						}
					}
					else if (s.equals("NotOnOrAfter"))
					{
						if ((s = attr.value) != null)
						{
							ZonedDateTime notOnOrAfter = DateTimeUtil.parse(s);
							if (notOnOrAfter != null && ZonedDateTime.now().compareTo(notOnOrAfter) >= 0)
							{
								saml.error = ReqProcessError.TimeInvalid;
								saml.errorMessage = "Current time is >= NotOnOrAfter";
								return;
							}
						}
					}
				}
			}
			while ((cs = reader.nextElementName2()) != null)
			{
				if (cs.equals("Issuer"))
				{
					sb.clearStr();
					reader.readNodeText(sb);
					SAMLIdpConfig idp;
					if ((idp = this.idp) != null)
					{
						if (!idp.getEntityId().equals(sb.toString()))
						{
							saml.error = ReqProcessError.IssuerInvalid;
							saml.errorMessage = "Issuer is not same as IDP";
							return;
						}
					}
				}
				else if (cs.equals("NameID"))
				{
					sb.clearStr();
					reader.readNodeText(sb);
					saml.nameId = sb.toString();
				}
				else if (cs.equals("SessionIndex"))
				{
					sb.clearStr();
					reader.readNodeText(sb);
					saml.sessionIndex.add(sb.toString());
				}
				else
				{
					reader.skipElement();
				}
			}
		}
		else
		{
			saml.error = ReqProcessError.MessageInvalid;
			saml.errorMessage = "XML element is not LogoutRequest";
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
		this.authMethods = new SAMLAuthMethod[]{SAMLAuthMethod.Unknown, SAMLAuthMethod.Password, SAMLAuthMethod.PasswordProtectedTransport, SAMLAuthMethod.WindowsAuth, SAMLAuthMethod.Kerberos};
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

	public void setAuthMethod(@Nonnull SAMLAuthMethod authMethod)
	{
		this.authMethods = new SAMLAuthMethod[]{authMethod};
	}

	public void setAuthMethods(@Nonnull SAMLAuthMethod[] authMethods)
	{
		this.authMethods = new SAMLAuthMethod[authMethods.length];
		ByteTool.copyArray(this.authMethods, 0, authMethods, 0, authMethods.length);
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

	@Nullable
	public String getLoginUrl()
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
			s = XmlUtil.toAttrText(idp.getServiceDispName());
			sb.append(s);
			sb.append(" IssueInstant=\"");
			sb.append(DateTimeUtil.clearMs(currTime).toInstant().toString());
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
			if (this.authMethods.length == 1)
			{
				sb.append("<samlp:RequestedAuthnContext Comparison=\"exact\">");
				sb.append("<saml:AuthnContextClassRef>");
				sb.append(SAMLAuthMethod.getString(this.authMethods[0]));
				sb.append("</saml:AuthnContextClassRef>");
				sb.append("</samlp:RequestedAuthnContext>");
			}
			else
			{
				sb.append("<samlp:RequestedAuthnContext Comparison=\"minimum\">");
				int i = 0;
				int j = this.authMethods.length;
				while (i < j)
				{
					sb.append("<saml:AuthnContextClassRef>");
					sb.append(SAMLAuthMethod.getString(this.authMethods[i]));
					sb.append("</saml:AuthnContextClassRef>");
					i++;
				}
				sb.append("</samlp:RequestedAuthnContext>");
			}
			sb.append("</samlp:AuthnRequest>");
			return this.buildRedirectUrl(idp.getSignOnLocation(), sb, this.hashType, false);
		}
		else
		{
			return null;
		}
	}

	@Nullable
	public String getLogoutUrl(@Nullable String nameID, @Nullable String sessionIndex)
	{
		SAMLIdpConfig idp;
		String metadataPath;
		if ((idp = this.idp) != null && (metadataPath = this.metadataPath) != null)
		{
			Timestamp currTime = DateTimeUtil.timestampNow();
			StringBuilderUTF8 sb = new StringBuilderUTF8();
			sb.append("<samlp:LogoutRequest xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\"");
			sb.append(" ID=\"SAML_");
			sb.appendI64(currTime.getTime() / 1000);
			sb.appendU32(currTime.getNanos());
			sb.appendUTF8Char((byte)'"');
			sb.append(" Version=\"2.0\"");
			sb.append(" IssueInstant=\"");
			sb.append(DateTimeUtil.clearMs(currTime).toInstant().toString());
			sb.appendUTF8Char((byte)'"');
			sb.append(" Destination=\"");
			sb.append(idp.getLogoutLocation());
			sb.appendUTF8Char((byte)'"');
			sb.append(">");
			sb.append("<saml:Issuer>https://");
			sb.append(this.host);
			sb.append(metadataPath);
			sb.append("</saml:Issuer>");
			if (nameID != null)
			{
				sb.append("<saml:NameID>");
				sb.append(XmlUtil.toAttr(nameID));
				sb.append("</saml:NameID>");
			}
			else
			{
				sb.append("<saml:NameID/>");
			}
			if (sessionIndex != null)
			{
				sb.append("<samlp:SessionIndex>");
				sb.append(XmlUtil.toAttr(sessionIndex));
				sb.append("</samlp:SessionIndex>");
			}
			sb.append("</samlp:LogoutRequest>");

			return this.buildRedirectUrl(idp.getLogoutLocation(), sb, this.hashType, false);
		}
		else
		{
			return null;
		}
	}

	@Nullable
	public String getLogoutResponseUrl(@Nonnull String msgId, @Nonnull SAMLStatusCode status)
	{
		StringBuilderUTF8 sb = new StringBuilderUTF8();
		this.getLogoutResponse(sb, msgId, status);
		return this.buildRedirectUrl(idp.getLogoutLocation(), sb, this.hashType, true);
	}

	@Nullable
	public StringBuilderUTF8 getMetadataXML()
	{
		if (this.signCert == null)
		{
			return null;
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
		sb.append("<md:SingleLogoutService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect\" Location=\"https://"+this.host+this.logoutPath+"\"/>");
		sb.append("<md:SingleLogoutService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\" Location=\"https://"+this.host+this.logoutPath+"\"/>");
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
		return sb;
	}

	public boolean getLogoutResponse(@Nonnull StringBuilderUTF8 sb, @Nonnull String id, @Nonnull SAMLStatusCode status)
	{
		String metadataPath;
		String serverHost;
		SAMLIdpConfig idp;
		if ((serverHost = this.host) != null && (metadataPath = this.metadataPath) != null && (idp = this.idp) != null)
		{
			Timestamp currTime = DateTimeUtil.timestampNow();
			sb.append("<samlp:LogoutResponse");
			sb.append(" ID=\"SAML_");
			sb.appendI64(currTime.getTime() / 1000);
			sb.appendU32(currTime.getNanos());
			sb.appendUTF8Char((byte)'"');
			sb.append(" Version=\"2.0\"");
			sb.append(" IssueInstant=\"");
			sb.append(DateTimeUtil.clearMs(currTime).toInstant().toString());
			sb.appendUTF8Char((byte)'"');
			sb.append(" InResponseTo=\"");
			sb.append(id);
			sb.appendUTF8Char((byte)'"');
			sb.append(" Destination=\"");
			sb.append(idp.getLogoutLocation());
			sb.appendUTF8Char((byte)'"');
			sb.append(" xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" xmlns=\"urn:oasis:names:tc:SAML:2.0:assertion\">");
			sb.append("<Issuer>https://");
			sb.append(serverHost);
			sb.append(metadataPath);
			sb.append("</Issuer>");
			sb.append("<samlp:Status>");
			sb.append("<samlp:StatusCode Value=\"");
			sb.append(SAMLStatusCode.getString(status));
			sb.append("\" />");
			sb.append("</samlp:Status>");
			sb.append("</samlp:LogoutResponse>");
			return true;
		}
		return false;
	}

	public void doLoginGet(@Nonnull HttpServletRequest req, @Nonnull HttpServletResponse resp) throws IOException
	{
		String url = getLoginUrl();
		if (url != null)
		{
			HTTPServerUtil.redirectURL(resp, req, url, 0);
		}
		else
		{
			resp.sendError(StatusCode.INTERNAL_SERVER_ERROR);
		}
	}

	public void doLogoutGet(@Nonnull HttpServletRequest req, @Nonnull HttpServletResponse resp, @Nullable String nameID, @Nullable String sessionId) throws IOException
	{
		String s;
		if (req.getParameter("SAMLResponse") != null)
		{
			SAMLLogoutResponse msg = this.doLogoutResp(req, resp);
			StringBuilderUTF8 sb = new StringBuilderUTF8();
			StringBuilderUTF8 sb2 = new StringBuilderUTF8();
			sb.clearStr();
			sb.append("<html><head><title>Logout Message</title></head><body>");
			sb.append("<h1>Result</h1>");
			sb.append("<font color=\"red\">Error:</font> ");
			sb.append(msg.error.toString());
			sb.append("<br/>");
			sb.append("<font color=\"red\">Error Message:</font> ");
			sb.append(msg.errorMessage);
			sb.append("<br/>");
			sb.append("<font color=\"red\">Status:</font> ");
			sb.append(msg.status.toString());
			sb.append("<br/>");
			if ((s = msg.rawResponse) != null)
			{
				sb.append("<hr/>");
				sb.append("<h1>RAW Response</h1>");
				MemoryReadingStream mstm = new MemoryReadingStream(new StaticByteArray(s.getBytes(StandardCharsets.UTF_8)));
				XMLReader.xmlWellFormat(this.encFact, mstm, 0, sb2);
				s = XmlUtil.toHTMLTextXMLColor(sb2.toString());
				sb.append(s);
			}
			sb.append("</body></html>");
			HTTPServerUtil.addDefHeaders(resp, req);
			HTTPServerUtil.addCacheControl(resp, 0);
			HTTPServerUtil.addContentType(resp, "text/html");
			HTTPServerUtil.sendContent(req, resp, "text/html", sb);
			return;
		}
		else if (req.getParameter("SAMLRequest") != null)
		{
			SAMLLogoutRequest msg = this.doLogoutReq(req, resp);
			SAMLIdpConfig idp;
			if (msg.error == ReqProcessError.Success && (s = msg.id) != null && (idp = this.idp) != null)
			{
				StringBuilderUTF8 sb = new StringBuilderUTF8();
				this.getLogoutResponse(sb, s, SAMLStatusCode.Success);
				String url = this.buildRedirectUrl(idp.getLogoutLocation(), sb, this.hashType, true);
				if (url != null)
				{
					HTTPServerUtil.redirectURL(resp, req, url, 0);
					return;
				}
			}
			StringBuilderUTF8 sb = new StringBuilderUTF8();
			StringBuilderUTF8 sb2 = new StringBuilderUTF8();
			sb.clearStr();
			sb.append("<html><head><title>Logout Response</title></head><body>");
			sb.append("<h1>Result</h1>");
			sb.append("<font color=\"red\">Error:</font> ");
			sb.append(msg.error.toString());
			sb.append("<br/>");
			sb.append("<font color=\"red\">Error Message:</font> ");
			sb.append(msg.errorMessage);
			sb.append("<br/>");
			sb.append("<font color=\"red\">ID:</font> ");
			sb.appendOpt(msg.id);
			sb.append("<br/>");
			sb.append("<font color=\"red\">NameID:</font> ");
			sb.appendOpt(msg.nameId);
			sb.append("<br/>");
			sb.append("<font color=\"red\">SessionIndex:</font> ");
			ArrayList<String> sessionIndex = msg.sessionIndex;
			int i = 0;
			int j = sessionIndex.size();
			while (i < j)
			{
				if (i > 0) sb.append("<br/>");
				sb.append(sessionIndex.get(i));
				i++;
			}
			sb.append("<br/>");
			if ((s = msg.rawResponse) != null)
			{
				sb.append("<hr/>");
				sb.append("<h1>RAW Response</h1>");
				MemoryReadingStream mstm = new MemoryReadingStream(new StaticByteArray(s.getBytes(StandardCharsets.UTF_8)));
				XMLReader.xmlWellFormat(this.encFact, mstm, 0, sb2);
				s = XmlUtil.toHTMLTextXMLColor(sb2.toString());
				sb.append(s);
			}
			sb.append("</body></html>");
			HTTPServerUtil.addDefHeaders(resp, req);
			HTTPServerUtil.addCacheControl(resp, 0);
			HTTPServerUtil.addContentType(resp, "text/html");
			HTTPServerUtil.sendContent(req, resp, "text/html", sb);
			return;
		}
		if ((s = this.getLogoutUrl(nameID, sessionId)) != null)
		{
			resp.sendRedirect(s);
		}
		else
		{
			resp.sendError(StatusCode.NOT_FOUND);
		}
	}

	public void doLogoutPost(@Nonnull HttpServletRequest req, @Nonnull HttpServletResponse resp)
	{

	}

	public void doMetadataGet(@Nonnull HttpServletRequest req, @Nonnull HttpServletResponse resp)
	{
		StringBuilderUTF8 sb = this.getMetadataXML();
		if (sb == null)
		{
			throw new IllegalArgumentException("Signature cert is not found");
		}
		this.addResponseHeaders(req, resp);
		resp.addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		resp.addHeader("Content-Type", "application/samlmetadata+xml");
		HTTPServerUtil.sendContent(req, resp, "application/samlmetadata+xml", sb.toString().getBytes());
	}

	@Nonnull
	public SAMLSSOResponse doSSOPost(@Nonnull HttpServletRequest req, @Nonnull HttpServletResponse resp)
	{
		int i;
		XMLAttrib attr;
		String s = req.getParameter("SAMLResponse");
		if (this.idp == null)
		{
			return new SAMLSSOResponse(ResponseError.IDPMissing, "Idp Config missing");
		}
		else if (s != null)
		{
			Base64Enc b64 = new Base64Enc();
			byte[] buff = b64.decodeBin(s);
			SAMLSSOResponse saml;

			String decMsg = null;
			PrivateKey key;
			if (this.signKey == null)
			{
				saml = new SAMLSSOResponse(ResponseError.SignKeyMissing, "Sign Key not exists");
				saml.rawResponse = new String(buff, StandardCharsets.UTF_8);
				return saml;
			}
			if ((key = CertUtil.createPrivateKey(this.signKey)) == null)
			{
				saml = new SAMLSSOResponse(ResponseError.SignKeyInvalid, "Sign Key is not Private Key");
				saml.rawResponse = new String(buff, StandardCharsets.UTF_8);
				return saml;
			}
			StringBuilderUTF8 sb = new StringBuilderUTF8();
			SharedObject<SAMLStatusCode> statusCode = new SharedObject<SAMLStatusCode>();
			statusCode.value = SAMLStatusCode.Unknown;
			if (SAMLUtil.decryptResponse(this.encFact, key, buff, sb, statusCode))
			{
				decMsg = sb.toString();
				saml = new SAMLSSOResponse(ResponseError.Success, "Decrypted");
				saml.rawResponse = new String(buff, StandardCharsets.UTF_8);
				saml.decResponse = decMsg;
				saml.status = statusCode.value;
				MemoryReadingStream mstm = new MemoryReadingStream(sb);
				XMLReader reader = new XMLReader(this.encFact, mstm, ParseMode.XML);
				try
				{
					if ((s = reader.nextElementName2()) != null && s.equals("Assertion"))
					{
						StringBuilderUTF8 sbTmp = new StringBuilderUTF8();
						i = reader.getAttribCount();
						while (i-- > 0)
						{
							attr = reader.getAttribNoCheck(i);
							if (attr.name != null && attr.name.equals("ID"))
							{
								saml.id = attr.value;
							}
						}
						while ((s = reader.nextElementName()) != null)
						{
							if (s.equals("Issuer"))
							{
								sbTmp.clearStr();
								reader.readNodeText(sbTmp);
								saml.issuer = sbTmp.toString();
							}
							else if (s.equals("Conditions"))
							{
								i = reader.getAttribCount();
								while (i-- > 0)
								{
									attr = reader.getAttribNoCheck(i);
									if (attr.name != null && attr.name.equals("NotBefore"))
									{
										saml.notBefore = Timestamp.from(Instant.parse(attr.value));
									}
									else if (attr.name != null && attr.name.equals("NotOnOrAfter"))
									{
										saml.notOnOrAfter = Timestamp.from(Instant.parse(attr.value));
									}
								}
								while ((s = reader.nextElementName()) != null)
								{
									if (s.equals("AudienceRestriction"))
									{
										while ((s = reader.nextElementName()) != null)
										{
											if (s.equals("Audience"))
											{
												sbTmp.clearStr();
												reader.readNodeText(sbTmp);
												saml.audience = sbTmp.toString();
											}
											else
											{
												reader.skipElement();
											}
										}
									}
									else
									{
										reader.skipElement();
									}
								}
							}
							else if (s.equals("AttributeStatement"))
							{
								while ((s = reader.nextElementName()) != null)
								{
									if (s.equals("Attribute"))
									{
										String attrName = null;
										i = reader.getAttribCount();
										while (i-- > 0)
										{
											attr = reader.getAttribNoCheck(i);
											if (attr.name != null && attr.name.equals("Name"))
											{
												attrName = attr.value;
											}
										}
										while ((s = reader.nextElementName()) != null)
										{
											if (s.equals("AttributeValue"))
											{
												sbTmp.clearStr();
												reader.readNodeText(sbTmp);
												if (attrName != null)
												{
													if (attrName.equals("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name"))
													{
														saml.name = sbTmp.toString();
													}
													else if (attrName.equals("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress"))
													{
														saml.emailAddress = sbTmp.toString();
													}
													else if (attrName.equals("http://schemas.xmlsoap.org/claims/EmailAddress"))
													{
														saml.emailAddress = sbTmp.toString();
													}
													else if (attrName.equals("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname"))
													{
														saml.givenname = sbTmp.toString();
													}
													else if (attrName.equals("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname"))
													{
														saml.surname = sbTmp.toString();
													}
												}
											}
											else
											{
												reader.skipElement();
											}
										}
									}
									else
									{
										reader.skipElement();
									}
								}
							}
							else if (s.equals("Subject"))
							{
								while ((s = reader.nextElementName()) != null)
								{
									if (s.equals("NameID"))
									{
										sbTmp.clearStr();
										reader.readNodeText(sbTmp);
										saml.nameID = sbTmp.toString();
									}
									else
									{
										reader.skipElement();
									}
								}
							}
							else if (s.equals("AuthnStatement"))
							{
								i = reader.getAttribCount();
								while (i-- > 0)
								{
									attr = reader.getAttribNoCheck(i);
									if ((s = attr.name) != null && s.equals("SessionIndex"))
									{
										saml.sessionIndex = attr.value;
									}
								}
								reader.skipElement();
							}
							else
							{
								reader.skipElement();
							}
						}
					}
					if (saml.error == ResponseError.Success && saml.issuer != null && !this.idp.getEntityId().equals(saml.issuer))
					{
						saml.error = ResponseError.UnexpectedIssuer;
						saml.errorMessage = "Unexpected Issuer";
					}
					if (saml.error == ResponseError.Success && saml.notBefore != null)
					{
						Timestamp currTime = DateTimeUtil.timestampNow();
						if (currTime.before(saml.notBefore))
						{
							saml.error = ResponseError.TimeOutOfRange;
							saml.errorMessage = "Current time cannot be before "+saml.notBefore;
						}
					}
					if (saml.error == ResponseError.Success && saml.notOnOrAfter != null)
					{
						Timestamp currTime = DateTimeUtil.timestampNow();
						if (!currTime.before(saml.notOnOrAfter))
						{
							saml.error = ResponseError.TimeOutOfRange;
							saml.errorMessage = "Current time cannot be on or after "+saml.notOnOrAfter;
						}
					}
					if (saml.error == ResponseError.Success && saml.audience != null)
					{
						if (!saml.audience.equals("https://"+this.host+this.metadataPath))
						{
							saml.error = ResponseError.InvalidAudience;
							saml.errorMessage = "Invalid Audience";
						}
					}
					if (saml.error == ResponseError.Success && saml.nameID == null && saml.name == null)
					{
						saml.error = ResponseError.UsernameMissing;
						saml.errorMessage = "User name not found";
					}
				}
				catch (DateTimeParseException ex)
				{
					saml.error = ResponseError.ResponseFormatError;
					saml.errorMessage = ex.getMessage();
					ex.printStackTrace();
				}
				return saml;
			}
			else
			{
				saml = new SAMLSSOResponse(ResponseError.DecryptFailed, "Failed in decrypting response message");
				saml.rawResponse = new String(buff, StandardCharsets.UTF_8);
				saml.status = statusCode.value;
				return saml;
			}
		}
		else
		{
			return new SAMLSSOResponse(ResponseError.ResponseNotFound, "SAMLResponse not found");
		}
	}

	@Nonnull
	public SAMLLogoutResponse doLogoutResp(@Nonnull HttpServletRequest req, @Nonnull HttpServletResponse resp)
	{
		SAMLIdpConfig idp;
		String samlResponse;
		SAMLLogoutResponse saml;

		if ((idp = this.idp) == null)
		{
			saml = new SAMLLogoutResponse(RespProcessError.IDPMissing, "Idp Config missing");
			return saml;
		}

		switch (verifyHTTPRedirect(idp, req))
		{
		case CertMissing:
			saml = new SAMLLogoutResponse(RespProcessError.IDPMissing, "Idp Config missing signature cert");
			return saml;
		case SignatureMissing:
			saml = new SAMLLogoutResponse(RespProcessError.ParamMissing, "Signature param missing");
			return saml;
		case SigAlgMissing:
			saml = new SAMLLogoutResponse(RespProcessError.ParamMissing, "SigAlg param missing");
			return saml;
		case SigAlgNotSupported:
		{
			saml = new SAMLLogoutResponse(RespProcessError.SigAlgNotSupported, "SigAlg not supported: " + req.getParameter("SigAlg"));
			return saml;
		}
		case QueryStringGetError:
			saml = new SAMLLogoutResponse(RespProcessError.QueryStringError, "Error in getting query string");
			return saml;
		case QueryStringSearchError:
			saml = new SAMLLogoutResponse(RespProcessError.QueryStringError, "Error in searching for payload end");
			return saml;
		case KeyError:
			saml = new SAMLLogoutResponse(RespProcessError.KeyError, "Error in extracting public key from cert");
			return saml;
		case SignatureInvalid:
			saml = new SAMLLogoutResponse(RespProcessError.SignatureInvalid, "Signature Invalid");
			return saml;
		case Valid:
			break;
		}

		if ((samlResponse = req.getParameter("SAMLResponse")) == null)
		{
			saml = new SAMLLogoutResponse(RespProcessError.ParamMissing, "SAMLResponse param missing");
			return saml;
		}
		Base64Enc b64 = new Base64Enc();
		byte[] dataBuff = b64.decodeBin(samlResponse);
		MemoryStream mstm = new MemoryStream();
		{
			Inflater inflater = new Inflater(true);
			inflater.setInput(dataBuff);
			byte[] tmpBuff = new byte[4096];
			int buffLen;
			try
			{
				while (!inflater.finished())
				{
					buffLen = inflater.inflate(tmpBuff);
					mstm.write(tmpBuff,0, buffLen);
				}
			}
			catch (DataFormatException ex)
			{
				ex.printStackTrace();
				saml = new SAMLLogoutResponse(RespProcessError.DataError, "Error in decompressing message: "+ex.getMessage());
				return saml;
			}
		}
		saml = new SAMLLogoutResponse(RespProcessError.Success, "Decompressed");
		saml.rawResponse = new String(mstm.getBuff(), 0, (int)mstm.getLength(), StandardCharsets.UTF_8);

		mstm.seekFromBeginning(0);
		this.parseSAMLLogoutResponse(saml, mstm);
		if (saml.status != SAMLStatusCode.Success)
		{
			saml.error = RespProcessError.StatusError;
		}
		return saml;
	}

	@Nonnull
	public SAMLLogoutRequest doLogoutReq(@Nonnull HttpServletRequest req, @Nonnull HttpServletResponse resp)
	{
		SAMLIdpConfig idp;
		String samlRequest;
		SAMLLogoutRequest saml;
	
		if ((idp = this.idp) == null)
		{
			saml = new SAMLLogoutRequest(ReqProcessError.IDPMissing, "Idp Config missing");
			return saml;
		}
		switch (verifyHTTPRedirect(idp, req))
		{
		case CertMissing:
			saml = new SAMLLogoutRequest(ReqProcessError.IDPMissing, "Idp Config missing signature cert");
			return saml;
		case SignatureMissing:
			saml = new SAMLLogoutRequest(ReqProcessError.ParamMissing, "Signature param missing");
			return saml;
		case SigAlgMissing:
			saml = new SAMLLogoutRequest(ReqProcessError.ParamMissing, "SigAlg param missing");
			return saml;
		case SigAlgNotSupported:
			saml = new SAMLLogoutRequest(ReqProcessError.SigAlgNotSupported, "SigAlg not supported: " + req.getParameter("SigAlg"));
			return saml;
		case QueryStringGetError:
			saml = new SAMLLogoutRequest(ReqProcessError.QueryStringError, "Error in getting query string");
			return saml;
		case QueryStringSearchError:
			saml = new SAMLLogoutRequest(ReqProcessError.QueryStringError, "Error in searching for payload end");
			return saml;
		case KeyError:
			saml = new SAMLLogoutRequest(ReqProcessError.KeyError, "Error in extracting public key from cert");
			return saml;
		case SignatureInvalid:
			saml = new SAMLLogoutRequest(ReqProcessError.SignatureInvalid, "Signature Invalid");
			return saml;
		case Valid:
			break;
		}
	
		if ((samlRequest = req.getParameter("SAMLRequest")) == null)
		{
			saml = new SAMLLogoutRequest(ReqProcessError.ParamMissing, "SAMLRequest param missing");
			return saml;
		}
		Base64Enc b64 = new Base64Enc();
		byte[] dataBuff = b64.decodeBin(samlRequest);
		MemoryStream mstm = new MemoryStream();
		{
			byte[] tmpBuff = new byte[4096];
			Inflater inflater = new Inflater(true);;
			inflater.setInput(dataBuff);
			try
			{
				while (!inflater.finished())
				{
					int size = inflater.inflate(tmpBuff);
					mstm.write(tmpBuff, 0, size);
				}
			}
			catch (DataFormatException ex)
			{
				ex.printStackTrace();
				saml = new SAMLLogoutRequest(ReqProcessError.MessageInvalid, "Error in decompressing data");
				return saml;
			}
		}
		saml = new SAMLLogoutRequest(ReqProcessError.Success, "Decompressed");
		saml.rawResponse = new String(mstm.getBuff(), 0, (int)mstm.getLength());
		mstm.seekFromBeginning(0);
		this.parseSAMLLogoutRequest(saml, mstm);
		return saml;
	}
}
