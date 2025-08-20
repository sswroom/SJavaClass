package org.sswr.util.data;

import java.util.List;

import org.sswr.util.basic.ArrayListDbl;

import jakarta.annotation.Nonnull;

public class NumArrayTool {
	public static void generateNormalRandom(@Nonnull ArrayListDbl out, @Nonnull RandomGenerator random, double average, double stddev, int count)
	{
		while (count-- > 0)
		{
			double u = 1 - random.nextDouble();
			double v = random.nextDouble();
			double z = Math.sqrt( -2.0 * Math.log( u ) ) * Math.cos( 2.0 * Math.PI * v );
			out.add(average + stddev * z);
		}
	}

	public static void generateExponentialRandom(@Nonnull ArrayListDbl out, @Nonnull RandomGenerator random, double scale, int count)
	{
		double inverseOfRate = -scale;
		while (count-- > 0)
		{
			out.add(inverseOfRate * Math.log(1 - random.nextDouble()));
		}
	}

	public static <K> void randomChoice(@Nonnull List<K> out, @Nonnull RandomGenerator random, List<K> srcList, int count)
	{
		int c = srcList.size();
		while (count-- > 0)
		{
			int r = (random.nextInt32() & 0x7fffffff) % c;
			out.add(srcList.get(r));
		}
	}
}
