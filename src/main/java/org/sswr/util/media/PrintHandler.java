package org.sswr.util.media;

import java.awt.print.PageFormat;

public interface PrintHandler
{
	public boolean beginPrint(PrintDocument doc);
	public boolean printPage(int pageNum, java.awt.Graphics2D printPage); //return has more pages 
	public boolean endPrint(PrintDocument doc);
	public int getNumberOfPages();
	public PageFormat getPageFormat(int pageNum);

}
