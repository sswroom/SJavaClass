package org.sswr.util.io;

import org.sswr.util.data.EncodingFactory;
import org.sswr.util.map.MapManager;
import org.sswr.util.net.SSLEngine;
import org.sswr.util.net.SocketFactory;
import org.sswr.util.net.WebBrowser;
import org.sswr.util.parser.ParserList;

public abstract class ParserBase
{
	public abstract String getName();
	public void setCodePage(int codePage) {};
	public void setParserList(ParserList parsers) {};
	public void setWebBrowser(WebBrowser browser) {};
	public void setMapManager(MapManager mapMgr) {};
	public void setEncFactory(EncodingFactory encFact) {};
	public void setProgressHandler(ProgressHandler progHdlr) {};
	public void setSocketFactory(SocketFactory sockf) {};
	public void setSSLEngine(SSLEngine ssl) {};
	public abstract void prepareSelector(FileSelector selector, ParserType t);
	public abstract ParserType getParserType();
}
