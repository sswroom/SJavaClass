package org.sswr.util.math;

import java.util.Objects;

import org.sswr.util.data.DataTools;

public class SpheroidInfo
{
	private int srid;
	private EarthEllipsoid.EarthEllipsoidType eet;
	private String name;


	public SpheroidInfo() {
	}

	public SpheroidInfo(int srid, EarthEllipsoid.EarthEllipsoidType eet, String name) {
		this.srid = srid;
		this.eet = eet;
		this.name = name;
	}

	public int getSrid() {
		return this.srid;
	}

	public void setSrid(int srid) {
		this.srid = srid;
	}

	public EarthEllipsoid.EarthEllipsoidType getEet() {
		return this.eet;
	}

	public void setEet(EarthEllipsoid.EarthEllipsoidType eet) {
		this.eet = eet;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof SpheroidInfo)) {
			return false;
		}
		SpheroidInfo spheroidInfo = (SpheroidInfo) o;
		return srid == spheroidInfo.srid && Objects.equals(eet, spheroidInfo.eet) && Objects.equals(name, spheroidInfo.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(srid, eet, name);
	}

	@Override
	public String toString() {
		return DataTools.toObjectString(this);
	}
}
