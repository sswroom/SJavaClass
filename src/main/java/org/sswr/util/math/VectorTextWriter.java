package org.sswr.util.math;

import org.sswr.util.math.geometry.Vector2D;

public interface VectorTextWriter
{
	public String getWriterName();
	public boolean toText(StringBuilder sb, Vector2D vec);
	public String getLastError();
}
