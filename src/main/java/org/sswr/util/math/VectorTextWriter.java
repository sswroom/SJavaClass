package org.sswr.util.math;

import org.sswr.util.math.geometry.Vector2D;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public interface VectorTextWriter
{
	@Nonnull
	public String getWriterName();
	public boolean toText(@Nonnull StringBuilder sb, @Nonnull Vector2D vec);
	@Nullable
	public String getLastError();
}
