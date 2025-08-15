package org.sswr.util.db;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class ColumnDef
{
	private String colName;
	private ColumnType colType;
	private int colSize;
	private int colDP;
	private boolean notNull;
	private boolean pk;
	private AutoIncType autoInc;
	private long autoIncStartIndex;
	private long autoIncStep;
	private String defVal;
	private String attr;

	public ColumnDef(@Nonnull String colName)
	{
		this.colName = colName;
		this.colType = ColumnType.Unknown;
		this.autoInc = AutoIncType.None;
		this.autoIncStartIndex = 1;
		this.autoIncStep = 1;
	}

	@Nonnull
	public String getColName() {
		return this.colName;
	}

	public void setColName(@Nonnull String colName) {
		this.colName = colName;
	}

	@Nonnull
	public ColumnType getColType() {
		return this.colType;
	}

	public void setColType(@Nonnull ColumnType colType) {
		this.colType = colType;
	}

	public int getColSize() {
		return this.colSize;
	}

	public void setColSize(int colSize) {
		this.colSize = colSize;
	}

	public int getColDP() {
		return this.colDP;
	}

	public void setColDP(int colDP) {
		this.colDP = colDP;
	}

	public boolean isNotNull() {
		return this.notNull;
	}

	public boolean isNull() {
		return !this.notNull;
	}

	public boolean getNotNull() {
		return this.notNull;
	}

	public void setNotNull(boolean notNull) {
		this.notNull = notNull;
	}

	public boolean isPk() {
		return this.pk;
	}

	public boolean getPk() {
		return this.pk;
	}

	public void setPk(boolean pk) {
		this.pk = pk;
	}

	@Nonnull
	public AutoIncType getAutoInc() {
		return this.autoInc;
	}

	public long getAutoIncStartIndex() {
		return this.autoIncStartIndex;
	}

	public long getAutoIncStep() {
		return this.autoIncStep;
	}

	public void setAutoIncNone()
	{
		this.autoInc = AutoIncType.None;
		this.autoIncStartIndex = 1;
		this.autoIncStep = 1;
	}

	public void setAutoInc(AutoIncType autoInc, long autoIncStartIndex, long autoIncStep) {
		this.autoInc = autoInc;
		this.autoIncStartIndex = autoIncStartIndex;
		this.autoIncStep = autoIncStep;
	}

	@Nullable
	public String getDefVal() {
		return this.defVal;
	}

	public void setDefVal(@Nullable String defVal) {
		this.defVal = defVal;
	}

	@Nullable
	public String getAttr() {
		return this.attr;
	}

	public void setAttr(@Nullable String attr) {
		this.attr = attr;
	}

	@Nonnull
	public String toColTypeStr()
	{
		return ColumnType.getString(this.colType, this.colSize);
	}

	@Nonnull
	public ColumnDef clone()
	{
		ColumnDef newObj;
		newObj = new ColumnDef(this.colName);
		newObj.setColType(this.colType);
		newObj.setColSize(this.colSize);
		newObj.setColDP(this.colDP);
		newObj.setNotNull(this.notNull);
		newObj.setPk(this.pk);
		newObj.setAutoInc(this.autoInc, this.autoIncStartIndex, this.autoIncStep);
		newObj.setDefVal(this.defVal);
		newObj.setAttr(this.attr);
		return newObj;
	}
}
