package org.sswr.util.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public abstract class WebSessionManager
{
	protected SessionHandler hdlr;

	protected WebSessionManager(SessionHandler hdlr)
	{
		this.hdlr = hdlr;
	}

	public abstract WebSession getSession(HttpServletRequest req, HttpServletResponse resp);
	public abstract WebSession createSession(HttpServletRequest req, HttpServletResponse resp);
	public abstract void deleteSession(HttpServletRequest req, HttpServletResponse resp);
}
