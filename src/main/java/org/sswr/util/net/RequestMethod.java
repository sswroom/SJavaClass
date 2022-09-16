package org.sswr.util.net;

public enum RequestMethod
{
	Unknown,
	HTTP_GET,
	HTTP_POST,
	HTTP_PUT,
	HTTP_PATCH,
	HTTP_DELETE,
	HTTP_CONNECT,
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

	public RequestMethod fromString(String meth)
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
		default:
			return HTTP_GET;
		}
	}
}
