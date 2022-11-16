package org.sswr.util.math.geometry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.sswr.util.math.Coord2DDbl;
import org.sswr.util.math.CoordinateSystem;
import org.sswr.util.math.RectAreaDbl;

public abstract class MultiGeometry<T extends Vector2D> extends Vector2D
{
	protected List<T> geometries;
	protected boolean hasZ;
	protected boolean hasM;

	public MultiGeometry(int srid, boolean hasZ, boolean hasM)
	{
		super(srid);
		this.geometries = new ArrayList<T>();
		this.hasZ = hasZ;
		this.hasM = hasM;
	}

	public void addGeometry(T geometry)
	{
		this.geometries.add(geometry);
	}

	public int getCount()
	{
		return this.geometries.size();
	}

	public T getItem(int index)
	{
		return this.geometries.get(index);
	}

	public Coord2DDbl getCenter()
	{
		RectAreaDbl bounds = new RectAreaDbl();
		this.getBounds(bounds);
		return new Coord2DDbl((bounds.tl.x + bounds.br.x) * 0.5, (bounds.tl.y + bounds.br.y) * 0.5);
	}

	public void getBounds(RectAreaDbl bounds)
	{
		int i = 1;
		int j = this.geometries.size();
		if (j == 0)
		{
			bounds.tl = new Coord2DDbl(0, 0);
			bounds.br = new Coord2DDbl(0, 0);
		}
		else
		{
			RectAreaDbl thisBounds = new RectAreaDbl();
			this.geometries.get(0).getBounds(bounds);
			while (i < j)
			{
				this.geometries.get(i).getBounds(thisBounds);
				bounds.tl.setMin(thisBounds.tl);
				bounds.br.setMax(thisBounds.br);
				i++;
			}
		}
	}

	public double calSqrDistance(Coord2DDbl pt, Coord2DDbl nearPt)
	{
		int j = this.geometries.size();
		if (j == 0)
		{
			nearPt.x = 0;
			nearPt.y = 0;
			return 1000000000;
		}
		Coord2DDbl minPt = new Coord2DDbl();
		double minDist = this.geometries.get(0).calSqrDistance(pt, minPt);
		Coord2DDbl thisPt = new Coord2DDbl();
		double thisDist;
		int i = 1;
		while (i < j)
		{
			thisDist = this.geometries.get(i).calSqrDistance(pt, thisPt);
			if (minDist > thisDist)
			{
				minDist = thisDist;
				minPt = thisPt;
			}
			i++;
		}
		nearPt.x = minPt.x;
		nearPt.y = minPt.y;
		return minDist;
	}

	public boolean joinVector(Vector2D vec)
	{
		if (this.getVectorType() != vec.getVectorType())
		{
			return false;
		}
		@SuppressWarnings("unchecked")
		MultiGeometry<T> obj = (MultiGeometry<T> )vec;
		int i = 0;
		int j = obj.getCount();
		while (i < j)
		{
			@SuppressWarnings("unchecked")
			T t = (T)obj.getItem(i).clone();
			this.addGeometry(t);
			i++;
		}
		return true;
	}

	public boolean hasZ()
	{
		return this.hasZ;
	}

	public boolean hasM()
	{
		return this.hasM;
	}

	public void convCSys(CoordinateSystem srcCSys, CoordinateSystem destCSys)
	{
		int i = this.getCount();
		while (i-- > 0)
		{
			this.getItem(i).convCSys(srcCSys, destCSys);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof MultiGeometry)) {
			return false;
		}
		@SuppressWarnings("unchecked")
		MultiGeometry<T> multiGeometry = (MultiGeometry<T>) o;
		if (this.getVectorType() != multiGeometry.getVectorType())
			return false;
		if (multiGeometry.geometries.size() != this.geometries.size())
			return false;
		int i = multiGeometry.geometries.size();
		while (i-- > 0)
		{
			if (!this.geometries.get(i).equals(multiGeometry.geometries.get(i)))
				return false;
		}
		return true;	}

	@Override
	public boolean equalsNearly(Vector2D vec) {
		if (vec == this)
			return true;
		if (!(vec instanceof MultiGeometry)) {
			return false;
		}
		if (this.getVectorType() != vec.getVectorType())
			return false;
		@SuppressWarnings("unchecked")
		MultiGeometry<T> multiGeometry = (MultiGeometry<T>) vec;
		if (multiGeometry.geometries.size() != this.geometries.size())
			return false;
		int i = multiGeometry.geometries.size();
		while (i-- > 0)
		{
			if (!this.geometries.get(i).equalsNearly(multiGeometry.geometries.get(i)))
				return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hash(geometries, hasZ, hasM);
	}

}
