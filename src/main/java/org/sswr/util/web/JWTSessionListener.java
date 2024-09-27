package org.sswr.util.web;

import jakarta.annotation.Nonnull;

public interface JWTSessionListener
{
	public void sessionDestroy(@Nonnull JWTSession sess);
}
