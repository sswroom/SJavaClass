package org.sswr.util.map;

import java.util.List;
import java.util.Objects;

import org.sswr.util.data.DataTools;
import org.sswr.util.math.CoordinateSystem;

public class FileGDBTableInfo
{
	private int nullableCnt;
	private byte geometryType;
	private byte tableFlags;
	private byte geometryFlags;
	private List<FileGDBFieldInfo> fields;

	private CoordinateSystem csys;
	private double xOrigin;
	private double yOrigin;
	private double xyScale;
	private double zOrigin;
	private double zScale;
	private double mOrigin;
	private double mScale;
	private double xyTolerance;
	private double zTolerance;
	private double mTolerance;
	private double xMin;
	private double yMin;
	private double xMax;
	private double yMax;
	private double zMin;
	private double zMax;
	private double mMin;
	private double mMax;
	private int spatialGridCnt;
	private double []spatialGrid;

	public FileGDBTableInfo() {
	}

	public FileGDBTableInfo(int nullableCnt, byte geometryType, byte tableFlags, byte geometryFlags, List<FileGDBFieldInfo> fields, CoordinateSystem csys, double xOrigin, double yOrigin, double xyScale, double zOrigin, double zScale, double mOrigin, double mScale, double xyTolerance, double zTolerance, double mTolerance, double xMin, double yMin, double xMax, double yMax, double zMin, double zMax, double mMin, double mMax, int spatialGridCnt, double[] spatialGrid) {
		this.nullableCnt = nullableCnt;
		this.geometryType = geometryType;
		this.tableFlags = tableFlags;
		this.geometryFlags = geometryFlags;
		this.fields = fields;
		this.csys = csys;
		this.xOrigin = xOrigin;
		this.yOrigin = yOrigin;
		this.xyScale = xyScale;
		this.zOrigin = zOrigin;
		this.zScale = zScale;
		this.mOrigin = mOrigin;
		this.mScale = mScale;
		this.xyTolerance = xyTolerance;
		this.zTolerance = zTolerance;
		this.mTolerance = mTolerance;
		this.xMin = xMin;
		this.yMin = yMin;
		this.xMax = xMax;
		this.yMax = yMax;
		this.zMin = zMin;
		this.zMax = zMax;
		this.mMin = mMin;
		this.mMax = mMax;
		this.spatialGridCnt = spatialGridCnt;
		this.spatialGrid = spatialGrid;
	}

	public int getNullableCnt() {
		return this.nullableCnt;
	}

	public void setNullableCnt(int nullableCnt) {
		this.nullableCnt = nullableCnt;
	}

	public byte getGeometryType() {
		return this.geometryType;
	}

	public void setGeometryType(byte geometryType) {
		this.geometryType = geometryType;
	}

	public byte getTableFlags() {
		return this.tableFlags;
	}

	public void setTableFlags(byte tableFlags) {
		this.tableFlags = tableFlags;
	}

	public byte getGeometryFlags() {
		return this.geometryFlags;
	}

	public void setGeometryFlags(byte geometryFlags) {
		this.geometryFlags = geometryFlags;
	}

	public List<FileGDBFieldInfo> getFields() {
		return this.fields;
	}

	public void setFields(List<FileGDBFieldInfo> fields) {
		this.fields = fields;
	}

	public CoordinateSystem getCsys() {
		return this.csys;
	}

	public void setCsys(CoordinateSystem csys) {
		this.csys = csys;
	}

	public double getXOrigin() {
		return this.xOrigin;
	}

	public void setXOrigin(double xOrigin) {
		this.xOrigin = xOrigin;
	}

	public double getYOrigin() {
		return this.yOrigin;
	}

	public void setYOrigin(double yOrigin) {
		this.yOrigin = yOrigin;
	}

	public double getXyScale() {
		return this.xyScale;
	}

	public void setXyScale(double xyScale) {
		this.xyScale = xyScale;
	}

	public double getZOrigin() {
		return this.zOrigin;
	}

	public void setZOrigin(double zOrigin) {
		this.zOrigin = zOrigin;
	}

	public double getZScale() {
		return this.zScale;
	}

	public void setZScale(double zScale) {
		this.zScale = zScale;
	}

	public double getMOrigin() {
		return this.mOrigin;
	}

	public void setMOrigin(double mOrigin) {
		this.mOrigin = mOrigin;
	}

	public double getMScale() {
		return this.mScale;
	}

	public void setMScale(double mScale) {
		this.mScale = mScale;
	}

	public double getXyTolerance() {
		return this.xyTolerance;
	}

	public void setXyTolerance(double xyTolerance) {
		this.xyTolerance = xyTolerance;
	}

	public double getZTolerance() {
		return this.zTolerance;
	}

	public void setZTolerance(double zTolerance) {
		this.zTolerance = zTolerance;
	}

	public double getMTolerance() {
		return this.mTolerance;
	}

	public void setMTolerance(double mTolerance) {
		this.mTolerance = mTolerance;
	}

	public double getXMin() {
		return this.xMin;
	}

	public void setXMin(double xMin) {
		this.xMin = xMin;
	}

	public double getYMin() {
		return this.yMin;
	}

	public void setYMin(double yMin) {
		this.yMin = yMin;
	}

	public double getXMax() {
		return this.xMax;
	}

	public void setXMax(double xMax) {
		this.xMax = xMax;
	}

	public double getYMax() {
		return this.yMax;
	}

	public void setYMax(double yMax) {
		this.yMax = yMax;
	}

	public double getZMin() {
		return this.zMin;
	}

	public void setZMin(double zMin) {
		this.zMin = zMin;
	}

	public double getZMax() {
		return this.zMax;
	}

	public void setZMax(double zMax) {
		this.zMax = zMax;
	}

	public double getMMin() {
		return this.mMin;
	}

	public void setMMin(double mMin) {
		this.mMin = mMin;
	}

	public double getMMax() {
		return this.mMax;
	}

	public void setMMax(double mMax) {
		this.mMax = mMax;
	}

	public int getSpatialGridCnt() {
		return this.spatialGridCnt;
	}

	public void setSpatialGridCnt(int spatialGridCnt) {
		this.spatialGridCnt = spatialGridCnt;
	}

	public double[] getSpatialGrid() {
		return this.spatialGrid;
	}

	public void setSpatialGrid(double[] spatialGrid) {
		this.spatialGrid = spatialGrid;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof FileGDBTableInfo)) {
			return false;
		}
		FileGDBTableInfo fileGDBTableInfo = (FileGDBTableInfo) o;
		return nullableCnt == fileGDBTableInfo.nullableCnt && geometryType == fileGDBTableInfo.geometryType && tableFlags == fileGDBTableInfo.tableFlags && geometryFlags == fileGDBTableInfo.geometryFlags && Objects.equals(fields, fileGDBTableInfo.fields) && Objects.equals(csys, fileGDBTableInfo.csys) && xOrigin == fileGDBTableInfo.xOrigin && yOrigin == fileGDBTableInfo.yOrigin && xyScale == fileGDBTableInfo.xyScale && zOrigin == fileGDBTableInfo.zOrigin && zScale == fileGDBTableInfo.zScale && mOrigin == fileGDBTableInfo.mOrigin && mScale == fileGDBTableInfo.mScale && xyTolerance == fileGDBTableInfo.xyTolerance && zTolerance == fileGDBTableInfo.zTolerance && mTolerance == fileGDBTableInfo.mTolerance && xMin == fileGDBTableInfo.xMin && yMin == fileGDBTableInfo.yMin && xMax == fileGDBTableInfo.xMax && yMax == fileGDBTableInfo.yMax && zMin == fileGDBTableInfo.zMin && zMax == fileGDBTableInfo.zMax && mMin == fileGDBTableInfo.mMin && mMax == fileGDBTableInfo.mMax && spatialGridCnt == fileGDBTableInfo.spatialGridCnt && Objects.equals(spatialGrid, fileGDBTableInfo.spatialGrid);
	}

	@Override
	public int hashCode() {
		return Objects.hash(nullableCnt, geometryType, tableFlags, geometryFlags, fields, csys, xOrigin, yOrigin, xyScale, zOrigin, zScale, mOrigin, mScale, xyTolerance, zTolerance, mTolerance, xMin, yMin, xMax, yMax, zMin, zMax, mMin, mMax, spatialGridCnt, spatialGrid);
	}

	@Override
	public String toString() {
		return DataTools.toObjectString(this);
	}
}
