package org.sswr.util.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sswr.util.io.ParsedObject;
import org.sswr.util.io.ParserType;
import org.sswr.util.io.stmdata.FileData;
import org.sswr.util.parser.ParserList;

public class MapManager
{
	public static class MapLayerInfo
	{
		public String fileName;
		public MapDrawLayer layer;
		public List<MapEnv> envList;
	}

	private Map<String, MapLayerInfo> layerArr;

	public MapManager()
	{
		this.layerArr = new HashMap<String, MapLayerInfo>();
	}

	public void dispose()
	{
		Iterator<MapLayerInfo> it = this.layerArr.values().iterator();
		while (it.hasNext())
		{
			it.next().layer.dispose();
		}
	}

	public MapDrawLayer loadLayer(String fileName, ParserList parsers, MapEnv env)
	{
		MapLayerInfo info = this.layerArr.get(fileName);
		if (info != null)
		{
			if (info.envList.indexOf(env) == -1)
			{
				info.envList.add(env);
			}
			return info.layer;
		}
		ParsedObject pobj;
		FileData fd = new FileData(fileName, false);
		pobj = parsers.parseFile(fd);
		fd.close();
		if (pobj != null && pobj.getParserType() != ParserType.MapLayer)
		{
			pobj.dispose();
			return null;
		}
		MapDrawLayer lyr;
		lyr = (MapDrawLayer)pobj;
		info = new MapLayerInfo();
		info.fileName = fileName;
		info.envList = new ArrayList<MapEnv>();
		info.layer = lyr;
		info.envList.add(env);
		this.layerArr.put(fileName, info);
		return lyr;		
	}

	public void clearMap(MapEnv env)
	{
		Iterator<MapLayerInfo> it = this.layerArr.values().iterator();
		MapLayerInfo info;
		while (it.hasNext())
		{
			info = it.next();
			int j = info.envList.indexOf(env);
			if (j != -1)
			{
				info.envList.remove(j);
				if (info.envList.size() == 0)
				{
					this.layerArr.remove(info.fileName);
					info.layer.dispose();
				}
			}
		}
	}
}
