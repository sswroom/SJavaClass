package org.sswr.util.math.chart;

import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.sswr.util.data.DateTimeUtil;
import org.sswr.util.data.SharedInt;
import org.sswr.util.data.StringUtil;
import org.sswr.util.data.TwinItem;
import org.sswr.util.math.Coord2DDbl;
import org.sswr.util.media.DrawImage;
import org.sswr.util.media.ImageUtil;
import org.sswr.util.media.Size2D;
import org.sswr.util.media.Size2DInt;
import org.sswr.util.media.StaticImage;
import org.sswr.util.media.DrawImage.DrawBrush;
import org.sswr.util.media.DrawImage.DrawFont;
import org.sswr.util.media.DrawImage.DrawFontStyle;
import org.sswr.util.media.DrawImage.DrawPen;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class ChartPlotter {
	private @Nullable String title;
	private @Nonnull String dateFormat;
	private @Nonnull String timeFormat;
	private @Nonnull String dblFormat;
	private double minDblVal;

	private @Nonnull Random rnd;

	private @Nonnull List<ChartParam> charts;
	private @Nullable String[] titleLines;
	private @Nullable String yUnit;
	private byte timeZoneQHR;

	private double refDbl;
	private int refInt;
	private Instant refTime;
	private boolean refExist;
	private @Nonnull RefType refType;
	private @Nonnull PointType pointType;
	private double pointSize;

	private int bgColor;
	private int boundColor;
	private int fontColor;
	private int gridColor;
	private int refLineColor;
	private double lineThick;
	private double barLength;

	private @Nonnull String fntName;
	private double fntSizePt;

	private @Nullable Axis xAxis;
	private @Nullable Axis y1Axis;
	private @Nullable Axis y2Axis;

	private @Nullable Axis getXAxis(@Nonnull ChartData data)
	{
		Axis axis;
		if ((axis = this.xAxis) != null)
		{
			if (axis.getType() != data.getType())
				return null;
			if (axis instanceof Int32Axis)
			{
				((Int32Axis)axis).extendRange((Int32Data)data);
				return axis;
			}
			else if (axis instanceof DoubleAxis)
			{
				((DoubleAxis)axis).extendRange((DoubleData)data);
				return axis;
			}
			else if (axis instanceof TimeAxis)
			{
				((TimeAxis)axis).extendRange((TimeData)data);
				return axis;
			}
			return null;
		}
		this.xAxis = newAxis(data);
		if ((axis = this.xAxis) != null)
		{
			axis.setLabelRotate(90);
		}
		return this.xAxis;
	}

	private @Nullable Axis getYAxis(@Nonnull ChartData data)
	{
		Axis axis;
		if ((axis = this.y1Axis) == null)
		{
			this.y1Axis = newAxis(data);
			return this.y1Axis;
		}
		else if (axis.getType() == data.getType())
		{
			if (axis instanceof Int32Axis)
			{
				((Int32Axis)axis).extendRange((Int32Data)data);
				return axis;
			}
			else if (axis instanceof DoubleAxis)
			{
				((DoubleAxis)axis).extendRange((DoubleData)data);
				return axis;
			}
			else if (axis instanceof TimeAxis)
			{
				((TimeAxis)axis).extendRange((TimeData)data);
				return axis;
			}
			return null;
		}
		else if ((axis = this.y2Axis) == null)
		{
			this.y2Axis = newAxis(data);
			return this.y2Axis;
		}
		else if (axis.getType() == data.getType())
		{
			if (axis instanceof Int32Axis)
			{
				((Int32Axis)axis).extendRange((Int32Data)data);
				return axis;
			}
			else if (axis instanceof DoubleAxis)
			{
				((DoubleAxis)axis).extendRange((DoubleData)data);
				return axis;
			}
			else if (axis instanceof TimeAxis)
			{
				((TimeAxis)axis).extendRange((TimeData)data);
				return axis;
			}
			return null;
		}
		return null;
	}

	public ChartPlotter(@Nullable String title)
	{
		this.title = null;

		this.timeFormat = "HH:mm";
		this.dateFormat = "yyyy/MM/dd";
		this.dblFormat = "0.00";
		this.minDblVal = 0.01;

		this.setTitle(title);
		this.xAxis = null;
		this.y1Axis = null;
		this.y2Axis = null;
		this.refTime = Instant.ofEpochMilli(0);
		this.timeZoneQHR = 0;
		this.barLength = 3.0;
		this.pointType = PointType.Null;
		this.pointSize = 0;
		this.yUnit = null;
		this.timeZoneQHR = DateTimeUtil.getLocalTZQhr();
		this.charts = new ArrayList<ChartParam>();
		this.refType = RefType.None;

		bgColor = 0xffffffff;
		boundColor = 0xff000000;
		fontColor = 0xff000000;
		gridColor = 0xffebebeb;
		refLineColor = 0xffff0000;
		this.lineThick = 1.0;
		this.rnd = new Random();
		
		this.fntName = "SimHei";
		fntSizePt = 12.0;
		
		this.refDbl = 0;
		this.refInt = 0;
		this.refTime = Instant.ofEpochMilli(0);
		this.refExist = false;		
	}

	public void setFontHeightPt(double ptSize)
	{
		if (ptSize > 0)
			fntSizePt = ptSize;
	}

	public void setFontName(@Nonnull String name)
	{
		this.fntName = name;
	}

	public void setYRefVal(int refVal, int col)
	{
		this.refInt = refVal;
		this.refLineColor = col;
		this.refExist = true;
	}

	public void setYRefVal(double refVal, int col)
	{
		this.refDbl = refVal;
		this.refLineColor = col;
		this.refExist = true;
	}

	public void setYRefVal(@Nonnull Timestamp refVal, int col)
	{
		this.refTime = refVal.toInstant();
		this.refLineColor = col;
		this.refExist = true;
	}

	public void setYRefType(@Nonnull RefType refType)
	{
		this.refType = refType;
	}

	public void setYUnit(@Nullable String yUnit)
	{
		this.yUnit = yUnit;
	}

	public void setLineThick(double lineThick)
	{
		this.lineThick = lineThick;
	}

	public void setTimeZoneQHR(byte timeZone)
	{
		this.timeZoneQHR = timeZone;
	}

	public void setBarLength(double barLength)
	{
		this.barLength = barLength;
	}

	public void setPointType(@Nonnull PointType pointType, double pointSize)
	{
		this.pointType = pointType;
		this.pointSize = pointSize;
	}

	public int getRndColor()
	{
		int r;
		int g;
		int b;
		
		r = (int)(64 + (this.rnd.nextInt() % 192));
		g = (int)(64 + (this.rnd.nextInt() % 192));
		b = 512 - r - g;
		if (b < 0)
			b = 0;
		else if (b > 255)
			b = 255;
		return 0xff000000 | (r << 16) | (g << 8) | b;
	}

	public boolean addLineChart(@Nonnull String name, @Nonnull ChartData yData, @Nonnull ChartData xData, int lineColor)
	{
		Axis yAxis;
		if ((getXAxis(xData)) == null || (yAxis = getYAxis(yData)) == null || xData.getCount() != yData.getCount())
		{
			return false;
		}
		this.charts.add(new ChartParam(name, yData, yAxis, xData, lineColor, 0, ChartType.Line));
		return true;
	}

	public boolean addFilledLineChart(@Nonnull String name, @Nonnull ChartData yData, @Nonnull ChartData xData, int lineColor, int fillColor)
	{
		Axis yAxis;
		if ((getXAxis(xData)) == null || (yAxis = getYAxis(yData)) == null || xData.getCount() != yData.getCount())
		{
			return false;
		}
		this.charts.add(new ChartParam(name, yData, yAxis, xData, lineColor, fillColor, ChartType.FilledLine));
		return true;
	}

	public boolean addScatter(@Nonnull String name, @Nonnull ChartData xData, @Nonnull ChartData yData, int lineColor)
	{
		return addScatter(name, yData, xData, null, lineColor);
	}

	public boolean addScatter(@Nonnull String name, @Nonnull ChartData xData, @Nonnull ChartData yData, @Nullable String[] labels, int lineColor)
	{
		Axis yAxis;
		if ((getXAxis(xData)) == null || (yAxis = getYAxis(yData)) == null || xData.getCount() != yData.getCount())
		{
			return false;
		}
		String[] chartlabels;
		ChartParam chart = new ChartParam(name, yData, yAxis, xData, lineColor, 0, ChartType.Scatter);
		if (labels != null)
		{
			chartlabels = new String[yData.getCount()];
			int i = yData.getCount();
			while (i-- > 0)
			{
				chartlabels[i] = labels[i];
			}
			chart.labels = chartlabels;
		}
		this.charts.add(chart);
		if (this.pointType == PointType.Null)
		{
			this.pointType = PointType.Circle;
		}
		return true;
	}

	public boolean addHistogramCount(@Nonnull String name, @Nonnull ChartData data, int barCount, int lineColor, int fillColor)
	{
		ChartData xData;
		ChartData yData;
		int i;
		double dmin;
		double dmax;
		if (data instanceof Int32Data)
		{
			Int32Data vdata = (Int32Data)data;
			int[] dataArr = vdata.getData();
			int dataCnt = vdata.getCount();
			int min = dataArr[0];
			int max = min;
			i = 1;
			while (i < dataCnt)
			{
				if (dataArr[i] < min) min = dataArr[i];
				if (dataArr[i] > max) max = dataArr[i];
				i++;
			}
			dmin = (double)min;
			dmax = (double)max;
			double interval = (dmax - dmin) / (double)barCount;
			double[] valArr = new double[barCount + 1];
			int[] cntArr =  new int[barCount + 1];
			i = 0;
			while (i < barCount)
			{
				cntArr[i] = 0;
				valArr[i] = dmin + interval * (double)(i + 1);
				i++;
			}
			cntArr[barCount] = 0;
			i = 0;
			while (i < dataCnt)
			{
				double v = (double)dataArr[i];
				cntArr[(int)((v - dmin) / interval)]++;
				i++;
			}
			cntArr[barCount - 1] += cntArr[barCount];
			xData = newData(valArr, 0, barCount);
			yData = newData(cntArr, 0, barCount);
		}
		else if (data instanceof DoubleData)
		{
			DoubleData vdata = (DoubleData)data;
			double[] dataArr = vdata.getData();
			int dataCnt = vdata.getCount();
			dmin = dataArr[0];
			dmax = dmin;
			i = 1;
			while (i < dataCnt)
			{
				if (dataArr[i] < dmin) dmin = dataArr[i];
				if (dataArr[i] > dmax) dmax = dataArr[i];
				i++;
			}
			double interval = (dmax - dmin) / (double)barCount;
			double[] valArr = new double[barCount + 1];
			int[] cntArr = new int[barCount + 1];
			i = 0;
			while (i < barCount)
			{
				cntArr[i] = 0;
				valArr[i] = dmin + interval * (double)(i + 1);
				i++;
			}
			cntArr[barCount] = 0;
			i = 0;
			while (i < dataCnt)
			{
				double v = dataArr[i];
				cntArr[(int)((v - dmin) / interval)]++;
				i++;
			}
			cntArr[barCount - 1] += cntArr[barCount];
			xData = newData(valArr, 0, barCount);
			yData = newData(cntArr, 0, barCount);
		}
		else
		{
			return false;
		}
		Axis xAxis;
		Axis yAxis;
		if ((xAxis = getXAxis(xData)) == null || (yAxis = getYAxis(yData)) == null)
		{
			return false;
		}
		((DoubleAxis)xAxis).extendRange(dmin);
		this.charts.add(new ChartParam(name, yData, yAxis, xData, lineColor, fillColor, ChartType.Histogram));
		return true;
	}

	public void setXRangeDate(@Nonnull Timestamp xVal)
	{
		Axis axis;
		if ((axis = this.xAxis) == null)
		{
			this.xAxis = new TimeAxis(xVal.toInstant());
		}
		else if (axis instanceof TimeAxis)
		{
			((TimeAxis)axis).extendRange(xVal.toInstant());
		}
	}

	public void setYRangeInt(int yVal)
	{
		Axis axis;
		if ((axis = this.y1Axis) != null && axis instanceof Int32Axis)
		{
			((Int32Axis)axis).extendRange(yVal);
		}
		else if ((axis = this.y2Axis) != null && axis instanceof Int32Axis)
		{
			((Int32Axis)axis).extendRange(yVal);
		}
	}

	public void setYRangeDbl(double yVal)
	{
		Axis axis;
		if ((axis = this.y1Axis) != null && axis instanceof DoubleAxis)
		{
			((DoubleAxis)axis).extendRange(yVal);
		}
		else if ((axis = this.y2Axis) != null && axis instanceof DoubleAxis)
		{
			((DoubleAxis)axis).extendRange(yVal);
		}
	}

	public void setTitle(@Nullable String title)
	{
		this.title = title;
		if (title == null)
		{
			this.titleLines = null;
		}
		else
		{
			this.titleLines = StringUtil.splitLine(title);
		}
	}

	public @Nullable String getTitle()
	{
		return this.title;
	}

	public void setDateFormat(@Nonnull String format)
	{
		this.dateFormat = format;
	}

	public @Nonnull String getDateFormat()
	{
		return this.dateFormat;
	}

	public void setTimeFormat(@Nonnull String format)
	{
		this.timeFormat = format;
	}

	public @Nonnull String getTimeFormat()
	{
		return this.timeFormat;
	}

	public void setDblFormat(@Nonnull String format)
	{
		this.dblFormat = format;
		int i = format.indexOf('.');
		if (i == -1)
		{
			this.minDblVal = 1.0;
		}
		else
		{
			i = format.length() - i - 1;
			this.minDblVal = 1.0;
			while (i-- > 0)
			{
				this.minDblVal = this.minDblVal * 0.1;
			}
		}
	}

	public @Nonnull String getDblFormat()
	{
		return this.dblFormat;
	}

	public void setXAxisName(@Nullable String xAxisName)
	{
		Axis axis;
		if ((axis = this.xAxis) != null) axis.setName(xAxisName);
	}

	public @Nullable String getXAxisName()
	{
		Axis axis;
		if ((axis = this.xAxis) != null) return axis.getName();
		return null;
	}

	public void setY1AxisName(@Nullable String y1AxisName)
	{
		Axis axis;
		if ((axis = this.y1Axis) != null) axis.setName(y1AxisName);
	}

	public @Nullable String getY1AxisName()
	{
		Axis axis;
		if ((axis = this.y1Axis) != null) return axis.getName();
		return null;
	}

	public void setY2AxisName(@Nullable String y2AxisName)
	{
		Axis axis;
		if ((axis = this.y2Axis) != null) axis.setName(y2AxisName);
	}

	public @Nullable String getY2AxisName()
	{
		Axis axis;
		if ((axis = this.y2Axis) != null) return axis.getName();
		return null;
	}

	public @Nullable Axis getXAxis()
	{
		return this.xAxis;
	}

	public @Nullable Axis getY1Axis()
	{
		return this.y1Axis;
	}

	public @Nullable Axis getY2Axis()
	{
		return this.y2Axis;
	}

	public @Nonnull DataType getXAxisType()
	{
		Axis axis;
		if ((axis = this.xAxis) != null) return axis.getType();
		return DataType.None;
	}

	public int getChartCount()
	{
		return this.charts.size();
	}

	public @Nullable ChartParam getChart(int index)
	{
		if (index < 0 || index >= this.charts.size())
			return null;
		return this.charts.get(index);
	}

	public void plot(@Nonnull DrawImage img, double x, double y, double width, double height)
	{
		if (height <= 0 || width <= 0)
			return;
		Axis xAxis;
		Axis y1Axis;
		if ((xAxis = this.xAxis) == null || (y1Axis = this.y1Axis) == null)
			return;

		DrawFont fnt;
		double fntH;
		double barLeng = this.barLength;
		double xLeng;
		double y1Leng;
		double y2Leng;
		boolean y2show;
		String s;

		int i;
		int j;
		ZonedDateTime dt1;
		ZonedDateTime dt2;

		DrawBrush bgBrush = img.newBrushARGB(bgColor);
		DrawPen boundPen = img.newPenARGB(boundColor, this.lineThick, null);
		DrawBrush fontBrush = img.newBrushARGB(fontColor);
		DrawPen gridPen = img.newPenARGB(gridColor, this.lineThick, null);
		DrawPen refLinePen = img.newPenARGB(refLineColor, this.lineThick, null);

		fnt = img.newFontPt(fntName, fntSizePt, DrawFontStyle.AntiAlias, 0);
		img.drawRect(new Coord2DDbl(x, y), new Size2D(width, height), null, bgBrush);

		Size2D rcSize = img.getTextSize(fnt, "AA");
		fntH = rcSize.getHeight();
		String[] titleLines;
		if ((titleLines = this.titleLines) != null)
		{
			i = 0;
			while (i < titleLines.length)
			{
				rcSize = img.getTextSize(fnt, titleLines[i]);
				img.drawString(new Coord2DDbl((x + (width / 2) - (rcSize.getWidth() * 0.5)), y), titleLines[i], fnt, fontBrush);
				y += fntH;
				height -= fntH;
				i++;
			}
		}

		double minXInt = fntH;
		int xMode;
		double labelRotate = xAxis.getLabelRotate();
		if (labelRotate < 45)
		{
			xMode = 1;
		}
		else
		{
			xMode = 0;
		}
		double sRotate = Math.sin(labelRotate * Math.PI / 180.0);
		double cRotate = Math.cos(labelRotate * Math.PI / 180.0);
		double rotLeng;
		xLeng = 0;
		y1Leng = 0;
		y2Leng = 0;
		if (xAxis instanceof Int32Axis)
		{
			Int32Axis iAxis = (Int32Axis)xAxis;
			s = String.valueOf(iAxis.getMax());
			rcSize = img.getTextSize(fnt, s);
			if (xMode == 1)
			{
				rotLeng = rcSize.getWidth() * 0.5 * sRotate + rcSize.getHeight() * cRotate;
				if (rcSize.getWidth() > minXInt) minXInt = rcSize.getWidth();
			}
			else
			{
				rotLeng = rcSize.getWidth() * sRotate + rcSize.getHeight() * 0.5 * cRotate;
			}
			xLeng = rotLeng;

			s = String.valueOf(iAxis.getMin());
			rcSize = img.getTextSize(fnt, s);
			if (xMode == 1)
			{
				rotLeng = rcSize.getWidth() * 0.5 * sRotate + rcSize.getHeight() * cRotate;
				if (rcSize.getWidth() > minXInt) minXInt = rcSize.getHeight();
			}
			else
			{
				rotLeng = rcSize.getWidth() * sRotate + rcSize.getHeight() * 0.5 * cRotate;
			}
			if (rotLeng > xLeng)
				xLeng = rotLeng;
		}
		else if (xAxis instanceof DoubleAxis)
		{
			DoubleAxis dAxis = (DoubleAxis)xAxis;
			DecimalFormat fmt = new DecimalFormat(this.dblFormat);
			s = fmt.format(dAxis.getMax());
			rcSize = img.getTextSize(fnt, s);
			if (xMode == 1)
			{
				rotLeng = rcSize.getWidth() * 0.5 * sRotate + rcSize.getHeight() * cRotate;
				if (rcSize.getWidth() > minXInt) minXInt = rcSize.getWidth();
			}
			else
			{
				rotLeng = rcSize.getWidth() * sRotate + rcSize.getHeight() * 0.5 * cRotate;
			}
			xLeng = rotLeng;
			
			s = fmt.format(dAxis.getMin());
			rcSize = img.getTextSize(fnt, s);
			if (xMode == 1)
			{
				rotLeng = rcSize.getWidth() * 0.5 * sRotate + rcSize.getHeight() * cRotate;
				if (rcSize.getWidth() > minXInt) minXInt = rcSize.getWidth();
			}
			else
			{
				rotLeng = rcSize.getWidth() * sRotate + rcSize.getHeight() * 0.5 * cRotate;
			}
			if (rotLeng > xLeng)
				xLeng = rotLeng;
		}
		else if (xAxis instanceof TimeAxis)
		{
			TimeAxis tAxis = (TimeAxis)xAxis;
			dt1 = DateTimeUtil.newZonedDateTime(tAxis.getMax(), this.timeZoneQHR);
			dt2 = DateTimeUtil.newZonedDateTime(tAxis.getMin(), this.timeZoneQHR);
			if (DateTimeUtil.isSameDay(dt1, dt2))
			{
				s = DateTimeUtil.toString(dt1, this.timeFormat);
				rcSize = img.getTextSize(fnt, s);
				if (xMode == 1)
				{
					rotLeng = rcSize.getWidth() * 0.5 * sRotate + rcSize.getHeight() * cRotate;
					if (rcSize.getWidth() > minXInt) minXInt = rcSize.getWidth();
				}
				else
				{
					rotLeng = rcSize.getWidth() * sRotate + rcSize.getHeight() * 0.5 * cRotate;
				}
				xLeng = rotLeng;
				if (DateTimeUtil.getMSPassedLocalDate(dt2) == 0)
				{
					s = DateTimeUtil.toString(dt2, this.dateFormat);
				}
				else
				{
					s = DateTimeUtil.toString(dt2, this.timeFormat);
				}
				rcSize = img.getTextSize(fnt, s);
				if (xMode == 1)
				{
					rotLeng = rcSize.getWidth() * 0.5 * sRotate + rcSize.getHeight() * cRotate;
					if (rcSize.getWidth() > minXInt) minXInt = rcSize.getWidth();
				}
				else
				{
					rotLeng = rcSize.getWidth() * sRotate + rcSize.getHeight() * 0.5 * cRotate;
				}
				if (rotLeng > xLeng)
					xLeng = rotLeng;
			}
			else
			{
				s = DateTimeUtil.toString(dt1, this.dateFormat);
				rcSize = img.getTextSize(fnt, s);
				if (xMode == 1)
				{
					rotLeng = rcSize.getWidth() * 0.5 * sRotate + rcSize.getHeight() * cRotate;
					if (rcSize.getWidth() > minXInt) minXInt = rcSize.getWidth();
				}
				else
				{
					rotLeng = rcSize.getWidth() * sRotate + rcSize.getHeight() * 0.5 * cRotate;
				}
				xLeng = rotLeng;
				s = DateTimeUtil.toString(dt1, this.timeFormat);
				rcSize = img.getTextSize(fnt, s);
				if (xMode == 1)
				{
					rotLeng = rcSize.getWidth() * 0.5 * sRotate + rcSize.getHeight() * cRotate;
					if (rcSize.getWidth() > minXInt) minXInt = rcSize.getWidth();
				}
				else
				{
					rotLeng = rcSize.getWidth() * sRotate + rcSize.getHeight() * 0.5 * cRotate;
				}
				if (rotLeng > xLeng)
					xLeng = rotLeng;
			}
		}
		if ((s = xAxis.getName()) != null)
		{
			rcSize = img.getTextSize(fnt, s);
			xLeng += rcSize.getHeight();
		}
		xLeng += barLeng;

		if (y1Axis instanceof Int32Axis)
		{
			Int32Axis iAxis = (Int32Axis)y1Axis;
			s = String.valueOf(iAxis.getMax());
			if (this.yUnit != null)
				s = s + this.yUnit;
			rcSize = img.getTextSize(fnt, s);
			y1Leng = rcSize.getWidth();

			s = String.valueOf(iAxis.getMin());
			if (this.yUnit != null)
				s = s + this.yUnit;
			rcSize = img.getTextSize(fnt, s);
			if (rcSize.getWidth() > y1Leng)
				y1Leng = rcSize.getWidth();
		}
		else if (y1Axis instanceof DoubleAxis)
		{
			DoubleAxis dAxis = (DoubleAxis)y1Axis;
			DecimalFormat fmt = new DecimalFormat(this.dblFormat);
			s = fmt.format(dAxis.getMax());
			if (this.yUnit != null)
				s = s + this.yUnit;
			rcSize = img.getTextSize(fnt, s);
			y1Leng = rcSize.getWidth();

			s = fmt.format(dAxis.getMin());
			if (this.yUnit != null)
				s = s + this.yUnit;
			rcSize = img.getTextSize(fnt, s);
			if (rcSize.getWidth() > y1Leng)
				y1Leng = rcSize.getWidth();
		}
		else if (y1Axis instanceof TimeAxis)
		{
			TimeAxis tAxis = (TimeAxis)y1Axis;
			dt1 = DateTimeUtil.newZonedDateTime(tAxis.getMax(), this.timeZoneQHR);
			s = DateTimeUtil.toString(dt1, this.dateFormat);
			rcSize = img.getTextSize(fnt, s);
			y1Leng = rcSize.getWidth();

			dt1 = DateTimeUtil.newZonedDateTime(tAxis.getMin(), this.timeZoneQHR);
			s = DateTimeUtil.toString(dt1, this.dateFormat);
			rcSize = img.getTextSize(fnt, s);;
			if (rcSize.getWidth() > y1Leng)
				y1Leng = rcSize.getWidth();
		}
		if ((s = y1Axis.getName()) != null)
		{
			rcSize = img.getTextSize(fnt, s);
			y1Leng += rcSize.getHeight();
		}
		y1Leng += barLeng;

		Axis y2Axis;
		if ((y2Axis = this.y2Axis) != null)
		{
			if (y2Axis instanceof Int32Axis)
			{
				Int32Axis iAxis = (Int32Axis)y2Axis;
				s = String.valueOf(iAxis.getMax());
				if (this.yUnit != null)
					s = s + this.yUnit;
				rcSize = img.getTextSize(fnt, s);
				y2Leng = rcSize.getWidth();

				s = String.valueOf(iAxis.getMin());
				if (this.yUnit != null)
					s = s + this.yUnit;
				rcSize = img.getTextSize(fnt, s);
				if (rcSize.getWidth() > y2Leng)
					y2Leng = rcSize.getWidth();

				y2Leng += barLeng;
				y2show = true;
			}
			else if (y2Axis instanceof DoubleAxis)
			{
				DoubleAxis dAxis = (DoubleAxis)y2Axis;
				DecimalFormat fmt = new DecimalFormat(this.dblFormat);
				s = fmt.format(dAxis.getMax());
				if (this.yUnit != null)
					s = s + this.yUnit;
				rcSize = img.getTextSize(fnt, s);
				y2Leng = rcSize.getWidth();

				s = fmt.format(dAxis.getMin());
				if (this.yUnit != null)
					s = s + this.yUnit;
				rcSize = img.getTextSize(fnt, s);
				if (rcSize.getWidth() > y2Leng)
					y2Leng = rcSize.getWidth();

				y2Leng += barLeng;
				y2show = true;
			}
			else if (y2Axis instanceof TimeAxis)
			{
				TimeAxis tAxis = (TimeAxis)y2Axis;
				dt1 = DateTimeUtil.newZonedDateTime(tAxis.getMax(), this.timeZoneQHR);
				s = DateTimeUtil.toString(dt1, this.dateFormat);
				rcSize = img.getTextSize(fnt, s);
				y2Leng = rcSize.getWidth();

				dt1 = DateTimeUtil.newZonedDateTime(tAxis.getMin(), this.timeZoneQHR);
				s = DateTimeUtil.toString(dt1, this.dateFormat);
				rcSize = img.getTextSize(fnt, s);
				if (rcSize.getWidth() > y2Leng)
					y2Leng = rcSize.getWidth();

				y2Leng += barLeng;
				y2show = true;
			}
			else
			{
				y2Leng = (rcSize.getHeight() / 2.0);
				y2show = false;
			}
		}
		else
		{
			y2Leng = (rcSize.getHeight() / 2.0);
			y2show = false;
		}

		img.drawLine(x + y1Leng, y, x + y1Leng, y + height - xLeng, boundPen);
		img.drawLine(x + y1Leng, y + height - xLeng, x + width - y2Leng, y + height - xLeng, boundPen);
		if (y2show)
		{
			img.drawLine(x + width - y2Leng, y, x + width - y2Leng, y + height - xLeng, boundPen);
		}
		
		List<Double> locations = new ArrayList<Double>();
		List<String> labels = new ArrayList<String>();
		if (xAxis instanceof Int32Axis)
		{
			Int32Axis iAxis = (Int32Axis)xAxis;
			calScaleMarkInt(locations, labels, iAxis.getMin(), iAxis.getMax(), width - y1Leng - y2Leng - this.pointSize * 2, minXInt, null);
		}
		else if (xAxis instanceof DoubleAxis)
		{
			DoubleAxis dAxis = (DoubleAxis)xAxis;
			calScaleMarkDbl(locations, labels, dAxis.getMin(), dAxis.getMax(), width - y1Leng - y2Leng - this.pointSize * 2, minXInt, this.dblFormat, minDblVal, null);
		}
		else if (xAxis instanceof TimeAxis)
		{
			TimeAxis tAxis = (TimeAxis)xAxis;
			calScaleMarkDate(locations, labels, Timestamp.from(tAxis.getMin()), Timestamp.from(tAxis.getMax()), width - y1Leng - y2Leng - this.pointSize * 2, minXInt, this.dateFormat, this.timeFormat);
		}
		else
		{
		}

		i = 0;
		while (i < locations.size())
		{
			img.drawLine((x + y1Leng + this.pointSize + locations.get(i)), (y + height - xLeng), (x + y1Leng + this.pointSize + locations.get(i)), (y + height - xLeng + barLeng), boundPen);
			i++;
		}

		if (xMode == 1)
		{
			i = 0;
			while (i < locations.size())
			{
				s = StringUtil.orEmpty(labels.get(i));
				Size2D strSize = img.getTextSize(fnt, s);
				img.drawStringRot(new Coord2DDbl((x + y1Leng + this.pointSize + locations.get(i)) - strSize.getHeight() * sRotate - strSize.getWidth() * 0.5 * cRotate, (y + height + barLeng) + strSize.getWidth() * 0.5 * sRotate - strSize.getHeight() * cRotate), StringUtil.orEmpty(labels.get(i)), fnt, fontBrush, labelRotate);
				i += 1;
			}
		}
		else
		{
			i = 0;
			while (i < locations.size())
			{
				s = StringUtil.orEmpty(labels.get(i));
				Size2D strSize = img.getTextSize(fnt, s);
				img.drawStringRot(new Coord2DDbl((x + y1Leng + this.pointSize + locations.get(i)) - strSize.getHeight() * 0.5 * sRotate - strSize.getWidth() * cRotate, (y + height - xLeng + barLeng) + strSize.getWidth() * sRotate - strSize.getHeight() * 0.5 * cRotate), StringUtil.orEmpty(labels.get(i)), fnt, fontBrush, labelRotate);
				i += 1;
			}
		}

		locations.clear();
		labels.clear();

		if (y1Axis instanceof Int32Axis)
		{
			Int32Axis iAxis = (Int32Axis)y1Axis;
			calScaleMarkInt(locations, labels, iAxis.getMin(), iAxis.getMax(), height - xLeng - fntH / 2 - this.pointSize * 2, fntH, this.yUnit);
		}
		else if (y1Axis instanceof DoubleAxis)
		{
			DoubleAxis dAxis = (DoubleAxis)y1Axis;
			calScaleMarkDbl(locations, labels, dAxis.getMin(), dAxis.getMax(), height - xLeng - fntH / 2 - this.pointSize * 2, fntH, this.dblFormat, minDblVal, this.yUnit);
		}
		else if (y1Axis instanceof TimeAxis)
		{
			TimeAxis tAxis = (TimeAxis)y1Axis;
			calScaleMarkDate(locations, labels, Timestamp.from(tAxis.getMin()), Timestamp.from(tAxis.getMax()), height - xLeng - fntH / 2 - this.pointSize * 2, fntH, this.dateFormat, this.timeFormat);
		}
		else
		{
		}

		i = 0;
		while (i < locations.size())
		{
			if (locations.get(i) != 0)
			{
				img.drawLine(x + y1Leng, y + height - this.pointSize - xLeng - locations.get(i), x + width - y2Leng, y + height - this.pointSize - xLeng - locations.get(i), gridPen);
			}
			img.drawLine(x + y1Leng, y + height - this.pointSize - xLeng - locations.get(i), x + y1Leng - barLeng, y + height - this.pointSize - xLeng - locations.get(i), boundPen);
			s = StringUtil.orEmpty(labels.get(i));
			rcSize = img.getTextSize(fnt, s);
			img.drawString(new Coord2DDbl(x + y1Leng - barLeng - rcSize.getWidth(), y + height - this.pointSize - xLeng - locations.get(i) - fntH / 2), s, fnt, fontBrush);
			i++;
		}

		if ((s = y1Axis.getName()) != null)
		{
			Size2D sz = img.getTextSize(fnt, s);
			img.drawStringRot(new Coord2DDbl((x + fntH / 2) - sz.getHeight() * 0.5, (y + (height - xLeng) / 2) + sz.getWidth() * 0.5), s, fnt, fontBrush, 90);
		}

		if ((s = xAxis.getName()) != null)
		{
			rcSize = img.getTextSize(fnt, s);
			img.drawString(new Coord2DDbl((x + y1Leng + (width - y1Leng - y2Leng) / 2 - rcSize.getWidth() / 2), (y + height - rcSize.getHeight())), s, fnt, fontBrush);
		}

		locations.clear();
		labels.clear();


	//	System::Drawing::PointF currPos[];
		Coord2DDbl[] currPos;
		int currPosLen;
		
		i = 0;
		while (i < this.charts.size())
		{
			ChartParam chart = this.charts.get(i);
			if (chart.chartType == ChartType.FilledLine)
			{
				currPosLen = chart.yData.getCount() + 2;
				currPos = new Coord2DDbl[currPosLen];
			}
			else
			{
				currPosLen = chart.yData.getCount();
				currPos = new Coord2DDbl[currPosLen];
			}
			int k = currPosLen;
			while (k-- > 0)
			{
				currPos[k] = new Coord2DDbl();
			}

			double xChartLeng = width - y1Leng - y2Leng - this.pointSize * 2.0;
			xAxis.calcX(chart.xData, currPos, x + y1Leng + this.pointSize, x + y1Leng + this.pointSize + xChartLeng);
			xChartLeng = height - xLeng - fntH / 2 - this.pointSize * 2;
			chart.yAxis.calcY(chart.yData, currPos, y + height - this.pointSize - xLeng, y + height - this.pointSize - xLeng - xChartLeng);

			if (chart.chartType == ChartType.FilledLine)
			{
				if (currPosLen >= 4)
				{
					j = currPosLen;
					currPos[j - 2].x = currPos[j - 3].x;
					currPos[j - 2].y = y + height - xLeng;
					currPos[j - 1].x = currPos[0].x;
					currPos[j - 1].y = y + height - xLeng;
					DrawPen p = img.newPenARGB(chart.lineColor, 1, null);
					DrawBrush b = img.newBrushARGB(chart.fillColor);
					img.drawPolygon(currPos, p, b);
				}
			}
			else if (chart.chartType == ChartType.Line)
			{
				if (currPosLen >= 2)
				{
					DrawPen pen = img.newPenARGB(chart.lineColor, this.lineThick, null);
					img.drawPolyline(currPos, pen);

					if (this.pointType == PointType.Circle && this.pointSize > 0)
					{
						DrawBrush b = img.newBrushARGB(chart.lineColor);
						j = currPosLen;
						while (j-- > 0)
						{
							img.drawEllipse(currPos[j].subtract(this.pointSize), new Size2D(this.pointSize * 2.0, this.pointSize * 2.0), null, b);
						}
					}
				}
			}
			else if (chart.chartType == ChartType.Histogram)
			{
				if (currPosLen > 0)
				{
					DrawPen p = img.newPenARGB(chart.lineColor, 1, null);
					DrawBrush b = img.newBrushARGB(chart.fillColor);
					Double lastX;
					if (currPosLen >= 2)
					{
						lastX = currPos[0].x - currPos[1].x + currPos[0].x;
					}
					else
					{
						lastX = x + y1Leng + this.pointSize;
					}
					double yBottom = y + height - this.pointSize - xLeng;
					Coord2DDbl[] pg = new Coord2DDbl[5];
					j = 0;
					while (j < currPosLen)
					{
						pg[0] = new Coord2DDbl(lastX, yBottom);
						pg[1] = new Coord2DDbl(lastX, currPos[j].y);
						pg[2] = currPos[j];
						pg[3] = new Coord2DDbl(pg[2].x, yBottom);
						pg[4] = pg[0];
						img.drawPolygon(pg, p, b);
						lastX = pg[2].x;
						j++;
					}
				}
			}
			else if (chart.chartType == ChartType.Scatter)
			{
				if (this.pointType == PointType.Circle)
				{
					double pointSize = this.pointSize;
					if (pointSize <= 0)
					{
						pointSize = 3;
					}
					DrawBrush b = img.newBrushARGB(chart.lineColor);
					String[] clabels;
					if ((clabels = chart.labels) != null)
					{
						j = currPosLen;
						while (j-- > 0)
						{
							img.drawEllipse(currPos[j].subtract(pointSize), new Size2D(pointSize * 2.0, pointSize * 2.0), null, b);
							if (clabels[j] != null)
							{
								img.drawString(new Coord2DDbl(currPos[j].x + pointSize, currPos[j].y - fntH * 0.5), clabels[j], fnt, b);
							}
						}
					}
					else
					{
						j = currPosLen;
						while (j-- > 0)
						{
							img.drawEllipse(currPos[j].subtract(pointSize), new Size2D(pointSize * 2.0, pointSize * 2.0), null, b);
						}
					}
				}
			}

			i += 1;
		}

		if (this.refExist)
		{
			double xChartLeng = height - xLeng - fntH / 2;
			int iMax = 0;
			int iMin = 0;
			float yPos;
			double dMax;
			double dMin;
			Instant tMax;
			Instant tMin;

			if (y1Axis instanceof Int32Axis)
			{
				Int32Axis iAxis = (Int32Axis)y1Axis;
				iMax = iAxis.getMax();
				iMin = iAxis.getMin();
				if (this.refInt >= iMin && this.refInt <= iMax)
				{
					yPos = (float)(y + height - xLeng - (double)(this.refInt - iMin) / (float)(iMax - iMin) * xChartLeng);
					img.drawLine(x + y1Leng, (double)yPos, x + width - y2Leng, (double)yPos, refLinePen);

					s = String.valueOf(this.refInt);
					if (this.yUnit != null)
						s = s + this.yUnit;
					if (this.refType == RefType.LeftAlign)
					{
						rcSize = img.getTextSize(fnt, s);
						img.drawString(new Coord2DDbl(x + y1Leng, yPos - rcSize.getHeight()), s, fnt, fontBrush);
					}
					else if (this.refType == RefType.RightAlign)
					{
						rcSize = img.getTextSize(fnt, s);
						img.drawString(new Coord2DDbl(x + width - y2Leng - rcSize.getWidth(), yPos - rcSize.getHeight()), s, fnt, fontBrush);
					}
				}
			}
			else if ((y2Axis = this.y2Axis) != null && y2Axis instanceof Int32Axis)
			{
				Int32Axis iAxis = (Int32Axis)y2Axis;
				iMax = iAxis.getMax();
				iMin = iAxis.getMin();
				if (this.refInt >= iMin && this.refInt <= iMax)
				{
					yPos = (float)(y + height - xLeng - (double)(this.refInt - iMin) / (float)(iMax - iMin) * xChartLeng);
					img.drawLine((double)(x + y1Leng), (double)yPos, (Double)(x + width - y2Leng), (double)yPos, refLinePen);

					s = String.valueOf(this.refInt);
					if (this.yUnit != null)
						s = s + this.yUnit;
					if (this.refType == RefType.LeftAlign)
					{
						rcSize = img.getTextSize(fnt, s);
						img.drawString(new Coord2DDbl(x + y1Leng, yPos - rcSize.getHeight()), s, fnt, fontBrush);
					}
					else if (this.refType == RefType.RightAlign)
					{
						rcSize = img.getTextSize(fnt, s);
						img.drawString(new Coord2DDbl(x + width - y2Leng - rcSize.getWidth(), yPos - rcSize.getHeight()), s, fnt, fontBrush);
					}
				}
			}

			if (y1Axis instanceof DoubleAxis)
			{
				DoubleAxis dAxis = (DoubleAxis)y1Axis;
				dMax = dAxis.getMax();
				dMin = dAxis.getMin();
				if (this.refDbl >= dMin && this.refDbl <= dMax)
				{
					DecimalFormat fmt = new DecimalFormat(this.dblFormat);
					yPos = (float)(y + height - xLeng - (this.refDbl - dMin) / (dMax - dMin) * xChartLeng);
					img.drawLine((double)(x + y1Leng), (double)yPos, (double)(x + width - y2Leng), (double)yPos, refLinePen);

					s = fmt.format(this.refDbl);
					if (this.yUnit != null)
						s = s + this.yUnit;
					if (this.refType == RefType.LeftAlign)
					{
						rcSize = img.getTextSize(fnt, s);
						img.drawString(new Coord2DDbl(x + y1Leng, yPos - rcSize.getHeight()), s, fnt, fontBrush);
					}
					else if (this.refType == RefType.RightAlign)
					{
						rcSize = img.getTextSize(fnt, s);
						img.drawString(new Coord2DDbl(x + width - y2Leng - rcSize.getWidth(), yPos - rcSize.getHeight()), s, fnt, fontBrush);
					}
				}
			}
			else if ((y2Axis = this.y2Axis) != null && y2Axis instanceof DoubleAxis)
			{
				DoubleAxis dAxis = (DoubleAxis)y2Axis;
				dMax = dAxis.getMax();
				dMin = dAxis.getMin();
				if (this.refDbl >= dMin && this.refDbl <= dMax)
				{
					DecimalFormat fmt = new DecimalFormat(this.dblFormat);
					yPos = (float)(y + height - xLeng - (this.refDbl - dMin) / (dMax - dMin) * xChartLeng);
					img.drawLine((double)(x + y1Leng), (double)yPos, (double)(x + width - y2Leng), (double)yPos, refLinePen);

					s = fmt.format(this.refDbl);
					if (this.yUnit != null)
						s = s + this.yUnit;
					if (this.refType == RefType.LeftAlign)
					{
						rcSize = img.getTextSize(fnt, s);
						img.drawString(new Coord2DDbl(x + y1Leng, yPos - rcSize.getHeight()), s, fnt, fontBrush);
					}
					else if (this.refType == RefType.RightAlign)
					{
						rcSize = img.getTextSize(fnt, s);
						img.drawString(new Coord2DDbl(x + width - y2Leng - rcSize.getWidth(), yPos - rcSize.getHeight()), s, fnt, fontBrush);
					}
				}
			}

			if (this.refTime.getEpochSecond() != 0)
			{
				if (y1Axis instanceof TimeAxis)
				{
					TimeAxis tAxis = (TimeAxis)y1Axis;
					tMax = tAxis.getMax();
					tMin = tAxis.getMin();
					if (this.refTime.compareTo(tMin) >= 0 && this.refTime.compareTo(tMax) <= 0)
					{
						yPos = (float)(y + height - xLeng - DateTimeUtil.timeDiffSec(this.refTime, tMin) / DateTimeUtil.timeDiffSec(tMax, tMin) * xChartLeng);
						img.drawLine((double)(x + y1Leng), (double)yPos, (double)(x + width - y2Leng), (double)yPos, refLinePen);
					}
				}
				else if ((y2Axis = this.y2Axis) != null && y2Axis instanceof TimeAxis)
				{
					TimeAxis tAxis = (TimeAxis)y2Axis;
					tMax = tAxis.getMax();
					tMin = tAxis.getMin();
					if (this.refTime.compareTo(tMin) >= 0 && this.refTime.compareTo(tMax) <= 0)
					{
						yPos = (float)(y + height - xLeng - DateTimeUtil.timeDiffSec(this.refTime, tMin) / DateTimeUtil.timeDiffSec(tMax, tMin) * xChartLeng);
						img.drawLine((double)(x + y1Leng), (double)yPos, (double)(x + width - y2Leng), (double)yPos, refLinePen);
					}
				}
			}
		}
	}

	public int getLegendCount()
	{
		return this.charts.size();	
	}

	public @Nullable String getLegend(SharedInt color, int index)
	{
		ChartParam cdata;
		if ((cdata = this.getChart(index)) == null)
			return null;
		color.value = cdata.lineColor;
		return cdata.name;
		
	}

	public boolean savePng(Size2DInt size, @Nonnull String fileName)
	{
		try
		{
			FileOutputStream fs = new FileOutputStream(fileName);
			BufferedImage img = new BufferedImage(size.getWidth(), size.getHeight(), BufferedImage.TYPE_INT_ARGB);
			DrawImage dimg = new DrawImage(img.createGraphics());
			
			this.plot(dimg, 0, 0, size.getWidth(), size.getHeight());
			dimg.dispose();
			StaticImage simg = new StaticImage(img);
			if (ImageUtil.saveAsPng(simg, fs))
			{
				fs.close();
				return true;
			}
			fs.close();
			return false;
		}
		catch (IOException ex)
		{
			return false;
		}
	}

	public static int calScaleMarkDbl(@Nonnull List<Double> locations, @Nonnull List<String> labels, double min, double max, double leng, double minLeng, @Nonnull String dblFormat, double minDblVal, @Nullable String unit)
	{
		int retCnt = 2;
		double scale;
		double lScale;
		double dScale;
		double pos;
		String s;

		DecimalFormat fmt = new DecimalFormat(dblFormat);
		s = fmt.format(min);
		locations.add(0.0);
		if (unit != null)
			s = s + unit;
		labels.add(s);

		scale = minLeng * (max - min) / leng;
		lScale = (int)(Math.log10(scale));
		dScale = Math.pow(10, lScale);
		if (scale / dScale <= 2)
			dScale = Math.pow(10, lScale) * 2;
		else if (scale / dScale <= 5)
			dScale = Math.pow(10, lScale) * 5;
		else
			dScale = Math.pow(10, lScale) * 10;

		if (dScale <= 0 || lScale <= -10)
		{
		}
		else
		{
			scale = (((int)(min / dScale)) + 1) * dScale;
			while (scale < max)
			{
				pos = ((scale - min) * leng / (max - min));
				if ((pos > minLeng) && (pos < leng - minLeng))
				{
					s = fmt.format(scale);
					locations.add(pos);
					if (unit != null)
						s = s + unit;
					labels.add(s);
					retCnt++;
				}
				scale += dScale;
			}
		}

		s = fmt.format(max);
		locations.add(leng);
		if (unit != null)
			s = s + unit;
		labels.add(s);
		return retCnt;
	}

	public static int calScaleMarkInt(@Nonnull List<Double> locations, @Nonnull List<String> labels, int min, int max, double leng, double minLeng, @Nullable String unit)
	{
		int retCnt = 2;
		double scale;
		double lScale;
		double dScale;
		float pos;
		String s;

		s = String.valueOf(min);
		locations.add(0.0);
		if (unit != null)
			s = s + unit;
		labels.add(s);

		scale = minLeng * (double)(max - min) / leng;
		lScale = (int)(Math.log10(scale));
		if (scale < 1)
			dScale = 1;
		else
		{
			dScale = Math.pow(10, lScale);
			if (scale / dScale <= 2)
				dScale = Math.pow(10, lScale) * 2;
			else if (scale / dScale <= 5)
				dScale = Math.pow(10, lScale) * 5;
			else
				dScale = Math.pow(10, lScale) * 10;
		}

		scale = (((int)(min / dScale)) + 1) * dScale;
		while (scale < max)
		{
			pos = (float)((scale - min) * leng / (float)(max - min));
			if ((pos > minLeng) && (pos < leng - minLeng))
			{
				s = String.valueOf(Math.round(scale));
				locations.add((double)pos);
				if (unit != null)
					s = s + unit;
				labels.add(s);
				retCnt++;
			}
			scale += dScale;
		}

		s = String.valueOf(max);
		locations.add(leng);
		if (unit != null)
			s = s + unit;
		labels.add(s);
		return retCnt;
	}

	public static int calScaleMarkDate(@Nonnull List<Double> locations, @Nonnull List<String> labels, @Nonnull Timestamp min, @Nonnull Timestamp max, double leng, double minLeng, @Nonnull String dateFormat, @Nullable String timeFormat)
	{
		int retCnt = 2;
		long timeDif;
		double scale;
		double lScale;
		int iScale;
	//	Double dScale;
		Timestamp currDate;
		double pos;
		boolean hasSecond = true;
		String s;
		if (timeFormat != null)
		{
			if (timeFormat.indexOf('s') == -1)
			{
				hasSecond = false;
			}
		}
		
		timeDif = max.getTime() - min.getTime();
		if (DateTimeUtil.ms2Days(timeDif) * minLeng / leng >= 20)
		{
			s = DateTimeUtil.toString(min, dateFormat);
			locations.add(0.0);
			labels.add(s);
			
			double lastPos = 0;
			currDate = DateTimeUtil.clearDayOfMonth(min);
			while (currDate.before(max))
			{
				currDate = DateTimeUtil.addMonth(currDate, 1);
				pos = DateTimeUtil.ms2Minutes(currDate.getTime() - min.getTime()) * leng / DateTimeUtil.ms2Minutes(max.getTime() - min.getTime());
				if ((pos >= lastPos + minLeng) && (pos < leng - minLeng))
				{
					s = DateTimeUtil.toString(currDate, dateFormat);
					locations.add(pos);
					labels.add(s);
					retCnt++;
				}
			}

			s = DateTimeUtil.toString(max, dateFormat);
			locations.add(leng);
			labels.add(s);
		}
		else if (timeFormat == null || DateTimeUtil.ms2Days(timeDif) * minLeng / leng >= 1)
		{
			s = DateTimeUtil.toString(min, dateFormat);
			locations.add(0.0);
			labels.add(s);

			scale = DateTimeUtil.ms2Days(timeDif) * minLeng / leng;
			lScale = (int)(Math.log10(scale));
			iScale = (int)Math.round(Math.pow(10, lScale));
			if (scale / iScale <= 2)
				iScale = (int)Math.round(Math.pow(10, lScale) * 2);
			else if (scale / iScale <= 5)
				iScale = (int)Math.round(Math.pow(10, lScale) * 5);
			else
				iScale = (int)Math.round(Math.pow(10, lScale) * 10);

			currDate = min;
			currDate = DateTimeUtil.addDay(DateTimeUtil.clearTime(currDate), iScale - (DateTimeUtil.getDay(currDate) % (iScale)));
			if (((double)(currDate.getTime() - min.getTime()) / (double)timeDif) < minLeng / leng)
			{
				currDate = DateTimeUtil.addDay(currDate, iScale);
			}
			while (currDate.before(max))
			{
				pos = DateTimeUtil.ms2Minutes(currDate.getTime() - min.getTime()) * leng / DateTimeUtil.ms2Minutes(max.getTime() - min.getTime());
				if ((pos > minLeng) && (pos < leng - minLeng))
				{
					s = DateTimeUtil.toString(currDate, dateFormat);
					locations.add(pos);
					labels.add(s);
					retCnt++;
				}
				currDate = DateTimeUtil.addDay(currDate, iScale);
			}

			s = DateTimeUtil.toString(max, dateFormat);
			locations.add(leng);
			labels.add(s);
		}
		else if (DateTimeUtil.ms2Hours(timeDif) * minLeng / leng >= 1)
		{
			if (DateTimeUtil.getMSPassedLocalDate(min) == 0)
			{
				s = DateTimeUtil.toString(min, dateFormat);
				locations.add(0.0);
				labels.add(s);
			}
			else
			{
				s = DateTimeUtil.toString(min, timeFormat);
				locations.add(0.0);
				labels.add(s);
			}
			
			scale = DateTimeUtil.ms2Hours(timeDif) * minLeng / leng;
			if (scale <= 2)
				iScale = 2;
			else if (scale <= 3)
				iScale = 3;
			else if (scale <= 6)
				iScale = 6;
			else if (scale <= 12)
				iScale = 12;
			else
				iScale = 24;

			currDate = min;
			currDate = DateTimeUtil.addHour(DateTimeUtil.clearTime(currDate), iScale + (int)(DateTimeUtil.ms2Hours(DateTimeUtil.getMSPassedLocalDate(min)) / iScale) * iScale);
			while (currDate.before(max))
			{
				pos = DateTimeUtil.ms2Minutes(currDate.getTime() - min.getTime()) * leng / DateTimeUtil.ms2Minutes(max.getTime() - min.getTime());
				if ((pos > minLeng) && (pos < leng - minLeng))
				{
					if (DateTimeUtil.getMSPassedLocalDate(currDate) == 0)
					{
						s = DateTimeUtil.toString(currDate, dateFormat);
						locations.add(pos);
						labels.add(s);
					}
					else
					{
						s = DateTimeUtil.toString(currDate, timeFormat);
						locations.add(pos);
						labels.add(s);
					}
					retCnt++;
				}
				currDate = DateTimeUtil.addHour(currDate, iScale);
			}

			if (DateTimeUtil.getMSPassedLocalDate(max) == 0)
			{
				s = DateTimeUtil.toString(max, dateFormat);
				locations.add(leng);
				labels.add(s);
			}
			else
			{
				s = DateTimeUtil.toString(max, timeFormat);
				locations.add(leng);
				labels.add(s);
			}
		}
		else if (!hasSecond || DateTimeUtil.ms2Minutes(timeDif) * minLeng / leng >= 1)
		{
			if (DateTimeUtil.getMSPassedLocalDate(min) == 0)
			{
				s = DateTimeUtil.toString(min, dateFormat);
				locations.add(0.0);
				labels.add(s);
			}
			else
			{
				s = DateTimeUtil.toString(min, timeFormat);
				locations.add(0.0);
				labels.add(s);
			}

			scale = DateTimeUtil.ms2Minutes(timeDif) * minLeng / leng;
			if (scale <= 1)
				iScale = 1;
			else if (scale <= 2)
				iScale = 2;
			else if (scale <= 5)
				iScale = 5;
			else if (scale <= 10)
				iScale = 10;
			else if (scale <= 20)
				iScale = 20;
			else if (scale <= 30)
				iScale = 30;
			else
				iScale = 60;

			currDate = min;
			currDate = DateTimeUtil.addMinute(DateTimeUtil.clearTime(currDate), iScale + (int)(DateTimeUtil.ms2Minutes(DateTimeUtil.getMSPassedLocalDate(min)) / iScale) * iScale);
			while (currDate.before(max))
			{
				pos = DateTimeUtil.ms2Minutes(currDate.getTime() - min.getTime()) * leng / DateTimeUtil.ms2Minutes(max.getTime() - min.getTime());
				if ((pos > minLeng) && (pos < leng - minLeng))
				{
					if (DateTimeUtil.getMSPassedLocalDate(currDate) == 0)
					{
						s = DateTimeUtil.toString(currDate, dateFormat);
						locations.add(pos);
						labels.add(s);
					}
					else
					{
						s = DateTimeUtil.toString(currDate, timeFormat);
						locations.add(pos);
						labels.add(s);
					}
					retCnt++;
				}
				currDate = DateTimeUtil.addMinute(currDate, iScale);
			}

			if (DateTimeUtil.getMSPassedLocalDate(max) == 0)
			{
				s = DateTimeUtil.toString(max, dateFormat);
				locations.add(leng);
				labels.add(s);
			}
			else
			{
				s = DateTimeUtil.toString(max, timeFormat);
				locations.add(leng);
				labels.add(s);
			}
		}
		else if (DateTimeUtil.ms2Seconds(timeDif) >= 1)
		{
			if (DateTimeUtil.getMSPassedLocalDate(min) == 0)
			{
				s = DateTimeUtil.toString(min, dateFormat);
				locations.add(0.0);
				labels.add(s);
			}
			else
			{
				s = DateTimeUtil.toString(min, timeFormat);
				locations.add(0.0);
				labels.add(s);
			}

			scale = DateTimeUtil.ms2Seconds(timeDif) * minLeng / leng;
			if (scale <= 1)
				iScale = 1;
			else if (scale <= 2)
				iScale = 2;
			else if (scale <= 5)
				iScale = 5;
			else if (scale <= 10)
				iScale = 10;
			else if (scale <= 20)
				iScale = 20;
			else if (scale <= 30)
				iScale = 30;
			else
				iScale = 60;

			currDate = min;
			currDate = DateTimeUtil.addSecond(DateTimeUtil.clearTime(currDate), iScale + (int)(DateTimeUtil.ms2Seconds(DateTimeUtil.getMSPassedLocalDate(min)) / iScale) * iScale);
			while (currDate.before(max))
			{
				pos = DateTimeUtil.ms2Minutes(currDate.getTime() - min.getTime()) * leng / DateTimeUtil.ms2Minutes(max.getTime() - min.getTime());
				if ((pos > minLeng) && (pos < leng - minLeng))
				{
					if (DateTimeUtil.getMSPassedLocalDate(currDate) == 0)
					{
						s = DateTimeUtil.toString(currDate, dateFormat);
						locations.add(pos);
						labels.add(s);
					}
					else
					{
						s = DateTimeUtil.toString(currDate, timeFormat);
						locations.add(pos);
						labels.add(s);
					}
					retCnt++;
				}
				currDate = DateTimeUtil.addSecond(currDate, iScale);
			}

			if (DateTimeUtil.getMSPassedLocalDate(max) == 0)
			{
				s = DateTimeUtil.toString(max, dateFormat);
				locations.add(leng);
				labels.add(s);
			}
			else
			{
				s = DateTimeUtil.toString(max, timeFormat);
				locations.add(leng);
				labels.add(s);
			}
		}
		else
		{
			s = DateTimeUtil.toString(min, timeFormat);
			locations.add(0.0);
			labels.add(s);

			s = DateTimeUtil.toString(max, timeFormat);
			locations.add(leng);
			labels.add(s);
		}
		return retCnt;
	}

	public static @Nonnull TimeData newData(@Nonnull Timestamp[] data, int ofst, int count)
	{
		return new TimeData(data, ofst, count);
	}

	public static @Nonnull Int32Data newData(@Nonnull int[] data, int ofst, int count)
	{
		return new Int32Data(data, ofst, count);
	}

	public static @Nonnull DoubleData newData(@Nonnull double[] data, int ofst, int count)
	{
		return new DoubleData(data, ofst, count);
	}

	public static @Nonnull TimeData newDataDate(@Nonnull long[] ticksData)
	{
		return new TimeData(ticksData);
	}

	public static @Nonnull TimeData newDataTime(@Nonnull List<Timestamp> data)
	{
		return new TimeData(data, 0, data.size());
	}

	public static @Nonnull TimeData newDataTime(@Nonnull List<Timestamp> data, int ofst, int count)
	{
		return new TimeData(data, ofst, count);
	}

	public static @Nonnull Int32Data newDataInt32(@Nonnull List<Integer> data)
	{
		return new Int32Data(data, 0, data.size());
	}

	public static @Nonnull Int32Data newDataInt32(@Nonnull List<Integer> data, int ofst, int count)
	{
		return new Int32Data(data, ofst, count);
	}

	public static @Nonnull DoubleData newDataDouble(@Nonnull List<Double> data)
	{
		return new DoubleData(data, 0, data.size());
	}

	public static @Nonnull DoubleData newDataDouble(@Nonnull List<Double> data, int ofst, int count)
	{
		return new DoubleData(data, ofst, count);
	}

	public static @Nonnull Int32Data newDataSeq(int startSeq, int count)
	{
		int[] iArr = new int[count];
		int i = 0;
		while (i < count)
		{
			iArr[i] = startSeq;
			i++;
			startSeq++;
		}
		return new Int32Data(iArr, 0, count);
	}

	public static @Nullable Axis newAxis(@Nonnull ChartData data)
	{
		if (data instanceof Int32Data)
		{
			return new Int32Axis((Int32Data)data);
		}
		else if (data instanceof DoubleData)
		{
			return new DoubleAxis((DoubleData)data);
		}
		else if (data instanceof TimeData)
		{
			return new TimeAxis((TimeData)data);
		}
		return null;
	}

	@Nonnull
	public static <K, V> ChartData newDataFromKey(@Nonnull List<TwinItem<K, V>> vals)
	{
		int i = 0;
		int j = vals.size();
		K k = vals.get(0).key;
		if (k instanceof Timestamp)
		{
			List<Timestamp> arr = new ArrayList<Timestamp>(j);
			while (i < j)
			{
				arr.add((Timestamp)vals.get(i).key);
				i++;
			}
			return newDataTime(arr);
		}
		else if (k instanceof Integer)
		{
			List<Integer> arr = new ArrayList<Integer>(j);
			while (i < j)
			{
				arr.add((Integer)vals.get(i).key);
				i++;
			}
			return newDataInt32(arr);
		}
		else if (k instanceof Double)
		{
			List<Double> arr = new ArrayList<Double>(j);
			while (i < j)
			{
				arr.add((Double)vals.get(i).key);
				i++;
			}
			return newDataDouble(arr);
		}
		return newDataSeq(0, j);
	}

	@Nonnull
	public static <K, V> ChartData newDataFromValue(@Nonnull List<TwinItem<K, V>> vals)
	{
		int i = 0;
		int j = vals.size();
		V v = vals.get(0).value;
		if (v instanceof Timestamp)
		{
			List<Timestamp> arr = new ArrayList<Timestamp>(j);
			while (i < j)
			{
				arr.add((Timestamp)vals.get(i).value);
				i++;
			}
			return newDataTime(arr);
		}
		else if (v instanceof Integer)
		{
			List<Integer> arr = new ArrayList<Integer>(j);
			while (i < j)
			{
				arr.add((Integer)vals.get(i).value);
				i++;
			}
			return newDataInt32(arr);
		}
		else if (v instanceof Double)
		{
			List<Double> arr = new ArrayList<Double>(j);
			while (i < j)
			{
				arr.add((Double)vals.get(i).value);
				i++;
			}
			return newDataDouble(arr);
		}
		return newDataSeq(0, j);
	}
}
