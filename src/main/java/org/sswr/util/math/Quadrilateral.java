package org.sswr.util.math;

import jakarta.annotation.Nonnull;

public class Quadrilateral
{
	public @Nonnull Coord2DDbl tl;
	public @Nonnull Coord2DDbl tr;
	public @Nonnull Coord2DDbl br;
	public @Nonnull Coord2DDbl bl;

	public Quadrilateral()
	{
		this.tl = new Coord2DDbl();
		this.tr = new Coord2DDbl();
		this.br = new Coord2DDbl();
		this.bl = new Coord2DDbl();
	}

	public Quadrilateral(@Nonnull Coord2DDbl tl, @Nonnull Coord2DDbl tr, @Nonnull Coord2DDbl br, @Nonnull Coord2DDbl bl)
	{
		this.tl = tl;
		this.tr = tr;
		this.br = br;
		this.bl = bl;
	}

	public double calcMaxTiltAngle()
	{
		double deg360 = Math.PI * 2;
		double deg90 = Math.PI * 0.5;
		double deg270 = Math.PI + deg90;
		double []dir = new double[4];
		dir[0] = Math.atan2(tl.y - tr.y, tl.x - tr.x);
		dir[1] = Math.atan2(tr.y - br.y, tr.x - br.x);
		dir[2] = Math.atan2(br.y - bl.y, br.x - bl.x);
		dir[3] = Math.atan2(bl.y - tl.y, bl.x - tl.x);
		double []ang = new double[4];
		ang[0] = dir[0] - dir[1];
		ang[1] = dir[1] - dir[2];
		ang[2] = dir[2] - dir[3];
		ang[3] = dir[3] - dir[0];
		double maxTiltAngle = 0;
		double tiltAngle;
		int i = 4;
		while (i-- > 0)
		{
			if (ang[i] < 0) ang[i] += deg360;
			if (ang[i] < Math.PI)
			{
				tiltAngle = ang[i] - deg90;
			}
			else
			{
				tiltAngle = ang[i] - deg270;
			}
			if (tiltAngle < 0)
			{
				tiltAngle = -tiltAngle;
			}
			if (tiltAngle > maxTiltAngle) maxTiltAngle = tiltAngle;
		}
		return maxTiltAngle;
	}

	public double calcArea()
	{
		return new Triangle(tl, tr, bl).calcArea() + new Triangle(tr, bl, br).calcArea();
	}

	public double calcLenLeft()
	{
		return tl.calcLengTo(bl);
	}

	public double calcLenTop()
	{
		return tl.calcLengTo(tr);
	}

	public double calcLenRight()
	{
		return tr.calcLengTo(br);
	}

	public double calcLenBottom()
	{
		return br.calcLengTo(bl);
	}

	@Nonnull
	public static Quadrilateral fromPolygon(@Nonnull Coord2DDbl []pg)
	{
		double minX = pg[3].x;
		double minY = pg[3].y;
		double maxX = minX;
		double maxY = minY;
		int i = 3;
		while (i-- > 0)
		{
			if (pg[i].x < minX) minX = pg[i].x;
			if (pg[i].x > maxX) maxX = pg[i].x;
			if (pg[i].y < minY) minY = pg[i].y;
			if (pg[i].y > maxY) maxY = pg[i].y;
		}
	
		double diff;
		double tldiff = pg[3].x - minX + pg[3].y - minY;
		int tlIndex = 3;
		i = 3;
		while (i-- > 0)
		{
			diff = pg[i].x - minX + pg[i].y - minY;
			if (diff < tldiff)
			{
				tldiff = diff;
				tlIndex = i;
			}
		}
		Quadrilateral quad = new Quadrilateral();
		switch (tlIndex)
		{
		case 0:
			quad.tl = pg[0];
			quad.br = pg[2];
			if (pg[1].x > pg[3].x)
			{
				quad.tr = pg[1];
				quad.bl = pg[3];
			}
			else
			{
				quad.tr = pg[3];
				quad.bl = pg[1];
			}
			break;
		case 1:
			quad.tl = pg[1];
			quad.br = pg[3];
			if (pg[0].x > pg[2].x)
			{
				quad.tr = pg[0];
				quad.bl = pg[2];
			}
			else
			{
				quad.tr = pg[2];
				quad.bl = pg[0];
			}
			break;
		case 2:
			quad.tl = pg[2];
			quad.br = pg[0];
			if (pg[1].x > pg[3].x)
			{
				quad.tr = pg[1];
				quad.bl = pg[3];
			}
			else
			{
				quad.tr = pg[3];
				quad.bl = pg[1];
			}
			break;
		default:
			quad.tl = pg[3];
			quad.br = pg[1];
			if (pg[0].x > pg[2].x)
			{
				quad.tr = pg[0];
				quad.bl = pg[2];
			}
			else
			{
				quad.tr = pg[2];
				quad.bl = pg[0];
			}
			break;
		}
		return quad;
	}
}
