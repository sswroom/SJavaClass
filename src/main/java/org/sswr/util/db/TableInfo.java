package org.sswr.util.db;

import java.util.List;

import jakarta.persistence.Table;

public class TableInfo
{
	public Table tableAnn;
	public List<DBColumnInfo> allCols;
	public List<DBColumnInfo> idCols;
}
