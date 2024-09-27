package org.sswr.util.net;

import jakarta.annotation.Nonnull;

public class MQTTUtil
{
	public static boolean topicMatch(@Nonnull String topic, @Nonnull String subPattern)
	{
		if (subPattern.equals("#"))
		{
			if (!topic.startsWith("$SYS/"))
			{
				return true;
			}
			return false;
		}
		int topicIndex = 0;
		int subPatternIndex = 0;
		int i;
		while (true)
		{
			i = subPattern.indexOf('+', subPatternIndex);
			if (i < 0)
				break;
			if (i > 0)
			{
				if (!topic.substring(topicIndex).startsWith(subPattern.substring(subPatternIndex, i)))
				{
					return false;
				}
				topicIndex += i - subPatternIndex;
				subPatternIndex = i;
			}
			i = topic.indexOf('/', topicIndex);
			if (subPatternIndex + 1 >= subPattern.length())
			{
				return (i < 0);
			}
			else if (i < 0)
			{
				return false;
			}
			subPatternIndex++;
			topicIndex = i;
		}
		i = subPattern.indexOf('#', subPatternIndex);
		if (i < 0)
		{
			return topic.substring(topicIndex).equals(subPattern.substring(subPatternIndex));
		}
		else if (i == 0)
		{
			return true;
		}
	
		if (!topic.substring(topicIndex).startsWith(subPattern.substring(subPatternIndex, i)))
		{
			return false;
		}
		return true;
	}
}
