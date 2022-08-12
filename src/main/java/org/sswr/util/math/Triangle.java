package org.sswr.util.math;

public class Triangle
{
	public Coord2DDbl []pt;

	public Triangle()
	{
		this.pt = new Coord2DDbl[3];
		this.pt[0] = new Coord2DDbl();
		this.pt[1] = new Coord2DDbl();
		this.pt[2] = new Coord2DDbl();
	}

	public Triangle(Coord2DDbl p1, Coord2DDbl p2, Coord2DDbl p3)
	{
		this.pt = new Coord2DDbl[3];
		this.pt[0] = p1;
		this.pt[1] = p2;
		this.pt[2] = p3;
	}

	public double calcArea()
	{
		double a1 = (pt[1].x - pt[0].x) * (pt[0].y - pt[1].y);
		double a2 = (pt[0].y - pt[1].y) * (pt[1].x - pt[2].x);
		double a3 = (pt[1].x - pt[0].x) * (pt[0].y - pt[1].y);
		return (a1 + a2 - a3) * 0.5;
	}	
}
