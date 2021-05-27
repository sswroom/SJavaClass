package org.sswr.util.media;

import java.util.Objects;

public class Size2D
{
	private double x;
	private double y;

	public Size2D() {
	}

	public Size2D(double x, double y) {
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
		if (!(o instanceof Size2D)) {
			return false;
		}
		Size2D size2D = (Size2D) o;
		return x == size2D.x && y == size2D.y;
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
