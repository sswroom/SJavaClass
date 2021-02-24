package org.sswr.util.media;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.metadata.IIOMetadata;

public class ImageList
{
	class ImageInfo
	{
		public BufferedImage img;
		public int delay;
	}

	private IIOMetadata metadata;
	private List<ImageInfo> imgList;

	public ImageList(IIOMetadata metadata)
	{
		this.metadata = metadata;
		this.imgList = new ArrayList<ImageInfo>();
	}

	public void addImage(BufferedImage img, int delay)
	{
		ImageInfo imgInfo = new ImageInfo();
		imgInfo.img = img;
		imgInfo.delay = delay;
		this.imgList.add(imgInfo);
	}
	
	public BufferedImage getImage(int index)
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

	public IIOMetadata getMetadata()
	{
		return this.metadata;
	}
}
