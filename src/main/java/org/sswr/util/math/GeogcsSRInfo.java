package org.sswr.util.math;

import java.util.Objects;

import org.sswr.util.data.DataTools;
import org.sswr.util.math.CoordinateSystem.PrimemType;
import org.sswr.util.math.CoordinateSystem.UnitType;

public class GeogcsSRInfo
{
	private int srid;
	private int datum;
	private String name;
	private PrimemType primem;
	private UnitType unit;
	private double unitScale;


	public GeogcsSRInfo() {
	}

	public GeogcsSRInfo(int srid, int datum, String name, PrimemType primem, UnitType unit, double unitScale) {
		this.srid = srid;
		this.datum = datum;
		this.name = name;
		this.primem = primem;
		this.unit = unit;
		this.unitScale = unitScale;
	}

	public int getSrid() {
		return this.srid;
	}

	public void setSrid(int srid) {
		this.srid = srid;
	}

	public int getDatum() {
		return this.datum;
	}

	public void setDatum(int datum) {
		this.datum = datum;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PrimemType getPrimem() {
		return this.primem;
	}

	public void setPrimem(PrimemType primem) {
		this.primem = primem;
	}

	public UnitType getUnit() {
		return this.unit;
	}

	public void setUnit(UnitType unit) {
		this.unit = unit;
	}

	public double getUnitScale() {
		return this.unitScale;
	}

	public void setUnitScale(double unitScale) {
		this.unitScale = unitScale;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof GeogcsSRInfo)) {
			return false;
		}
		GeogcsSRInfo geogcsSRInfo = (GeogcsSRInfo) o;
		return srid == geogcsSRInfo.srid && datum == geogcsSRInfo.datum && Objects.equals(name, geogcsSRInfo.name) && Objects.equals(primem, geogcsSRInfo.primem) && Objects.equals(unit, geogcsSRInfo.unit) && unitScale == geogcsSRInfo.unitScale;
	}

	@Override
	public int hashCode() {
		return Objects.hash(srid, datum, name, primem, unit, unitScale);
	}

	@Override
	public String toString() {
		return DataTools.toObjectString(this);
	}
}
