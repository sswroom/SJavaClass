package org.sswr.util.web;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public abstract class WebSessionManager
{
	protected SessionHandler hdlr;

	protected WebSessionManager(SessionHandler hdlr)
	{
		this.hdlr = hdlr;
	}

	@Nullable
	public abstract WebSession getSession(@Nonnull HttpServletRequest req, @Nonnull HttpServletResponse resp);
	@Nonnull
	public abstract WebSession createSession(@Nonnull HttpServletRequest req, @Nonnull HttpServletResponse resp);
	public abstract void deleteSession(@Nonnull HttpServletRequest req, @Nonnull HttpServletResponse resp);
}
