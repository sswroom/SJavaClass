package org.sswr.util.basic;

import java.util.Objects;

import jakarta.annotation.Nonnull;

public class Point
{
	private double x;
	private double y;

	public Point() {
	}

	public Point(double x, double y) {
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

	public @Nonnull Point x(double x) {
		setX(x);
		return this;
	}

	public @Nonnull Point y(double y) {
		setY(y);
		return this;
	}

	public @Nonnull Point clone()
	{
		return new Point(this.x, this.y);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof Point)) {
			return false;
		}
		Point point = (Point) o;
		return x == point.x && y == point.y;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

	@Override
	public String toString() {
		return "{" +
			" x='" + getX() + "'" +
			", y='" + getY() + "'" +
			"}";
	}
}
