package org.sswr.util.media;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.Locale;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.SimpleDoc;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.DocumentName;

import org.sswr.util.data.DataTools;
import org.sswr.util.math.unit.Distance;
import org.sswr.util.math.unit.Distance.DistanceUnit;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class MyPrintDocument implements PrintDocument, java.awt.print.Pageable, java.awt.print.Printable
{
	private PrintHandler hdlr;
	private String docName;
	private boolean started;
	private boolean running;
	private PageOrientation po;
	private double paperWidth;
	private double paperHeight;
	private DocPrintJob job;

	public MyPrintDocument(@Nonnull DocPrintJob job, @Nonnull PrintHandler hdlr)
	{
		this.hdlr = hdlr;
		this.docName = null;
		this.started = false;
		this.running = false;
		this.job = job;
		this.po = PageOrientation.Portrait;
		PaperSize psize = new PaperSize(PaperSize.PaperType.PT_A4);
		this.paperWidth = psize.getWidthMM();
		this.paperHeight = psize.getHeightMM();
	}

	public void close()
	{
		this.waitForEnd();
	}

	public boolean isError()
	{
		return false;
	}

	public void setDocName(@Nullable String docName)
	{
		this.docName = docName;
	}

	public void start()
	{
		if (this.started)
			return;

		if (this.hdlr.beginPrint(this))
		{
			this.started = true;
			this.running = true;
			DocAttributeSet daset = new HashDocAttributeSet();
			if (this.docName != null)
				daset.add(new DocumentName(this.docName, Locale.getDefault()));
			Doc doc = new SimpleDoc(this, DocFlavor.SERVICE_FORMATTED.PAGEABLE, daset);
			PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
			try
			{
				this.job.print(doc, aset);
			}
			catch (PrintException ex)
			{
				ex.printStackTrace();
			}
		}
	}

	public void waitForEnd()
	{
		while (this.running)
		{
			try
			{
				Thread.sleep(10);
			}
			catch (InterruptedException ex)
			{
			}
		}
	}

	@Override
	public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException
	{
		if (pageIndex > 0 && !this.running)
			return Printable.NO_SUCH_PAGE;
		System.out.println("Class = "+this.job.getClass().toString());
		System.out.println("Bounds = "+DataTools.toObjectString(g.getClipBounds()));
		System.out.println("Paper Size = "+pf.getWidth()+", "+pf.getHeight());
		g.setClip(null);
		g.setColor(Color.BLACK);
		g.drawRect(10, 10, (int)pf.getWidth() - 20, (int)pf.getHeight() - 20);
		this.running = this.hdlr.printPage(pageIndex, (Graphics2D)g);
		return Printable.PAGE_EXISTS;
	}

	@Override
	public int getNumberOfPages()
	{
		return 1;
	}

	@Override
	public PageFormat getPageFormat(int pageIndex) throws IndexOutOfBoundsException {
		PageFormat pf = new PageFormat();
		Paper paper = new Paper();
		paper.setSize(Distance.convert(DistanceUnit.Millimeter, DistanceUnit.Point, this.paperWidth), Distance.convert(DistanceUnit.Millimeter, DistanceUnit.Point, this.paperHeight));
		pf.setPaper(paper);
		pf.setOrientation((this.po == PageOrientation.Landscape)?PageFormat.LANDSCAPE:PageFormat.PORTRAIT);
		return pf;
	}

	@Override
	public Printable getPrintable(int pageIndex) throws IndexOutOfBoundsException {
		return this;
	}
}
