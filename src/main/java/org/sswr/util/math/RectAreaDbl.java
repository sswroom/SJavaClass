package org.sswr.util.math;

public class RectAreaDbl
{
	public Coord2DDbl min;
	public Coord2DDbl max;
	
	public RectAreaDbl()
	{
		this.min = new Coord2DDbl();
		this.max = new Coord2DDbl();
	}

	public RectAreaDbl(Coord2DDbl min, Coord2DDbl max)
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

	public Coord2DDbl getMin()
	{
		return this.min;
	}

	public Coord2DDbl getMax()
	{
		return this.max;
	}

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

	public Coord2DDbl getSize()
	{
		return new Coord2DDbl(this.max.x - this.min.x, this.max.y - this.min.y);
	}

	public double getArea()
	{
		return this.getWidth() * this.getHeight();
	}

	public Quadrilateral toQuadrilateral()
	{
		return new Quadrilateral(getMin(), new Coord2DDbl(max.x, min.y), getMax(), new Coord2DDbl(min.x, max.y));
	}

	public RectAreaDbl reorder()
	{
		return new RectAreaDbl(this.min.clone().setMin(this.max), this.min.clone().setMax(this.max));
	}

	public RectAreaDbl expand(double size)
	{
		return new RectAreaDbl(min.subtract(size), max.add(size));
	}

	public boolean equals(RectAreaDbl v)
	{
		return this.min.equals(v.min) && this.max.equals(v.max);
	}

	public boolean notEquals(RectAreaDbl v)
	{
		return !this.min.equals(v.min) || !this.max.equals(v.max);
	}

	public RectAreaDbl multiply(double v)
	{
		return new RectAreaDbl(this.min.multiply(v), this.max.multiply(v));
	}

	public RectAreaDbl divide(double v)
	{
		double mul = 1 / v;
		return new RectAreaDbl(this.min.multiply(mul), this.max.multiply(mul));
	}

	public boolean overlapOrTouch(RectAreaDbl rect)
	{
		return rect.min.x <= this.max.x && rect.max.x >= this.min.x && rect.min.y <= this.max.y && rect.max.y >= this.min.y;	
	}

	public RectAreaDbl clone()
	{
		return new RectAreaDbl(min.clone(), max.clone());
	}

	public RectAreaDbl overlapArea(RectAreaDbl area)
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
	
	public RectAreaDbl mergeArea(RectAreaDbl area)
	{
		return new RectAreaDbl(this.min.clone().setMin(area.min), this.max.clone().setMax(area.max));
	}

	public RectAreaDbl mergePoint(Coord2DDbl pt)
	{
		return new RectAreaDbl(this.min.clone().setMin(pt), this.max.clone().setMax(pt));
	}

	public static RectAreaDbl getRectArea(Coord2DDbl []points)
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

	public static RectAreaDbl fromQuadrilateral(Quadrilateral quad)
	{
		Coord2DDbl min = quad.tl.clone().setMin(quad.tr).setMin(quad.br).setMin(quad.bl);
		Coord2DDbl max = quad.tl.clone().setMax(quad.tr).setMax(quad.br).setMax(quad.bl);
		return new RectAreaDbl(min, max);
	}
}
