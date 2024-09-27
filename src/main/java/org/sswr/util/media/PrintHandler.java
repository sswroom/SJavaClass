package org.sswr.util.media;

import java.awt.print.PageFormat;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public interface PrintHandler
{
	public boolean beginPrint(@Nonnull PrintDocument doc);
	public boolean printPage(int pageNum, @Nonnull java.awt.Graphics2D printPage); //return has more pages 
	public boolean endPrint(@Nonnull PrintDocument doc);
	public int getNumberOfPages();
	@Nullable
	public PageFormat getPageFormat(int pageNum);

}
