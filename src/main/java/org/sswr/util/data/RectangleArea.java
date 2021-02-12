package org.sswr.util.data;

import java.util.Objects;

public class RectangleArea
{
	private double x;
	private double y;
	private double w;
	private double h;

	public RectangleArea() {
	}

	public RectangleArea(double x, double y, double w, double h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
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

	public double getW() {
		return this.w;
	}

	public void setW(double w) {
		this.w = w;
	}

	public double getH() {
		return this.h;
	}

	public void setH(double h) {
		this.h = h;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof RectangleArea)) {
			return false;
		}
		RectangleArea rectangleArea = (RectangleArea) o;
		return x == rectangleArea.x && y == rectangleArea.y && w == rectangleArea.w && h == rectangleArea.h;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y, w, h);
	}

	@Override
	public String toString() {
		return "{" +
			" x='" + getX() + "'" +
			", y='" + getY() + "'" +
			", w='" + getW() + "'" +
			", h='" + getH() + "'" +
			"}";
	}
}
