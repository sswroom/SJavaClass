package org.sswr.util.media;

import java.util.ArrayList;
import java.util.List;

import org.sswr.util.math.Coord2DDbl;
import org.sswr.util.math.RectAreaDbl;
import org.sswr.util.math.unit.Distance;
import org.sswr.util.math.unit.Distance.DistanceUnit;
import org.sswr.util.media.PaperSize.PaperType;

public class PageSplitter {
	private double drawWidth;
	private double drawHeight;

	public PageSplitter()
	{
		Size2D size = PaperSize.paperTypeGetSizeMM(PaperType.PT_A4);
		this.drawWidth = Distance.convert(DistanceUnit.Millimeter, DistanceUnit.Meter, size.getHeight());
		this.drawHeight = Distance.convert(DistanceUnit.Millimeter, DistanceUnit.Meter, size.getWidth());
	}

	public void setDrawSize(double width, double height, DistanceUnit unit)
	{
		this.drawWidth = Distance.convert(unit, DistanceUnit.Meter, width);
		this.drawHeight = Distance.convert(unit, DistanceUnit.Meter, height);
	}

	public List<RectAreaDbl> splitDrawings(RectAreaDbl objectArea, double objectBuffer, double pageOverlapBuffer, double scale)
	{
		double drawMapWidth = drawWidth * scale;
		double drawMapHeight = drawHeight * scale;
		if (pageOverlapBuffer >= drawMapHeight || pageOverlapBuffer >= drawMapWidth)
		{
			return null;
		}
		objectArea = objectArea.reorder().expand(objectBuffer);
		double objWidth = objectArea.getWidth();
		double objHeight = objectArea.getHeight();
		int paperXCount = 1;
		int paperYCount = 1;
		double totalDrawWidth = drawMapWidth;
		double totalDrawHeight = drawMapHeight;
		while (totalDrawWidth < objWidth)
		{
			paperXCount++;
			totalDrawWidth += drawMapWidth - pageOverlapBuffer;
		}
		while (totalDrawHeight < objHeight)
		{
			paperYCount++;
			totalDrawHeight += drawMapHeight - pageOverlapBuffer;
		}
		List<RectAreaDbl> ret = new ArrayList<RectAreaDbl>();
		Coord2DDbl center = objectArea.getCenter();
		Coord2DDbl tl = new Coord2DDbl(center.x - totalDrawWidth * 0.5, center.y - totalDrawHeight * 0.5);
		double currX;
		double currY = tl.y;
		int i = 0;
		int j = 0;
		while (j < paperYCount)
		{
			currX = tl.x;
			i = 0;
			while (i < paperXCount)
			{
				ret.add(new RectAreaDbl(new Coord2DDbl(currX, currY), new Coord2DDbl(currX + drawMapWidth, currY + drawMapHeight)));
				currX += drawMapWidth - pageOverlapBuffer;
				i++;
			}
			currY += drawMapHeight - pageOverlapBuffer;
			j++;
		}
		return ret;
	}	
}
