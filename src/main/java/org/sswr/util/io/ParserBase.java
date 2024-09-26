package org.sswr.util.io;

import org.sswr.util.data.EncodingFactory;
import org.sswr.util.map.MapManager;
import org.sswr.util.net.SSLEngine;
import org.sswr.util.net.SocketFactory;
import org.sswr.util.net.WebBrowser;
import org.sswr.util.parser.ParserList;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public abstract class ParserBase
{
	@Nonnull
	public abstract String getName();
	public void setCodePage(int codePage) {};
	public void setParserList(@Nullable ParserList parsers) {};
	public void setWebBrowser(@Nullable WebBrowser browser) {};
	public void setMapManager(@Nullable MapManager mapMgr) {};
	public void setEncFactory(@Nullable EncodingFactory encFact) {};
	public void setProgressHandler(@Nullable ProgressHandler progHdlr) {};
	public void setSocketFactory(@Nullable SocketFactory sockf) {};
	public void setSSLEngine(@Nullable SSLEngine ssl) {};
	public abstract void prepareSelector(@Nonnull FileSelector selector, @Nonnull ParserType t);
	@Nonnull
	public abstract ParserType getParserType();
}
