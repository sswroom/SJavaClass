package org.sswr.util.crypto.cert;

import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Map;

import org.sswr.util.crypto.cert.MyX509File.FileType;
import org.sswr.util.net.HTTPOSClient;
import org.sswr.util.parser.X509Parser;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class MyX509Env
{
	protected KeyStore trustStore;
	protected Map<String, Certificate> trustStoreMap;

	@Nullable
	public Map<String, Certificate> getTrustStore()
	{
		if (this.trustStore == null)
		{
			this.trustStore = CertUtil.loadDefaultTrustStore();
			if (this.trustStore != null)
			{
				this.trustStoreMap = CertUtil.buildCNMap(this.trustStore);
			}
			if (this.trustStoreMap != null)
			{
				CertUtil.loadMyTrusts(this.trustStoreMap);
			}
		}
		return this.trustStoreMap;
	}

	@Nullable
	public MyX509CRL getCRL(@Nonnull String url)
	{
		byte[] crlBytes = HTTPOSClient.getAsBytes(url, 200);
		if (crlBytes == null)
		{
			return null;
		}
		int i = url.lastIndexOf('/');
		MyX509File x509 = X509Parser.parseBuff(crlBytes, 0, crlBytes.length, url.substring(i + 1));
		if (x509 == null)
		{
			return null;
		}
		if (x509.getFileType() == FileType.CRL)
		{
			return (MyX509CRL)x509;
		}
		return null;
	}
}
