package org.sswr.util.media;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.sswr.util.math.Coord2DDbl;
import org.sswr.util.math.unit.Angle;
import org.sswr.util.math.unit.Angle.AngleUnit;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class DrawImage
{
	public static enum DrawFontStyle
	{
		Normal,
		Bold,
		Italic,
		AntiAlias
	}

	public static class DrawFont
	{
		private Font font;
		private double fontSizePt;

		public DrawFont(@Nonnull String name, double fontSizePt, @Nonnull DrawFontStyle dfs, int codePage)
		{
			if (dfs == DrawFontStyle.Bold)
			{
				name = name + "-BOLD-";
			}
			else if (dfs == DrawFontStyle.Italic)
			{
				name = name + "-ITALIC-";
			}
			else
			{
				name = name + "-PLAIN-";
			}
			this.font = Font.decode(name+Math.round(fontSizePt));
			this.fontSizePt = fontSizePt;
			System.out.println(name+Math.round(fontSizePt));
		}

		public Font getFont()
		{
			return this.font;
		}

		public double getPixelHeight()
		{
			return this.fontSizePt;// * 96.0 / 72.0;
		}
	}

	public static class DrawBrush
	{
		private int color;
		public DrawBrush(int color)
		{
			this.color = color;
		}

		public void init(@Nonnull Graphics2D g)
		{
			g.setColor(new Color(color, true));
		}
	}

	public static class DrawPen
	{
		private int color;
		private double thick;
		private @Nullable int[] pattern;
		public DrawPen(int color, double thick, @Nullable int[] pattern)
		{
			this.color = color;
			this.thick = thick;
			this.pattern = pattern;
		}

		public void init(@Nonnull Graphics2D g)
		{
			g.setColor(new Color(color, true));
			int[] ipattern;
			if ((ipattern = this.pattern) == null)
			{
				g.setStroke(new BasicStroke((float)thick));
			}
			else
			{
				float[] pattern = new float[ipattern.length];
				int i = 0;
				int j = ipattern.length;
				while (i < j)
				{
					pattern[i] = (float)ipattern[i];
					i++;
				}
				g.setStroke(new BasicStroke(i, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, pattern, 0));
			}
		}
	}

	private Graphics2D g;
	public DrawImage(@Nonnull Graphics2D g)
	{
		this.g = g;
	}

	public void dispose()
	{
		this.g.dispose();
	}

	public boolean drawLine(double x1, double y1, double x2, double y2, @Nonnull DrawPen p)
	{
		int ix1 = (int)Math.round(x1);
		int iy1 = (int)Math.round(y1);
		int ix2 = (int)Math.round(x2);
		int iy2 = (int)Math.round(y2);
		p.init(this.g);
		this.g.drawLine(ix1, iy1, ix2, iy2);
		return true;
	}

	public boolean drawPolyline(@Nonnull Coord2DDbl[] points, @Nonnull DrawPen p)
	{
		int[] xPoints = new int[points.length];
		int[] yPoints = new int[points.length];
		int i = 0;
		int j = points.length;
		while (i < j)
		{
			xPoints[i] = (int)Math.round(points[i].x);
			yPoints[i] = (int)Math.round(points[i].y);
			i++;
		}
		p.init(g);
		this.g.drawPolyline(xPoints, yPoints, points.length);
		return true;
	}

	public boolean drawPolygon(@Nonnull Coord2DDbl[] points, @Nullable DrawPen p, @Nullable DrawBrush b)
	{
		int[] xPoints = new int[points.length];
		int[] yPoints = new int[points.length];
		int i = 0;
		int j = points.length;
		while (i < j)
		{
			xPoints[i] = (int)Math.round(points[i].x);
			yPoints[i] = (int)Math.round(points[i].y);
			i++;
		}
		if (b != null)
		{
			b.init(g);
			this.g.fillPolygon(xPoints, yPoints, points.length);
		}
		if (p != null)
		{
			p.init(g);
			this.g.drawPolygon(xPoints, yPoints, points.length);
		}
		return true;
	}

	public boolean drawRect(@Nonnull Coord2DDbl tl, @Nonnull Size2D size, @Nullable DrawPen p, @Nullable DrawBrush b)
	{
		int tlx = (int)Math.round(tl.x);
		int tly = (int)Math.round(tl.y);
		int brx = (int)Math.round(tl.x + size.getWidth());
		int bry = (int)Math.round(tl.y + size.getHeight());
		if (b != null)
		{
			b.init(g);
			this.g.fillRect(tlx, tly, brx - tlx, bry - tly);
		}
		if (p != null)
		{
			p.init(g);
			this.g.drawRect(tlx, tly, brx - tlx, bry - tly);
		}
		return true;
	}

	public boolean drawEllipse(@Nonnull Coord2DDbl tl, @Nonnull Size2D size, @Nullable DrawPen p, @Nullable DrawBrush b)
	{
		int tlx = (int)Math.round(tl.x);
		int tly = (int)Math.round(tl.y);
		int brx = (int)Math.round(tl.x + size.getWidth());
		int bry = (int)Math.round(tl.y + size.getHeight());
		if (b != null)
		{
			b.init(g);
			this.g.fillOval(tlx, tly, brx - tlx, bry - tly);
		}
		if (p != null)
		{
			p.init(g);
			this.g.drawOval(tlx, tly, brx - tlx, bry - tly);
		}
		return true;
	}

	public boolean drawString(@Nonnull Coord2DDbl tl, @Nonnull String str, @Nonnull DrawFont f, @Nonnull DrawBrush b)
	{
		this.g.setFont(f.getFont());
		b.init(g);
		this.g.drawString(str, (float)tl.x, (float)(tl.y + f.getPixelHeight()));
		return true;
	}

	public boolean drawStringRot(@Nonnull Coord2DDbl center, @Nonnull String str, @Nonnull DrawFont f, @Nonnull DrawBrush b, double angleDegreeACW)
	{
		this.g.setFont(f.getFont());
		b.init(g);
		double rangle = Angle.convert(AngleUnit.Degree, AngleUnit.Radian, angleDegreeACW);
		this.g.rotate(-rangle, center.x, center.y);
		this.g.drawString(str, (float)center.x, (float)center.y);
		this.g.rotate(rangle, center.x, center.y);
		return true;
	}

	public @Nonnull DrawPen newPenARGB(int color, double thick, @Nullable int[] pattern)
	{
		return new DrawPen(color, thick, pattern);
	}

	public @Nonnull DrawBrush newBrushARGB(int color)
	{
		return new DrawBrush(color);
	}

	public @Nonnull DrawFont newFontPt(@Nonnull String name, double ptSize, @Nonnull DrawFontStyle fontStyle, int codePage)
	{
		return new DrawFont(name, ptSize, fontStyle, codePage);
	}

	public @Nonnull DrawFont newFontPx(@Nonnull String name, double pxSize, @Nonnull DrawFontStyle fontStyle, int codePage)
	{
		return new DrawFont(name, pxSize * 72.0 / 96.0, fontStyle, codePage);
	}

	public @Nonnull Size2D getTextSize(@Nonnull DrawFont fnt, @Nonnull String txt)
	{
		Font f = fnt.getFont();
		Rectangle2D rect = f.getStringBounds(txt, this.g.getFontRenderContext());
		return new Size2D(rect.getWidth(), rect.getHeight());
	}
}
