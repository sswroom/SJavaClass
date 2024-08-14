package org.sswr.util.media;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.sswr.util.data.ArtificialQuickSort;
import org.sswr.util.io.ParsedObject;
import org.sswr.util.io.ParserType;

public class ImageList extends ParsedObject
{
	class ImageInfo
	{
		public StaticImage img;
		public int delay;
	}

	private List<ImageInfo> imgList;

	public ImageList(String sourceName)
	{
		super(sourceName);
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

	public void sortImage()
	{
		if (this.imgList.size() <= 1)
		{
			return;
		}
		ArtificialQuickSort.sort(this.imgList, new Comparator<ImageInfo>(){

			@Override
			public int compare(ImageInfo arg0, ImageInfo arg1) {
				int size0 = arg0.img.getWidth() * arg0.img.getHeight();
				int size1 = arg1.img.getWidth() * arg1.img.getHeight();
				if (size0 > size1)
				{
					return -1;
				}
				else if (size0 < size1)
				{
					return 1;
				}
				else
				{
					return 0;
				}
			}
		});
	}

	@Override
	public ParserType getParserType() {
		return ParserType.ImageList;
	}
}
