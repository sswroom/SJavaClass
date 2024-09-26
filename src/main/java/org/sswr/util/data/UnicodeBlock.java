package org.sswr.util.data;

import java.util.Objects;

import jakarta.annotation.Nonnull;

public class UnicodeBlock
{
	private int firstCode;
	private int lastCode;
	private boolean dblWidth;
	private String name;

	public UnicodeBlock() {
	}

	public UnicodeBlock(int firstCode, int lastCode, boolean dblWidth, @Nonnull String name) {
		this.firstCode = firstCode;
		this.lastCode = lastCode;
		this.dblWidth = dblWidth;
		this.name = name;
	}

	public int getFirstCode() {
		return this.firstCode;
	}

	public void setFirstCode(int firstCode) {
		this.firstCode = firstCode;
	}

	public int getLastCode() {
		return this.lastCode;
	}

	public void setLastCode(int lastCode) {
		this.lastCode = lastCode;
	}

	public boolean isDblWidth() {
		return this.dblWidth;
	}

	public boolean getDblWidth() {
		return this.dblWidth;
	}

	public void setDblWidth(boolean dblWidth) {
		this.dblWidth = dblWidth;
	}

	@Nonnull 
	public String getName() {
		return this.name;
	}

	public void setName(@Nonnull String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof UnicodeBlock)) {
			return false;
		}
		UnicodeBlock unicodeBlock = (UnicodeBlock) o;
		return firstCode == unicodeBlock.firstCode && lastCode == unicodeBlock.lastCode && dblWidth == unicodeBlock.dblWidth && Objects.equals(name, unicodeBlock.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(firstCode, lastCode, dblWidth, name);
	}

	@Override
	public String toString() {
		return DataTools.toObjectString(this);
	}
}
