package org.sswr.util.map;

import java.util.Objects;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.DataTools;

import jakarta.annotation.Nonnull;

public class FileGDBFieldInfo {
	private String name;
	private String alias;
	private byte fieldType;
	private int fieldSize;
	private byte flags;
	private byte defSize;
	private byte[] defValue;


	public FileGDBFieldInfo() {
	}

	public FileGDBFieldInfo(@Nonnull String name, @Nonnull String alias, byte fieldType, int fieldSize, byte flags, byte defSize, @Nonnull byte[] defValue) {
		this.name = name;
		this.alias = alias;
		this.fieldType = fieldType;
		this.fieldSize = fieldSize;
		this.flags = flags;
		this.defSize = defSize;
		this.defValue = defValue;
	}

	@Nonnull
	public String getName() {
		return this.name;
	}

	public void setName(@Nonnull String name) {
		this.name = name;
	}

	@Nonnull
	public String getAlias() {
		return this.alias;
	}

	public void setAlias(@Nonnull String alias) {
		this.alias = alias;
	}

	public byte getFieldType() {
		return this.fieldType;
	}

	public void setFieldType(byte fieldType) {
		this.fieldType = fieldType;
	}

	public int getFieldSize() {
		return this.fieldSize;
	}

	public void setFieldSize(int fieldSize) {
		this.fieldSize = fieldSize;
	}

	public byte getFlags() {
		return this.flags;
	}

	public void setFlags(byte flags) {
		this.flags = flags;
	}

	public byte getDefSize() {
		return this.defSize;
	}

	public void setDefSize(byte defSize) {
		this.defSize = defSize;
	}

	@Nonnull
	public byte[] getDefValue() {
		return this.defValue;
	}

	public void setDefValue(@Nonnull byte[] defValue) {
		this.defValue = defValue;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof FileGDBFieldInfo)) {
			return false;
		}
		FileGDBFieldInfo fileGDBFieldInfo = (FileGDBFieldInfo) o;
		return Objects.equals(name, fileGDBFieldInfo.name) && Objects.equals(alias, fileGDBFieldInfo.alias) && fieldType == fileGDBFieldInfo.fieldType && fieldSize == fileGDBFieldInfo.fieldSize && flags == fileGDBFieldInfo.flags && defSize == fileGDBFieldInfo.defSize && Objects.equals(defValue, fileGDBFieldInfo.defValue);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, alias, fieldType, fieldSize, flags, defSize, defValue);
	}

	@Override
	public String toString() {
		return DataTools.toObjectString(this);
	}

	@Nonnull
	public FileGDBFieldInfo clone()
	{
		FileGDBFieldInfo newField = new FileGDBFieldInfo();
		newField.name = this.name;
		newField.alias = this.alias;
		newField.fieldType = this.fieldType;
		newField.fieldSize = this.fieldSize;
		newField.flags = this.flags;
		newField.defSize = this.defSize;
		newField.defValue = null;
		if (this.defValue != null)
		{
			newField.defValue = new byte[this.defSize];
			ByteTool.copyArray(newField.defValue, 0, this.defValue, 0, this.defSize);
		}
		return newField;	
	}
}
