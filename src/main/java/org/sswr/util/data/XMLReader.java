package org.sswr.util.data;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.sswr.util.data.XMLNode.NodeType;
import org.sswr.util.io.IOStream;

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

	private void parseElementNS()
	{
		XMLAttrib attr;
		String name;
		String value;
		int i = 0;
		int j = this.attrList.size();
		while (i < j)
		{
			attr = this.attrList.get(i);
			if ((name = attr.name) != null && (value = attr.value) != null)
			{
				if (name.equals("xmlns"))
				{
					this.nsMap.put("", value);
				}
				else if (name.startsWith("xmlns:"))
				{
					this.nsMap.put(name.substring(6), value);
				}
			}
			i++;
		}
		if ((name = this.nodeText) != null)
		{
			i = name.indexOf(':');
			if (i == -1)
			{
				this.ns = this.nsMap.get("");
			}
			else
			{
				this.ns = this.nsMap.get(name.substring(0, i));
			}
		}
		else
		{
			this.ns = this.nsMap.get("");
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

	@Nonnull
	public String getCurrPath()
	{
		Iterator<String> it = this.pathList.iterator();
		if (!it.hasNext())
		{
			return "/";
		}
		StringBuilder sb = new StringBuilder();
		while (it.hasNext())
		{
			sb.append('/');
			sb.append(it.next());
		}
		return sb.toString();
	}

	public int getPathLev()
	{
		return this.pathList.size();
	}

	@Nonnull
	public NodeType getNodeType()
	{
		return this.nt;
	}

	@Nullable
	public String getNodeText()
	{
		return this.nodeText;
	}

	@Nonnull
	public String getNodeTextNN()
	{
		String s;
		if ((s = this.nodeText) == null)
			return "";
		return s;
	}

	@Nullable 
	public String getNodeOriText()
	{
		return this.nodeOriText;
	}

	@Nullable
	public String getNamespace()
	{
		return this.ns;
	}

	@Nonnull
	public String getElementName()
	{
		String s;
		if ((this.nt == NodeType.Element || this.nt == NodeType.ElementEnd) && (s = this.nodeText) != null)
		{
			int i = s.indexOf(':');
			return s.substring(i + 1);
		}
		else
		{
			return "";
		}
	}

	public int getAttribCount()
	{
		return this.attrList.size();
	}

	@Nonnull
	public XMLAttrib getAttribNoCheck(int index)
	{
		return this.attrList.get(index);
	}

	@Nullable
	public XMLAttrib getAttrib(int index)
	{
		if (index >= this.attrList.size())
			return null;
		return this.attrList.get(index);
	}

	@Nullable
	public XMLAttrib getAttrib(@Nonnull String name)
	{
		int i = this.attrList.size();
		XMLAttrib attr;
		while (i-- > 0)
		{
			attr = this.attrList.get(i);
			if (attr.name != null && attr.name.equals(name))
				return attr;
		}
		return null;
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
										sb.appendUTF8Char(StringUtil.hex2UInt8C(this.readBuff, parseOfst + 2));
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
							this.pathList.remove(this.pathList.size() - 1);
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
									wcs = (int)StringUtil.toUInt32(this.readBuff, parseOfst + 2);
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
										wcs = (int)StringUtil.toUInt32(this.readBuff, parseOfst + 2);
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
										wcs = (int)StringUtil.toUInt32(this.readBuff, parseOfst + 2);
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
						if (sbText.getLength() > 0)
						{
							if (this.nodeText == null)
							{
								this.nodeText = sbText.toString();
								this.nodeOriText = sbOri.toString();
							}
							else if (isEqual)
							{
								Encoding enc;
								XMLAttrib attr = this.attrList.get(this.attrList.size() - 1);
								attr.value = null;
								if ((enc = this.enc) != null && !this.stmEnc)
								{
									int len = enc.countUTF8Chars(sbText.getBytes(), 0, sbText.getLength());
									byte[] utf8s = new byte[len + 1];
									enc.utf8FromBytes(utf8s, 0, sbText.getBytes(), 0, sbText.getLength(), null);
									attr.value = new String(utf8s, 0, len, StandardCharsets.UTF_8);
	
									len = enc.countUTF8Chars(sbOri.getBytes(), 0, sbOri.getLength());
									utf8s = new byte[len + 1];
									enc.utf8FromBytes(utf8s, 0, sbOri.getBytes(), 0, sbOri.getLength(), null);
									attr.valueOri = new String(utf8s, 0, len, StandardCharsets.UTF_8);
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
					}
					else if (c == '/')
					{
						if (sbText.getLength() > 0)
						{
							if (this.nodeText == null)
							{
								this.nodeText = sbText.toString();
								this.nodeOriText = sbOri.toString();
							}
							else if (isEqual)
							{
								Encoding enc;
								XMLAttrib attr = this.attrList.get(this.attrList.size() - 1);
								attr.value = null;
								if ((enc = this.enc) != null && !this.stmEnc)
								{
									int len = enc.countUTF8Chars(sbText.getBytes(), 0, sbText.getLength());
									byte[] utf8s = new byte[len + 1];
									enc.utf8FromBytes(utf8s, 0, sbText.getBytes(), 0, sbText.getLength(), null);
									attr.value = new String(utf8s, 0, len, StandardCharsets.UTF_8);
	
									len = enc.countUTF8Chars(sbOri.getBytes(), 0, sbOri.getLength());
									utf8s = new byte[len + 1];
									enc.utf8FromBytes(utf8s, 0, sbOri.getBytes(), 0, sbOri.getLength(), null);
									attr.valueOri = new String(utf8s, 0, len, StandardCharsets.UTF_8);
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
						if (parseOfst + 1 >= this.buffSize)
						{
							parseOfst = 0;
							this.buffSize = 1;
							int readSize = this.fillBuffer();
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
							if (this.mode == ParseMode.XML && this.nodeText != null)
							{
								this.parseElementNS();
							}
							return this.nodeText != null;
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
						if (sbText.getLength() > 0)
						{
							if (this.nodeText == null)
							{
								this.nodeText = sbText.toString();
								this.nodeOriText = sbOri.toString();
							}
							else if (isEqual)
							{
								Encoding enc;
								XMLAttrib attr = this.attrList.get(this.attrList.size() - 1);
								attr.value = null;
								if ((enc = this.enc) != null && !this.stmEnc)
								{
									int len = enc.countUTF8Chars(sbText.getBytes(), 0, sbText.getLength());
									byte[] utf8s = new byte[len + 1];
									enc.utf8FromBytes(utf8s, 0, sbText.getBytes(), 0, sbText.getLength(), null);
									attr.value = new String(utf8s, 0, len, StandardCharsets.UTF_8);
	
									len = enc.countUTF8Chars(sbOri.getBytes(), 0, sbOri.getLength());
									utf8s = new byte[len + 1];
									enc.utf8FromBytes(utf8s, 0, sbOri.getBytes(), 0, sbOri.getLength(), null);
									attr.valueOri = new String(utf8s, 0, len, StandardCharsets.UTF_8);
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
						this.parseOfst = parseOfst + 1;
						this.emptyNode = false;
						if (this.mode == ParseMode.XML && this.nodeText != null)
						{
							this.parseElementNS();
						}
						return this.nodeText != null;
					}
					else if (c == '=')
					{
						if (sbText.getLength() > 0)
						{
							XMLAttrib attr = new XMLAttrib(sbText.toString(), null);
							this.attrList.add(attr);
							sbText.clearStr();
							sbOri.clearStr();
						}
						if (this.nodeText == null)
						{
							this.nt = NodeType.Unknown;
							this.parseError = 30;
							return false;
						}
						XMLAttrib attr;
						if (this.attrList.size() == 0 || (attr = this.attrList.get(this.attrList.size() - 1)) == null)
						{
							this.nt = NodeType.Unknown;
							this.parseError = 31;
							return false;
						}
						if ((nns = attr.value) == null || nns.length() == 0)
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
						sbOri.appendUTF8Char((byte)'\"');
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
						sbOri.appendUTF8Char((byte)'\'');
					}
					else
					{
						sbText.appendUTF8Char(c);
						sbOri.appendUTF8Char(c);
					}
					parseOfst++;
				}
			}
		}
		else
		{
			Encoding enc;
			StringBuilderUTF8 sbText = this.sbTmp;
			StringBuilderUTF8 sbOri = new StringBuilderUTF8();
			sbText.clearStr();
			byte c;
			//UInt8 b[1];
			this.nt = NodeType.Text;
			while (true)
			{
				if (parseOfst >= this.buffSize)
				{
					parseOfst = 0;
					this.buffSize = 0;
					int readSize = this.fillBuffer();
					if (readSize <= 0)
					{
						if ((enc = this.enc) != null && !this.stmEnc)
						{
							int len = enc.countUTF8Chars(sbText.getBytes(), 0, sbText.getLength());
							byte[] utf8s = new byte[len + 1];
							enc.utf8FromBytes(utf8s, 0, sbText.getBytes(), 0, sbText.getLength(), null);
							this.nodeText = new String(utf8s, 0, len, StandardCharsets.UTF_8);
	
							len = enc.countUTF8Chars(sbOri.getBytes(), 0, sbOri.getLength());
							utf8s = new byte[len + 1];
							enc.utf8FromBytes(utf8s, 0, sbOri.getBytes(), 0, sbOri.getLength(), null);
							this.nodeOriText = new String(utf8s, 0, len, StandardCharsets.UTF_8);
						}
						else
						{
							this.nodeText = sbText.toString();
							this.nodeOriText = sbOri.toString();
						}
						this.parseOfst = parseOfst;
						return true;
					}
					this.buffSize += readSize;
				}
				c = this.readBuff[parseOfst];
				if (c == '<')
				{
					if (isHTMLScript && !StringUtil.startsWithC(this.readBuff, parseOfst + 1, (this.buffSize - parseOfst - 1), "/script>"))
					{
						sbText.appendUTF8Char(c);
						sbOri.appendUTF8Char(c);
					}
					else
					{
						if ((enc = this.enc) != null && !this.stmEnc)
						{
							int len = enc.countUTF8Chars(sbText.getBytes(), 0, sbText.getLength());
							byte[] utf8s = new byte[len + 1];
							enc.utf8FromBytes(utf8s, 0, sbText.getBytes(), 0, sbText.getLength(), null);
							this.nodeText = new String(utf8s, 0, len, StandardCharsets.UTF_8);
	
							len = enc.countUTF8Chars(sbOri.getBytes(), 0, sbOri.getLength());
							utf8s = new byte[len + 1];
							enc.utf8FromBytes(utf8s, 0, sbOri.getBytes(), 0, sbOri.getLength(), null);
							this.nodeOriText = new String(utf8s, 0, len, StandardCharsets.UTF_8);
						}
						else
						{
							this.nodeText = sbText.toString();
							this.nodeOriText = sbOri.toString();
						}
						this.parseOfst = parseOfst;
						return true;
					}
				}
				else if (c == '&' && !isHTMLScript)
				{
					int l = this.buffSize - parseOfst;
					if (l >= 4 && this.readBuff[parseOfst + 3] == ';')
					{
						if (StringUtil.startsWithC(this.readBuff, parseOfst, l, "&lt;"))
						{
							sbText.appendUTF8Char((byte)'<');
							sbOri.appendC(this.readBuff, parseOfst, 4);
							parseOfst += 3;
						}
						else if (StringUtil.startsWithC(this.readBuff, parseOfst, l, "&gt;"))
						{
							sbText.appendUTF8Char((byte)'>');
							sbOri.appendC(this.readBuff, parseOfst, 4);
							parseOfst += 3;
						}
						else if (this.mode == ParseMode.HTML)
						{
							if (XmlUtil.htmlAppendCharRef(this.readBuff, parseOfst, 4, sbText))
							{
								sbOri.appendC(this.readBuff, parseOfst, 4);
								parseOfst += 3;
							}
							else
							{
								sbText.appendUTF8Char((byte)'&');
								sbOri.appendC(this.readBuff, parseOfst, 1);
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
						if (StringUtil.startsWithC(this.readBuff, parseOfst, l, "&amp;"))
						{
							sbText.appendUTF8Char((byte)'&');
							sbOri.appendC(this.readBuff, parseOfst, 5);
							parseOfst += 4;
						}
						else if (this.readBuff[parseOfst + 1] == '#')
						{
							sbText.appendUTF8Char(StringUtil.hex2UInt8C(this.readBuff, parseOfst + 2));
							sbOri.appendC(this.readBuff, parseOfst, 5);
							parseOfst += 4;
						}
						else if (this.mode == ParseMode.HTML)
						{
							if (XmlUtil.htmlAppendCharRef(this.readBuff, parseOfst, 5, sbText))
							{
								sbOri.appendC(this.readBuff, parseOfst, 5);
								parseOfst += 4;
							}
							else
							{
								sbText.appendUTF8Char((byte)'&');
								sbOri.appendC(this.readBuff, parseOfst, 1);
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
						if (StringUtil.startsWithC(this.readBuff, parseOfst, l, "&quot;"))
						{
							sbText.appendUTF8Char((byte)'\"');
							sbOri.appendC(this.readBuff, parseOfst, 6);
							parseOfst += 5;
						}
						else if (StringUtil.startsWithC(this.readBuff, parseOfst, l, "&apos;"))
						{
							sbText.appendUTF8Char((byte)'\'');
							sbOri.appendC(this.readBuff, parseOfst, 6);
							parseOfst += 5;
						}
						else if (this.mode == ParseMode.HTML)
						{
							if (XmlUtil.htmlAppendCharRef(this.readBuff, parseOfst, 6, sbText))
							{
								sbOri.appendC(this.readBuff, parseOfst, 6);
								parseOfst += 5;
							}
							else
							{
								sbText.appendUTF8Char((byte)'&');
								sbOri.appendC(this.readBuff, parseOfst, 1);
							}
						}
						else
						{
							this.parseError = 38;
							return false;
						}
					}
					else if (this.mode == ParseMode.HTML)
					{
						sbText.appendUTF8Char((byte)'&');
						sbOri.appendUTF8Char((byte)'&');
					}
					else
					{
						this.parseError = 39;
						return false;
					}
				}
				else
				{
					sbText.appendUTF8Char(c);
					sbOri.appendUTF8Char(c);
				}
				parseOfst++;
			}
		}
	}

	public boolean readNodeText(@Nonnull StringBuilderUTF8 sb)
	{
		if (this.getNodeType() == NodeType.Element)
		{
			if (this.emptyNode)
			{
				return true;
			}
			int pathLev = this.pathList.size();
			NodeType nt;
			boolean succ = true;
			while ((succ = this.readNext()) != false)
			{
				nt = this.getNodeType();
				if (nt == NodeType.ElementEnd && pathLev == this.pathList.size())
				{
					break;
				}
				else if (nt == NodeType.Text)
				{
					sb.appendOpt(this.nodeText);
				}
				else if (nt == NodeType.CData)
				{
					sb.appendOpt(this.nodeText);
				}
			}
			return succ;
		}
		else
		{
			return this.readNext();
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

	@Nullable
	public String nextElementName2()
	{
		while (true)
		{
			if (!this.readNext())
				return null;
			if (this.nt == NodeType.Element)
			{
				String name = this.nodeText;
				if (name == null) name = "";
				int i = name.indexOf(':');
				return name.substring(i + 1);
			}
			if (this.nt == NodeType.ElementEnd)
				return null;
		}
	}

	public boolean skipElement()
	{
		if (this.nt == NodeType.Element)
		{
			if (this.emptyNode)
			{
				return true;
			}
			if (this.mode == ParseMode.HTML)
			{
				if (this.isHTMLSkip())
				{
					return true;
				}
			}
			int initLev = this.pathList.size();
			boolean succ = true;
			while ((succ = this.readNext()) != false)
			{
				if (this.nt == NodeType.ElementEnd && initLev >= this.pathList.size())
				{
					break;
				}
			}
			return succ;
		}
		else
		{
			return this.readNext();
		}
	}

	public boolean isElementEmpty()
	{
		return this.nt == NodeType.Element && this.emptyNode;
	}

	public boolean isComplete()
	{
		return this.pathList.size() == 0 && this.parseOfst == this.buffSize;	
	}

	public boolean hasError()
	{
		return this.parseError != 0;
	}

	public int getErrorCode()
	{
		return this.parseError;
	}

	public boolean toString(@Nonnull StringBuilderUTF8 sb)
	{
		int i;
		int j;
		String s;
		XMLAttrib attr;
		switch (this.nt)
		{
		case Document:
			sb.appendUTF8Char((byte)'<');
			sb.appendUTF8Char((byte)'?');
			sb.appendOpt(this.nodeText);
			i = 0;
			j = this.attrList.size();
			while (i < j)
			{
				attr = this.attrList.get(i);
				sb.appendUTF8Char((byte)' ');
				sb.append(attr.toString());
				i++;
			}
			sb.appendUTF8Char((byte)'?');
			sb.appendUTF8Char((byte)'>');
			return true;
		case Element:
			sb.appendUTF8Char((byte)'<');
			sb.appendOpt(this.nodeText);
			i = 0;
			j = this.attrList.size();
			while (i < j)
			{
				attr = this.attrList.get(i);
				sb.appendUTF8Char((byte)' ');
				sb.append(attr.toString());
				i++;
			}
	
			if (this.emptyNode)
			{
				sb.appendUTF8Char((byte)'/');
			}
			sb.appendUTF8Char((byte)'>');
			return true;
		case ElementEnd:
			sb.appendUTF8Char((byte)'<');
			sb.appendUTF8Char((byte)'/');
			sb.appendOpt(this.nodeText);
			sb.appendUTF8Char((byte)'>');
			return true;
		case Text:
			if ((s = this.nodeOriText) != null)
			{
				sb.append(s);
			}
			else if (this.mode == ParseMode.XML && (s = this.nodeText) != null)
			{
				sb.append(XmlUtil.toXMLText(s));
			}
			else
			{
				sb.appendOpt(this.nodeText);
			}
			return true;
		case CData:
			sb.append("<![CDATA[");
			sb.appendOpt(this.nodeText);
			sb.append("]]>");
			return true;
		case Comment:
			sb.append("<!--");
			if ((s = this.nodeText) != null)
			{
				sb.append(s);
			}
			sb.append("-.");
			return true;
		case Attribute:
		case Unknown:
			break;
		case DocType:
			sb.appendUTF8Char((byte)'<');
			sb.appendUTF8Char((byte)'!');
			sb.appendOpt(this.nodeText);
			i = 0;
			j = this.attrList.size();
			while (i < j)
			{
				attr = this.attrList.get(i);
				sb.appendUTF8Char((byte)' ');
				sb.append(attr.toString());
				i++;
			}
	
			if (this.emptyNode)
			{
				sb.appendUTF8Char((byte)'/');
			}
			sb.appendUTF8Char((byte)'>');
			return true;
		}
		return false;
	}

	public static boolean xmlWellFormat(@Nullable EncodingFactory encFact, @Nonnull IOStream stm, int lev, @Nonnull StringBuilderUTF8 sb)
	{
		boolean toWrite;
		NodeType thisNT;
		NodeType lastNT = NodeType.Unknown;
		XMLReader reader = new XMLReader(encFact, stm, ParseMode.XML);
		while (reader.readNext())
		{
			toWrite = true;
			thisNT = reader.getNodeType();
			if (thisNT == NodeType.Text)
			{
				toWrite = false;
				String s = reader.getNodeText();
				char[] sarr = (s != null)?s.toCharArray():new char[0];
				int csptr = 0;
				char c;
				while (csptr < sarr.length)
				{
					c = sarr[csptr];
					if (c == '\t' || c == ' ' || c == '\r' || c == '\n')
					{

					}
					else
					{
						toWrite = true;
						break;
					}
				}
			}
			if (toWrite)
			{
				if (lastNT == NodeType.Element && (thisNT == NodeType.Text || thisNT == NodeType.CData))
				{

				}
				else if (thisNT == NodeType.ElementEnd && (lastNT == NodeType.Text || lastNT == NodeType.CData))
				{

				}
				else
				{
					if (lastNT == NodeType.Element || lastNT == NodeType.Text || lastNT == NodeType.CData)
					{
						sb.append("\r\n");
					}
					sb.appendChar('\t', reader.getPathLev() + lev);
				}
				
				reader.toString(sb);
				if (thisNT != NodeType.Element && thisNT != NodeType.Text && thisNT != NodeType.CData)
				{
					sb.append("\r\n");
				}
				lastNT = thisNT;
			}
		}
		return true;
	}
}
