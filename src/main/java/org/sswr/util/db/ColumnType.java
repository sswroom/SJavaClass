package org.sswr.util.db;

import jakarta.annotation.Nonnull;

public enum ColumnType
{
	Unknown,
	UTF8Char,
	UTF16Char,
	UTF32Char,
	VarUTF8Char,
	VarUTF16Char,
	VarUTF32Char,
	Date,
	DateTime,
	DateTimeTZ,
	Double,
	Float,
	Bool,
	Byte,
	Int16,
	Int32,
	Int64,
	UInt16,
	UInt32,
	UInt64,
	Binary,
	Vector,
	UUID
	;

	@Nonnull
	public static String getString(@Nonnull ColumnType colType, int colSize)
	{
		switch (colType)
		{
		case UInt32:
			return "UNSIGNED INTEGER";
		case Int32:
			return "INTEGER";
		case UTF8Char:
			return "CHAR(" + colSize + ")";
		case UTF16Char:
			return "UTF16CHAR(" + colSize + ")";
		case UTF32Char:
			return "UTF32CHAR(" + colSize + ")";
		case VarUTF8Char:
			return "VARCHAR(" + colSize + ")";
		case VarUTF16Char:
			return "VARUTF16CHAR(" + colSize + ")";
		case VarUTF32Char:
			return "VARUTF32CHAR(" + colSize + ")";
		case Date:
			return "DATE";
		case DateTime:
			return "DATETIME(" + colSize + ")";
		case DateTimeTZ:
			return "DATETIMETZ(" + colSize + ")";
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
