package org.sswr.util.parser;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

import org.sswr.util.io.FileParser;
import org.sswr.util.io.FileSelector;
import org.sswr.util.io.PackageFile;
import org.sswr.util.io.ParsedObject;
import org.sswr.util.io.ParserType;
import org.sswr.util.io.StreamData;
import org.sswr.util.io.StreamDataInputStream;
import org.sswr.util.media.ImageList;
import org.sswr.util.media.ImageUtil;
import org.sswr.util.media.StaticImage;

public class ImageParser extends FileParser {

	@Override
	public ParsedObject parseFileHdr(StreamData fd, PackageFile pkgFile, ParserType targetType, byte[] hdr, int hdrOfst, int hdrSize) {
		String fmt = ImageUtil.getImageFmt(hdr);
		if (fmt.equals("dat"))
			return null;
		try
		{
			ImageReader reader = ImageIO.getImageReadersBySuffix(fmt).next();
			InputStream is = new StreamDataInputStream(fd);
			ImageInputStream iis = ImageIO.createImageInputStream(is);
			reader.setInput(iis);
			ImageList imgList = new ImageList(fd.getFullFileName());
			int i = 0;
			int j = reader.getNumImages(true);
			while (i < j)
			{
				try
				{
					BufferedImage bimg = reader.read(i, null);
					IIOMetadata metadata = reader.getImageMetadata(i);
					imgList.add(new StaticImage(bimg, metadata), 0);
					if (fmt.equals("jpg"))
					{
						break;
					}
				}
				catch (IIOException ex2)
				{
					//ex2.printStackTrace();
				}
				i++;
			}
			reader.dispose();
			iis.close();
			is.close();
			if (imgList.size() == 0)
			{
				return null;
			}
			imgList.sortImage();
			return imgList;
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
//			if (StreamUtil.seekFromBeginning(stm, 0))
//			{
//					byte[] buff = stm.readAllBytes();
//					System.out.println(buff.length +" bytes left");
//			}

			return null;
		}
	}

	@Override
	public String getName() {
		return "Image";
	}

	@Override
	public void prepareSelector(FileSelector selector, ParserType t) {
		if (t == ParserType.Unknown || t == ParserType.ImageList)
		{
			selector.addFilter("*.jpg", "JPEG Image");
			selector.addFilter("*.jpeg", "JPEG Image");
			selector.addFilter("*.gif", "GIF Image");
			selector.addFilter("*.png", "PNG Image");
			selector.addFilter("*.bmp", "BMP Image");
			selector.addFilter("*.wbmp", "WBMP Image");
		}
	}

	@Override
	public ParserType getParserType() {
		return ParserType.ImageList;
	}
}
