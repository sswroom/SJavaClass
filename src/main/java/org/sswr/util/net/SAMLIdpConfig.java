package org.sswr.util.net;

import org.sswr.util.crypto.cert.MyX509Cert;
import org.sswr.util.crypto.cert.MyX509File;
import org.sswr.util.data.EncodingFactory;
import org.sswr.util.data.StringBuilderUTF8;
import org.sswr.util.data.XMLAttrib;
import org.sswr.util.data.XMLReader;
import org.sswr.util.data.textbinenc.Base64Enc;
import org.sswr.util.io.IOStream;
import org.sswr.util.io.LogTool;
import org.sswr.util.parser.X509Parser;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class SAMLIdpConfig {
	private @Nonnull String serviceDispName;
	private @Nonnull String entityId;
	private @Nonnull String signOnLocation;
	private @Nonnull String logoutLocation;
	private @Nullable MyX509Cert encryptionCert;
	private @Nullable MyX509Cert signingCert;

	public SAMLIdpConfig(@Nonnull String serviceDispName, @Nonnull String entityId, @Nonnull String signOnLocation, @Nonnull String logoutLocation, @Nullable MyX509Cert encryptionCert, @Nullable MyX509Cert signingCert)
	{
		this.serviceDispName = serviceDispName;
		this.entityId = entityId;
		this.signOnLocation = signOnLocation;
		this.logoutLocation = logoutLocation;
		this.encryptionCert = encryptionCert;
		this.signingCert = signingCert;
	}

	@Nonnull
	public String getServiceDispName()
	{
		return this.serviceDispName;
	}

	@Nonnull
	public String getEntityId()
	{
		return this.entityId;
	}

	@Nonnull
	public String getSignOnLocation()
	{
		return this.signOnLocation;
	}

	@Nonnull
	public String getLogoutLocation()
	{
		return this.logoutLocation;
	}

	@Nullable
	public MyX509Cert getEncryptionCert()
	{
		return this.encryptionCert;
	}

	@Nullable
	public MyX509Cert getSigningCert()
	{
		return this.signingCert;
	}

	@Nullable
	public static SAMLIdpConfig parseMetadata(@Nonnull TCPClientFactory clif, @Nullable SSLEngine ssl, @Nonnull EncodingFactory encFact, String path)
	{
		LogTool log = new LogTool();
		IOStream stm;
		String s;
		int i;
		int j;
		XMLAttrib attr;
		StringBuilderUTF8 sb = new StringBuilderUTF8();
		byte[] buff;
		MyX509File file;
		if ((stm = URL.openStream(path, null, clif, ssl, 10000, log)) == null)
		{
			return null;
		}
		XMLReader reader = new XMLReader(encFact, stm, XMLReader.ParseMode.XML);
		if ((s = reader.nextElementName()) != null)
		{
			if (s.equals("EntityDescriptor"))
			{
				String serviceDispName = null;
				String entityId = null;
				String signOnLocation = null;
				String logoutLocation = null;
				MyX509Cert encryptionCert = null;
				MyX509Cert signingCert = null;
				int type;
				i = 0;
				j = reader.getAttribCount();
				while (i < j)
				{
					attr = reader.getAttribNoCheck(i);
					if ((s = attr.name) != null && s.equals("entityID"))
					{
						if (attr.value != null)
						{
							entityId = attr.value;
						}
					}
					i++;
				}
				while ((s = reader.nextElementName()) != null)
				{
					if (s.equals("RoleDescriptor"))
					{
						i = 0;
						j = reader.getAttribCount();
						while (i < j)
						{
							attr = reader.getAttribNoCheck(i);
							if ((s = attr.name) != null && s.equals("ServiceDisplayName"))
							{
								if (attr.value != null)
								{
									serviceDispName = attr.value;
								}
							}
							i++;
						}
						reader.skipElement();
					}
					else if (s.equals("IDPSSODescriptor"))
					{
						while ((s = reader.nextElementName()) != null)
						{
							if (s.equals("KeyDescriptor"))
							{
								type = 0;
								i = 0;
								j = reader.getAttribCount();
								while (i < j)
								{
									attr = reader.getAttribNoCheck(i);
									if ((s = attr.name) != null && s.equals("use"))
									{
										if ((s = attr.value) != null && s.equals("encryption"))
										{
											type = 1;
										}
										else if ((s = attr.value) != null && s.equals("signing"))
										{
											type = 2;
										}
									}
									i++;
								}
								while ((s = reader.nextElementName()) != null)
								{
									if (s.equals("KeyInfo"))
									{
										while ((s = reader.nextElementName()) != null)
										{
											if (s.equals("X509Data"))
											{
												while ((s = reader.nextElementName()) != null)
												{
													if (s.equals("X509Certificate"))
													{
														sb.clearStr();
														reader.readNodeText(sb);
														if (type == 1)
														{
															Base64Enc b64 = new Base64Enc();
															buff = b64.decodeBin(sb.toString());
															if ((file = X509Parser.parseBinary(buff, 0, buff.length)) != null)
															{
																if (file.getFileType() == MyX509File.FileType.Cert)
																{
																	encryptionCert = (MyX509Cert)file;
																}
															}
														}
														else if (type == 2)
														{
															Base64Enc b64 = new Base64Enc();
															buff = b64.decodeBin(sb.toString());
															if ((file = X509Parser.parseBinary(buff, 0, buff.length)) != null)
															{
																if (file.getFileType() == MyX509File.FileType.Cert)
																{
																	signingCert = (MyX509Cert)file;
																}
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
									else
									{
										reader.skipElement();
									}
								}
							}
							else if (s.equals("SingleLogoutService"))
							{
								type = 0;
								i = 0;
								j = reader.getAttribCount();
								while (i < j)
								{
									attr = reader.getAttribNoCheck(i);
									if ((s = attr.name) != null && s.equals("Binding"))
									{
										if ((s = attr.value) != null && s.equals("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect"))
										{
											type = 1;
										}
									}
									else if ((s = attr.name) != null && s.equals("Location"))
									{
										if (type == 1 && (s = attr.value) != null)
										{
											logoutLocation = s;
										}
									}
									i++;
								}
								reader.skipElement();
							}
							else if (s.equals("SingleSignOnService"))
							{
								type = 0;
								i = 0;
								j = reader.getAttribCount();
								while (i < j)
								{
									attr = reader.getAttribNoCheck(i);
									if ((s = attr.name) != null && s.equals("Binding"))
									{
										if ((s = attr.value) != null && s.equals("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect"))
										{
											type = 1;
										}
									}
									else if ((s = attr.name) != null && s.equals("Location"))
									{
										if (type == 1 && (s = attr.value) != null)
										{
											signOnLocation = s;
										}
									}
									i++;
								}
								reader.skipElement();
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
				if (serviceDispName != null && entityId != null && signOnLocation != null && logoutLocation != null)
				{
					SAMLIdpConfig cfg = new SAMLIdpConfig(serviceDispName, entityId, signOnLocation, logoutLocation, encryptionCert, signingCert);
					stm.close();
					return cfg;
				}
			}
		}
		stm.close();
		return null;
	}
}
