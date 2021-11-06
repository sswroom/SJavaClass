package org.sswr.util.math;

import java.util.Objects;

import org.sswr.util.data.DataTools;
import org.sswr.util.data.SharedDouble;

public class Point2D extends Vector2D
{
	protected double x;
	protected double y;

	public Point2D(int srid, double x, double y)
	{
		super(srid);
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return this.x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return this.y;
	}

	public void setY(double y) {
		this.y = y;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof Point2D)) {
			return false;
		}
		Point2D point = (Point2D) o;
		return x == point.x && y == point.y && srid == point.srid;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

	@Override
	public String toString() {
		return DataTools.toObjectString(this);
	}

	public VectorType getVectorType()
	{
		return VectorType.Point;
	}

	public void getCenter(SharedDouble x, SharedDouble y)
	{
		x.value = this.x;
		y.value = this.y;
	}

	public Vector2D clone()
	{
		return new Point2D(this.srid, this.x, this.y);
	}

	public void getBounds(SharedDouble minX, SharedDouble minY, SharedDouble maxX, SharedDouble maxY)
	{
		minX.value = this.x;
		minY.value = this.y;
		maxX.value = this.x;
		maxY.value = this.y;
	}

	public double calSqrDistance(double x, double y, SharedDouble nearPtX, SharedDouble nearPtY)
	{
		double xDiff = x - this.x;
		double yDiff = y - this.y;
		if (nearPtX != null && nearPtY != null)
		{
			nearPtX.value = this.x;
			nearPtY.value = this.y;
		}
		return xDiff * xDiff + yDiff * yDiff;
	}

	public boolean joinVector(Vector2D vec)
	{
		return false;
	}

	public void convCSys(CoordinateSystem srcCSys, CoordinateSystem destCSys)
	{
		SharedDouble tmpX = new SharedDouble();
		SharedDouble tmpY = new SharedDouble();
		CoordinateSystem.convertXYZ(srcCSys, destCSys, this.x, this.y, 0, tmpX, tmpY, null);
		this.x = tmpX.value;
		this.y = tmpY.value;
	}
}
