package org.sswr.util.parser;

import java.util.ArrayList;
import java.util.List;

import org.sswr.util.data.EncodingFactory;
import org.sswr.util.io.FileParser;
import org.sswr.util.io.FileSelector;
import org.sswr.util.io.ObjectParser;
import org.sswr.util.io.PackageFile;
import org.sswr.util.io.ParsedObject;
import org.sswr.util.io.ParserBase;
import org.sswr.util.io.ParserType;
import org.sswr.util.io.StreamData;
import org.sswr.util.map.MapManager;
import org.sswr.util.net.SSLEngine;
import org.sswr.util.net.SocketFactory;
import org.sswr.util.net.WebBrowser;

public class ParserList
{
	private List<FileParser> filePArr;
	private List<ObjectParser> objPArr;

	protected ParserList()
	{
		this.filePArr = new ArrayList<FileParser>();
		this.objPArr = new ArrayList<ObjectParser>();
	}

	public void addFileParser(FileParser parser)
	{
		this.filePArr.add(parser);
		parser.setParserList(this);
	}
	
	public void addObjectParser(ObjectParser parser)
	{
		this.objPArr.add(parser);
		parser.setParserList(this);
	}
	
	public void setCodePage(int codePage)
	{
		ParserBase parser;
		int i = this.filePArr.size();
		while (i-- > 0)
		{
			parser = this.filePArr.get(i);
			parser.setCodePage(codePage);
		}
		i = this.objPArr.size();
		while (i-- > 0)
		{
			parser = this.objPArr.get(i);
			parser.setCodePage(codePage);
		}
	}
	
	public void setMapManager(MapManager mapMgr)
	{
		ParserBase parser;
		int i = this.filePArr.size();
		while (i-- > 0)
		{
			parser = this.filePArr.get(i);
			parser.setMapManager(mapMgr);
		}
		i = this.objPArr.size();
		while (i-- > 0)
		{
			parser = this.objPArr.get(i);
			parser.setMapManager(mapMgr);
		}
	}
	
	public void setEncFactory(EncodingFactory encFact)
	{
		ParserBase parser;
		int i = this.filePArr.size();
		while (i-- > 0)
		{
			parser = this.filePArr.get(i);
			parser.setEncFactory(encFact);
		}
		i = this.objPArr.size();
		while (i-- > 0)
		{
			parser = this.objPArr.get(i);
			parser.setEncFactory(encFact);
		}
	}
	
	public void setWebBrowser(WebBrowser browser)
	{
		ParserBase parser;
		int i = this.filePArr.size();
		while (i-- > 0)
		{
			parser = this.filePArr.get(i);
			parser.setWebBrowser(browser);
		}
		i = this.objPArr.size();
		while (i-- > 0)
		{
			parser = this.objPArr.get(i);
			parser.setWebBrowser(browser);
		}
	}
	
	public void setSocketFactory(SocketFactory sockf)
	{
		ParserBase parser;
		int i = this.filePArr.size();
		while (i-- > 0)
		{
			parser = this.filePArr.get(i);
			parser.setSocketFactory(sockf);
		}
		i = this.objPArr.size();
		while (i-- > 0)
		{
			parser = this.objPArr.get(i);
			parser.setSocketFactory(sockf);
		}
	}
	
	public void setSSLEngine(SSLEngine ssl)
	{
		ParserBase parser;
		int i = this.filePArr.size();
		while (i-- > 0)
		{
			parser = this.filePArr.get(i);
			parser.setSSLEngine(ssl);
		}
		i = this.objPArr.size();
		while (i-- > 0)
		{
			parser = this.objPArr.get(i);
			parser.setSSLEngine(ssl);
		}
	}
	
	public void prepareSelector(FileSelector selector, ParserType t)
	{
		ParserBase parser;
		int i;
		int j = this.filePArr.size();
		i = 0;
		while (i < j)
		{
			parser = this.filePArr.get(i);
			parser.prepareSelector(selector, t);
			i++;
		}
		i = 0;
		j = this.objPArr.size();
		while (i < j)
		{
			parser = this.objPArr.get(i);
			parser.prepareSelector(selector, t);
			i++;
		}
	}
	
	public ParsedObject parseFile(StreamData fd, PackageFile pkgFile, ParserType targetType)
	{
		int i = 0;
		int j = this.filePArr.size();
		FileParser parser;
		ParsedObject result;
		if (fd.getDataSize() <= 0)
			return null;
		byte[] hdr = new byte[FileParser.hdrSize];
		int readSize;
		readSize = fd.getRealData(0, FileParser.hdrSize, hdr, 0);
		if (readSize != FileParser.hdrSize)
		{
			if (readSize != fd.getDataSize())
			{
				int hdrSize = readSize;
				while (true)
				{
					readSize = fd.getRealData(hdrSize, FileParser.hdrSize - hdrSize, hdr, hdrSize);
					if (readSize == 0)
					{
						return null;
					}
					hdrSize += readSize;
					if (hdrSize == FileParser.hdrSize)
					{
						break;
					}
					else if (hdrSize == fd.getDataSize())
					{
						break;
					}
				}	
			}
		}
		while (i < j)
		{
			parser = this.filePArr.get(i);
			if ((result = parser.parseFileHdr(fd, pkgFile, targetType, hdr, 0, FileParser.hdrSize)) != null)
			{
				return result;
			}
			i++;
		}
		return null;
	}
	
	public ParsedObject parseFile(StreamData fd, PackageFile pkgFile)
	{
		return parseFile(fd, pkgFile, ParserType.Unknown);
	}
	
	public ParsedObject parseFile(StreamData fd)
	{
		return parseFile(fd, null, ParserType.Unknown);
	}
	
	public ParsedObject parseFileType(StreamData fd, ParserType t)
	{
		ParsedObject pobj = this.parseFile(fd, null, t);
		ParsedObject pobj2;
		while (pobj != null)
		{
			if (pobj.getParserType() == t)
				return pobj;
			pobj2 = this.parseObjectType(pobj, t);
			pobj.dispose();
			pobj = pobj2;
		}
		return null;
	}
	
	public ParsedObject parseObject(ParsedObject pobj)
	{
		return parseObjectType(pobj, ParserType.Unknown);
	}
	
	public ParsedObject parseObjectType(ParsedObject pobj, ParserType targetType)
	{
		int i = 0;
		int j = this.objPArr.size();
		ObjectParser parser;
		ParsedObject result;
		while (i < j)
		{
			parser = this.objPArr.get(i);
			if ((result = parser.parseObject(pobj, null, targetType)) != null)
			{
				return result;
			}
			i++;
		}
		return null;
	}
}
