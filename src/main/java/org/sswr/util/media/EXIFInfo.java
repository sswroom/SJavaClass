package org.sswr.util.media;

import java.util.Objects;

import org.sswr.util.data.DataTools;

import jakarta.annotation.Nonnull;

public class EXIFInfo
{
	private int id;
	private String name;

	public EXIFInfo() {
	}

	public EXIFInfo(int id, @Nonnull String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
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
		if (!(o instanceof EXIFInfo)) {
			return false;
		}
		EXIFInfo eXIFInfo = (EXIFInfo) o;
		return id == eXIFInfo.id && Objects.equals(name, eXIFInfo.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name);
	}

	@Override
	public String toString() {
		return DataTools.toObjectString(this);
	}

}
