package org.sswr.util.db;

public enum ColumnType
{
	Unknown,
	UInt32,
	Int32,
	VarChar,
	Char,
	DateTime, //acc = 1/333s
	Double,
	Float,
	Bool,
	Byte,
	Int16,
	Int64,
	UInt16,
	UInt64,
	Binary,
	Vector,
	NVarChar,
	NChar,
	DateTime2, //acc = 0.1ms
	UUID
	;

	public static String getString(ColumnType colType, int colSize)
	{
		switch (colType)
		{
		case UInt32:
			return "UNSIGNED INTEGER";
		case Int32:
			return "INTEGER";
		case VarChar:
			return "VARCHAR(" + colSize + ")";
		case Char:
			return "CHAR(" + colSize + ")";
		case NVarChar:
			return "NVARCHAR(" + colSize + ")";
		case NChar:
			return "NCHAR(" + colSize + ")";
		case DateTime:
			return "DATETIME";
		case DateTime2:
			return "DATETIME2";
		case Double:
			return "DOUBLE";
		case Float:
			return "FLOAT";
		case Bool:
			return "BIT";
		case Byte:
			return "TINYINT";
		case Int16:
			return "SMALLINT";
		case Int64:
			return "BIGINT";
		case UInt16:
			return "UNSIGNED SMALLINT";
		case UInt64:
			return "UNSIGNED BIGINT";
		case Binary:
			return "BINARY(" + colSize + ")";
		case Vector:
			return "GEOMETRY";
		case UUID:
			return "UUID";
		case Unknown:
		default:
			return "UNKNOWN(" + colSize + ")";
		}
	}
}
