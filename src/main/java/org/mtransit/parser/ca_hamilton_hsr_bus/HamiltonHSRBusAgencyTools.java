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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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

	private static final String GO = "GO";
	private static final String TRANSIT_TERMINAL_SHORT = "TT"; // Transit Terminal
	private static final String EAST = "East";
	private static final String GREENE_SHORT = "Grn";
	private static final String HIGH_SCHOOL_SHORT = "HS"; // High School
	private static final String HAMILTON_AIRPORT_SHORT = "Airport"; // Hamilton
	private static final String HAMILTON_WATERFRONT_SHORT = "Waterfront"; // Hamilton
	private static final String HERITAGE = "Heritage";
	private static final String MAC_NAB = "MacNab";
	private static final String WEST = "West";
	private static final String BURLINGTON = "Burlington";

	@Override
	public boolean mergeHeadsign(@NotNull MTrip mTrip, @NotNull MTrip mTripToMerge) {
		List<String> headsignsValues = Arrays.asList(mTrip.getHeadsignValue(), mTripToMerge.getHeadsignValue());
		if (mTrip.getRouteId() == 1L) {
			if (Arrays.asList( //
					"1A University Plz", //
					"Hamilton " + GO + " Ctr" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Hamilton " + GO + " Ctr", mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					"Downtown", //
					"Eastgate Sq", //
					"Fiesta Mall" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Fiesta Mall", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 5L) {
			if (Arrays.asList( //
					"Downtown", // <>
					"5A Greenhill @ Cochrane", //
					"5E Quigley @ Greenhill", //
					"Jones @ King", //
					"Main @ MacNab", //
					EAST //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(EAST, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					"Downtown", // <>
					"Meadowlands", //
					"52 Head St", //
					"Head St", //
					"52 Pirie @ Governors", //
					"5C Meadowlands", //
					"5C West Hamilton Loop", //
					"West Hamilton Loop", //
					WEST //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(WEST, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 10L) {
			if (Arrays.asList( //
					"Downtown", //
					"Eastgate Sq" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Eastgate Sq", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 20L) {
			if (Arrays.asList( //
					"Mtn TC P&R", //
					"Waterfront @ Pier 8" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Waterfront @ Pier 8", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 21L) {
			if (Arrays.asList( //
					"Fennell @ West 5th", //
					"Mohawk Collegw", //
					"Mohawk College", //
					"MacNab TT" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("MacNab TT", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 23L) {
			if (Arrays.asList( //
					"Upper Gage @ Mohawk", //
					"MacNab Terminal", //
					"MacNab TT" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("MacNab TT", mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					"Upper Gage @ Rymal", //
					"Rymal @ Upper Gage" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Rymal @ Upper Gage", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 24L) {
			if (Arrays.asList( //
					"Upper Sherman @ Mohawk", //
					"John @ Jackson", //
					"MacNab TT" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("MacNab TT", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 26L) {
			if (Arrays.asList( //
					"Rymal @ Upper Wellington Only", //
					"King @ John", //
					"MacNab TT" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("MacNab TT", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 33L) {
			if (Arrays.asList( //
					"Fennell @ West 5th", //
					"Mohawk Terminal", //
					"MacNab TT" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("MacNab TT", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 34L) {
			if (Arrays.asList( //
					"34A Upper Horning Loop", //
					"Glancaster & Upper Horning Loops", //
					"Glancaster Loop" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Glancaster Loop", mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					"Mohawk @ Upper Paradise", //
					"Downtown" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Downtown", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 35L) {
			if (Arrays.asList( //
					"Mohawk College", //
					"St Elizabeth Vlg" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("St Elizabeth Vlg", mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					"John @ Jackson", //
					"MacNab Terminal", //
					"MacNab TT" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("MacNab TT", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 41L) {
			if (Arrays.asList( //
					"Mohawk @ Upper James", // <>
					"Mohawk @ Upper Gage", // <>
					"41A Chedoke Hosp", //
					"Mohawk @ Garth", //
					"Meadowlands" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Meadowlands", mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					"Mohawk @ Upper James", // <>
					"Mohawk @ Upper Gage", // <>
					"Lime Rdg Mall", //
					"Gage @ Ind" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Gage @ Ind", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 43L) {
			if (Arrays.asList( //
					"Lime Rdg Mall", // <>
					"Winterberry @ Paramount Only", //
					"Stone Church @ Upper Paradise", //
					"Meadowlands" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Meadowlands", mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					"Lime Rdg Mall", // <>
					"Bishop Ryan Secondary School", //
					"Highland @ Saltfleet HS" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Highland @ Saltfleet HS", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 44L) {
			if (Arrays.asList( //
					"Rymal @ Upper James", //
					"Rymal @ Upper Gage", //
					"Eastgate Sq", //
					"Confederation Walmart", //
					"Confederation " + GO + " Sta" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Confederation " + GO + " Sta", mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					"Rymal @ Upper James", //
					"Glancaster Loop", //
					"Ancaster Business Pk" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Ancaster Business Pk", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 52L) {
			if (Arrays.asList( //
					"Ogilvie @ Governors", //
					"Orchard @ Pleasant" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Orchard @ Pleasant", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 55L) {
			if (Arrays.asList( //
					"55A Levi Loop", //
					"Jones @ Hwy 8" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Jones @ Hwy 8", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 56L) {
			if (Arrays.asList( //
					"Confederation Walmart", //
					"Lakeland CC" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Lakeland CC", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 4344L) { // STM ST. THOMAS MORE
			if (Arrays.asList( //
					"Scenic Loop", //
					"Upper Paradise @ Mohawk", //
					"Rymal @ Upper James" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Rymal @ Upper James", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 4342L) { // SHER - SHERWOOD SECONDARY
			if (Arrays.asList( //
					"Upper Ottawa @ Fennell", //
					"Upper Gage & Lincoln Alexander South" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Upper Gage & Lincoln Alexander South", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 4343L) { // JDB - ST. JEAN DE BREBEUF
			if (Arrays.asList( //
					"John @ Jackson", //
					"Upper Gage @ Rymal", //
					"Rymal @ Upper Gage" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Rymal @ Upper Gage", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 4013L
				|| mTrip.getRouteId() == 4106L) { // ANCASTER FAIR SHUTTLE
			if (Arrays.asList( //
					"Meadowlands", //
					"Downtown" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Downtown", mTrip.getHeadsignId());
				return true;
			}
		}
		throw new MTLog.Fatal("Unexpected trips to merge %s & %s", mTrip, mTripToMerge);
	}

	private static final Pattern BURLINGTON_ = CleanUtils.cleanWords("burlinton");
	private static final String BURLINGTON_REPLACEMENT = CleanUtils.cleanWordsReplacement(BURLINGTON);

	private static final String COMMUNITY_CENTRE_SHORT = "CC"; // Community Center
	private static final Pattern COMMUNITY_CENTRE = CleanUtils.cleanWords("community centre", "community center");
	private static final String COMMUNITY_CENTRE_REPLACEMENT = CleanUtils.cleanWordsReplacement(COMMUNITY_CENTRE_SHORT);

	private static final Pattern HAMILTON_AIRPORT = CleanUtils.cleanWords("hamilton airport");
	private static final String HAMILTON_AIRPORT_REPLACEMENT = CleanUtils.cleanWordsReplacement(HAMILTON_AIRPORT_SHORT);

	private static final Pattern HAMILTON_WATERFRONT = CleanUtils.cleanWords("hamilton waterfront");
	private static final String HAMILTON_WATERFRONT_REPLACEMENT = CleanUtils.cleanWordsReplacement(HAMILTON_WATERFRONT_SHORT);

	private static final Pattern MAC_NAB_LC = CleanUtils.cleanWords("macnab");
	private static final String MAC_NAB_LC_REPLACEMENT = CleanUtils.cleanWordsReplacement(MAC_NAB);

	private static final String PARK_AND_RIDE_SHORT = "P&R"; // Park & Ride
	private static final Pattern PARK_AND_RIDE = CleanUtils.cleanWords("park 'n' ride");
	private static final String PARK_AND_RIDE_REPLACEMENT = CleanUtils.cleanWordsReplacement(PARK_AND_RIDE_SHORT);

	private static final String POWER_CENTRE_SHORT = "TC"; // Power Center
	private static final Pattern POWER_CENTRE = CleanUtils.cleanWords("power centre", "power center");
	private static final String POWER_CENTRE_REPLACEMENT = CleanUtils.cleanWordsReplacement(POWER_CENTRE_SHORT);

	private static final String SALTFLEET_SCHOOL_SHORT = "Saltfleet HS"; // Saltfleet High School
	private static final Pattern SALTFLEET_SCHOOL = CleanUtils.cleanWords("saltfleet high school", "saltfleet school");
	private static final String SALTFLEET_SCHOOL_REPLACEMENT = CleanUtils.cleanWordsReplacement(SALTFLEET_SCHOOL_SHORT);

	private static final String TRANSIT_CENTRE_SHORT = "TC"; // Transit Center
	private static final Pattern TRANSIT_CENTRE = CleanUtils.cleanWords("transit centre", "transit center");
	private static final String TRANSIT_CENTRE_REPLACEMENT = CleanUtils.cleanWordsReplacement(TRANSIT_CENTRE_SHORT);

	private static final Pattern GO_ = CleanUtils.cleanWords("go");
	private static final String GO_REPLACEMENT = CleanUtils.cleanWordsReplacement(GO);

	private static final Pattern TRANSIT_TERMINAL = CleanUtils.cleanWords("transit terminal");
	private static final String TRANSIT_TERMINAL_REPLACEMENT = CleanUtils.cleanWordsReplacement(TRANSIT_TERMINAL_SHORT);

	private static final Pattern HIGH_SCHOOL_ = CleanUtils.cleanWords("high school");
	private static final String HIGH_SCHOOL_REPLACEMENT = CleanUtils.cleanWordsReplacement(HIGH_SCHOOL_SHORT);

	private static final Pattern GREENE_ = CleanUtils.cleanWords("greene");
	private static final String GREENE_REPLACEMENT = CleanUtils.cleanWordsReplacement(GREENE_SHORT);

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
		tripHeadsign = MAC_NAB_LC.matcher(tripHeadsign).replaceAll(MAC_NAB_LC_REPLACEMENT);
		tripHeadsign = PARK_AND_RIDE.matcher(tripHeadsign).replaceAll(PARK_AND_RIDE_REPLACEMENT);
		tripHeadsign = POWER_CENTRE.matcher(tripHeadsign).replaceAll(POWER_CENTRE_REPLACEMENT);
		tripHeadsign = SALTFLEET_SCHOOL.matcher(tripHeadsign).replaceAll(SALTFLEET_SCHOOL_REPLACEMENT);
		tripHeadsign = TRANSIT_CENTRE.matcher(tripHeadsign).replaceAll(TRANSIT_CENTRE_REPLACEMENT);
		tripHeadsign = TRANSIT_TERMINAL.matcher(tripHeadsign).replaceAll(TRANSIT_TERMINAL_REPLACEMENT);
		tripHeadsign = GO_.matcher(tripHeadsign).replaceAll(GO_REPLACEMENT);
		tripHeadsign = GREENE_.matcher(tripHeadsign).replaceAll(GREENE_REPLACEMENT);
		tripHeadsign = HERITAGE_.matcher(tripHeadsign).replaceAll(HERITAGE_REPLACEMENT);
		tripHeadsign = HIGH_SCHOOL_.matcher(tripHeadsign).replaceAll(HIGH_SCHOOL_REPLACEMENT);
		tripHeadsign = STARTS_WITH_ALDER_SHOT_GO_DASH_.matcher(tripHeadsign).replaceAll(EMPTY);
		tripHeadsign = CleanUtils.CLEAN_AT.matcher(tripHeadsign).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		tripHeadsign = CleanUtils.CLEAN_AND.matcher(tripHeadsign).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		tripHeadsign = CleanUtils.SAINT.matcher(tripHeadsign).replaceAll(CleanUtils.SAINT_REPLACEMENT);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	private String[] getIgnoredWords() {
		return new String[]{
				"CC", "GO", "P&R", "TC",
		};
	}

	private static final Pattern PLATFORM_ = CleanUtils.cleanWords("platform");
	private static final String PLATFORM_REPLACEMENT = CleanUtils.cleanWordsReplacement("P");

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, gStopName, getIgnoredWords());
		gStopName = GO_.matcher(gStopName).replaceAll(GO_REPLACEMENT);
		gStopName = PLATFORM_.matcher(gStopName).replaceAll(PLATFORM_REPLACEMENT);
		gStopName = CleanUtils.CLEAN_AT.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
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
