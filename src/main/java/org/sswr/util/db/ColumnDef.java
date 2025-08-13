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
	private boolean autoInc;
	private String defVal;
	private String attr;

	public ColumnDef(@Nonnull String colName)
	{
		this.colName = colName;
		this.colType = ColumnType.Unknown;
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

	public boolean isAutoInc() {
		return this.autoInc;
	}

	public boolean getAutoInc() {
		return this.autoInc;
	}

	public void setAutoInc(boolean autoInc) {
		this.autoInc = autoInc;
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
		newObj.setAutoInc(this.autoInc);
		newObj.setDefVal(this.defVal);
		newObj.setAttr(this.attr);
		return newObj;
	}
}
