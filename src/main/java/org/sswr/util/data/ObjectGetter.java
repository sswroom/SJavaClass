package org.sswr.util.data;

import jakarta.annotation.Nonnull;

public interface ObjectGetter
{
	public Object getObjectByName(@Nonnull String name);
}
