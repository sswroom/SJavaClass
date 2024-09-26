package org.sswr.util.data;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public interface TableBuilder
{
	public void appendRow();
	public void appendRow(@Nullable Iterable<?> rowData);
	@Nonnull
	public byte[] build();
}
