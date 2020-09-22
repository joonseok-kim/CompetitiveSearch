package org.joonseok.comset;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import COMSETsystem.CityMap;
import COMSETsystem.LocationOnRoad;
import COMSETsystem.Road;

/**
 * Data model that considers distances from an agent's current location to zones.
 * 
 * @author Joon-Seok Kim (jkim258 at gmu.edu)
 *
 */
public abstract class DistanceBasedDataModel extends AbstractDataModel {
	protected boolean homeZoneEnabled;
	protected double weightForAngle;
	protected DistanceBasedDataModel(CityMap map) {
		super(map);
		WorldParameters params = WorldParameters.getInstance();
		homeZoneEnabled = params.homeZoneEnabled;
		weightForAngle = params.weightForAngle;
	}

	protected Road getTargetRoad(LocationOnRoad currentLocation, long currentTime, Random rnd) {
		predicateTest(currentTime);
		List<Road> roads = null;
		
		WorldParameters params = WorldParameters.getInstance();
		// Sample multiple zones
		int[] zoneIndices = new int[params.sampleSize];
		final int len = zoneIndices.length;
		for (int i = 0; i < len; i++) {
			int zoneNumber;
			do {
				zoneNumber = sampleIndex(rnd);
				// check availability
				Zone selectedZone = zones.get(zoneNumber);
				roads = selectedZone.getRoads();
			} while (roads.size() <= 0);

			zoneIndices[i] = zoneNumber;
		}

		// distance indices
		double[] probs = new double[len];
		double[] nProbs = new double[len];
		
		double sum = 0.0;
		// Adjust cdf depends on distance
		for (int i = 0; i < len; i++) {
			Zone selectedZone = zones.get(zoneIndices[i]);
			roads = selectedZone.getRoads();
			Road road = roads.get(0);
			long distance = map.travelTimeBetween(currentLocation.road.to, road.from);
			probs[i] = (distance <= 0) ? 1 : Math.pow((double) distance, params.exponent);
			if (homeZoneEnabled) {
				probs[i] *= (1-towardsHome(currentLocation, road, rnd));
			}
			sum += probs[i];
		}
		
		if (sum > 0.0) {
			for (int i = 0; i < len; i++) {
				nProbs[i] = probs[i] * 1.0 / sum;
			}
		}
		else {
			for (int i = 0; i < len; i++) {
				nProbs[i] = 1.0 / (double)probs.length;
			}
		}

		double[] cProbs = new double[nProbs.length];
		sum = 0.0;
		for (int i = 0; i < nProbs.length; i++) {
			sum += nProbs[i];
			cProbs[i] = sum;
		}
		
		// Select one
		final double randomValue = rnd.nextDouble();
		int index = Arrays.binarySearch(cProbs, randomValue);
		if (index < 0) {
			index = -index - 1;
		}
		if (index >= 0 && index < nProbs.length && randomValue < cProbs[index]) {
			// DO NOTHING
		}
		else {
			index = len - 1;
		}

		Zone selectedZone = zones.get(zoneIndices[index]);
		roads = selectedZone.getRoads();
		int rndRoad = rnd.nextInt(roads.size());
		return roads.get(rndRoad);
	}

	protected double towardsHome(LocationOnRoad currentLocation, Road targetRoad, Random rnd) {
		int belongingGroupNumber = rnd.hashCode() % zones.size();
		List<Road> zone = zones.get(belongingGroupNumber).getRoads();
		Road home = zone.get(0);

		double lat1 = targetRoad.from.latitude;
		double lon1 = targetRoad.from.longitude;

		double lat2 = (home.from.latitude + home.to.latitude) / 2;
		double lon2 = (home.from.longitude + home.to.longitude) / 2;

		double[] l = currentLocation.toLatLon();

		double x1 = lon1 - l[1];
		double y1 = lat1 - l[0];

		double x2 = lon2 - l[1];
		double y2 = lat2 - l[0];

		double a1 = Math.atan2(y1, x1);
		double a2 = Math.atan2(y2, x2);

		double r = Math.abs(a2 - a1);
		r = r > Math.PI ? 2 * Math.PI - r : r;
		r *= weightForAngle;
		return r / Math.PI;
	}
}
