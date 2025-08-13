package org.sswr.util.io;

import java.nio.charset.StandardCharsets;

import org.sswr.util.media.StandardColor;

import jakarta.annotation.Nonnull;

public abstract class StyledTextWriter implements IOWriter
{
		public abstract void close();
		public boolean write(@Nonnull String str)
		{
			byte[] strArr = str.getBytes(StandardCharsets.UTF_8);
			int i = 0;
			int j = strArr.length;
			while (i < j)
			{
				if (!writeChar(strArr[i])) return false;
				i++;
			}
			return true;
		}

		public boolean writeLine(@Nonnull String str)
		{
			return write(str) && write("\r\n");
		}

		public boolean writeLine()
		{
			return write("\r\n");
		}

		public abstract boolean writeChar(byte c);
		public abstract void setTextColor(@Nonnull StandardColor fgColor);
		public abstract void resetTextColor();
}
