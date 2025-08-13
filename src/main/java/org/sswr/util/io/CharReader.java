package org.sswr.util.io;

import java.io.IOException;

import org.sswr.util.data.StringBuilderUTF8;

import jakarta.annotation.Nonnull;

public class CharReader
{
	public UTF8Reader reader;
	public StringBuilderUTF8 sb;
	public int charInd;

	public CharReader(@Nonnull UTF8Reader reader)
	{
		this.sb = new StringBuilderUTF8();
		this.reader = reader;
		this.charInd = 0;
	}

	public void close() throws IOException
	{
		this.reader.close();
	}

	public char nextChar()
	{
		if (this.charInd >= sb.getLength())
		{
			nextLine();
		}
		if (this.charInd >= sb.getLength())
		{
			return 0;
		}
		return (char)sb.getBytes()[this.charInd++];
	}

	public char currChar()
	{
		if (this.charInd >= sb.getLength())
		{
			nextLine();
		}
		if (this.charInd >= sb.getLength())
		{
			return 0;
		}
		return (char)sb.getBytes()[this.charInd];
	}

	@Nonnull
	public String currLine()
	{
		if (this.charInd >= sb.getLength())
		{
			return "";
		}
		return sb.substring(this.charInd);
	}

	private boolean appendLine()
	{
		boolean succ = this.reader.readLine(sb, 512);
		if (succ)
		{
			this.reader.getLastLineBreak(sb);
		}
		return succ;
	}

	public boolean nextLine()
	{
		sb.clearStr();
		this.charInd = 0;
		return this.appendLine();
	}

	public void skipWS()
	{
		while (true)
		{
			if (this.charInd >= sb.getLength())
			{
				if (!nextLine())
				{
					return;
				}
			}
			switch (sb.getBytes()[this.charInd])
			{
				case ' ':
				case '\t':
				case '\r':
				case '\n':
					this.charInd++;
					break;
				default:
					return;
			}
		}
	}

	public boolean startsWith(@Nonnull String s)
	{
		int len = s.length();
		while (true)
		{
			if (this.charInd + len <= this.sb.getLength())
			{
				return this.sb.substring(this.charInd, this.charInd + len).equals(s);
			}
			if (this.charInd > 0)
			{
				this.sb.removeRange(0, this.charInd);
				this.charInd = 0;
			}
			if (!this.appendLine())
			{
				return false;
			}
		}
	}
}
