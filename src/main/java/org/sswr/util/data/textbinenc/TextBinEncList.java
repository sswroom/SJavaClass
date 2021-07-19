package org.sswr.util.data.textbinenc;

import java.util.ArrayList;
import java.util.List;

public class TextBinEncList
{
	private static List<TextBinEnc> encList;

	public static List<TextBinEnc> getEncList()
	{
		if (encList == null)
		{
			encList = new ArrayList<TextBinEnc>();
			encList.add(new Base64Enc());
			encList.add(new ANSITextBinEnc());
			encList.add(new UTF8TextBinEnc());
			encList.add(new CPPByteArrBinEnc());
			encList.add(new CPPTextBinEnc());
			encList.add(new HexTextBinEnc());
	//		encList.add(new QuotedPrintableEnc());
			encList.add(new UCS2TextBinEnc());
	//		encList.add(new SNMPOIDBinEnc());
			encList.add(new Base32Enc());
		}
		return encList;
	}
}
