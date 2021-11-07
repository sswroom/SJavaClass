package org.sswr.util.math;

import org.sswr.util.data.SharedDouble;

public class WKTWriter
{
	private String lastError;

	public WKTWriter()
	{
		this.lastError = null;
	}

	public boolean generateWKT(StringBuilder sb,Vector2D vec)
	{
		if (vec == null)
		{
			this.lastError = "Input vector is null";
			return false;
		}
		switch (vec.getVectorType())
		{
		case Point:
			sb.append("POINT(");
			if (vec.support3D())
			{
				Point3D pt = (Point3D)vec;
				SharedDouble x = new SharedDouble();
				SharedDouble y = new SharedDouble();
				SharedDouble z = new SharedDouble();
				pt.getCenter3D(x, y, z);
				sb.append(x.value);
				sb.append(" ");;
				sb.append(y.value);
				sb.append(" ");;
				sb.append(z.value);
			}
			else
			{
				Point2D pt = (Point2D)vec;
				SharedDouble x = new SharedDouble();
				SharedDouble y = new SharedDouble();
				pt.getCenter(x, y);
				sb.append(x.value);
				sb.append(" ");;
				sb.append(y.value);
			}
			sb.append(")");
			return true;
		case Polygon:
			sb.append("POLYGON(");
			{
				Polygon pg = (Polygon)vec;
				int nPtOfst;
				int nPoint;
				int []ptOfstList = pg.getPtOfstList();
				double []pointList = pg.getPointList();
				nPtOfst = ptOfstList.length;
				nPoint = pointList.length >> 1;
				int i;
				int j;
				int k;
				k = 0;
				i = 0;
				j = nPtOfst - 1;
				while (i < j)
				{
					sb.append("(");
					while (k < ptOfstList[i + 1])
					{
						sb.append(pointList[k * 2]);
						sb.append(" ");;
						sb.append(pointList[k * 2 + 1]);
						k++;
						if (k < ptOfstList[i + 1])
						{
							sb.append(",");
						}
					}
					sb.append("),");
					i++;
				}
				sb.append("(");
				while (k < nPoint)
				{
					sb.append(pointList[k * 2]);
					sb.append(" ");;
					sb.append(pointList[k * 2 + 1]);
					k++;
					if (k < nPoint)
					{
						sb.append(",");
					}
				}
				sb.append(")");
			}
			sb.append(")");
			return true;
		case Polyline:
			sb.append("POLYLINE(");
			{
				Polyline pl = (Polyline)vec;
				int nPtOfst;
				int nPoint;
				int []ptOfstList = pl.getPtOfstList();
				double []pointList = pl.getPointList();
				nPtOfst = ptOfstList.length;
				nPoint = pointList.length >> 1;
				int i;
				int j;
				int k;
				k = 0;
				i = 0;
				j = nPtOfst - 1;
				while (i < j)
				{
					sb.append("(");
					while (k < ptOfstList[i + 1])
					{
						sb.append(pointList[k * 2]);
						sb.append(" ");;
						sb.append(pointList[k * 2 + 1]);
						k++;
						if (k < ptOfstList[i + 1])
						{
							sb.append(",");
						}
					}
					sb.append("),");
					i++;
				}
				sb.append("(");
				while (k < nPoint)
				{
					sb.append(pointList[k * 2]);
					sb.append(" ");;
					sb.append(pointList[k * 2 + 1]);
					k++;
					if (k < nPoint)
					{
						sb.append(",");
					}
				}
				sb.append(")");
			}
			sb.append(")");
			return true;
		case Multipoint:
		case Image:
		case String:
		case Ellipse:
		case PieArea:
		case Unknown:
		default:
			this.lastError = "Unsupported vector type";
			return false;
		}
	}

	public String generateWKT(Vector2D vec)
	{
		StringBuilder sb = new StringBuilder();
		if (generateWKT(sb, vec))
		{
			return sb.toString();
		}
		return null;
	}

	public String getLastError()
	{
		return this.lastError;
	}
}
