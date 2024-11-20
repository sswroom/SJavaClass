package org.sswr.util.net;

import jakarta.annotation.Nonnull;

public enum RequestMethod
{
	Unknown,
	HTTP_GET,
	HTTP_POST,
	HTTP_PUT,
	HTTP_PATCH,
	HTTP_DELETE,
	HTTP_CONNECT,
	HTTP_HEAD,
	HTTP_OPTIONS,
	HTTP_TRACE,
	RTSP_DESCRIBE,
	RTSP_ANNOUNCE,
	RTSP_GET_PARAMETER,
	RTSP_OPTIONS,
	RTSP_PAUSE,
	RTSP_PLAY,
	RTSP_RECORD,
	RTSP_REDIRECT,
	RTSP_SETUP,
	RTSP_SET_PARAMETER,
	RTSP_TEARDOWN;

	@Nonnull
	public static RequestMethod fromString(@Nonnull String meth)
	{
		switch (meth.toUpperCase())
		{
		case "POST":
			return HTTP_POST;
		case "PUT":
			return HTTP_PUT;
		case "PATCH":
			return HTTP_PATCH;
		case "DELETE":
			return HTTP_DELETE;
		case "CONNECT":
			return HTTP_CONNECT;
		case "HEAD":
			return HTTP_HEAD;
		case "OPTIONS":
			return HTTP_OPTIONS;
		case "TRACE":
			return HTTP_TRACE;
		default:
			return HTTP_GET;
		}
	}
}
