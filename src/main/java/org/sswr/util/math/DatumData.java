package org.sswr.util.math;

import java.util.Objects;

import org.sswr.util.data.DataTools;
import org.sswr.util.math.unit.Angle;

public class DatumData
{
	private int srid;
	private SpheroidData spheroid;
	private String name;
	private double x0;
	private double y0;
	private double z0;
	private double cX;
	private double cY;
	private double cZ;
	private double xAngle;
	private double yAngle;
	private double zAngle;
	private double scale;
	private Angle.AngleUnit aunit;


	public DatumData() {
	}

	public DatumData(int srid, SpheroidData spheroid, String name, double x0, double y0, double z0, double cX, double cY, double cZ, double xAngle, double yAngle, double zAngle, double scale, Angle.AngleUnit aunit) {
		this.srid = srid;
		this.spheroid = spheroid;
		this.name = name;
		this.x0 = x0;
		this.y0 = y0;
		this.z0 = z0;
		this.cX = cX;
		this.cY = cY;
		this.cZ = cZ;
		this.xAngle = xAngle;
		this.yAngle = yAngle;
		this.zAngle = zAngle;
		this.scale = scale;
		this.aunit = aunit;
	}

	public int getSrid() {
		return this.srid;
	}

	public void setSrid(int srid) {
		this.srid = srid;
	}

	public SpheroidData getSpheroid() {
		return this.spheroid;
	}

	public void setSpheroid(SpheroidData spheroid) {
		this.spheroid = spheroid;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getX0() {
		return this.x0;
	}

	public void setX0(double x0) {
		this.x0 = x0;
	}

	public double getY0() {
		return this.y0;
	}

	public void setY0(double y0) {
		this.y0 = y0;
	}

	public double getZ0() {
		return this.z0;
	}

	public void setZ0(double z0) {
		this.z0 = z0;
	}

	public double getCX() {
		return this.cX;
	}

	public void setCX(double cX) {
		this.cX = cX;
	}

	public double getCY() {
		return this.cY;
	}

	public void setCY(double cY) {
		this.cY = cY;
	}

	public double getCZ() {
		return this.cZ;
	}

	public void setCZ(double cZ) {
		this.cZ = cZ;
	}

	public double getXAngle() {
		return this.xAngle;
	}

	public void setXAngle(double xAngle) {
		this.xAngle = xAngle;
	}

	public double getYAngle() {
		return this.yAngle;
	}

	public void setYAngle(double yAngle) {
		this.yAngle = yAngle;
	}

	public double getZAngle() {
		return this.zAngle;
	}

	public void setZAngle(double zAngle) {
		this.zAngle = zAngle;
	}

	public double getScale() {
		return this.scale;
	}

	public void setScale(double scale) {
		this.scale = scale;
	}

	public Angle.AngleUnit getAunit() {
		return this.aunit;
	}

	public void setAunit(Angle.AngleUnit aunit) {
		this.aunit = aunit;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof DatumData)) {
			return false;
		}
		DatumData datumData = (DatumData) o;
		return srid == datumData.srid && Objects.equals(spheroid, datumData.spheroid) && Objects.equals(name, datumData.name) && x0 == datumData.x0 && y0 == datumData.y0 && z0 == datumData.z0 && cX == datumData.cX && cY == datumData.cY && cZ == datumData.cZ && xAngle == datumData.xAngle && yAngle == datumData.yAngle && zAngle == datumData.zAngle && scale == datumData.scale && Objects.equals(aunit, datumData.aunit);
	}

	@Override
	public int hashCode() {
		return Objects.hash(srid, spheroid, name, x0, y0, z0, cX, cY, cZ, xAngle, yAngle, zAngle, scale, aunit);
	}

	@Override
	public String toString() {
		return DataTools.toObjectString(this);
	}
}
