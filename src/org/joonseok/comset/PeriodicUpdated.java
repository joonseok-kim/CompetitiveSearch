package org.joonseok.comset;

import java.util.function.Predicate;

/**
 * Predicate to update periodically
 * 
 * @author Joon-Seok Kim (jkim258 at gmu.edu)
 *
 */
public class PeriodicUpdated implements Predicate<Long> {
	long lastUpdate = Long.MIN_VALUE;
	int period = 1;

	PeriodicUpdated(int periodInSecond) {
		if (periodInSecond > 0)
			this.period = periodInSecond;
	}

	@Override
	public boolean test(Long t) {
		if (lastUpdate + period <= t) {
			lastUpdate = t;
			return true;
		}
		return false;
	}
}
