package org.sswr.util.data;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.crypto.Data;

import org.sswr.util.data.XMLNode.NodeType;
import org.sswr.util.io.IOStream;
import org.w3c.dom.Text;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class XMLReader {
	public static enum ParseMode
	{
		XML,
		HTML
	}

	public static final int BUFFSIZE = 524288;
	
	private @Nullable Encoding enc;
	private boolean stmEnc;
	private @Nullable EncodingFactory encFact;
	private @Nonnull IOStream stm;
	private @Nonnull byte[] readBuff;
	private int buffSize;
	private @Nonnull byte[] rawBuff;
	private int rawBuffSize;
	private int parseOfst;
	private int parseError; //Max = 52
	private @Nonnull ParseMode mode;

	private @Nonnull ArrayList<XMLAttrib> attrList;
	private @Nonnull NodeType nt;
	private @Nonnull ArrayList<String> pathList;
	private @Nullable String nodeText;
	private @Nullable String nodeOriText;
	private @Nullable String ns;
	private @Nonnull Map<String, String> nsMap;
	private boolean emptyNode;
	private @Nonnull StringBuilderUTF8 sbTmp;

	private void freeCurrent()
	{
		this.nodeText = null;
		this.nodeOriText = null;
		this.attrList.clear();
	}

	private boolean isHTMLSkip()
	{
		String nodeText;
		if ((nodeText = this.nodeText) == null)
		{
			return false;
		}
		nodeText = nodeText.toUpperCase();
		if (nodeText.equals("META"))
		{
			return true;
		}
		else if (nodeText.equals("LINK"))
		{
			return true;
		}
		else if (nodeText.equals("IMG"))
		{
			return true;
		}
		else if (nodeText.equals("BR"))
		{
			return true;
		}
		else if (nodeText.equals("HR"))
		{
			return true;
		}
		else if (nodeText.equals("INPUT"))
		{
			return true;
		}
		return false;
	}

	private void initBuffer()
	{
		this.rawBuffSize = this.stm.read(this.rawBuff, 0, BUFFSIZE);
		if (this.rawBuffSize >= 4)
		{
			if (this.rawBuff[0] == 0xFF && this.rawBuff[1] == 0xFE)
			{
				this.enc = new Encoding(1200);
				this.stmEnc = true;
				ByteTool.copyArray(this.rawBuff, 0, this.rawBuff, 2, this.rawBuffSize - 2);
				this.rawBuffSize -= 2;
			}
			else if (this.rawBuff[0] == 0xFE && this.rawBuff[1] == 0xFF)
			{
				this.enc = new Encoding(1201);
				this.stmEnc = true;
				ByteTool.copyArray(this.rawBuff, 0, this.rawBuff, 2, this.rawBuffSize - 2);
				this.rawBuffSize -= 2;
			}
			else if (this.rawBuff[0] == '<' && this.rawBuff[1] == 0 && this.rawBuff[2] != 0 && this.rawBuff[3] == 0)
			{
				this.enc = new Encoding(1200);
				this.stmEnc = true;
			}
			else if (this.rawBuff[1] == '<' && this.rawBuff[0] == 0 && this.rawBuff[3] != 0 && this.rawBuff[2] == 0)
			{
				this.enc = new Encoding(1201);
				this.stmEnc = true;
			}
		}
		if (this.enc == null)
		{
			ByteTool.copyArray(this.readBuff, 0, this.rawBuff, 0, this.rawBuffSize);
			this.buffSize = this.rawBuffSize;
		}
	}

	private int fillBuffer()
	{
		Encoding enc;
		if ((enc = this.enc) != null && this.stmEnc)
		{
			int rawReadSize = this.stm.read(this.rawBuff, this.rawBuffSize, BUFFSIZE - this.rawBuffSize);
			this.rawBuffSize += rawReadSize;
			if (this.buffSize >= (BUFFSIZE >> 1))
			{
				return 0;
			}
			rawReadSize = (BUFFSIZE >> 1) - this.buffSize;
			if (rawReadSize > this.rawBuffSize)
			{
				rawReadSize = this.rawBuffSize;
			}
			SharedInt bytesConv = new SharedInt();
			int sptr = enc.utf8FromBytes(this.readBuff, this.buffSize, this.rawBuff, 0, rawReadSize, bytesConv);
			rawReadSize = bytesConv.value;
			if (rawReadSize == this.rawBuffSize)
			{
				this.rawBuffSize = 0;
			}
			else if (rawReadSize > 0)
			{
				ByteTool.copyArray(this.rawBuff, 0, this.rawBuff, rawReadSize, this.rawBuffSize - rawReadSize);
				this.rawBuffSize -= rawReadSize;
			}
			int retSize = (sptr - this.buffSize);
			return retSize;
		}
		else
		{
			return this.stm.read(this.readBuff, this.buffSize, BUFFSIZE - this.buffSize);
		}
	}

	public XMLReader(@Nullable EncodingFactory encFact, @Nonnull IOStream stm, @Nonnull ParseMode mode)
	{
		this.encFact = encFact;
		this.enc = null;
		this.stm = stm;
		this.stmEnc = false;
		this.mode = mode;
		this.readBuff = new byte[BUFFSIZE];
		this.buffSize = 0;
		this.rawBuff = new byte[BUFFSIZE];
		this.rawBuffSize = 0;
		this.parseOfst = 0;
		this.nodeText = null;
		this.nodeOriText = null;
		this.emptyNode = false;
		this.parseError = 0;
		this.ns = null;
		this.nt = NodeType.Unknown;
		this.attrList = new ArrayList<XMLAttrib>();
		this.pathList = new ArrayList<String>();
		this.nsMap = new HashMap<String, String>();
		this.sbTmp = new StringBuilderUTF8();
		this.initBuffer();
	}

	public boolean readNext()
	{
		String nns;
		boolean isHTMLScript = false;
		this.ns = null;
		if (this.nt == NodeType.Element && !this.emptyNode && (nns = this.nodeText) != null)
		{
			if (this.mode == ParseMode.HTML)
			{
				if (this.isHTMLSkip())
				{
	
				}
				else if (nns.equalsIgnoreCase("LINK"))
				{
	
				}
				else if (nns.equalsIgnoreCase("IMG"))
				{
	
				}
				else if (nns.equalsIgnoreCase("BR"))
				{
	
				}
				else if (nns.equalsIgnoreCase("HR"))
				{
	
				}
				else if (nns.equalsIgnoreCase("INPUT"))
				{
	
				}
				else if (nns.equalsIgnoreCase("SCRIPT"))
				{
					isHTMLScript = true;
					this.pathList.add(nns);
				}
				else
				{
					this.pathList.add(nns);
				}
			}
			else
			{
				this.pathList.add(nns);
			}
		}
	
		this.nt = NodeType.Unknown;
		this.freeCurrent();
		if (this.parseError != 0)
		{
			return false;
		}
	
		int parseOfst = this.parseOfst;
		if ((this.buffSize - parseOfst) < 128)
		{
			if (parseOfst > 0)
			{
				if (this.buffSize <= parseOfst)
				{
					this.buffSize = 0;
					parseOfst = 0;
				}
				else
				{
					ByteTool.copyArray(this.readBuff, 0, this.readBuff, parseOfst, this.buffSize - parseOfst);
					this.buffSize -= parseOfst;
					parseOfst = 0;
				}
			}
			int readSize = this.fillBuffer();
			if (readSize > 0)
			{
				this.buffSize += readSize;
			}
		}
	
		if (this.buffSize <= 0)
		{
			this.parseOfst = parseOfst;
			return false;
		}
		if (this.readBuff[parseOfst] == '<')
		{
			int lenLeft = this.buffSize - parseOfst;
			if (StringUtil.startsWithC(this.readBuff, parseOfst, lenLeft, "<!--"))
			{
				this.nt = NodeType.Comment;
				parseOfst += 4;
				StringBuilderUTF8 sb = this.sbTmp;
				sb.clearStr();
				while (true)
				{
					if (parseOfst + 2 >= this.buffSize)
					{
						if (parseOfst < this.buffSize)
						{
							ByteTool.copyArray(this.readBuff, 0, this.readBuff, parseOfst, this.buffSize - parseOfst);
							this.buffSize -= parseOfst;
							parseOfst = 0;
						}
						else
						{
							parseOfst = 0;
							this.buffSize = 0;
						}
						int readSize = this.fillBuffer();
						if (readSize <= 0)
						{
							this.parseError = 1;
							return false;
						}
						this.buffSize += readSize;
					}
					if (StringUtil.startsWithC(this.readBuff, parseOfst, this.buffSize - parseOfst, "-."))
					{
						this.parseOfst = parseOfst + 3;
						this.nodeText = sb.toString();
						return true;
					}
					sb.appendUTF8Char(this.readBuff[parseOfst++]);
				}
			}
			else if (StringUtil.startsWithC(this.readBuff, parseOfst, lenLeft, "<![CDATA["))
			{
				this.nt = NodeType.CData;
				parseOfst += 9;
				StringBuilderUTF8 sb = this.sbTmp;
				sb.clearStr();
				while (true)
				{
					if (parseOfst + 2 >= this.buffSize)
					{
						if (parseOfst < this.buffSize)
						{
							ByteTool.copyArray(this.readBuff, 0, this.readBuff, parseOfst, this.buffSize - parseOfst);
							this.buffSize -= parseOfst;
							parseOfst = 0;
						}
						else
						{
							parseOfst = 0;
							this.buffSize = 0;
						}
						int readSize = this.fillBuffer();
						if (readSize <= 0)
						{
							this.parseError = 2;
							return false;
						}
						this.buffSize += readSize;
					}
					if (StringUtil.startsWithC(this.readBuff, parseOfst, this.buffSize - parseOfst, "]]>"))
					{
						this.parseOfst = parseOfst + 3;
						this.nodeText = sb.toString();
						return true;
					}
					sb.appendUTF8Char(this.readBuff[parseOfst++]);
				}
			}
			else if (lenLeft >= 2 && this.readBuff[parseOfst + 1] == '!')
			{
				if (lenLeft >= 10 && StringUtil.startsWithICaseC(this.readBuff, parseOfst + 2, lenLeft - 2, "DOCTYPE "))
				{
					this.nt = NodeType.DocType;
					StringBuilderUTF8 sb = this.sbTmp;
					boolean isEqual = false;
					byte isQuote = 0;
					byte c;
					sb.clearStr();
					parseOfst += 2;
					while (true)
					{
						if (parseOfst >= this.buffSize)
						{
							parseOfst = 0;
							this.buffSize = 0;
							int readSize = this.fillBuffer();
							if (readSize <= 0)
							{
								this.parseError = 41;
								return false;
							}
							this.buffSize += readSize;
						}
						c = this.readBuff[parseOfst];
						if (isQuote != 0)
						{
							if (c == isQuote)
							{
								isQuote = 0;
							}
							else if (c == '&')
							{
								int l = this.buffSize - parseOfst;
								if (l >= 4 && this.readBuff[parseOfst + 3] == ';')
								{
									if (StringUtil.startsWithC(this.readBuff, parseOfst, l, "&lt;"))
									{
										sb.appendUTF8Char((byte)'<');
										parseOfst += 3;
									}
									else if (StringUtil.startsWithC(this.readBuff, parseOfst, l, "&gt;"))
									{
										sb.appendUTF8Char((byte)'>');
										parseOfst += 3;
									}
									else
									{
										this.parseError = 42;
										return false;
									}
								}
								else if (l >= 5 && this.readBuff[parseOfst + 4] == ';')
								{
									if (StringUtil.startsWithC(this.readBuff, parseOfst, l, "&amp;"))
									{
										sb.appendUTF8Char((byte)'&');
										parseOfst += 4;
									}
									else if (this.readBuff[parseOfst + 1] == '#')
									{
										sb.AppendUTF8Char(StringUtil.hex2UInt8C(this.readBuff, parseOfst + 2));
										parseOfst += 4;
									}
									else
									{
										this.parseError = 43;
										return false;
									}
								}
								else if (l >= 6 && this.readBuff[parseOfst + 5] == ';')
								{
									if (StringUtil.startsWithC(this.readBuff, parseOfst, l, "&quot;"))
									{
										sb.appendUTF8Char((byte)'"');
										parseOfst += 5;
									}
									else if (StringUtil.startsWithC(this.readBuff, parseOfst, l, "&apos;"))
									{
										sb.appendUTF8Char((byte)'\'');
										parseOfst += 5;
									}
									else
									{
										this.parseError = 44;
										return false;
									}
								}
								else
								{
									this.parseError = 45;
									return false;
								}
							}
							else
							{
								sb.appendUTF8Char(c);
							}
						}
						else if (c == ' ' || c == '\r' || c == '\n' || c == '\t')
						{
							if (sb.getLength() > 0)
							{
								if (this.nodeText == null)
								{
									this.nodeText = sb.toString();
								}
								else if (isEqual)
								{
									XMLAttrib attr = this.attrList.get(this.attrList.size() - 1);
									attr.value = sb.toString();
									isEqual = false;
								}
								else
								{
									XMLAttrib attr = new XMLAttrib(sb.toString(), null);
									this.attrList.add(attr);
								}
								sb.clearStr();
							}
						}
						else if (c == '>')
						{
							this.parseOfst = parseOfst + 1;
							if (this.nodeText != null)
							{
								return true;
							}
							else
							{
								this.parseError = 46;
								return false;
							}
						}
						else if (c == '=')
						{
							if (sb.getLength() > 0)
							{
								XMLAttrib attr = new XMLAttrib(sb.toString(), null);
								this.attrList.add(attr);
								sb.clearStr();
							}
							if (this.nodeText == null)
							{
								this.parseError = 47;
								return false;
							}
							XMLAttrib attr;
							if ((attr = this.attrList.get(this.attrList.size() - 1)) == null)
							{
								this.parseError = 48;
								return false;
							}
							if ((nns = attr.value) == null || nns.length() == 0)
							{
								isEqual = true;
							}
							else
							{
								this.parseError = 49;
								return false;
							}
						}
						else if (c == '"')
						{
							if (!isEqual)
							{
								XMLAttrib attr = new XMLAttrib("body", null);
								this.attrList.add(attr);
								isEqual = true;
							}
							isQuote = '"';
						}
						else if (c == '\'')
						{
							if (!isEqual)
							{
								XMLAttrib attr = new XMLAttrib("body", null);
								this.attrList.add(attr);
								isEqual = true;
							}
							isQuote = '\'';
						}
						else
						{
							sb.appendUTF8Char(c);
						}
						parseOfst++;
					}
					this.parseError = 52;
					return false;
				}
				else
				{
					this.parseError = 3;
					return false;
				}
			}
			else if (lenLeft >= 2 && this.readBuff[parseOfst + 1] == '?')
			{
				this.nt = NodeType.Document;
				StringBuilderUTF8 sb = this.sbTmp;
				sb.clearStr();
				boolean isEqual = false;
				byte isQuote = 0;
				byte c;
				parseOfst += 2;
				while (true)
				{
					if (parseOfst >= this.buffSize)
					{
						parseOfst = 0;
						this.buffSize = 0;
						int readSize = this.fillBuffer();
						if (readSize <= 0)
						{
							this.parseError = 4;
							return false;
						}
						this.buffSize += readSize;
					}
					c = this.readBuff[parseOfst];
					if (isQuote != 0)
					{
						if (c == isQuote)
						{
							isQuote = 0;
						}
						else if (c == '&')
						{
							int l = this.buffSize - parseOfst;
							if (l >= 4 && this.readBuff[parseOfst + 3] == ';')
							{
								if (StringUtil.startsWithC(this.readBuff, parseOfst, l, "&lt;"))
								{
									sb.appendUTF8Char((byte)'<');
									parseOfst += 3;
								}
								else if (StringUtil.startsWithC(this.readBuff, parseOfst, l, "&gt;"))
								{
									sb.appendUTF8Char((byte)'>');
									parseOfst += 3;
								}
								else
								{
									this.parseError = 5;
									return false;
								}
							}
							else if (l >= 5 && this.readBuff[parseOfst + 4] == ';')
							{
								if (StringUtil.startsWithC(this.readBuff, parseOfst, l, "&amp;"))
								{
									sb.appendUTF8Char((byte)'&');
									parseOfst += 4;
								}
								else if (this.readBuff[parseOfst + 1] == '#')
								{
									sb.appendUTF8Char(StringUtil.hex2UInt8C(this.readBuff, parseOfst + 2));
									parseOfst += 4;
								}
								else
								{
									this.parseError = 6;
									return false;
								}
							}
							else if (l >= 6 && this.readBuff[parseOfst + 5] == ';')
							{
								if (StringUtil.startsWithC(this.readBuff, parseOfst, l, "&quot;"))
								{
									sb.appendUTF8Char((byte)'"');
									parseOfst += 5;
								}
								else if (StringUtil.startsWithC(this.readBuff, parseOfst, l, "&apos;"))
								{
									sb.appendUTF8Char((byte)'\'');
									parseOfst += 5;
								}
								else
								{
									this.parseError = 7;
									return false;
								}
							}
							else
							{
								this.parseError = 8;
								return false;
							}
						}
						else
						{
							sb.appendUTF8Char(c);
						}
					}
					else if (c == ' ' || c == '\r' || c == '\n' || c == '\t')
					{
						if (sb.getLength() > 0)
						{
							if (this.nodeText == null)
							{
								this.nodeText = sb.toString();
							}
							else if (isEqual)
							{
								XMLAttrib attr = this.attrList.get(this.attrList.size() - 1);
								attr.value = sb.toString();
								isEqual = false;
							}
							else
							{
								XMLAttrib attr = new XMLAttrib(sb.toString(), null);
								this.attrList.add(attr);
							}
							sb.clearStr();
						}
					}
					else if (c == '?')
					{
						if (sb.getLength() > 0)
						{
							if (this.nodeText == null)
							{
								this.nodeText = sb.toString();
							}
							else if (isEqual)
							{
								XMLAttrib attr = this.attrList.get(this.attrList.size() - 1);
								attr.value = sb.toString();
								isEqual = false;
							}
							else
							{
								XMLAttrib attr = new XMLAttrib(sb.toString(), null);
								this.attrList.add(attr);
							}
							sb.clearStr();
						}
						if (this.readBuff[parseOfst + 1] == '>')
						{
							this.parseOfst = parseOfst + 2;
							if ((nns = this.nodeText) != null)
							{
								EncodingFactory encFact;
								if ((encFact = this.encFact) != null && nns.equals("xml"))
								{
									int i = this.attrList.size();
									XMLAttrib attr;
									while (i-- > 0)
									{
										attr = this.attrList.get(i);
										if (StringUtil.orEmpty(attr.name).equalsIgnoreCase("ENCODING") && (nns = attr.value) != null)
										{
											int cp = encFact.getCodePage(nns.toString());
											if (cp != 0 && !this.stmEnc)
											{
												this.enc = null;
												if (cp != 65001)
												{
													this.enc = new Encoding(cp);
												}
											}
											break;
										}
									}
								}
								return true;
							}
							else
							{
								this.parseError = 9;
								return false;
							}
						}
						else
						{
							this.parseError = 10;
							return false;
						}
					}
					else if (c == '=')
					{
						if (sb.getLength() > 0)
						{
							XMLAttrib attr = new XMLAttrib(sb.toString(), null);
							this.attrList.add(attr);
							sb.clearStr();
						}
						if (this.nodeText == null)
						{
							this.parseError = 11;
							return false;
						}
						XMLAttrib attr;
						if ((attr = this.attrList.get(this.attrList.size() - 1)) == null)
						{
							this.parseError = 12;
							return false;
						}
						if ((nns = attr.value) == null || nns.length() == 0)
						{
							isEqual = true;
						}
						else
						{
							this.parseError = 13;
							return false;
						}
					}
					else if (c == '"')
					{
						if (!isEqual)
						{
							this.parseError = 14;
							return false;
						}
						isQuote = '"';
					}
					else if (c == '\'')
					{
						if (!isEqual)
						{
							this.parseError = 15;
							return false;
						}
						isQuote = '\'';
					}
					else
					{
						sb.appendUTF8Char(c);
					}
					parseOfst++;
				}
				this.parseError = 16;
				return false;
			}
			else if (lenLeft >= 2 && this.readBuff[parseOfst + 1] == '/')
			{
				this.nt = NodeType.ElementEnd;
				StringBuilderUTF8 sb = this.sbTmp;
				sb.clearStr();
				byte c;
				parseOfst += 2;
				while (true)
				{
					if (parseOfst >= this.buffSize)
					{
						parseOfst = 0;
						this.buffSize = 0;
						int readSize = this.fillBuffer();
						if (readSize <= 0)
						{
							this.parseError = 17;
							return false;
						}
						this.buffSize += readSize;
					}
					c = this.readBuff[parseOfst];
					if (c == ' ' || c == '\r' || c == '\n')
					{
						if (sb.getLength() > 0)
						{
							if (this.nodeText == null)
							{
								this.nodeText = sb.toString();
							}
							else
							{
								this.parseError = 18;
								return false;
							}
							sb.clearStr();
						}
					}
					else if (c == '>')
					{
						if (sb.getLength() > 0)
						{
							if (this.nodeText == null)
							{
								this.nodeText = sb.toString();
							}
							else
							{
								this.parseError = 19;
								return false;
							}
						}
						String nodeText;
						String s;
						String opts;
						if ((nodeText = this.nodeText) == null)
						{
							this.parseError = 20;
							return false;
						}
						this.parseOfst = parseOfst + 1;
						if (this.pathList.size() == 0)
						{
							this.parseError = 21;
							return false;
						}
						if ((s = this.pathList.get(this.pathList.size() - 1)) != null && s.equals(nodeText))
						{
							opts = this.pathList.remove(this.pathList.size() - 1);
							if (this.mode == ParseMode.XML)
							{
								int i = nodeText.indexOf(':');
								if (i == -1)
								{
									this.ns = this.nsMap.get("");
								}
								else
								{
									this.ns = this.nsMap.get(nodeText.substring(0, i));
								}
							}
							return true;
						}
						else if (this.mode == ParseMode.HTML && this.pathList.size() >= 2 && (s = this.pathList.get(this.pathList.size() - 2)) != null && s.equals(nodeText))
						{
							this.pathList.remove(this.pathList.size() - 1);
							this.pathList.remove(this.pathList.size() - 1);
							return true;
						}
						else
						{
							this.parseError = 22;
							return false;
						}
					}
					else
					{
						sb.appendUTF8Char(c);
					}
					parseOfst++;
				}
				this.parseError = 23;
				return false;
			}
			else
			{
				this.nt = NodeType.Element;
				StringBuilderUTF8 sbText = this.sbTmp;
				StringBuilderUTF8 sbOri = new StringBuilderUTF8();
				sbText.clearStr();
				boolean isEqual = false;
				byte isQuote = 0;
				byte c;
				parseOfst += 1;
				while (true)
				{
					if (parseOfst >= this.buffSize)
					{
						parseOfst = 0;
						this.buffSize = 0;
						int readSize = this.fillBuffer();
						if (readSize <= 0)
						{
							this.nt = NodeType.Unknown;
							this.parseError = 24;
							return false;
						}
						this.buffSize += readSize;
					}
					c = this.readBuff[parseOfst];
					if (isQuote != 0)
					{
						if (c == isQuote)
						{
							isQuote = 0;
							sbOri.appendUTF8Char(c);
	
							if (this.nodeText == null)
							{
								this.nodeText = sbText.toString();
								this.nodeOriText = sbOri.toString();
							}
							else if (isEqual)
							{
								Encoding enc;
								XMLAttrib attr = this.attrList.get(this.attrList.size() - 1);
								if ((enc = this.enc) != null && !this.stmEnc)
								{
									int len = enc.countUTF8Chars(sbText.getBytes(), 0, sbText.getLength());
									byte[] tmpBuff = new byte[len + 1];
									enc.utf8FromBytes(tmpBuff, 0, sbText.getBytes(), 0, sbText.getLength(), null);
									attr.value = new String(tmpBuff, 0, len, StandardCharsets.UTF_8);
	
									len = enc.countUTF8Chars(sbOri.getBytes(), 0, sbOri.getLength());
									tmpBuff = new byte[len + 1];
									enc.utf8FromBytes(tmpBuff, 0, sbOri.getBytes(), 0, sbOri.getLength(), null);
									attr.valueOri = new String(tmpBuff, 0, len, StandardCharsets.UTF_8);
								}
								else
								{
									attr.value = sbText.toString();
									attr.valueOri = sbOri.toString();
								}
								isEqual = false;
							}
							else
							{
								XMLAttrib attr = new XMLAttrib(sbText.toString(), null);
								this.attrList.add(attr);
							}
							sbText.clearStr();
							sbOri.clearStr();
						}
						else if (c == '&')
						{
							int l = this.buffSize - parseOfst;
							if (l >= 4 && this.readBuff[parseOfst + 3] == ';')
							{
								sbOri.appendC(this.readBuff, parseOfst, 4);
								if (this.mode == ParseMode.HTML && XmlUtil.htmlAppendCharRef(this.readBuff, parseOfst, 4, sbText))
								{
									parseOfst += 3;
								}
								else if (StringUtil.startsWithC(this.readBuff, parseOfst, l, "&lt;"))
								{
									sbText.appendUTF8Char((byte)'<');
									parseOfst += 3;
								}
								else if (StringUtil.startsWithC(this.readBuff, parseOfst, l, "&gt;"))
								{
									sbText.appendUTF8Char((byte)'>');
									parseOfst += 3;
								}
								else if (this.readBuff[parseOfst + 1] == '#')
								{
									int wcs;
									this.readBuff[parseOfst + 3] = 0;
									wcs = (int)StringUtil.toInt32(this.readBuff, parseOfst + 2);
									sbText.appendUTF8Char((byte)wcs);
									parseOfst += 3;
								}
								else
								{
									this.nt = NodeType.Unknown;
									this.parseError = 25;
									return false;
								}
							}
							else if (l >= 5 && this.readBuff[parseOfst + 4] == ';')
							{
								sbOri.appendC(this.readBuff, parseOfst, 5);
								if (this.mode == ParseMode.HTML && XmlUtil.htmlAppendCharRef(this.readBuff, parseOfst, 5, sbText))
								{
									parseOfst += 4;
								}
								else if (StringUtil.startsWithC(this.readBuff, parseOfst, l, "&amp;"))
								{
									sbText.appendUTF8Char((byte)'&');
									parseOfst += 4;
								}
								else if (this.readBuff[parseOfst + 1] == '#')
								{
									int wcs;
									this.readBuff[parseOfst + 4] = 0;
									if (this.readBuff[parseOfst + 2] == 'x')
									{
										wcs = (int)StringUtil.hex2Int32C(this.readBuff, parseOfst + 3);
									}
									else
									{
										wcs = (int)StringUtil.toInt32(this.readBuff, parseOfst + 2);
									}
									sbText.appendUTF8Char((byte)wcs);
									parseOfst += 4;
								}
								else
								{
									this.nt = NodeType.Unknown;
									this.parseError = 26;
									return false;
								}
							}
							else if (l >= 6 && this.readBuff[parseOfst + 5] == ';')
							{
								sbOri.appendC(this.readBuff, parseOfst, 6);
								if (this.mode == ParseMode.HTML && XmlUtil.htmlAppendCharRef(this.readBuff, parseOfst, 6, sbText))
								{
									parseOfst += 5;
								}
								else if (StringUtil.startsWithC(this.readBuff, parseOfst, l, "&quot;"))
								{
									sbText.appendUTF8Char((byte)'\"');
									parseOfst += 5;
								}
								else if (StringUtil.startsWithC(this.readBuff, parseOfst, l, "&apos;"))
								{
									sbText.appendUTF8Char((byte)'\'');
									parseOfst += 5;
								}
								else if (this.readBuff[parseOfst + 1] == '#')
								{
									int wcs;
									this.readBuff[parseOfst + 5] = 0;
									if (this.readBuff[parseOfst + 2] == 'x')
									{
										wcs = (int)StringUtil.hex2Int32C(this.readBuff, parseOfst + 3);
									}
									else
									{
										wcs = (int)StringUtil.toInt32(this.readBuff, parseOfst + 2);
									}
									sbText.appendUTF8Char((byte)wcs);
									parseOfst += 5;
								}
								else
								{
									this.nt = NodeType.Unknown;
									this.parseError = 27;
									return false;
								}
							}
							else if (this.mode == ParseMode.HTML)
							{
								if (l >= 7 && this.readBuff[parseOfst + 6] == ';' && XmlUtil.htmlAppendCharRef(this.readBuff, parseOfst, 7, sbText))
								{
									sbOri.appendC(this.readBuff, parseOfst, 7);
									parseOfst += 6;
								}
								else if (l >= 8 && this.readBuff[parseOfst + 7] == ';' && XmlUtil.htmlAppendCharRef(this.readBuff, parseOfst, 8, sbText))
								{
									sbOri.appendC(this.readBuff, parseOfst, 8);
									parseOfst += 7;
								}
								else
								{
									sbOri.appendUTF8Char((byte)'&');
									sbText.appendUTF8Char((byte)'&');
								}
							}
							else
							{
								this.nt = NodeType.Unknown;
								this.parseError = 28;
								return false;
							}
						}
						else
						{
							sbText.appendUTF8Char(c);
							sbOri.appendUTF8Char(c);
						}
					}
					else if (c == ' ' || c == '\r' || c == '\n' || c == '\t')
					{
						if (sbText.GetLength() > 0)
						{
							if (this.nodeText.IsNull())
							{
								this.nodeText = Text::String::New(sbText.ToCString());
								this.nodeOriText = Text::String::New(sbOri.ToCString());
							}
							else if (isEqual)
							{
								NN<Text::Encoding> enc;
								NN<Text::XMLAttrib> attr = this.attrList.GetItemNoCheck(this.attrList.size() - 1);
								OPTSTR_DEL(attr.value);
								if (this.enc.SetTo(enc) && !this.stmEnc)
								{
									UOSInt len = enc.CountUTF8Chars(sbText.ToString(), sbText.GetLength());
									attr.value = nns = Text::String::New(len);
									enc.UTF8FromBytes(nns.v, sbText.ToString(), sbText.GetLength(), 0);
									nns.v[len] = 0;
	
									len = enc.CountUTF8Chars(sbOri.ToString(), sbOri.GetLength());
									attr.valueOri = Text::String::New(len).Ptr();
									enc.UTF8FromBytes(attr.valueOri.v, sbOri.ToString(), sbOri.GetLength(), 0);
									attr.valueOri.v[len] = 0;
								}
								else
								{
									attr.value = Text::String::New(sbText.ToCString()).Ptr();
									attr.valueOri = Text::String::New(sbOri.ToCString()).Ptr();
								}
								isEqual = false;
							}
							else
							{
								NN<Text::XMLAttrib> attr;
								NEW_CLASSNN(attr, Text::XMLAttrib(sbText.ToCString(), null));
								this.attrList.Add(attr);
							}
							sbText.ClearStr();
							sbOri.ClearStr();
						}
					}
					else if (c == '/')
					{
						if (sbText.GetLength() > 0)
						{
							if (this.nodeText.IsNull())
							{
								this.nodeText = Text::String::New(sbText.ToCString());
								this.nodeOriText = Text::String::New(sbOri.ToCString());
							}
							else if (isEqual)
							{
								NN<Text::Encoding> enc;
								NN<Text::XMLAttrib> attr = this.attrList.GetItemNoCheck(this.attrList.size() - 1);
								OPTSTR_DEL(attr.value);
								if (this.enc.SetTo(enc) && !this.stmEnc)
								{
									UOSInt len = enc.CountUTF8Chars(sbText.ToString(), sbText.GetLength());
									attr.value = nns = Text::String::New(len);
									enc.UTF8FromBytes(nns.v, sbText.ToString(), sbText.GetLength(), 0);
									nns.v[len] = 0;
	
									len = enc.CountUTF8Chars(sbOri.ToString(), sbOri.GetLength());
									attr.valueOri = Text::String::New(len).Ptr();
									enc.UTF8FromBytes(attr.valueOri.v, sbOri.ToString(), sbOri.GetLength(), 0);
									attr.valueOri.v[len] = 0;
								}
								else
								{
									attr.value = Text::String::New(sbText.ToCString()).Ptr();
									attr.valueOri = Text::String::New(sbOri.ToCString()).Ptr();
								}
								isEqual = false;
							}
							else
							{
								NN<Text::XMLAttrib> attr;
								NEW_CLASSNN(attr, Text::XMLAttrib(sbText.ToCString(), null));
								this.attrList.Add(attr);
							}
							sbText.ClearStr();
							sbOri.ClearStr();
						}
						if (parseOfst + 1 >= this.buffSize)
						{
							parseOfst = 0;
							this.buffSize = 1;
							UOSInt readSize = this.FillBuffer();
							if (readSize <= 0)
							{
								this.nt = NodeType.Unknown;
								this.parseError = 40;
								return false;
							}
							this.buffSize += readSize;
						}
						if (this.readBuff[parseOfst + 1] == '>')
						{
							this.parseOfst = parseOfst + 2;
							this.emptyNode = true;
							if (this.mode == ParseMode::PM_XML && this.nodeText != 0)
							{
								this.ParseElementNS();
							}
							return this.nodeText != 0;
						}
						else
						{
							this.nt = NodeType.Unknown;
							this.parseError = 29;
							return false;
						}
					}
					else if (c == '>')
					{
						if (sbText.GetLength() > 0)
						{
							if (this.nodeText.IsNull())
							{
								this.nodeText = Text::String::New(sbText.ToCString());
								this.nodeOriText = Text::String::New(sbOri.ToCString());
							}
							else if (isEqual)
							{
								NN<Text::Encoding> enc;
								NN<Text::XMLAttrib> attr = this.attrList.GetItemNoCheck(this.attrList.size() - 1);
								OPTSTR_DEL(attr.value);
								if (this.enc.SetTo(enc) && !this.stmEnc)
								{
									UOSInt len = enc.CountUTF8Chars(sbText.ToString(), sbText.GetLength());
									attr.value = nns = Text::String::New(len);
									enc.UTF8FromBytes(nns.v, sbText.ToString(), sbText.GetLength(), 0);
									nns.v[len] = 0;
	
									len = enc.CountUTF8Chars(sbOri.ToString(), sbOri.GetLength());
									attr.valueOri = Text::String::New(len).Ptr();
									enc.UTF8FromBytes(attr.valueOri.v, sbOri.ToString(), sbOri.GetLength(), 0);
									attr.valueOri.v[len] = 0;
								}
								else
								{
									attr.value = Text::String::New(sbText.ToCString()).Ptr();
									attr.valueOri = Text::String::New(sbOri.ToCString()).Ptr();
								}
								isEqual = false;
							}
							else
							{
								NN<Text::XMLAttrib> attr;
								NEW_CLASSNN(attr, Text::XMLAttrib(sbText.ToCString(), null));
								this.attrList.Add(attr);
							}
							sbText.ClearStr();
							sbOri.ClearStr();
						}
						this.parseOfst = parseOfst + 1;
						this.emptyNode = false;
						if (this.mode == ParseMode::PM_XML && this.nodeText != 0)
						{
							this.ParseElementNS();
						}
						return this.nodeText != 0;
					}
					else if (c == '=')
					{
						if (sbText.GetLength() > 0)
						{
							NN<Text::XMLAttrib> attr;
							NEW_CLASSNN(attr, Text::XMLAttrib(sbText.ToCString(), null));
							this.attrList.Add(attr);
							sbText.ClearStr();
							sbOri.ClearStr();
						}
						if (this.nodeText.IsNull())
						{
							this.nt = NodeType.Unknown;
							this.parseError = 30;
							return false;
						}
						NN<Text::XMLAttrib> attr;
						if (!this.attrList.GetItem(this.attrList.size() - 1).SetTo(attr))
						{
							this.nt = NodeType.Unknown;
							this.parseError = 31;
							return false;
						}
						if (!attr.value.SetTo(nns) || nns.v[0] == 0)
						{
							isEqual = true;
						}
						else
						{
							this.nt = NodeType.Unknown;
							this.parseError = 32;
							return false;
						}
					}
					else if (c == '"')
					{
						if (!isEqual)
						{
							this.nt = NodeType.Unknown;
							this.parseError = 33;
							return false;
						}
						isQuote = '"';
						sbOri.AppendUTF8Char('\"');
					}
					else if (c == '\'')
					{
						if (!isEqual)
						{
							this.nt = NodeType.Unknown;
							this.parseError = 34;
							return false;
						}
						isQuote = '\'';
						sbOri.AppendUTF8Char('\'');
					}
					else
					{
						sbText.AppendUTF8Char(c);
						sbOri.AppendUTF8Char(c);
					}
					parseOfst++;
				}
				this.nt = NodeType.Unknown;
				this.parseError = 35;
				return false;
			}
		}
		else
		{
			NN<Text::Encoding> enc;
			NN<Text::StringBuilderUTF8> sbText = this.sbTmp;
			Text::StringBuilderUTF8 sbOri;
			sbText.ClearStr();
			UTF8Char c;
			UInt8 b[1];
			this.nt = NodeType.Text;
			while (true)
			{
				if (parseOfst >= this.buffSize)
				{
					parseOfst = 0;
					this.buffSize = 0;
					UOSInt readSize = this.FillBuffer();
					if (readSize <= 0)
					{
						if (this.enc.SetTo(enc) && !this.stmEnc)
						{
							UOSInt len = enc.CountUTF8Chars(sbText.ToString(), sbText.GetLength());
							this.nodeText = nns = Text::String::New(len);
							enc.UTF8FromBytes(nns.v, sbText.ToString(), sbText.GetLength(), 0);
							nns.v[len] = 0;
	
							len = enc.CountUTF8Chars(sbOri.ToString(), sbOri.GetLength());
							this.nodeOriText = nns = Text::String::New(len);
							enc.UTF8FromBytes(nns.v, sbOri.ToString(), sbOri.GetLength(), 0);
							nns.v[len] = 0;
						}
						else
						{
							this.nodeText = Text::String::New(sbText.ToCString());
							this.nodeOriText = Text::String::New(sbOri.ToCString());
						}
						this.parseOfst = parseOfst;
						return true;
					}
					this.buffSize += readSize;
				}
				c = this.readBuff[parseOfst];
				if (c == '<')
				{
					if (isHTMLScript && !Text::StrStartsWithC(&this.readBuff[parseOfst + 1], (this.buffSize - parseOfst - 1), UTF8STRC("/script>")))
					{
						sbText.AppendUTF8Char(c);
						sbOri.AppendUTF8Char(c);
					}
					else
					{
						if (this.enc.SetTo(enc) && !this.stmEnc)
						{
							UOSInt len = enc.CountUTF8Chars(sbText.ToString(), sbText.GetLength());
							this.nodeText = nns = Text::String::New(len);
							enc.UTF8FromBytes(nns.v, sbText.ToString(), sbText.GetLength(), 0);
							nns.v[len] = 0;
	
							len = enc.CountUTF8Chars(sbOri.ToString(), sbOri.GetLength());
							this.nodeOriText = nns = Text::String::New(len);
							enc.UTF8FromBytes(nns.v, sbOri.ToString(), sbOri.GetLength(), 0);
							nns.v[len] = 0;
						}
						else
						{
							this.nodeText = Text::String::New(sbText.ToCString()).Ptr();
							this.nodeOriText = Text::String::New(sbOri.ToCString()).Ptr();
						}
						this.parseOfst = parseOfst;
						return true;
					}
				}
				else if (c == '&' && !isHTMLScript)
				{
					UOSInt l = this.buffSize - parseOfst;
					if (l >= 4 && this.readBuff[parseOfst + 3] == ';')
					{
						if (Text::StrStartsWithC(&this.readBuff[parseOfst], l, UTF8STRC("&lt;")))
						{
							sbText.AppendUTF8Char('<');
							sbOri.AppendC(&this.readBuff[parseOfst], 4);
							parseOfst += 3;
						}
						else if (Text::StrStartsWithC(&this.readBuff[parseOfst], l, UTF8STRC("&gt;")))
						{
							sbText.AppendUTF8Char('>');
							sbOri.AppendC(&this.readBuff[parseOfst], 4);
							parseOfst += 3;
						}
						else if (this.mode == Text::XMLReader::PM_HTML)
						{
							if (Text::XML::HTMLAppendCharRef(&this.readBuff[parseOfst], 4, sbText))
							{
								sbOri.AppendC(&this.readBuff[parseOfst], 4);
								parseOfst += 3;
							}
							else
							{
								sbText.AppendUTF8Char('&');
								sbOri.AppendC(&this.readBuff[parseOfst], 1);
							}
						}
						else
						{
							this.parseError = 36;
							return false;
						}
					}
					else if (l >= 5 && this.readBuff[parseOfst + 4] == ';')
					{
						if (Text::StrStartsWithC(&this.readBuff[parseOfst], l, UTF8STRC("&amp;")))
						{
							sbText.AppendUTF8Char('&');
							sbOri.AppendC(&this.readBuff[parseOfst], 5);
							parseOfst += 4;
						}
						else if (this.readBuff[parseOfst + 1] == '#')
						{
							b[0] = Text::StrHex2UInt8C(&this.readBuff[parseOfst + 2]);
							sbText.AppendUTF8Char(b[0]);
							sbOri.AppendC(&this.readBuff[parseOfst], 5);
							parseOfst += 4;
						}
						else if (this.mode == Text::XMLReader::PM_HTML)
						{
							if (Text::XML::HTMLAppendCharRef(&this.readBuff[parseOfst], 5, sbText))
							{
								sbOri.AppendC(&this.readBuff[parseOfst], 5);
								parseOfst += 4;
							}
							else
							{
								sbText.AppendUTF8Char('&');
								sbOri.AppendC(&this.readBuff[parseOfst], 1);
							}
						}
						else
						{
							this.parseError = 37;
							return false;
						}
					}
					else if (l >= 6 && this.readBuff[parseOfst + 5] == ';')
					{
						if (Text::StrStartsWithC(&this.readBuff[parseOfst], l, UTF8STRC("&quot;")))
						{
							sbText.AppendUTF8Char('\"');
							sbOri.AppendC(&this.readBuff[parseOfst], 6);
							parseOfst += 5;
						}
						else if (Text::StrStartsWithC(&this.readBuff[parseOfst], l, UTF8STRC("&apos;")))
						{
							sbText.AppendUTF8Char('\'');
							sbOri.AppendC(&this.readBuff[parseOfst], 6);
							parseOfst += 5;
						}
						else if (this.mode == Text::XMLReader::PM_HTML)
						{
							if (Text::XML::HTMLAppendCharRef(&this.readBuff[parseOfst], 6, sbText))
							{
								sbOri.AppendC(&this.readBuff[parseOfst], 6);
								parseOfst += 5;
							}
							else
							{
								sbText.AppendUTF8Char('&');
								sbOri.AppendC(&this.readBuff[parseOfst], 1);
							}
						}
						else
						{
							this.parseError = 38;
							return false;
						}
					}
					else if (this.mode == Text::XMLReader::PM_HTML)
					{
						sbText.AppendUTF8Char('&');
						sbOri.AppendUTF8Char('&');
					}
					else
					{
						this.parseError = 39;
						return false;
					}
				}
				else
				{
					sbText.AppendUTF8Char(c);
					sbOri.AppendUTF8Char(c);
				}
				parseOfst++;
			}
		}
	}

	@Nullable
	public String nextElementName()
	{
		while (true)
		{
			if (!this.readNext())
				return null;
			if (this.nt == NodeType.Element)
				return this.nodeText;
			if (this.nt == NodeType.ElementEnd)
				return null;
		}
	}
}
