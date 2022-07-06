package org.sswr.util.media;

public interface PrintHandler
{
	public boolean beginPrint(PrintDocument doc);
	public boolean printPage(int pageNum, java.awt.Graphics2D printPage); //return has more pages 
	public boolean endPrint(PrintDocument doc);

}
