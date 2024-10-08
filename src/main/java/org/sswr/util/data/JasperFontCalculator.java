package org.sswr.util.data;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sswr.util.data.JasperUtil.JasperFont;
import org.sswr.util.data.JasperUtil.JasperTextField;
import org.sswr.util.io.ResourceLoader;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class JasperFontCalculator
{
	private Map<String, JasperFont> fontMap;
	private BufferedImage img;
	private Graphics2D g;
	private FontRenderContext frc;
	private Class<?> dataClass;

	public JasperFontCalculator(@Nonnull Class<?> dataClass) throws FileNotFoundException
	{
		this.dataClass = dataClass;
		List<JasperFont> list = JasperUtil.loadFontProperties(dataClass);
		if (list == null)
		{
			throw new FileNotFoundException("Error in parsing fonts.xml");
		}
		this.fontMap = DataTools.createStringMap(list, "name", null);
		this.img = new BufferedImage(320, 240, BufferedImage.TYPE_INT_ARGB);
		this.g = img.createGraphics();
		this.frc = g.getFontRenderContext();
	}

	@Nonnull
	private Font loadFont(@Nonnull String fontName) throws IOException, FontFormatException
	{
		JasperFont f = this.fontMap.get(fontName);
		if (f == null)
		{
			throw new IllegalArgumentException("fontName "+fontName+" not found");
		}
		InputStream is = ResourceLoader.load(dataClass, f.normal, null);
		if (is == null)
		{
			throw new IllegalArgumentException("font file "+f.normal+" not found");
		}
		return Font.createFont(Font.TRUETYPE_FONT, is);
	}

	public double calcStringWidth(@Nonnull String fontName, float ptSize, @Nonnull String text) throws IOException, FontFormatException
	{
		Font font = loadFont(fontName).deriveFont(ptSize);
		return font.getStringBounds(text, frc).getWidth();
	}

	public float calcFontSize(@Nonnull String fontName, float maxPtSize, @Nonnull String text, double maxWidth) throws IOException, FontFormatException
	{
		float currSize = maxPtSize;
		Font font = loadFont(fontName);
		while (currSize > 3)
		{
			if (font.deriveFont(currSize).getStringBounds(text, frc).getWidth() <= maxWidth)
			{
				return currSize;
			}
			currSize -= 0.5f;
		}
		return currSize;
	}

	@Nullable
	private List<String> splitLineText(@Nonnull Font font, @Nonnull String text, double maxWidth)
	{
		List<String> ret = new ArrayList<String>();
		char []carr = text.toCharArray();
		int lineStart = 0;
		int lineEnd = carr.length;
		while (lineStart < carr.length)
		{
			double w = font.getStringBounds(carr, lineStart, lineEnd, frc).getWidth();
			if (w < maxWidth)
			{
//				System.out.println("W = "+w+", line = "+new String(carr, lineStart, lineEnd - lineStart));
				ret.add(new String(carr, lineStart, lineEnd - lineStart));
				lineStart = lineEnd;
				lineEnd = carr.length;
			}
			else
			{
				while (true)
				{
					lineEnd--;
					if (lineEnd <= lineStart)
						return null;
					if (carr[lineEnd] == ' ')
					{
						w = font.getStringBounds(new String(carr, lineStart, lineEnd - lineStart), frc).getWidth();
						if (w < maxWidth)
						{
//							System.out.println("W = "+w+", line = "+new String(carr, lineStart, lineEnd - lineStart));
							ret.add(new String(carr, lineStart, lineEnd - lineStart));
							lineStart = lineEnd + 1;
							lineEnd = carr.length;
							break;
						}
					}
					else if (CharUtil.isEng(carr[lineEnd]) && CharUtil.isEng(carr[lineEnd - 1]))
					{
						String s = new String(carr, lineStart, lineEnd - lineStart) + "-";
						w = font.getStringBounds(s, frc).getWidth();
						if (w < maxWidth)
						{
//							System.out.println("W = "+w+", line = "+s);
							ret.add(s);
							lineStart = lineEnd;
							lineEnd = carr.length;
							break;
						}
					}
				}
			}
		}
		return ret;
	}

	@Nonnull
	public JasperTextField calcTextFieldBySize(@Nonnull String fontName, float maxPtSize, @Nonnull String text, double maxWidth, double maxHeight) throws IOException, FontFormatException
	{
		float currSize = maxPtSize;
		Font font = loadFont(fontName);
		List<String> strs;
		while (currSize >= 3)
		{
//			System.out.println("Font size = "+currSize);
			strs = splitLineText(font.deriveFont(currSize), text, maxWidth);
			if (strs != null && strs.size() * currSize * 96.0 / 72.0 < maxHeight)
			{
				JasperTextField ret = new JasperTextField();
				ret.lines = DataTools.toArray(String.class, strs);
				ret.size = currSize;
				return ret;
			}
/*			else if (strs == null)
			{
				System.out.println("Font Size = "+currSize+", cannot split");
			}
			else
			{
				System.out.println("Font Size = "+currSize+", splitted to "+strs.size()+" lines");
			}*/
			currSize -= 0.5f;
		}
		JasperTextField ret = new JasperTextField();
		ret.lines = new String[]{text};
		ret.size = currSize;
		return ret;
	}
}
