package org.sswr.util.io;

import java.io.IOException;

import jakarta.annotation.Nonnull;

public class CharReader
{
	public UTF8Reader reader;
	public StringBuilder sb;
	public int charInd;

	public CharReader(@Nonnull UTF8Reader reader)
	{
		this.sb = new StringBuilder();
		this.reader = reader;
		this.charInd = 0;
	}

	public void close() throws IOException
	{
		this.reader.close();
	}

	public char nextChar()
	{
		if (this.charInd >= sb.length())
		{
			nextLine();
		}
		if (this.charInd >= sb.length())
		{
			return 0;
		}
		return sb.charAt(this.charInd++);
	}

	public char currChar()
	{
		if (this.charInd >= sb.length())
		{
			nextLine();
		}
		if (this.charInd >= sb.length())
		{
			return 0;
		}
		return sb.charAt(this.charInd);
	}

	@Nonnull
	public String currLine()
	{
		if (this.charInd >= sb.length())
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
		sb.setLength(0);
		this.charInd = 0;
		return this.appendLine();
	}

	public void skipWS()
	{
		while (true)
		{
			if (this.charInd >= sb.length())
			{
				if (!nextLine())
				{
					return;
				}
			}
			switch (sb.charAt(this.charInd))
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
			if (this.charInd + len <= this.sb.length())
			{
				return this.sb.substring(this.charInd, this.charInd + len).equals(s);
			}
			if (this.charInd > 0)
			{
				this.sb.delete(0, this.charInd);
				this.charInd = 0;
			}
			if (!this.appendLine())
			{
				return false;
			}
		}
	}
}
