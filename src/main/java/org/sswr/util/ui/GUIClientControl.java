package org.sswr.util.ui;

import org.sswr.util.math.Coord2DDbl;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public interface GUIClientControl extends GUIControl
{
	public void updateFont();
	public void clearChildren();
	public boolean myEraseBkg(@Nullable Object hdc);

	public @Nonnull Coord2DDbl getClientOfst();
	public @Nonnull Coord2DDbl getClientSize();
	public void addChild(@Nonnull GUIControl child);
	public boolean isChildVisible();
	
	public int getChildCount();
	public @Nullable GUIControl getChild(int index);
	public java.util.Iterator<GUIControl> childIterator();
	public void focusChild(@Nonnull GUIControl child);

	public void updateChildrenSize(boolean redraw);
	public void onSizeChanged(boolean updateScn);
	public void setDPI(double hdpi, double ddpi);

	public @Nullable Object getContainer();

	public void destroyObject();
}
