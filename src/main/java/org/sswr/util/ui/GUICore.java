package org.sswr.util.ui;

import java.util.Optional;

import org.sswr.util.data.SharedDouble;
import org.sswr.util.math.Coord2DInt;
import org.sswr.util.media.DrawEngine;
import org.sswr.util.media.MonitorMgr;
import org.sswr.util.media.Size2D;
import org.sswr.util.ui.GUIControl.GUIKey;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public abstract class GUICore
{
	public enum DisplayRotation
	{
		None,
		Landscape,
		Port
	}

	@Nullable
	public static GUICore create()
	{
		try
		{
			Class<?> cls = Class.forName("org.sswr.swing.SwingGUICore");
			if (GUICore.class.isAssignableFrom(cls))
			{
				return (GUICore) cls.getDeclaredConstructor().newInstance();
			}
		}
		catch (ClassNotFoundException e)
		{
		}
		catch (ReflectiveOperationException e)
		{
		}
		return null;
	}

	public abstract @Nonnull GUIFormInner initForm(@Nullable GUIClientControl parent, double initW, double initH, @Nonnull GUICore ui);
	public abstract void run();
	public abstract void processMessages();
	public abstract void waitForMessages();
	public abstract void exit();
	public abstract @Nonnull DrawEngine createDrawEngine();
	public abstract double getMagnifyRatio(@Nullable MonitorHandle hMonitor);
	public abstract void useDevice(boolean useSystem, boolean useDisplay);
	public abstract void setNoDisplayOff(boolean noDispOff);
	public abstract void displayOff();
	public abstract void suspend();
	public abstract @Nonnull Coord2DInt getDesktopSize();
	public abstract @Nonnull Coord2DInt getCursorPos();
	public abstract void setDisplayRotate(@Nullable MonitorHandle hMonitor, @Nonnull DisplayRotation rot);
	public abstract void getMonitorDPIs(@Nullable MonitorHandle hMonitor, @Nonnull SharedDouble hdpi, @Nonnull SharedDouble ddpi);
	public abstract void setMonitorMgr(@Nullable MonitorMgr monMgr);
	public abstract @Nullable MonitorMgr getMonitorMgr();
	public abstract boolean isForwarded();
	public abstract int getScrollBarSize();

	public abstract void showMsgOK(@Nonnull String message, @Nonnull String title, @Nullable GUIControl ctrl);
	public abstract boolean showMsgYesNo(@Nonnull String message, @Nonnull String title, @Nullable GUIControl ctrl);
/*	public abstract @Nonnull GUIButton newButton(@Nonnull GUIClientControl parent, @Nonnull String text);
	public abstract @Nonnull GUICheckBox newCheckBox(@Nonnull GUIClientControl parent, @Nonnull String text, boolean checked);
	public abstract @Nonnull GUICheckedListBox newCheckedListBox(@Nonnull GUIClientControl parent);
	public abstract @Nonnull GUIComboBox newComboBox(@Nonnull GUIClientControl parent, boolean allowEdit);
	public abstract @Nonnull GUIDateTimePicker newDateTimePicker(@Nonnull GUIClientControl parent, boolean calendarSelect);
	public abstract @Nonnull GUIGroupBox newGroupBox(@Nonnull GUIClientControl parent, @Nonnull String text);
	public abstract @Nonnull GUIHScrollBar newHScrollBar(@Nonnull GUIClientControl parent, double width);
	public abstract @Nonnull GUILabel newLabel(@Nonnull GUIClientControl parent, @Nonnull String text);
	public abstract @Nonnull GUIListBox newListBox(@Nonnull GUIClientControl parent, boolean multiSelect);
	public abstract @Nonnull GUIPictureBox newPictureBox(@Nonnull GUIClientControl parent, @Nonnull DrawEngine eng, boolean hasBorder, boolean allowResize);
	public abstract @Nonnull GUIPictureBoxSimple newPictureBoxSimple(@Nonnull GUIClientControl parent, @Nonnull DrawEngine eng, boolean hasBorder);
	public abstract @Nonnull GUIProgressBar newProgressBar(@Nonnull GUIClientControl parent, long totalCnt);
	public abstract @Nonnull GUIRadioButton newRadioButton(@Nonnull GUIClientControl parent, @Nonnull String initText, boolean selected);
	public abstract @Nonnull GUIRealtimeLineChart newRealtimeLineChart(@Nonnull GUIClientControl parent, @Nonnull DrawEngine eng, int lineCnt, int sampleCnt, int updateIntervalMS, @Nullable ColorSess colorSess);
	public abstract @Nonnull GUITabControl newTabControl(@Nonnull GUIClientControl parent);
	public abstract @Nonnull GUITextBox newTextBox(@Nonnull GUIClientControl parent, @Nonnull String initText);
	public abstract @Nonnull GUITextBox newTextBox(@Nonnull GUIClientControl parent, @Nonnull String initText, boolean multiLine);
	public abstract @Nonnull GUITrackBar newTrackBar(@Nonnull GUIClientControl parent, int minVal, int maxVal, int currVal);
	public abstract @Nonnull GUIHSplitter newHSplitter(@Nonnull GUIClientControl parent, int width, boolean isRight);
	public abstract @Nonnull GUIVSplitter newVSplitter(@Nonnull GUIClientControl parent, int height, boolean isBottom);
	public abstract @Nonnull GUIFileDialog newFileDialog(@Nonnull String compName, @Nonnull String appName, @Nonnull String dialogName, boolean isSave);
	public abstract @Nonnull GUIFolderDialog newFolderDialog();
	public abstract @Nonnull GUIFontDialog newFontDialog(@Nullable String fontName, double fontSizePt, boolean isBold, boolean isItalic);

	public abstract @Nonnull GUIPanelBase newPanelBase(@Nonnull GUIPanel master, @Nullable ControlHandle parentHWnd);
	public abstract @Nonnull GUIPanelBase newPanelBase(@Nonnull GUIPanel master, @Nonnull GUIClientControl parent);

	public abstract @Nonnull GUIListView newListView(@Nonnull GUIClientControl parent, @Nonnull ListViewStyle lvstyle, int colCount);
	public abstract @Nonnull GUIPanel newPanel(@Nonnull GUIClientControl parent);
	public abstract @Nonnull GUIPictureBoxDD newPictureBoxDD(@Nonnull GUIClientControl parent, @Nonnull ColorManagerSess colorSess, boolean allowEnlarge, boolean directMode);*/

	public abstract int guiKey2OSKey(@Nonnull GUIKey guiKey);
	public abstract @Nonnull GUIKey osKey2GUIKey(int osKey);
	public abstract @Nonnull String guiKeyGetName(@Nonnull GUIKey guiKey);
}
