package org.sswr.util.media;

import java.awt.print.PageFormat;
import java.awt.print.Paper;

import org.sswr.util.math.unit.Distance;
import org.sswr.util.math.unit.Distance.DistanceUnit;

import jakarta.annotation.Nonnull;

public class PaperSize
{
	public enum PaperType
	{
		PT_DEFAULT,
		PT_4A0,
		PT_2A0,
		PT_A0,
		PT_A1,
		PT_A2,
		PT_A3,
		PT_A4,
		PT_A5,
		PT_A6,
		PT_A7,
		PT_A8,
		PT_A9,
		PT_A10,
		PT_B0,
		PT_B1,
		PT_B2,
		PT_B3,
		PT_B4,
		PT_B5,
		PT_B6,
		PT_B7,
		PT_B8,
		PT_B9,
		PT_B10,
		PT_C0,
		PT_C1,
		PT_C2,
		PT_C3,
		PT_C4,
		PT_C5,
		PT_C6,
		PT_C7,
		PT_C8,
		PT_C9,
		PT_C10,
		PT_LETTER,
		PT_GOV_LETTER,
		PT_LEGAL,
		PT_JUNIOR_LEGAL,
		PT_ANSI_A,
		PT_ANSI_B,
		PT_ANSI_C,
		PT_ANSI_D,
		PT_ANSI_E,
		PT_CREDIT_CARD,
		PT_3R,
		PT_4R,
		PT_4D,
		PT_5R,
		PT_6R,
		PT_8R,
		PT_S8R,
		PT_10R,
		PT_S10R,
		PT_11R,
		PT_S11R,
		PT_12R,
		PT_S12R
	}

	private double widthMM;
	private double heightMM;

	public PaperSize(@Nonnull PaperType paperType)
	{
		Size2D size = paperTypeGetSizeMM(paperType);
		this.widthMM = size.getWidth();
		this.heightMM = size.getHeight();
	}

	public double getWidthMM()
	{
		return this.widthMM;
	}

	public double getHeightMM()
	{
		return this.heightMM;
	}

	@Nonnull
	public PageFormat toPageForamt(@Nonnull PageOrientation po)
	{
		PageFormat pf = new PageFormat();
		Paper paper = new Paper();
		if (po == PageOrientation.Portrait)
		{
			paper.setSize(Distance.convert(DistanceUnit.Millimeter, DistanceUnit.Point, this.widthMM), Distance.convert(DistanceUnit.Millimeter, DistanceUnit.Point, this.heightMM));
			pf.setPaper(paper);
			pf.setOrientation(PageFormat.PORTRAIT);
		}
		else
		{
			paper.setSize(Distance.convert(DistanceUnit.Millimeter, DistanceUnit.Point, this.heightMM), Distance.convert(DistanceUnit.Millimeter, DistanceUnit.Point, this.widthMM));
			pf.setPaper(paper);
			pf.setOrientation(PageFormat.LANDSCAPE);
		}
		return pf;
	}

	@Nonnull
	public static Size2D paperTypeGetSizeMM(@Nonnull PaperType paperType)
	{
		double widthMM = 100;
		double heightMM = 100;
		double sqr2 = Math.sqrt(2);
		switch (paperType)
		{
		case PT_4A0:
			widthMM = Math.sqrt(4000000 / sqr2);
			heightMM = widthMM * sqr2;
			break;
		case PT_2A0:
			widthMM = Math.sqrt(2000000 / sqr2);
			heightMM = widthMM * sqr2;
			break;
		case PT_A0:
			widthMM = Math.sqrt(1000000 / sqr2);
			heightMM = widthMM * sqr2;
			break;
		case PT_A1:
			widthMM = Math.sqrt(500000 / sqr2);
			heightMM = widthMM * sqr2;
			break;
		case PT_A2:
			widthMM = Math.sqrt(250000 / sqr2);
			heightMM = widthMM * sqr2;
			break;
		case PT_A3:
			widthMM = Math.sqrt(125000 / sqr2);
			heightMM = widthMM * sqr2;
			break;
		default:
		case PT_DEFAULT:
		case PT_A4:
			widthMM = Math.sqrt(62500 / sqr2);
			heightMM = widthMM * sqr2;
			break;
		case PT_A5:
			widthMM = Math.sqrt(31250 / sqr2);
			heightMM = widthMM * sqr2;
			break;
		case PT_A6:
			widthMM = Math.sqrt(15625 / sqr2);
			heightMM = widthMM * sqr2;
			break;
		case PT_A7:
			widthMM = Math.sqrt(7812.5 / sqr2);
			heightMM = widthMM * sqr2;
			break;
		case PT_A8:
			widthMM = Math.sqrt(3906.25 / sqr2);
			heightMM = widthMM * sqr2;
			break;
		case PT_A9:
			widthMM = Math.sqrt(1953.125 / sqr2);
			heightMM = widthMM * sqr2;
			break;
		case PT_A10:
			widthMM = Math.sqrt(976.5625 / sqr2);
			heightMM = widthMM * sqr2;
			break;
		case PT_B0:
			widthMM = 1000;
			heightMM = widthMM * sqr2;
			break;
		case PT_B1:
			widthMM = 500 * sqr2;
			heightMM = widthMM * sqr2;
			break;
		case PT_B2:
			widthMM = 500;
			heightMM = widthMM * sqr2;
			break;
		case PT_B3:
			widthMM = 250 * sqr2;
			heightMM = widthMM * sqr2;
			break;
		case PT_B4:
			widthMM = 250;
			heightMM = widthMM * sqr2;
			break;
		case PT_B5:
			widthMM = 125 * sqr2;
			heightMM = widthMM * sqr2;
			break;
		case PT_B6:
			widthMM = 125;
			heightMM = widthMM * sqr2;
			break;
		case PT_B7:
			widthMM = 62.5 * sqr2;
			heightMM = widthMM * sqr2;
			break;
		case PT_B8:
			widthMM = 62.5;
			heightMM = widthMM * sqr2;
			break;
		case PT_B9:
			widthMM = 31.25 * sqr2;
			heightMM = widthMM * sqr2;
			break;
		case PT_B10:
			widthMM = 31.25;
			heightMM = widthMM * sqr2;
			break;
		case PT_C0:
			heightMM = 1000 / Math.pow(2, -3.0 / 8.0) + 0.2;
			widthMM = heightMM / sqr2;
			break;
		case PT_C1:
			heightMM = 1000 / Math.pow(2, 1.0 / 8.0) + 0.2;
			widthMM = heightMM / sqr2;
			break;
		case PT_C2:
			heightMM = 1000 / Math.pow(2, 5.0 / 8.0) + 0.2;
			widthMM = heightMM / sqr2;
			break;
		case PT_C3:
			heightMM = 1000 / Math.pow(2, 9.0 / 8.0) + 0.2;
			widthMM = heightMM / sqr2;
			break;
		case PT_C4:
			heightMM = 1000 / Math.pow(2, 13.0 / 8.0) + 0.2;
			widthMM = heightMM / sqr2;
			break;
		case PT_C5:
			heightMM = 1000 / Math.pow(2, 17.0 / 8.0) + 0.2;
			widthMM = heightMM / sqr2;
			break;
		case PT_C6:
			heightMM = 1000 / Math.pow(2, 21.0 / 8.0) + 0.2;
			widthMM = heightMM / sqr2;
			break;
		case PT_C7:
			heightMM = 1000 / Math.pow(2, 25.0 / 8.0) + 0.2;
			widthMM = heightMM / sqr2;
			break;
		case PT_C8:
			heightMM = 1000 / Math.pow(2, 29.0 / 8.0) + 0.2;
			widthMM = heightMM / sqr2;
			break;
		case PT_C9:
			heightMM = 1000 / Math.pow(2, 33.0 / 8.0) + 0.2;
			widthMM = heightMM / sqr2;
			break;
		case PT_C10:
			heightMM = 1000 / Math.pow(2, 37.0 / 8.0) + 0.2;
			widthMM = heightMM / sqr2;
			break;
		case PT_LETTER:
		case PT_ANSI_A:
			widthMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 8.5) * 1000;
			heightMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 11) * 1000;
			break;
		case PT_GOV_LETTER:
			widthMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 8.0) * 1000;
			heightMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 10.5) * 1000;
			break;
		case PT_LEGAL:
			widthMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 8.5) * 1000;
			heightMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 14) * 1000;
			break;
		case PT_JUNIOR_LEGAL:
			widthMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 5.0) * 1000;
			heightMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 8.0) * 1000;
			break;
		case PT_ANSI_B:
			widthMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 11) * 1000;
			heightMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 17) * 1000;
			break;
		case PT_ANSI_C:
			widthMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 17) * 1000;
			heightMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 22) * 1000;
			break;
		case PT_ANSI_D:
			widthMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 22) * 1000;
			heightMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 34) * 1000;
			break;
		case PT_ANSI_E:
			widthMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 34) * 1000;
			heightMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 44) * 1000;
			break;
		case PT_CREDIT_CARD:
			widthMM = 53.98;
			heightMM = 85.60;
			break;
		case PT_3R:
			widthMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 3) * 1000;
			heightMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 5) * 1000;
			break;
		case PT_4R:
			widthMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 4) * 1000;
			heightMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 6) * 1000;
			break;
		case PT_4D:
			widthMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 4.5) * 1000;
			heightMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 6) * 1000;
			break;
		case PT_5R:
			widthMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 5) * 1000;
			heightMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 7) * 1000;
			break;
		case PT_6R:
			widthMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 6) * 1000;
			heightMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 8) * 1000;
			break;
		case PT_8R:
			widthMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 8) * 1000;
			heightMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 10) * 1000;
			break;
		case PT_S8R:
			widthMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 8) * 1000;
			heightMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 12) * 1000;
			break;
		case PT_10R:
			widthMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 10) * 1000;
			heightMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 12) * 1000;
			break;
		case PT_S10R:
			widthMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 10) * 1000;
			heightMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 15) * 1000;
			break;
		case PT_11R:
			widthMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 11) * 1000;
			heightMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 14) * 1000;
			break;
		case PT_S11R:
			widthMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 11) * 1000;
			heightMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 17) * 1000;
			break;
		case PT_12R:
			widthMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 12) * 1000;
			heightMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 15) * 1000;
			break;
		case PT_S12R:
			widthMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 12) * 1000;
			heightMM = Distance.convert(Distance.DistanceUnit.Inch, Distance.DistanceUnit.Meter, 18) * 1000;
			break;
		}
		return new Size2D(widthMM, heightMM);
	}
}
