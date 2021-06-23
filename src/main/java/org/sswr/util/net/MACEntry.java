package org.sswr.util.net;

import java.util.Objects;

import org.sswr.util.data.DataTools;

public class MACEntry
{
	private long rangeStart;
	private long rangeEnd;
	private String name;

	public MACEntry() {
	}

	public MACEntry(long rangeStart, long rangeEnd, String name) {
		this.rangeStart = rangeStart;
		this.rangeEnd = rangeEnd;
		this.name = name;
	}

	public long getRangeStart() {
		return this.rangeStart;
	}

	public void setRangeStart(long rangeStart) {
		this.rangeStart = rangeStart;
	}

	public long getRangeEnd() {
		return this.rangeEnd;
	}

	public void setRangeEnd(long rangeEnd) {
		this.rangeEnd = rangeEnd;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof MACEntry)) {
			return false;
		}
		MACEntry mACEntry = (MACEntry) o;
		return rangeStart == mACEntry.rangeStart && rangeEnd == mACEntry.rangeEnd && Objects.equals(name, mACEntry.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(rangeStart, rangeEnd, name);
	}

	@Override
	public String toString() {
		return DataTools.toObjectString(this);
	}

}
