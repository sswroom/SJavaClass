package org.sswr.util.math;

public class RectAreaDbl
{
	public Coord2DDbl tl;
	public Coord2DDbl br;
	
	public RectAreaDbl()
	{
		this.tl = new Coord2DDbl();
		this.br = new Coord2DDbl();
	}

	public RectAreaDbl(Coord2DDbl tl, Coord2DDbl br)
	{
		this.tl = tl;
		this.br = br;
	}

	public RectAreaDbl(double left, double top, double width, double height)
	{
		this.tl = new Coord2DDbl(left, top);
		this.br = new Coord2DDbl(left + width, top + height);
	}

	public boolean containPt(double x, double y)
	{
		return (x >= tl.x && x < br.x && y >= tl.y && y < br.y);
	}

	public Coord2DDbl getTL()
	{
		return this.tl;
	}

	public Coord2DDbl getTR()
	{
		return new Coord2DDbl(this.br.x, this.tl.y);
	}

	public Coord2DDbl getBR()
	{
		return this.br;
	}

	public Coord2DDbl getBL()
	{
		return new Coord2DDbl(this.tl.x, this.br.y);
	}

	public Coord2DDbl getCenter()
	{
		return new Coord2DDbl((this.tl.x + this.br.x) * 0.5, (this.tl.y + this.br.y) * 0.5);
	}

	public double getWidth()
	{
		return this.br.x - this.tl.x;
	}

	public double getHeight()
	{
		return this.br.y - this.tl.y;
	}

	public Coord2DDbl getSize()
	{
		return new Coord2DDbl(this.br.x - this.tl.x, this.br.y - this.tl.y);
	}

	public double getArea()
	{
		return this.getWidth() * this.getHeight();
	}

	public Quadrilateral toQuadrilateral()
	{
		return new Quadrilateral(getTL(), getTR(), getBR(), getBL());
	}

	public RectAreaDbl reorder()
	{
		return new RectAreaDbl(this.tl.clone().setMin(this.br), this.tl.clone().setMax(this.br));
	}

	public boolean overlapOrTouch(RectAreaDbl rect)
	{
		return rect.tl.x <= this.br.x && rect.br.x >= this.tl.x && rect.tl.y <= this.br.y && rect.br.y >= this.tl.y;	
	}

	public static void getRectArea(RectAreaDbl area, Coord2DDbl []points)
	{
		int i = points.length - 1;
		Coord2DDbl min = points[i].clone();
		Coord2DDbl max = min.clone();
		while (i-- > 0)
		{
			min.setMin(points[i]);
			max.setMax(points[i]);
		}
		area.tl = min;
		area.br = max;
	}

	public static RectAreaDbl FromQuadrilateral(Quadrilateral quad)
	{
		Coord2DDbl min = quad.tl.clone().setMin(quad.tr).setMin(quad.br).setMin(quad.bl);
		Coord2DDbl max = quad.tl.clone().setMax(quad.tr).setMax(quad.br).setMax(quad.bl);
		return new RectAreaDbl(min, max);
	}
}
