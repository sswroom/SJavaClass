package org.sswr.util.media;

import java.util.Objects;

public class Size2D
{
	private double width;
	private double height;

	public Size2D() {
	}

	public Size2D(double width, double height) {
		this.width = width;
		this.height = height;
	}

	public double getWidth() {
		return this.width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double getHeight() {
		return this.height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof Size2D)) {
			return false;
		}
		Size2D size2D = (Size2D) o;
		return width == size2D.width && height == size2D.height;
	}

	@Override
	public int hashCode() {
		return Objects.hash(width, height);
	}

	@Override
	public String toString() {
		return "{" +
			" width='" + getWidth() + "'" +
			", height='" + getHeight() + "'" +
			"}";
	}
}
