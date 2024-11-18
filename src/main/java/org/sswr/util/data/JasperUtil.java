package org.sswr.util.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.sswr.util.io.ResourceLoader;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class JasperUtil
{
	public static class JasperFont
	{
		public String name;
		public String normal;
		public String bold;
		public String pdfEncoding;
		public boolean pdfEmbedded;
	}

	public static class JasperTextField
	{
		public String lines[];
		public float size;		
	}

	public static class FontHandler extends DefaultHandler
	{
		private List<JasperFont> fontList;
		private JasperFont currFont;
		private int currField;

		public FontHandler()
		{
			this.fontList = new ArrayList<JasperFont>();
			this.currFont = null;
			this.currField = 0;
		}

		@Override
		public void characters(char[] ch, int start, int length)
		{
			if (this.currFont != null)
			{
				switch (this.currField)
				{
				case 1:
					this.currFont.normal = new String(ch, start, length);
					this.currFont.bold = this.currFont.normal;
					break;
				case 2:
					this.currFont.bold = new String(ch, start, length);
					break;
				case 3:
					this.currFont.pdfEncoding = new String(ch, start, length);
					break;
				case 4:
					this.currFont.pdfEmbedded = new String(ch, start, length).equals("true");
					break;
				}
			}
		}

		@Override
		public void startDocument()
		{
		}

		@Override
		public void endDocument()
		{
		}

		@Override
		public void endElement(String uri, String localName, String qName)
		{
			if (qName.equals("fontFamily"))
			{
				if (this.currFont != null)
				{
					this.fontList.add(this.currFont);
					this.currFont = null;
				}
			}
			else
			{
				this.currField = 0;
			}
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)
		{
			String name;
			switch (qName)
			{
			case "fontFamily":
				name = attributes.getValue("name");
				if (this.currFont == null && name != null)
				{
					this.currFont = new JasperFont();
					this.currFont.name = name;
					this.currField = 0;
				}
				break;
			case "normal":
				this.currField = 1;
				break;
			case "bold":
				this.currField = 2;
				break;
			case "pdfEncoding":
				this.currField = 3;
				break;
			case "pdfEmbedded":
				this.currField = 4;
				break;
			}
		}

		@Nonnull
		public List<JasperFont> getList()
		{
			return this.fontList;
		}
	}

	@Nonnull
	public static String styledCJKString(@Nonnull String dispStr, @Nonnull String cjkFont, @Nonnull String engFont)
	{
		return "<style fontName='"+(StringUtil.hasCJKChar(dispStr)?cjkFont:engFont)+"'>"+XmlUtil.toAttr(dispStr)+"</style>";
	}

	@Nonnull
	public static String styledCJKBString(@Nonnull String dispStr, @Nonnull String cjkFont, @Nonnull String cjkbFont, @Nonnull String engFont)
	{
		if (StringUtil.hasCJKChar(dispStr))
		{
			int type = 0;
			StringBuilder sb = new StringBuilder();
			UTF32Reader reader = new UTF32Reader(dispStr);
			int c;
			while (true)
			{
				c = reader.nextChar();
				if (c == 0)
					break;
				if (c < 0x10000)
				{
					if (type == 2)
					{
						sb.append("</style>");
						type = 0;
					}
					if (type != 1)
					{
						sb.append("<style fontName='"+cjkFont+"'>");
						type = 1;
					}
					if (c == '&')
						sb.append("&amp;");
					else if (c == '<')
						sb.append("&lt;");
					else if (c == '>')
						sb.append("&gt;");
					else if (c == '\'')
						sb.append("&apos;");
					else if (c == '\"')
						sb.append("&quot;");
					else if (c == '\n')
						sb.append("&#10;");
					else
						sb.append((char)c);
				}
				else
				{
					if (type == 1)
					{
						sb.append("</style>");
						type = 0;
					}
					if (type != 2)
					{
						sb.append("<style fontName='"+cjkbFont+"'>");
						type = 2;
					}
					StringUtil.appendUTF32Char(sb, c);
				}
			}
			if (type != 0)
			{
				sb.append("</style>");
			}
			return sb.toString();
		}
		else
		{
			return "<style fontName='"+engFont+"'>"+XmlUtil.toAttr(dispStr)+"</style>";
		}
	}

	@Nullable
	public static List<JasperFont> loadFontProperties(@Nonnull Class<?> dataClass)
	{
		try
		{
			SAXParserFactory parserFactory = SAXParserFactory.newInstance();
			SAXParser parser = parserFactory.newSAXParser();
			FontHandler hdlr = new FontHandler();
			InputStream is = ResourceLoader.load(dataClass, "fonts.xml", null);
			if (is == null)
			{
				System.out.println("Error in loading fonts.xml");
				return null;
			}
			parser.parse(new InputSource(is), hdlr);
			return hdlr.getList();
		}
		catch (ParserConfigurationException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (SAXException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

	@Nonnull
	public static String toStyled(@Nonnull String text, float ptSize)
	{
		return "<style size=\""+ptSize+"\">"+XmlUtil.toXMLText(text)+"</style>";
	}

	@Nonnull
	public static String toStyled(@Nonnull JasperTextField txt)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<style size=\""+txt.size+"\">");
		int i = 0;
		int j = txt.lines.length;
		while (i < j)
		{
			if (i > 0)
			{
				sb.append("\r\n");
			}
			sb.append(XmlUtil.toXMLText(txt.lines[i]));
			i++;
		}
		sb.append("</style>");
		return sb.toString();
	}
}
