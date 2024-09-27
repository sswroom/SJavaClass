package org.sswr.util.web;

import jakarta.annotation.Nonnull;

public interface SessionHandler {
	public boolean onSessionCheck(@Nonnull WebSession sess);
	public boolean onSessionDeleted(@Nonnull WebSession sess);
}
