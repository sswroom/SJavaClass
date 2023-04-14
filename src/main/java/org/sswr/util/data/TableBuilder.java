package org.sswr.util.data;

public interface TableBuilder
{
	public void appendRow();
	public void appendRow(Iterable<?> rowData);
	public byte[] build();
}
