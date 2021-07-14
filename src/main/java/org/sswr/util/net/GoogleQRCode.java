package org.sswr.util.net;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class GoogleQRCode
{
	private int width;
	private int height;
	private String dataStr;

	public GoogleQRCode(int width, int height, String dataStr)
	{
		this.width = width;
		this.height = height;
		this.dataStr = dataStr;
	}

	public String getImageUrl()
	{
		return "https://chart.googleapis.com/chart?cht=qr&chs="+this.width+"x"+this.height+"&chl="+URLEncoder.encode(dataStr, StandardCharsets.UTF_8);
	}
}
