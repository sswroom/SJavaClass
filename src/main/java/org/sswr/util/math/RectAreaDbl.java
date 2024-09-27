package org.sswr.util.math;

import jakarta.annotation.Nonnull;

public class RectAreaDbl
{
	public @Nonnull Coord2DDbl min;
	public @Nonnull Coord2DDbl max;
	
	public RectAreaDbl()
	{
		this.min = new Coord2DDbl();
		this.max = new Coord2DDbl();
	}

	public RectAreaDbl(@Nonnull Coord2DDbl min, @Nonnull Coord2DDbl max)
	{
		this.min = min;
		this.max = max;
	}

	public RectAreaDbl(double minX, double minY, double width, double height)
	{
		this.min = new Coord2DDbl(minX, minY);
		this.max = new Coord2DDbl(minX + width, minY + height);
	}

	public boolean containPt(double x, double y)
	{
		return (x >= min.x && x < max.x && y >= min.y && y < max.y);
	}

	@Nonnull
	public Coord2DDbl getMin()
	{
		return this.min;
	}

	@Nonnull
	public Coord2DDbl getMax()
	{
		return this.max;
	}

	@Nonnull
	public Coord2DDbl getCenter()
	{
		return new Coord2DDbl((this.min.x + this.max.x) * 0.5, (this.min.y + this.max.y) * 0.5);
	}

	public double getWidth()
	{
		return this.max.x - this.min.x;
	}

	public double getHeight()
	{
		return this.max.y - this.min.y;
	}

	@Nonnull
	public Coord2DDbl getSize()
	{
		return new Coord2DDbl(this.max.x - this.min.x, this.max.y - this.min.y);
	}

	public double getArea()
	{
		return this.getWidth() * this.getHeight();
	}

	@Nonnull
	public Quadrilateral toQuadrilateral()
	{
		return new Quadrilateral(getMin(), new Coord2DDbl(max.x, min.y), getMax(), new Coord2DDbl(min.x, max.y));
	}

	@Nonnull
	public RectAreaDbl reorder()
	{
		return new RectAreaDbl(this.min.clone().setMin(this.max), this.min.clone().setMax(this.max));
	}

	@Nonnull
	public RectAreaDbl expand(double size)
	{
		return new RectAreaDbl(min.subtract(size), max.add(size));
	}

	public boolean equals(@Nonnull RectAreaDbl v)
	{
		return this.min.equals(v.min) && this.max.equals(v.max);
	}

	public boolean notEquals(@Nonnull RectAreaDbl v)
	{
		return !this.min.equals(v.min) || !this.max.equals(v.max);
	}

	@Nonnull
	public RectAreaDbl multiply(double v)
	{
		return new RectAreaDbl(this.min.multiply(v), this.max.multiply(v));
	}

	@Nonnull
	public RectAreaDbl divide(double v)
	{
		double mul = 1 / v;
		return new RectAreaDbl(this.min.multiply(mul), this.max.multiply(mul));
	}

	public boolean overlapOrTouch(@Nonnull RectAreaDbl rect)
	{
		return rect.min.x <= this.max.x && rect.max.x >= this.min.x && rect.min.y <= this.max.y && rect.max.y >= this.min.y;	
	}

	@Nonnull
	public RectAreaDbl clone()
	{
		return new RectAreaDbl(min.clone(), max.clone());
	}

	@Nonnull
	public RectAreaDbl overlapArea(@Nonnull RectAreaDbl area)
	{
		RectAreaDbl ret = area.clone();
		if (ret.min.x <= this.min.x)
		{
			ret.min.x = this.min.x;
		}
		if (ret.min.y <= this.min.y)
		{
			ret.min.y = this.min.y;
		}
		if (ret.max.x >= this.max.x)
		{
			ret.max.x = this.max.x;
		}
		if (ret.max.y >= this.max.y)
		{
			ret.max.y = this.max.y;
		}
		return ret;
	}
	
	@Nonnull
	public RectAreaDbl mergeArea(@Nonnull RectAreaDbl area)
	{
		return new RectAreaDbl(this.min.clone().setMin(area.min), this.max.clone().setMax(area.max));
	}

	@Nonnull
	public RectAreaDbl mergePoint(@Nonnull Coord2DDbl pt)
	{
		return new RectAreaDbl(this.min.clone().setMin(pt), this.max.clone().setMax(pt));
	}

	@Nonnull
	public static RectAreaDbl getRectArea(@Nonnull Coord2DDbl []points)
	{
		int i = points.length - 1;
		Coord2DDbl min = points[i].clone();
		Coord2DDbl max = min.clone();
		while (i-- > 0)
		{
			min.setMin(points[i]);
			max.setMax(points[i]);
		}
		return new RectAreaDbl(min, max);
	}

	@Nonnull
	public static RectAreaDbl fromQuadrilateral(@Nonnull Quadrilateral quad)
	{
		Coord2DDbl min = quad.tl.clone().setMin(quad.tr).setMin(quad.br).setMin(quad.bl);
		Coord2DDbl max = quad.tl.clone().setMax(quad.tr).setMax(quad.br).setMax(quad.bl);
		return new RectAreaDbl(min, max);
	}
}
