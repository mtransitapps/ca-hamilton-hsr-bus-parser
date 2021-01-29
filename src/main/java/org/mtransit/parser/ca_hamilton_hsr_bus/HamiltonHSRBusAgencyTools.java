package org.mtransit.parser.ca_hamilton_hsr_bus;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.StringUtils;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;

import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Pattern;

import static org.mtransit.parser.StringUtils.EMPTY;

// https://www.hamilton.ca/city-initiatives/strategies-actions/open-data-program
// http://googlehsr.hamilton.ca/latest/google_transit.zip
public class HamiltonHSRBusAgencyTools extends DefaultAgencyTools {

	public static void main(@Nullable String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-hamilton-hsr-bus-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new HamiltonHSRBusAgencyTools().start(args);
	}

	@Nullable
	private HashSet<Integer> serviceIdInts;

	@Override
	public void start(@NotNull String[] args) {
		MTLog.log("Generating HSR bus data...");
		long start = System.currentTimeMillis();
		this.serviceIdInts = extractUsefulServiceIdInts(args, this, true);
		super.start(args);
		MTLog.log("Generating HSR bus data... DONE in %s.", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludingAll() {
		return this.serviceIdInts != null && this.serviceIdInts.isEmpty();
	}

	@Override
	public boolean excludeCalendar(@NotNull GCalendar gCalendar) {
		if (this.serviceIdInts != null) {
			return excludeUselessCalendarInt(gCalendar, this.serviceIdInts);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(@NotNull GCalendarDate gCalendarDates) {
		if (this.serviceIdInts != null) {
			return excludeUselessCalendarDateInt(gCalendarDates, this.serviceIdInts);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	@Override
	public boolean excludeTrip(@NotNull GTrip gTrip) {
		if (this.serviceIdInts != null) {
			return excludeUselessTripInt(gTrip, this.serviceIdInts);
		}
		return super.excludeTrip(gTrip);
	}

	@Override
	public boolean excludeRoute(@NotNull GRoute gRoute) {
		return super.excludeRoute(gRoute);
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	@Override
	public long getRouteId(@NotNull GRoute gRoute) {
		if (!Utils.isDigitsOnly(gRoute.getRouteShortName())) {
			//noinspection deprecation
			return Long.parseLong(gRoute.getRouteId()); // good enough
		}
		return Long.parseLong(gRoute.getRouteShortName());
	}

	@Nullable
	@Override
	public String getRouteShortName(@NotNull GRoute gRoute) {
		if (Utils.isDigitsOnly(gRoute.getRouteShortName())) {
			return String.valueOf(Integer.valueOf(gRoute.getRouteShortName()));
		}
		return super.getRouteShortName(gRoute);
	}

	@Nullable
	@Override
	public String getRouteColor(@NotNull GRoute gRoute) {
		String routeColor = gRoute.getRouteColor();
		if ("FFFF00".equalsIgnoreCase(routeColor)) { // YELLOW - too light
			routeColor = "FFEA00"; // YELLOW - darker
		}
		if (StringUtils.isEmpty(routeColor)) {
			throw new MTLog.Fatal("Unexpected route color for %s", gRoute);
		}
		return routeColor;
	}

	@NotNull
	@Override
	public String getRouteLongName(@NotNull GRoute gRoute) {
		return CleanUtils.cleanLabel(gRoute.getRouteLongNameOrDefault().toLowerCase(Locale.ENGLISH));
	}

	private static final String AGENCY_COLOR = "F4CB0B"; // YELLOW (flag)

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	@Override
	public void setTripHeadsign(@NotNull MRoute mRoute, @NotNull MTrip mTrip, @NotNull GTrip gTrip, @NotNull GSpec gtfs) {
		mTrip.setHeadsignString(
				cleanTripHeadsign(gTrip.getTripHeadsignOrDefault()),
				gTrip.getDirectionIdOrDefault()
		);
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	private static final Pattern STARTS_WITH_RSN_LETTER = Pattern.compile("(^[\\d]+[a-z] )", Pattern.CASE_INSENSITIVE);

	@Nullable
	@Override
	public String selectDirectionHeadSign(@Nullable String headSign1, @Nullable String headSign2) {
		if (StringUtils.equals(headSign1, headSign2)) {
			return null; // can NOT select
		}
		final boolean startsWithLetter1 = headSign1 != null && STARTS_WITH_RSN_LETTER.matcher(headSign1).find();
		final boolean startsWithLetter2 = headSign2 != null && STARTS_WITH_RSN_LETTER.matcher(headSign2).find();
		if (startsWithLetter1) {
			if (!startsWithLetter2) {
				return headSign2;
			}
		} else if (startsWithLetter2) {
			return headSign1;
		}
		return null;
	}

	@Override
	public boolean mergeHeadsign(@NotNull MTrip mTrip, @NotNull MTrip mTripToMerge) {
		throw new MTLog.Fatal("Unexpected trips to merge %s & %s", mTrip, mTripToMerge);
	}

	private static final String BURLINGTON = "Burlington";
	private static final Pattern BURLINGTON_ = CleanUtils.cleanWords("burlinton");
	private static final String BURLINGTON_REPLACEMENT = CleanUtils.cleanWordsReplacement(BURLINGTON);

	private static final String COMMUNITY_CENTRE_SHORT = "CC"; // Community Center
	private static final Pattern COMMUNITY_CENTRE = CleanUtils.cleanWords("community centre", "community center");
	private static final String COMMUNITY_CENTRE_REPLACEMENT = CleanUtils.cleanWordsReplacement(COMMUNITY_CENTRE_SHORT);

	private static final String HAMILTON_AIRPORT_SHORT = "Airport"; // Hamilton
	private static final Pattern HAMILTON_AIRPORT = CleanUtils.cleanWords("hamilton airport");
	private static final String HAMILTON_AIRPORT_REPLACEMENT = CleanUtils.cleanWordsReplacement(HAMILTON_AIRPORT_SHORT);

	private static final String HAMILTON_WATERFRONT_SHORT = "Waterfront"; // Hamilton
	private static final Pattern HAMILTON_WATERFRONT = CleanUtils.cleanWords("hamilton waterfront");
	private static final String HAMILTON_WATERFRONT_REPLACEMENT = CleanUtils.cleanWordsReplacement(HAMILTON_WATERFRONT_SHORT);

	private static final String POWER_CENTRE_SHORT = "PC"; // Power Center
	private static final Pattern POWER_CENTRE = CleanUtils.cleanWords("power centre", "power center");
	private static final String POWER_CENTRE_REPLACEMENT = CleanUtils.cleanWordsReplacement(POWER_CENTRE_SHORT);

	private static final String SALTFLEET_SCHOOL_SHORT = "Saltfleet HS"; // Saltfleet High School
	private static final Pattern SALTFLEET_SCHOOL = CleanUtils.cleanWords("saltfleet high school", "saltfleet school");
	private static final String SALTFLEET_SCHOOL_REPLACEMENT = CleanUtils.cleanWordsReplacement(SALTFLEET_SCHOOL_SHORT);

	private static final String HIGH_SCHOOL_SHORT = "HS"; // High School
	private static final Pattern HIGH_SCHOOL_ = CleanUtils.cleanWords("high school");
	private static final String HIGH_SCHOOL_REPLACEMENT = CleanUtils.cleanWordsReplacement(HIGH_SCHOOL_SHORT);

	private static final String GREENE_SHORT = "Grn";
	private static final Pattern GREENE_ = CleanUtils.cleanWords("greene");
	private static final String GREENE_REPLACEMENT = CleanUtils.cleanWordsReplacement(GREENE_SHORT);

	private static final String HERITAGE = "Heritage";
	private static final Pattern HERITAGE_ = CleanUtils.cleanWords("Heratige");
	private static final String HERITAGE_REPLACEMENT = CleanUtils.cleanWordsReplacement(HERITAGE);

	private static final Pattern STARTS_WITH_ALDER_SHOT_GO_DASH_ = Pattern.compile("(^(aldershot go - ))", Pattern.CASE_INSENSITIVE); // route 18

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, tripHeadsign, getIgnoredWords());
		tripHeadsign = CleanUtils.keepToAndRemoveVia(tripHeadsign);
		tripHeadsign = BURLINGTON_.matcher(tripHeadsign).replaceAll(BURLINGTON_REPLACEMENT);
		tripHeadsign = COMMUNITY_CENTRE.matcher(tripHeadsign).replaceAll(COMMUNITY_CENTRE_REPLACEMENT);
		tripHeadsign = HAMILTON_AIRPORT.matcher(tripHeadsign).replaceAll(HAMILTON_AIRPORT_REPLACEMENT);
		tripHeadsign = HAMILTON_WATERFRONT.matcher(tripHeadsign).replaceAll(HAMILTON_WATERFRONT_REPLACEMENT);
		tripHeadsign = POWER_CENTRE.matcher(tripHeadsign).replaceAll(POWER_CENTRE_REPLACEMENT);
		tripHeadsign = SALTFLEET_SCHOOL.matcher(tripHeadsign).replaceAll(SALTFLEET_SCHOOL_REPLACEMENT);
		tripHeadsign = GREENE_.matcher(tripHeadsign).replaceAll(GREENE_REPLACEMENT);
		tripHeadsign = HERITAGE_.matcher(tripHeadsign).replaceAll(HERITAGE_REPLACEMENT);
		tripHeadsign = HIGH_SCHOOL_.matcher(tripHeadsign).replaceAll(HIGH_SCHOOL_REPLACEMENT);
		tripHeadsign = STARTS_WITH_ALDER_SHOT_GO_DASH_.matcher(tripHeadsign).replaceAll(EMPTY);
		tripHeadsign = CleanUtils.CLEAN_AT.matcher(tripHeadsign).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		tripHeadsign = CleanUtils.CLEAN_AND.matcher(tripHeadsign).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		tripHeadsign = CleanUtils.SAINT.matcher(tripHeadsign).replaceAll(CleanUtils.SAINT_REPLACEMENT);
		tripHeadsign = CleanUtils.fixMcXCase(tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	private String[] getIgnoredWords() {
		return new String[]{
				"CC", "GO", "P&R", "TC", "VIA",
		};
	}

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, gStopName, getIgnoredWords());
		gStopName = CleanUtils.CLEAN_AT.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		gStopName = CleanUtils.fixMcXCase(gStopName);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}

	@Override
	public int getStopId(@NotNull GStop gStop) {
		//noinspection deprecation
		String stopId = gStop.getStopId();
		if (stopId.length() > 0) {
			if (Utils.isDigitsOnly(stopId)) {
				return Integer.parseInt(stopId);
			}
			stopId = CleanUtils.cleanMergedID(stopId);
			if (Utils.isDigitsOnly(stopId)) {
				return Integer.parseInt(stopId);
			}
		}
		throw new MTLog.Fatal("Unexpected stop ID for %s!", gStop);
	}
}
