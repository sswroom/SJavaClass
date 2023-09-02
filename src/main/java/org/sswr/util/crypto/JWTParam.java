package org.sswr.util.crypto;

public class JWTParam
{
	private String iss;
	private String sub;
	private String aud;
	private long exp;
	private long nbf;
	private long iat;
	private String jti;

	public JWTParam()
	{

	}

	public void clear()
	{
		this.iss = null;
		this.sub = null;
		this.aud = null;
		this.exp = 0;
		this.nbf = 0;
		this.iat = 0;
		this.jti = null;
	}

	public void setIssuer(String issuer)
	{
		this.iss = issuer;
	}

	public String getIssuer()
	{
		return this.iss;
	}

	public boolean isIssuerValid(String issuer)
	{
		return this.iss == null || this.iss.equals(issuer);
	}

	public void setSubject(String subject)
	{
		this.sub = subject;
	}

	public String getSubject()
	{
		return this.sub;
	}

	public void setAudience(String audience)
	{
		this.aud = audience;
	}

	public String getAudience()
	{
		return this.aud;
	}

	public void setExpirationTime(long t)
	{
		this.exp = t;
	}

	public long getExpirationTime()
	{
		return this.exp;
	}

	public void setNotBefore(long t)
	{
		this.nbf = t;
	}

	public long getNotBefore()
	{
		return this.nbf;
	}

	public void setIssuedAt(long t)
	{
		this.iat = t;
	}

	public long getIssuedAt()
	{
		return this.iat;
	}

	public void setJWTId(String id)
	{
		this.jti = id;
	}

	public String getJWTId()
	{
		return this.jti;
	}

	public boolean isExpired(long timeMillis)
	{
		if (this.nbf != 0 && timeMillis < this.nbf * 1000)
			return true;
		if (this.exp != 0 && timeMillis >= this.exp * 1000)
			return true;
		return false;
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		boolean found = false;
		sb.append("Params [");
		if (this.iss != null)
		{
			if (found) sb.append(", ");
			sb.append("iss="+this.iss);
			found = true;
		}
		if (this.sub != null)
		{
			if (found) sb.append(", ");
			sb.append("sub="+this.sub);
			found = true;
		}
		if (this.aud != null)
		{
			if (found) sb.append(", ");
			sb.append("aud="+this.aud);
			found = true;
		}
		if (this.exp != 0)
		{
			if (found) sb.append(", ");
			sb.append("exp="+this.exp);
			found = true;
		}
		if (this.nbf != 0)
		{
			if (found) sb.append(", ");
			sb.append("nbf="+this.nbf);
			found = true;
		}
		if (this.iat != 0)
		{
			if (found) sb.append(", ");
			sb.append("iat="+this.iat);
			found = true;
		}
		if (this.jti != null)
		{
			if (found) sb.append(", ");
			sb.append("jti="+this.jti);
			found = true;
		}
		sb.append("]");
		return sb.toString();
	}
}
