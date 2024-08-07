package org.sswr.util.web;

public interface SessionHandler {
	public boolean onSessionCheck(WebSession sess);
	public boolean onSessionDeleted(WebSession sess);
}
