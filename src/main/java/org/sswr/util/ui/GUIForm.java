package org.sswr.util.ui;

import java.util.Iterator;

import org.sswr.util.data.StringBuilderUTF8;
import org.sswr.util.math.Coord2DDbl;
import org.sswr.util.math.Coord2DInt;
import org.sswr.util.media.DrawImage;
import org.sswr.util.media.DrawImage.DrawFont;
import org.sswr.util.media.MonitorInfo;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class GUIForm implements GUIClientControl {
	private @Nonnull GUIFormInner inner;

	public GUIForm(@Nullable GUIClientControl parent, double initW, double initH, @Nonnull GUICore ui)
	{
		this.inner = ui.initForm(parent, initW, initH, ui);
	}

	@Override
	public @Nullable Object getFont() {
		return this.inner.getFont();
	}

	@Override
	public void show() {
		this.inner.show();
	}

	@Override
	public void close() {
		this.inner.close();
	}

	@Override
	public void setText(@Nonnull String text) {
		this.inner.setText(text);
	}

	@Override
	public @Nullable String getText() {
		return this.inner.getText();
	}

	@Override
	public boolean getText(@Nonnull StringBuilderUTF8 sb) {
		return this.inner.getText(sb);
	}

	@Override
	public void setSize(double width, double height) {
		this.inner.setSize(width, height);
	}

	@Override
	public void setSizeP(@Nonnull Coord2DInt size) {
		this.inner.setSizeP(size);
	}

	@Override
	public @Nonnull Coord2DDbl getSize() {
		return this.inner.getSize();
	}

	@Override
	public @Nonnull Coord2DInt getSizeP() {
		return this.inner.getSizeP();
	}

	@Override
	public void setPosition(double x, double y) {
		this.inner.setPosition(x, y);
	}

	@Override
	public @Nonnull Coord2DInt getPositionP() {
		return this.inner.getPositionP();
	}

	@Override
	public @Nonnull Coord2DInt getScreenPosP() {
		return this.inner.getScreenPosP();
	}

	@Override
	public void setArea(double left, double top, double right, double bottom, boolean updateScn) {
		this.inner.setArea(left, top, right, bottom, updateScn);
	}

	@Override
	public void setAreaP(int left, int top, int right, int bottom, boolean updateScn) {
		this.inner.setAreaP(left, top, right, bottom, updateScn);
	}

	@Override
	public void setRect(double left, double top, double width, double height, boolean updateScn) {
		this.inner.setRect(left, top, width, height, updateScn);
	}

	@Override
	public void setFont(@Nullable String fontName, double ptSize, boolean isBold) {
		this.inner.setFont(fontName, ptSize, isBold);
	}

	@Override
	public void initFont() {
		this.inner.initFont();
	}

	@Override
	public void setDockType(@Nonnull DockType dockType) {
		this.inner.setDockType(dockType);
	}

	@Override
	public @Nonnull DockType getDockType() {
		return this.inner.getDockType();
	}

	@Override
	public void setVisible(boolean isVisible) {
		this.inner.setVisible(isVisible);
	}

	@Override
	public boolean getVisible() {
		return this.inner.getVisible();
	}

	@Override
	public void setEnabled(boolean isEnable) {
		this.inner.setEnabled(isEnable);
	}

	@Override
	public void setBGColor(int bgColor) {
		this.inner.setBGColor(bgColor);
	}

	@Override
	public @Nullable Object getBGBrush() {
		return this.inner.getBGBrush();
	}

	@Override
	public boolean isFormFocused() {
		return this.inner.isFormFocused();
	}

	@Override
	public void focus() {
		this.inner.focus();
	}

	@Override
	public int getScrollHPos() {
		return this.inner.getScrollHPos();
	}

	@Override
	public int getScrollVPos() {
		return this.inner.getScrollVPos();
	}

	@Override
	public void scrollTo(int x, int y) {
		this.inner.scrollTo(x, y);
	}

	@Override
	public @Nonnull String getObjectClass() {
		return this.inner.getObjectClass();
	}

	@Override
	public int onNotify(int code, int lParam) {
		return this.inner.onNotify(code, lParam);
	}

	@Override
	public void onPosChanged(boolean updateScn) {
		this.inner.onPosChanged(updateScn);
	}

	@Override
	public void onShow() {
		this.inner.onShow();
	}

	@Override
	public void onMonitorChanged() {
		this.inner.onMonitorChanged();
	}

	@Override
	public void handleSizeChanged(@Nonnull UIListener handler) {
		this.inner.handleSizeChanged(handler);
	}

	@Override
	public void updatePos(boolean redraw) {
		this.inner.updatePos(redraw);
	}

	@Override
	public void updateBGColor() {
		this.inner.updateBGColor();
	}

	@Override
	public void redraw() {
		this.inner.redraw();
	}

	@Override
	public void setCapture() {
		this.inner.setCapture();
	}

	@Override
	public void releaseCapture() {
		this.inner.releaseCapture();
	}

	@Override
	public void setCursor(@Nonnull CursorType curType) {
		this.inner.setCursor(curType);
	}

	@Override
	public int getColorBg() {
		return this.inner.getColorBg();
	}

	@Override
	public int getColorText() {
		return this.inner.getColorText();
	}

	@Override
	public int getColorTextAlt() {
		return this.inner.getColorTextAlt();
	}

	@Override
	public int getColorHightlight() {
		return this.inner.getColorHightlight();
	}

	@Override
	public int getColorHightlightText() {
		return this.inner.getColorHightlightText();
	}

	@Override
	public @Nullable GUIClientControl getParent() {
		return this.inner.getParent();
	}

	@Override
	public @Nullable GUIForm getRootForm() {
		return this.inner.getRootForm();
	}

	@Override
	public @Nullable ControlHandle getHandle() {
		return this.inner.getHandle();
	}

	@Override
	public @Nullable ControlHandle getDisplayHandle() {
		return this.inner.getDisplayHandle();
	}

	@Override
	public @Nullable MonitorHandle getHMonitor() {
		return this.inner.getHMonitor();
	}

	@Override
	public @Nullable MonitorInfo getMonitorInfo() {
		return this.inner.getMonitorInfo();
	}

	@Override
	public double getHDPI() {
		return this.inner.getHDPI();
	}

	@Override
	public double getDDPI() {
		return this.inner.getDDPI();
	}

	@Override
	public @Nullable DrawFont createDrawFont(@Nonnull DrawImage img) {
		return this.inner.createDrawFont(img);
	}

	@Override
	public @Nonnull DragErrorType handleDropEvents(@Nonnull GUIDropHandler hdlr) {
		return this.inner.handleDropEvents(hdlr);
	}

	@Override
	public void updateFont() {
		this.inner.updateFont();
	}

	@Override
	public void clearChildren() {
		this.inner.clearChildren();
	}

	@Override
	public boolean myEraseBkg(@Nullable Object hdc) {
		return this.inner.myEraseBkg(hdc);
	}

	@Override
	public @Nonnull Coord2DDbl getClientOfst() {
		return this.inner.getClientOfst();
	}

	@Override
	public @Nonnull Coord2DDbl getClientSize() {
		return this.inner.getClientSize();
	}

	@Override
	public void addChild(@Nonnull GUIControl child) {
		this.inner.addChild(child);
	}

	@Override
	public boolean isChildVisible() {
		return this.inner.isChildVisible();
	}

	@Override
	public int getChildCount() {
		return this.inner.getChildCount();
	}

	@Override
	public @Nullable GUIControl getChild(int index) {
		return this.inner.getChild(index);
	}

	@Override
	public Iterator<GUIControl> childIterator() {
		return this.inner.childIterator();
	}

	@Override
	public void focusChild(@Nonnull GUIControl child) {
		this.inner.focusChild(child);
	}

	@Override
	public void updateChildrenSize(boolean redraw) {
		this.inner.updateChildrenSize(redraw);
	}

	@Override
	public void onSizeChanged(boolean updateScn) {
		this.inner.onSizeChanged(updateScn);
	}

	@Override
	public void setDPI(double hdpi, double ddpi) {
		this.inner.setDPI(hdpi, ddpi);
	}

	@Override
	public @Nullable Object getContainer() {
		return this.inner.getContainer();
	}

	@Override
	public void destroyObject() {
		this.inner.destroyObject();
	}
	
}
