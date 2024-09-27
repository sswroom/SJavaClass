package org.sswr.util.net;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class FailoverHandler<T extends FailoverChannel>
{
	private FailoverType foType;
	private int lastIndex;
	private List<T> channelList;

	public FailoverHandler(@Nonnull FailoverType foType)
	{
		this.foType = foType;
		this.lastIndex = 0;
		this.channelList = new ArrayList<T>();
	}

	@Nullable
	public synchronized T getCurrChannel()
	{
		if (this.channelList.size() == 0)
		{
			return null;
		}
		int initIndex;
		int currIndex;
		T channel;
		switch (this.foType)
		{
		case ACTIVE_PASSIVE:
			initIndex = this.lastIndex;
			break;
		case MASTER_SLAVE:
			initIndex = 0;
			break;
		case ROUND_ROBIN:
			initIndex = (this.lastIndex + 1) % this.channelList.size();
			break;
		default:
			return null;
		}
		channel = this.channelList.get(initIndex);
		if (!channel.channelFail())
		{
			this.lastIndex = initIndex;
			return channel;
		}
		currIndex = (initIndex + 1) % this.channelList.size();
		while (currIndex != initIndex)
		{
			channel = this.channelList.get(currIndex);
			if (!channel.channelFail())
			{
				this.lastIndex = currIndex;
				return channel;
			}
			currIndex = (currIndex + 1) % this.channelList.size();
		}
		return null;
	}

	@Nonnull
	public synchronized List<T> getOtherChannels()
	{
		List<T> chList = new ArrayList<T>();
		T channel;
		int j = this.channelList.size();
		int i = (this.lastIndex + 1) % j;
		while (i != this.lastIndex)
		{
			channel = this.channelList.get(i);
			if (!channel.channelFail())
			{
				chList.add(channel);
			}
			i = (i + 1) % j;
		}
		return chList;
	}

	public synchronized void setCurrChannel(@Nonnull T channel)
	{
		int i = this.channelList.indexOf(channel);
		if (i >= 0)
		{
			this.lastIndex = i;
		}
	}

	@Nonnull
	public synchronized List<T> getAllChannels()
	{
		List<T> ret = new ArrayList<T>();
		ret.addAll(this.channelList);
		return ret;
	}

	public synchronized void addChannel(@Nonnull T channel)
	{
		this.channelList.add(channel);
	}
}
