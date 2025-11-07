package org.sswr.util.db;

import java.lang.reflect.Field;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.EnumType;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;

import org.sswr.util.data.FieldGetter;
import org.sswr.util.data.FieldSetter;

public class DBColumnInfo {
	public Field field;
	public FieldGetter<Object> getter;
	public FieldSetter setter;
	public EnumType enumType;
	public String colName;
	public boolean isId;
	public JoinColumn joinCol;
	public GenerationType genType;
	public AttributeConverter<?,?> converter;
}
