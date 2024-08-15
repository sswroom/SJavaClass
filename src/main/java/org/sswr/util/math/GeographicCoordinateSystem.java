package org.sswr.util.math;

import org.sswr.util.basic.Vector3;
import org.sswr.util.math.geometry.LineString;
import org.sswr.util.math.unit.Angle;
import org.sswr.util.math.unit.Distance;

public class GeographicCoordinateSystem extends CoordinateSystem
{
	private String csysName;
	private DatumData datum;
	private PrimemType primem;
	private UnitType unit;

	public GeographicCoordinateSystem(String sourceName, int srid, String csysName, DatumData datum, PrimemType primem, UnitType unit)
	{
		super(sourceName, srid, csysName);
		this.csysName = csysName;
		this.datum = new DatumData();
		this.datum.setSpheroid(new SpheroidData(datum.getSpheroid()));
		this.datum.setSrid(datum.getSrid());
		Double aRatio = Angle.getUnitRatio(datum.getAunit());
		this.datum.setName(datum.getName());
		this.datum.setX0(datum.getX0());
		this.datum.setY0(datum.getY0());
		this.datum.setZ0(datum.getZ0());
		this.datum.setCX(datum.getCX());
		this.datum.setCY(datum.getCY());
		this.datum.setCZ(datum.getCZ());
		this.datum.setXAngle(datum.getXAngle() * aRatio);
		this.datum.setYAngle(datum.getYAngle() * aRatio);
		this.datum.setZAngle(datum.getZAngle() * aRatio);
		this.datum.setScale(datum.getScale());
		this.datum.setAunit(Angle.AngleUnit.Radian);
		this.primem = primem;
		this.unit = unit;
	}

	public double calSurfaceDistance(Coord2DDbl pos1, Coord2DDbl pos2, Distance.DistanceUnit unit)
	{
		return this.datum.getSpheroid().getEllipsoid().calSurfaceDistance(pos1.y, pos1.x, pos2.y, pos2.x, unit);
	}

	public double calLineStringDistance(LineString lineString, boolean include3D, Distance.DistanceUnit unit)
	{
		return this.datum.getSpheroid().getEllipsoid().calLineStringDistance(lineString, include3D, unit);
	}

	public CoordinateSystem clone()
	{
		return new GeographicCoordinateSystem(this.sourceName, this.srid, this.csysName, this.datum, this.primem, this.unit);
	}

	public CoordinateSystemType getCoordSysType()
	{
		return CoordinateSystemType.Geographic;
	}

	public boolean isProjected()
	{
		return false;
	}

	public void toString(StringBuilder sb)
	{
		sb.append("Geographic File Name: ");
		sb.append(this.sourceName);
		sb.append("\r\nSRID: ");
		sb.append(this.srid);
		sb.append("\r\nGeographic Name: ");
		sb.append(this.csysName);
		sb.append("\r\nDatum Name: ");
		sb.append(this.datum.getName());
		sb.append("\r\nRotate Center: ");
		sb.append(this.datum.getX0());
		sb.append(", ");
		sb.append(this.datum.getY0());
		sb.append(", ");
		sb.append(this.datum.getZ0());
		sb.append("\r\nShifting: ");
		sb.append(this.datum.getCX());
		sb.append(", ");
		sb.append(this.datum.getCY());
		sb.append(", ");
		sb.append(this.datum.getCZ());
		sb.append("\r\nRotation: ");
		sb.append(this.datum.getXAngle());
		sb.append(", ");
		sb.append(this.datum.getYAngle());
		sb.append(", ");
		sb.append(this.datum.getZAngle());
		sb.append("\r\nScale Factor: ");
		sb.append(this.datum.getScale());
		sb.append("\r\nSemi-Major Axis: ");
		sb.append(this.datum.getSpheroid().getEllipsoid().getSemiMajorAxis());
		sb.append("\r\nInverse Flattening: ");
		sb.append(this.datum.getSpheroid().getEllipsoid().getInverseFlattening());
	}

	public EarthEllipsoid getEllipsoid()
	{
		return this.datum.getSpheroid().getEllipsoid();
	}

	public String getDatumName()
	{
		return this.datum.getName();
	}

	public DatumData getDatum()
	{
		return this.datum;
	}

	public PrimemType getPrimem()
	{
		return this.primem;
	}

	public UnitType getUnit()
	{
		return this.unit;
	}

	public Vector3 toCartesianCoordRad(Vector3 lonLatH)
	{
		Vector3 tmpPos = this.datum.getSpheroid().getEllipsoid().toCartesianCoordRad(lonLatH);
		if (this.datum.getScale() == 0 && this.datum.getXAngle() == 0 && this.datum.getYAngle() == 0 && this.datum.getZAngle() == 0)
		{
			return new Vector3(tmpPos.val[0] + datum.getCX(),
				tmpPos.val[1] + datum.getCY(),
				tmpPos.val[2] + datum.getCZ());
		}
		else
		{
			tmpPos.val[0] -= this.datum.getX0();
			tmpPos.val[1] -= this.datum.getY0();
			tmpPos.val[2] -= this.datum.getZ0();
			double s = 1 + this.datum.getScale() * 0.000001;
			return new Vector3(
				s * (                     tmpPos.val[0] - datum.getZAngle() * tmpPos.val[1] + datum.getYAngle() * tmpPos.val[2]) + datum.getCX() + this.datum.getX0(),
				s * ( datum.getZAngle() * tmpPos.val[0] +                     tmpPos.val[1] - datum.getXAngle() * tmpPos.val[2]) + datum.getCY() + this.datum.getY0(),
				s * (-datum.getYAngle() * tmpPos.val[0] + datum.getXAngle() * tmpPos.val[1] +                     tmpPos.val[2]) + datum.getCZ() + this.datum.getZ0());
		}
	}

	public Vector3 fromCartesianCoordRad(Vector3 coord)
	{
		Vector3 tmpPos;
		if (this.datum.getScale() == 0 && this.datum.getXAngle() == 0 && this.datum.getYAngle() == 0 && this.datum.getZAngle() == 0)
		{
			tmpPos = new Vector3(
				coord.val[0] - this.datum.getCX(),
				coord.val[1] - this.datum.getCY(),
				coord.val[2] - this.datum.getCZ());
		}
		else
		{
			tmpPos = new Vector3(
				coord.val[0] - this.datum.getX0() - datum.getCX(),
				coord.val[1] - this.datum.getY0() - datum.getCY(),
				coord.val[2] - this.datum.getZ0() - datum.getCZ());
			double s = 1 / (1 + this.datum.getScale() * 0.000001);
			tmpPos = new Vector3(
				s * (                          tmpPos.val[0] + this.datum.getZAngle() * tmpPos.val[1] - this.datum.getYAngle() * tmpPos.val[2]) + this.datum.getX0(),
				s * (-this.datum.getZAngle() * tmpPos.val[0] +                          tmpPos.val[1] + this.datum.getXAngle() * tmpPos.val[2]) + this.datum.getY0(),
				s * ( this.datum.getYAngle() * tmpPos.val[0] - this.datum.getXAngle() * tmpPos.val[1] +                          tmpPos.val[2]) + this.datum.getZ0());
		}
		return this.datum.getSpheroid().getEllipsoid().fromCartesianCoordRad(tmpPos);
	}

	public Vector3 toCartesianCoordDeg(Vector3 lonLatH)
	{
		return this.toCartesianCoordRad(new Vector3(lonLatH.val[0] * Math.PI / 180.0, lonLatH.val[1] * Math.PI / 180.0, lonLatH.val[2]));
	}

	public Vector3 fromCartesianCoordDeg(Vector3 coord)
	{
		Vector3 lonLatH = this.fromCartesianCoordRad(coord);
		lonLatH.val[0] = lonLatH.val[0] * 180.0 / Math.PI;
		lonLatH.val[1] = lonLatH.val[1] * 180.0 / Math.PI;
		return lonLatH;
	}
}
