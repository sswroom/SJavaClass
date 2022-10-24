package org.sswr.util.net;

public class MIME
{
	public static String getMIMEFromExt(String ext)
	{
		if (ext.length() > 4 || ext.length() < 2)
		{
			return "application/octet-stream";
		}
		ext = ext.toLowerCase();
		switch (ext)
		{
		case "3gp":
			return "video/3gpp";
		case "3g2":
			return "video/3gpp2";
		case "aac":
			return "audio/x-aac";
		case "aif":
			return "audio/aiff";
		case "asf":
			return "video/x-ms-asf";
		case "bmp":
			return "image/x-bmp";
		case "css":
			return "text/css";
		case "csv":
			return "text/csv";
		case "dbf":
			return "application/dbf";
		case "doc":
			return "application/msword";
		case "docm":
			return "application/vnd.ms-word.document.macroEnabled.12";
		case "docx":
			return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
		case "dot":
			return "application/msword";
		case "dotm":
			return "application/vnd.ms-word.template.macroEnabled.12";
		case "dotx":
			return "application/vnd.openxmlformats-officedocument.wordprocessingml.template";
		case "eml":
			return "message/rfc822";
		case "exe":
			return "application/x-exe";
		case "flv":
			return "video/x-flv";
		case "gif":
			return "image/gif";
		case "glb":
			return "model/gltf-binary";
		case "gpx":
			return "application/gpx+xml";
		case "gz":
			return "application/x-gzip";
		case "htm":
			return "text/html";
		case "html":
			return "text/html";
		case "ico":
			return "image/vnd.microsoft.icon";
		case "igs":
			return "model/iges";
		case "iso":
			return "application/x-iso9660-image";
		case "jp2":
			return "image/jpeg2000";
		case "jpeg":
			return "image/jpeg";
		case "jpg":
			return "image/jpeg";
		case "js":
			return "application/javascript";
		case "json":
			return "application/json";
		case "kml":
			return "application/vnd.google-earth.kml+xml";
		case "kmz":
			return "application/vnd.google-earth.kmz";
		case "lnk":
			return "application/x-ms-shortcut";
		case "m1v":
			return "video/MPV";
		case "m2v":
			return "video/MPV";
		case "m2p":
			return "video/MP2P";
		case "m2ts":
			return "video/MP2T";
		case "m2t":
			return "video/MP2T";
		case "m3u8":
			return "application/vnd.apple.mpegurl";
		case "m4a":
			return "audio/x-m4a";
		case "mdb":
			return "application/vnd.ms-access";
		case "mkv":
			return "video/x-matroska";
		case "mov":
			return "video/quicktime";
		case "mp2":
			return "audio/mpeg";
		case "mp3":
			return "audio/mpeg";
		case "mp4":
			return "video/mp4";
		case "mpg":
			return "video/mpeg";
		case "ogg":
			return "application/ogg";
		case "p10":
			return "application/pkcs10";
		case "p7c":
			return "application/pkcs7-mime";
		case "p7m":
			return "application/pkcs7-mime";
		case "p7s":
			return "application/pkcs7-signature";
		case "pac":
			return "application/x-ns-proxy-autoconfig";
		case "pdf":
			return "application/pdf";
		case "pic":
			return "image/x-pict";
		case "png":
			return "image/png";
		case "pnt":
			return "image/x-maxpaint";
		case "rar":
			return "application/x-rar-compressed";
		case "svg":
			return "image/svg+xml";
		case "swf":
			return "application/x-shockwave-flash";
		case "tar":
			return "application/x-tar";
		case "tga":
			return "image/x-targa";
		case "tif":
			return "image/tiff";
		case "ts":
			return "video/MP2T";
		case "txt":
			return "text/plain";
		case "wav":
			return "audio/x-wav";
		case "webm":
			return "video/webm";
		case "wma":
			return "audio/x-ms-wma";
		case "wmv":
			return "video/x-ms-wmv";
		case "wrl":
			return "model/vrml";
		case "x3d":
			return "model/x3d+xml";
		case "x3dv":
			return "model/x3d+vrml";
		case "x3db":
			return "model/x3d+binary";
		case "xla":
			return "application/vnd.ms-excel";
		case "xlam":
			return "application/vnd.ms-excel.addin.macroEnabled.12";
		case "xls":
			return "application/vnd.ms-excel";
		case "xlsb":
			return "application/vnd.ms-excel.sheet.binary.macroEnabled.12";
		case "xlsm":
			return "application/vnd.ms-excel.sheet.macroEnabled.12";
		case "xlsx":
			return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
		case "xlt":
			return "application/vnd.ms-excel";
		case "xltm":
			return "application/vnd.ms-excel.template.macroEnabled.12";
		case "xltx":
			return "application/vnd.openxmlformats-officedocument.spreadsheetml.template";
		case "xml":
			return "text/xml";
		case "zip":
			return "application/zip";
		default:
			return "application/octet-stream";
		}
	}
	
	public static String getMIMEFromFileName(String fileName)
	{
		int i = fileName.lastIndexOf('.');
		if (i == -1)
		{
			return "application/octet-stream";
		}
		return getMIMEFromExt(fileName.substring(i + 1));
	}
}
