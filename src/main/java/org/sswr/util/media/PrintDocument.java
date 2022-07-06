package org.sswr.util.media;

public interface PrintDocument
{
	public enum PageOrientation
	{
		Landscape,
		Portrait
	}
	public void setDocName(String docName);
	public void setNextPagePaperSizeMM(double width, double height);
	public void setNextPageOrientation(PageOrientation po);
	public void waitForEnd();
}
