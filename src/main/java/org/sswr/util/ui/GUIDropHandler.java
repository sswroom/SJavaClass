package org.sswr.util.ui;

import jakarta.annotation.Nonnull;

public interface GUIDropHandler {
	public enum DragEffect
	{
		None,
		Copy,
		Move,
		Link
	}
	public @Nonnull DragEffect dragEnter(@Nonnull GUIDropData data);
	public void dropData(@Nonnull GUIDropData data, int x, int y);
}
