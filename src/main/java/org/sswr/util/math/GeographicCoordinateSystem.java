package org.sswr.util.math;

import org.sswr.util.data.SharedDouble;
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

	public void toCartesianCoordRad(double lat, double lon, double h, SharedDouble x, SharedDouble y, SharedDouble z)
	{
		SharedDouble tmpX = new SharedDouble();
		SharedDouble tmpY = new SharedDouble();
		SharedDouble tmpZ = new SharedDouble();
		this.datum.getSpheroid().getEllipsoid().toCartesianCoordRad(lat, lon, h, tmpX, tmpY, tmpZ);
		if (this.datum.getScale() == 0 && this.datum.getXAngle() == 0 && this.datum.getYAngle() == 0 && this.datum.getZAngle() == 0)
		{
			x.value = tmpX.value + datum.getCX();
			y.value = tmpY.value + datum.getCY();
			z.value = tmpZ.value + datum.getCZ();
		}
		else
		{
			tmpX.value -= this.datum.getX0();
			tmpY.value -= this.datum.getY0();
			tmpZ.value -= this.datum.getZ0();
			double s = 1 + this.datum.getScale() * 0.000001;
			x.value = s * (                     tmpX.value - datum.getZAngle() * tmpY.value + datum.getYAngle() * tmpZ.value) + datum.getCX() + this.datum.getX0();
			y.value = s * ( datum.getZAngle() * tmpX.value +                     tmpY.value - datum.getXAngle() * tmpZ.value) + datum.getCY() + this.datum.getY0();
			z.value = s * (-datum.getYAngle() * tmpX.value + datum.getXAngle() * tmpY.value +                     tmpZ.value) + datum.getCZ() + this.datum.getZ0();
		}
	}

	public void fromCartesianCoordRad(double x, double y, double z, SharedDouble lat, SharedDouble lon, SharedDouble h)
	{
		double tmpX;
		double tmpY;
		double tmpZ;
		if (this.datum.getScale() == 0 && this.datum.getXAngle() == 0 && this.datum.getYAngle() == 0 && this.datum.getZAngle() == 0)
		{
			tmpX = x - this.datum.getCX();
			tmpY = y - this.datum.getCY();
			tmpZ = z - this.datum.getCZ();
		}
		else
		{
			x = x - this.datum.getX0() - datum.getCX();
			y = y - this.datum.getY0() - datum.getCY();
			z = z - this.datum.getZ0() - datum.getCZ();
			double s = 1 / (1 + this.datum.getScale() * 0.000001);
			tmpX = s * (                          x + this.datum.getZAngle() * y - this.datum.getYAngle() * z) + this.datum.getX0();
			tmpY = s * (-this.datum.getZAngle() * x +                          y + this.datum.getXAngle() * z) + this.datum.getY0();
			tmpZ = s * ( this.datum.getYAngle() * x - this.datum.getXAngle() * y +                          z) + this.datum.getZ0();
		}
		this.datum.getSpheroid().getEllipsoid().fromCartesianCoordRad(tmpX, tmpY, tmpZ, lat, lon, h);
	}

	public void toCartesianCoordDeg(double dlat, double dlon, double h, SharedDouble x, SharedDouble y, SharedDouble z)
	{
		this.toCartesianCoordRad(dlat * Math.PI / 180.0, dlon * Math.PI / 180.0, h, x, y, z);
	}

	public void fromCartesianCoordDeg(double x, double y, double z, SharedDouble dlat, SharedDouble dlon, SharedDouble h)
	{
		this.fromCartesianCoordRad(x, y, z, dlat, dlon, h);
		dlat.value = dlat.value * 180.0 / Math.PI;
		dlon.value = dlon.value * 180.0 / Math.PI;
	}
}
