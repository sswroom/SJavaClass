package org.sswr.util.io;

import java.io.PrintStream;

import org.sswr.util.media.StandardColor;

import jakarta.annotation.Nonnull;

public class ConsoleWriter extends StyledTextWriter
{
	private PrintStream stm;
	private StandardColor bgColor;
	private OSType osType;

	public ConsoleWriter()
	{
		stm = System.out;
		osType = OSInfo.getOSType();
		this.bgColor = StandardColor.Black;
	}

	@Override
	public void close() {
	}

	private boolean isWindows()
	{
		return osType == OSType.WindowsNT || osType == OSType.WindowsNT64 || osType == OSType.WindowsSvr;
	}

	@Override
	public boolean writeStr(@Nonnull String str) {
		this.stm.print(str);
		return true;
	}

	public boolean writeLine(@Nonnull String str) {
		this.stm.println(str);
		return true;
	}

	public boolean writeLine() {
		this.stm.println();
		return true;
	}

	@Override
	public boolean writeChar(byte c) {
		this.stm.write(c);;
		return true;
	}

	public void setBGColor(@Nonnull StandardColor bgColor) {
		this.bgColor = bgColor;
	}

	@Override
	public void setTextColor(@Nonnull StandardColor fgCol) {
		if (isWindows())
		{
			//net.java.dev.jna:jna-platform:5.4.0
			//net.java.dev.jna:jna:5.4.0
/*			Function GetStdHandleFunc = Function.getFunction("kernel32", "GetStdHandle");
			DWORD STD_OUTPUT_HANDLE = new DWORD(-11);
			HANDLE hOut = (HANDLE) GetStdHandleFunc.invoke(HANDLE.class, new Object[]{STD_OUTPUT_HANDLE});

			DWORD COLOR = new DWORD(color);
			Function SetConsoleTextAttribute = Function.getFunction("kernel32", "SetConsoleTextAttribute");
			SetConsoleTextAttribute.invoke(BOOL.class, new Object[]{hOut, COLOR});*/
		}
		else
		{
			int fgColor = fgCol.ordinal();
			int bgColor = this.bgColor.ordinal();
			fgColor = ((fgColor & ~5) | ((fgColor & 1) << 2) | ((fgColor & 4) >> 2));
			bgColor = ((bgColor & ~5) | ((bgColor & 1) << 2) | ((bgColor & 4) >> 2));
			if ((fgColor & 8) != 0)
			{
				this.stm.print("\033[1;3"+(fgColor & 7)+";4"+(bgColor & 7)+"m");
			}
			else
			{
				this.stm.print("\033[0;3"+(fgColor & 7)+";4"+(bgColor & 7)+"m");
			}

		}
	}

	@Override
	public void resetTextColor() {
		if (isWindows())
		{
			setBGColor(StandardColor.Black);
			setTextColor(StandardColor.Gray);
		}
		else
		{
			this.stm.print("\033[0m");
		}
	}
	
}
