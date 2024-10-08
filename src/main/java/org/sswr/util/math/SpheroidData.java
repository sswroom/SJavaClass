package org.sswr.util.math;

import java.util.Objects;

import org.sswr.util.data.DataTools;

import jakarta.annotation.Nonnull;

public class SpheroidData
{
	private int srid;
	private String name;
	private EarthEllipsoid ellipsoid;

	public SpheroidData() {
	}

	public SpheroidData(int srid, @Nonnull String name, @Nonnull EarthEllipsoid ellipsoid) {
		this.srid = srid;
		this.name = name;
		this.ellipsoid = ellipsoid;
	}

	public SpheroidData(@Nonnull SpheroidData data)
	{
		this.srid = data.srid;
		this.name = data.name;
		this.ellipsoid = data.ellipsoid.clone();
	}

	public int getSrid() {
		return this.srid;
	}

	public void setSrid(int srid) {
		this.srid = srid;
	}

	@Nonnull
	public String getName() {
		return this.name;
	}

	public void setName(@Nonnull String name) {
		this.name = name;
	}

	@Nonnull
	public EarthEllipsoid getEllipsoid() {
		return this.ellipsoid;
	}

	public void setEllipsoid(@Nonnull EarthEllipsoid ellipsoid) {
		this.ellipsoid = ellipsoid;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof SpheroidData)) {
			return false;
		}
		SpheroidData spheroidData = (SpheroidData) o;
		return srid == spheroidData.srid && Objects.equals(name, spheroidData.name) && Objects.equals(ellipsoid, spheroidData.ellipsoid);
	}

	@Override
	public int hashCode() {
		return Objects.hash(srid, name, ellipsoid);
	}

	@Override
	public String toString() {
		return DataTools.toObjectString(this);
	}
}
