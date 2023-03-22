package org.sswr.util.data;

import java.io.IOException;

public interface TableBuilder
{
	public void appendRow();
	public void appendRow(Iterable<?> rowData);
	public byte[] build();
}
