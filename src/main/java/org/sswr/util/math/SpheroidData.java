package org.sswr.util.math;

import java.util.Objects;

import org.sswr.util.data.DataTools;

public class SpheroidData
{
	private int srid;
	private String name;
	private EarthEllipsoid ellipsoid;

	public SpheroidData() {
	}

	public SpheroidData(int srid, String name, EarthEllipsoid ellipsoid) {
		this.srid = srid;
		this.name = name;
		this.ellipsoid = ellipsoid;
	}

	public int getSrid() {
		return this.srid;
	}

	public void setSrid(int srid) {
		this.srid = srid;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public EarthEllipsoid getEllipsoid() {
		return this.ellipsoid;
	}

	public void setEllipsoid(EarthEllipsoid ellipsoid) {
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
