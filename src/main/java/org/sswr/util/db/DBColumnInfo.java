package org.sswr.util.db;

import java.lang.reflect.Field;

import javax.persistence.EnumType;
import javax.persistence.GenerationType;
import javax.persistence.JoinColumn;

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
}
