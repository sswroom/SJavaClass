package org.sswr.util.math.geometry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.sswr.util.math.Coord2DDbl;
import org.sswr.util.math.CoordinateSystem;
import org.sswr.util.math.RectAreaDbl;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public abstract class MultiGeometry<T extends Vector2D> extends Vector2D
{
	private static boolean VERBOSE = false;
	protected List<T> geometries;

	public MultiGeometry(int srid)
	{
		super(srid);
		this.geometries = new ArrayList<T>();
	}

	public void addGeometry(@Nonnull T geometry)
	{
		this.geometries.add(geometry);
	}

	public int getCount()
	{
		return this.geometries.size();
	}

	@Nonnull
	public Iterator<T> iterator()
	{
		return this.geometries.iterator();
	}

	@Nullable
	public T getItem(int index)
	{
		return this.geometries.get(index);
	}

	@Nonnull
	public T getItemNN(int index)
	{
		return this.geometries.get(index);
	}

	@Nonnull
	public Coord2DDbl getCenter()
	{
		return this.getBounds().getCenter();
	}

	@Nonnull
	public RectAreaDbl getBounds()
	{
		RectAreaDbl bounds;
		Iterator<T> it = this.iterator();
		if (!it.hasNext())
		{
			bounds = new RectAreaDbl(0, 0, 0, 0);
		}
		else
		{
			bounds = it.next().getBounds();
			while (it.hasNext())
			{
				bounds = bounds.mergeArea(it.next().getBounds());
			}
		}
		return bounds;
	}

	public double calBoundarySqrDistance(@Nonnull Coord2DDbl pt, @Nonnull Coord2DDbl nearPt)
	{
		Iterator<T> it = this.iterator();
		if (!it.hasNext())
		{
			nearPt.x = 0;
			nearPt.y = 0;
			return 1000000000;
		}
		Coord2DDbl minPt = new Coord2DDbl();
		double minDist = it.next().calBoundarySqrDistance(pt, minPt);
		Coord2DDbl thisPt = new Coord2DDbl();
		Double thisDist;
		while (it.hasNext())
		{
			thisDist = it.next().calBoundarySqrDistance(pt, thisPt);
			if (minDist > thisDist)
			{
				minDist = thisDist;
				minPt = thisPt;
			}
		}
		nearPt.x = minPt.x;
		nearPt.y = minPt.y;
		return minDist;
	}

	public double calSqrDistance(@Nonnull Coord2DDbl pt, @Nonnull Coord2DDbl nearPt)
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

	public double calArea()
	{
		double totalArea = 0;
		Iterator<T> it = this.geometries.iterator();
		while (it.hasNext())
		{
			totalArea += it.next().calArea();
		}
		return totalArea;
	}

	public boolean joinVector(@Nonnull Vector2D vec)
	{
		if (this.getVectorType() != vec.getVectorType())
		{
			return false;
		}
		@SuppressWarnings("unchecked")
		MultiGeometry<T> obj = (MultiGeometry<T> )vec;
		Iterator<T> it = obj.iterator();
		while (it.hasNext())
		{
			@SuppressWarnings("unchecked")
			T t = (T)it.next().clone();
			this.addGeometry(t);
		}
		return true;
	}

	public boolean hasZ()
	{
		if (this.geometries.size() > 0)
			this.geometries.get(0).hasZ();
		return false;
	}

	public boolean hasM()
	{
		if (this.geometries.size() > 0)
			this.geometries.get(0).hasM();
		return false;
	}

	public void convCSys(@Nonnull CoordinateSystem srcCSys, @Nonnull CoordinateSystem destCSys)
	{
		int i = this.getCount();
		while (i-- > 0)
		{
			this.getItemNN(i).convCSys(srcCSys, destCSys);
		}
		this.srid = destCSys.getSRID();
	}

	@Override
	public boolean equals(@Nonnull Vector2D vec, boolean sameTypeOnly, boolean nearlyVal) {
		if (vec == this)
			return true;
		if (this.getVectorType() != vec.getVectorType())
		{
			if (!sameTypeOnly && this.geometries.size() == 1)
			{
				return this.geometries.get(0).equals(vec, sameTypeOnly, nearlyVal);
			}
			if (VERBOSE)
				System.out.println("MultiGeometry: Vector type different");
			return false;
		}
		@SuppressWarnings("unchecked")
		MultiGeometry<T> multiGeometry = (MultiGeometry<T>)vec;
		if (multiGeometry.getCount() != this.getCount())
		{
			if (VERBOSE)
				System.out.println("MultiGeometry: Vector count different: "+multiGeometry.getCount()+" != "+this.getCount());
			return false;
		}
		int i = multiGeometry.geometries.size();
		while (i-- > 0)
		{
			if (!this.geometries.get(i).equals(multiGeometry.geometries.get(i), sameTypeOnly, nearlyVal))
				return false;
		}
		return true;
	}

	public boolean insideOrTouch(@Nonnull Coord2DDbl coord)
	{
		Iterator<T> it = this.geometries.iterator();
		while (it.hasNext())
		{
			if (it.next().insideOrTouch(coord))
				return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(geometries);
	}

}
