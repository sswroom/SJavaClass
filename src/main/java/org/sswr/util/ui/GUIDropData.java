package org.sswr.util.ui;

import org.sswr.util.data.StringBuilderUTF8;
import org.sswr.util.io.MyStream;

import jakarta.annotation.Nullable;
import jakarta.annotation.Nonnull;

public interface GUIDropData
{
	public int getCount();
	public @Nullable String getName(int index);
	public boolean getDataText(@Nonnull String name, @Nonnull StringBuilderUTF8 sb);
	public @Nullable MyStream getDataStream(@Nonnull String name);
}
