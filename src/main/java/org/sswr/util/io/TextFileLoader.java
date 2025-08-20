package org.sswr.util.io;

import org.sswr.util.basic.ArrayListDbl;
import org.sswr.util.data.StringBuilderUTF8;
import org.sswr.util.data.StringUtil;
import org.sswr.util.io.FileStream.BufferType;
import org.sswr.util.io.FileStream.FileMode;
import org.sswr.util.io.FileStream.FileShare;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class TextFileLoader {
	public static @Nullable ArrayListDbl loadDoubleList(@Nonnull String fileName)
	{
		double dVal;
		FileStream fs = new FileStream(fileName, FileMode.ReadOnly, FileShare.DenyNone, BufferType.Normal);
		if (fs.isError())
			return null;
		StreamReader reader = new StreamReader(fs, 0);
		StringBuilderUTF8 sb = new StringBuilderUTF8();
		ArrayListDbl dblList = new ArrayListDbl();
		while (reader.readLine(sb, 511))
		{
			dVal = StringUtil.toDoubleS(sb.toString(), Double.NaN);
			if (Double.isNaN(dVal))
			{
				return null;
			}
			dblList.add(dVal);
			sb.clearStr();
		}
		return dblList;
	}
}
