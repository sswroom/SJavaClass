package org.sswr.util.media;

import java.util.ArrayList;
import java.util.List;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class Printer
{
	private PrintService printer;
	public Printer(@Nonnull String printerName)
	{
		this.printer = null;
		PrintService printers[] = PrintServiceLookup.lookupPrintServices(null, null);
		int i = printers.length;
		while (i-- > 0)
		{
			if (printers[i].getName().equals(printerName))
			{
				this.printer = printers[i];
				break;
			}
		}
	}

	public boolean isError()
	{
		return this.printer == null;
	}

	@Nullable
	public PrintDocument startPrint(@Nonnull PrintHandler hdlr)
	{
		if (this.printer == null)
		{
			return null;
		}
		MyPrintDocument doc = new MyPrintDocument(this.printer.createPrintJob(), hdlr);
		if (doc.isError())
		{
			return null;
		}
		doc.start();
		return doc;
	}

	public void endPrint(@Nonnull PrintDocument doc)
	{
		((MyPrintDocument)doc).close();
	}

	@Nonnull
	public static List<String> getPrinterNames()
	{
		List<String> retList = new ArrayList<String>();
		PrintService printers[] = PrintServiceLookup.lookupPrintServices(null, null);
		int i = 0;
		int j = printers.length;
		while (i < j)
		{
			retList.add(printers[i].getName());
			i++;
		}
		return retList;
	}
}
