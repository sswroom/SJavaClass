package org.sswr.util.media;

import java.util.Objects;

public class Size2DInt {
	private int width;
	private int height;

	public Size2DInt() {
	}

	public Size2DInt(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public int getWidth() {
		return this.width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return this.height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof Size2DInt)) {
			return false;
		}
		Size2DInt size2D = (Size2DInt) o;
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
