package org.sswr.util.media;

import java.util.ArrayList;
import java.util.List;

public class ImageList
{
	class ImageInfo
	{
		public StaticImage img;
		public int delay;
	}

	private List<ImageInfo> imgList;

	public ImageList()
	{
		this.imgList = new ArrayList<ImageInfo>();
	}

	public void add(StaticImage img, int delay)
	{
		ImageInfo imgInfo = new ImageInfo();
		imgInfo.img = img;
		imgInfo.delay = delay;
		this.imgList.add(imgInfo);
	}
	
	public StaticImage getImage(int index)
	{
		ImageInfo imgInfo = this.imgList.get(index);
		if (imgInfo == null)
		{
			return null;
		}
		return imgInfo.img;
	}

	public int getDelay(int index)
	{
		ImageInfo imgInfo = this.imgList.get(index);
		if (imgInfo == null)
		{
			return 0;
		}
		return imgInfo.delay;
	}

	public int size()
	{
		return this.imgList.size();
	}
}
