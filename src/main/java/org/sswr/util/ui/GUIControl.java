package org.sswr.util.ui;

import org.sswr.util.data.StringBuilderUTF8;
import org.sswr.util.math.Coord2DDbl;
import org.sswr.util.math.Coord2DInt;
import org.sswr.util.media.DrawImage;
import org.sswr.util.media.DrawImage.DrawFont;
import org.sswr.util.media.MonitorInfo;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public interface GUIControl
{
	public enum DockType
	{
		None,
		Left,
		Top,
		Right,
		Bottom,
		Fill
	}

	public enum MouseButton
	{
		Left,
		Right,
		Middle,
		X1,
		X2
	}

	public enum CursorType
	{
		Arrow,
		Hand,
		SizeH,
		SizeV,
		SizeNW,
		SizeNE
	}

	public enum GUIKey
	{
		GK_NONE,
		GK_BACKSPACE,
		GK_TAB,
		GK_CLEAR,
		GK_ENTER,
		GK_SHIFT,
		GK_CONTROL,
		GK_ALT,
		GK_PAUSE,
		GK_CAPITAL,
		GK_KANA,
		GK_JUNJA,
		GK_FINAL,
		GK_KANJI,
		GK_ESCAPE,
		GK_CONVERT,
		GK_NONCONVERT,
		GK_ACCEPT,
		GK_MODECHANGE,
		GK_SPACE,
		GK_PAGEUP,
		GK_PAGEDOWN,
		GK_END,
		GK_HOME,
		GK_LEFT,
		GK_UP,
		GK_RIGHT,
		GK_DOWN,
		GK_SELECT,
		GK_PRINT,
		GK_EXECUTE,
		GK_PRINTSCREEN,
		GK_INSERT,
		GK_DELETE,
		GK_HELP,
		GK_0,
		GK_1,
		GK_2,
		GK_3,
		GK_4,
		GK_5,
		GK_6,
		GK_7,
		GK_8,
		GK_9,
		GK_A,
		GK_B,
		GK_C,
		GK_D,
		GK_E,
		GK_F,
		GK_G,
		GK_H,
		GK_I,
		GK_J,
		GK_K,
		GK_L,
		GK_M,
		GK_N,
		GK_O,
		GK_P,
		GK_Q,
		GK_R,
		GK_S,
		GK_T,
		GK_U,
		GK_V,
		GK_W,
		GK_X,
		GK_Y,
		GK_Z,
		GK_LWIN,
		GK_RWIN,
		GK_APPS,
		GK_SLEEP,
		GK_NUMPAD0,
		GK_NUMPAD1,
		GK_NUMPAD2,
		GK_NUMPAD3,
		GK_NUMPAD4,
		GK_NUMPAD5,
		GK_NUMPAD6,
		GK_NUMPAD7,
		GK_NUMPAD8,
		GK_NUMPAD9,
		GK_MULTIPLY,
		GK_ADD,
		GK_SEPARATOR,
		GK_SUBTRACT,
		GK_DECIMAL,
		GK_DIVIDE,
		GK_F1,
		GK_F2,
		GK_F3,
		GK_F4,
		GK_F5,
		GK_F6,
		GK_F7,
		GK_F8,
		GK_F9,
		GK_F10,
		GK_F11,
		GK_F12,
		GK_F13,
		GK_F14,
		GK_F15,
		GK_F16,
		GK_F17,
		GK_F18,
		GK_F19,
		GK_F20,
		GK_F21,
		GK_F22,
		GK_F23,
		GK_F24,
		GK_NUMLOCK,
		GK_SCROLLLOCK,
		GK_OEM_1,
		GK_OEM_PLUS,
		GK_OEM_COMMA,
		GK_OEM_MINUS,
		GK_OEM_PERIOD,
		GK_OEM_2,
		GK_OEM_3,
		GK_OEM_4,
		GK_OEM_5,
		GK_OEM_6,
		GK_OEM_7,
		GK_NUMPAD_ENTER
	}

	public enum DragErrorType
	{
		NoError,
		Unknown,
		OutOfMemory,
		InvalidControl,
		AlreadyRegister
	}

	public interface MouseEventHandler
	{
		public @Nonnull EventState handle(@Nonnull Coord2DInt scnCoord, @Nonnull MouseButton btn);
	}

	public @Nullable Object getFont();
	public void show();
	public void close();
	public void setText(@Nonnull String text);
	public @Nullable String getText();
	public boolean getText(@Nonnull StringBuilderUTF8 sb);
	public void setSize(double width, double height);
	public void setSizeP(@Nonnull Coord2DInt size);
	public @Nonnull Coord2DDbl getSize();
	public @Nonnull Coord2DInt getSizeP();
	public void setPosition(double x, double y);
	public @Nonnull Coord2DInt getPositionP();
	public @Nonnull Coord2DInt getScreenPosP();
	public void setArea(double left, double top, double right, double bottom, boolean updateScn);
	public void setAreaP(int left, int top, int right, int bottom, boolean updateScn);
	public void setRect(double left, double top, double width, double height, boolean updateScn);
	public void setFont(@Nullable String fontName, double ptSize, boolean isBold);
	public void initFont();
	public void setDockType(@Nonnull DockType dockType);
	public @Nonnull DockType getDockType();
	public void setVisible(boolean isVisible);
	public boolean getVisible();
	public void setEnabled(boolean isEnable);
	public void setBGColor(int bgColor);
	public @Nullable Object getBGBrush();
	public boolean isFormFocused();
	public void focus();
	public int getScrollHPos();
	public int getScrollVPos();
	public void scrollTo(int x, int y);

	public @Nonnull String getObjectClass();
	public int onNotify(int code, int lParam);
	public void onSizeChanged(boolean updateScn);
	public void onPosChanged(boolean updateScn);
	public void onShow();
	public void onMonitorChanged();

	public void handleSizeChanged(@Nonnull UIListener handler);

	public void updateFont();
	public void updatePos(boolean redraw);
	public void updateBGColor();
	public void redraw();
	public void setCapture();
	public void releaseCapture();
	public void setCursor(@Nonnull CursorType curType);

	public int getColorBg();
	public int getColorText();
	public int getColorTextAlt();
	public int getColorHightlight();
	public int getColorHightlightText();

	public @Nullable GUIClientControl getParent();
	public @Nullable GUIForm getRootForm();
	public @Nullable ControlHandle getHandle();
	public @Nullable ControlHandle getDisplayHandle();
	public @Nullable MonitorHandle getHMonitor();
	public @Nullable MonitorInfo getMonitorInfo();
	public void setDPI(double hdpi, double ddpi);
	public double getHDPI();
	public double getDDPI();

	public @Nullable DrawFont createDrawFont(@Nonnull DrawImage img);

	public @Nonnull DragErrorType handleDropEvents(@Nonnull GUIDropHandler hdlr);

	public void destroyObject();	
}
