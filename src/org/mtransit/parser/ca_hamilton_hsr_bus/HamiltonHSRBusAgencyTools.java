package org.mtransit.parser.ca_hamilton_hsr_bus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.Pair;
import org.mtransit.parser.SplitUtils;
import org.mtransit.parser.Utils;
import org.mtransit.parser.SplitUtils.RouteTripSpec;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.gtfs.data.GTripStop;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MDirectionType;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;
import org.mtransit.parser.mt.data.MTripStop;

// https://www.hamilton.ca/city-initiatives/strategies-actions/open-data-program
// http://googlehsr.hamilton.ca/latest/google_transit.zip
public class HamiltonHSRBusAgencyTools extends DefaultAgencyTools {

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-hamilton-hsr-bus-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new HamiltonHSRBusAgencyTools().start(args);
	}

	private HashSet<String> serviceIds;

	@Override
	public void start(String[] args) {
		System.out.printf("\nGenerating HSR bus data...");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this);
		super.start(args);
		System.out.printf("\nGenerating HSR bus data... DONE in %s.\n", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludingAll() {
		return this.serviceIds != null && this.serviceIds.isEmpty();
	}

	@Override
	public boolean excludeCalendar(GCalendar gCalendar) {
		if (this.serviceIds != null) {
			return excludeUselessCalendar(gCalendar, this.serviceIds);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(GCalendarDate gCalendarDates) {
		if (this.serviceIds != null) {
			return excludeUselessCalendarDate(gCalendarDates, this.serviceIds);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	@Override
	public boolean excludeTrip(GTrip gTrip) {
		if (this.serviceIds != null) {
			return excludeUselessTrip(gTrip, this.serviceIds);
		}
		return super.excludeTrip(gTrip);
	}

	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	private static final String RSN_STM = "STM";
	private static final String RSN_TC = "TC";
	private static final String RSN_PEACH = "PEACH";
	private static final String RSN_CAN = "CAN";
	private static final String RSN_FOF = "FOF";
	private static final String RSN_SHER = "SHER";
	private static final String RSN_VAN = "VAN";
	private static final String RSN_C_NEW = "C.NEW";
	private static final String RSN_C_JDB = "S.JDB";

	private static final long RID_STM = 100_001L;
	private static final long RID_TC = 100_002L;
	private static final long RID_PEACH = 100_003L;
	private static final long RID_CAN = 100_004L;
	private static final long RID_FOF = 100_005L;
	private static final long RID_SHER = 100_006L;
	private static final long RID_VAN = 100_007L;
	private static final long RID_C_NEW = 100_008L;
	private static final long RID_C_JDB = 100_009L;

	@Override
	public long getRouteId(GRoute gRoute) {
		if (!Utils.isDigitsOnly(gRoute.getRouteShortName())) {
			if (RSN_STM.equals(gRoute.getRouteShortName())) {
				return RID_STM;
			}
			if (RSN_TC.equals(gRoute.getRouteShortName())) {
				return RID_TC;
			}
			if (RSN_PEACH.equals(gRoute.getRouteShortName())) {
				return RID_PEACH;
			}
			if (RSN_CAN.equals(gRoute.getRouteShortName())) {
				return RID_CAN;
			}
			if (RSN_FOF.equals(gRoute.getRouteShortName())) {
				return RID_FOF;
			}
			if (RSN_SHER.equals(gRoute.getRouteShortName())) {
				return RID_SHER;
			}
			if (RSN_VAN.equals(gRoute.getRouteShortName())) {
				return RID_VAN;
			}
			if (RSN_C_NEW.equals(gRoute.getRouteShortName())) {
				return RID_C_NEW;
			}
			if (RSN_C_JDB.equals(gRoute.getRouteShortName())) {
				return RID_C_JDB;
			}
			System.out.printf("\nUnexpected route ID for %s\n", gRoute);
			System.exit(-1);
			return -1L;
		}
		return Long.parseLong(gRoute.getRouteShortName());
	}

	@Override
	public String getRouteShortName(GRoute gRoute) {
		if (Utils.isDigitsOnly(gRoute.getRouteShortName())) {
			return String.valueOf(Integer.valueOf(gRoute.getRouteShortName()));
		}
		return super.getRouteShortName(gRoute);
	}

	@Override
	public String getRouteColor(GRoute gRoute) {
		String routeColor = gRoute.getRouteColor();
		if ("FFFF00".equalsIgnoreCase(routeColor)) { // YELLOW - too light
			routeColor = "FFEA00"; // YELLOW - darker
		}
		if (StringUtils.isEmpty(routeColor)) {
			System.out.printf("\nUnexpected route color for %s\n", gRoute);
			System.exit(-1);
			return null;
		}
		return routeColor;
	}

	@Override
	public String getRouteLongName(GRoute gRoute) {
		return CleanUtils.cleanLabel(gRoute.getRouteLongName().toLowerCase(Locale.ENGLISH));
	}

	private static final String AGENCY_COLOR = "F4CB0B"; // YELLOW (flag)

	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	private static HashMap<Long, RouteTripSpec> ALL_ROUTE_TRIPS2;
	static {
		HashMap<Long, RouteTripSpec> map2 = new HashMap<Long, RouteTripSpec>();
		map2.put(18L, new RouteTripSpec(18L, //
				0, MTrip.HEADSIGN_TYPE_STRING, "Clockwise", //
				1, MTrip.HEADSIGN_TYPE_STRING, "Counter-Clockwise") //
				.addTripSort(0, //
						Arrays.asList(new String[] { //
						"355340", // "1800", // ALDERSHOT GO VIA STATION PLATFORM 10
								"355342", // ==
								"355343", // !=
								"355365", // !=
								"355366", // ==
								"355340", // "1800", // ALDERSHOT GO VIA STATION PLATFORM 10
						})) //
				.addTripSort(1, //
						Arrays.asList(new String[] { //
						"355340", // "1800", // ALDERSHOT GO VIA STATION PLATFORM 10
								"355342", // ==
								"355854", // !=
								"355879", // !=
								"355366", // ==
								"355340", // "1800", // ALDERSHOT GO VIA STATION PLATFORM 10
						//
						})) //
				.compileBothTripSort());
		ALL_ROUTE_TRIPS2 = map2;
	}

	@Override
	public int compareEarly(long routeId, List<MTripStop> list1, List<MTripStop> list2, MTripStop ts1, MTripStop ts2, GStop ts1GStop, GStop ts2GStop) {
		if (ALL_ROUTE_TRIPS2.containsKey(routeId)) {
			return ALL_ROUTE_TRIPS2.get(routeId).compare(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop, this);
		}
		return super.compareEarly(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop);
	}

	@Override
	public ArrayList<MTrip> splitTrip(MRoute mRoute, GTrip gTrip, GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return ALL_ROUTE_TRIPS2.get(mRoute.getId()).getAllTrips();
		}
		return super.splitTrip(mRoute, gTrip, gtfs);
	}

	@Override
	public Pair<Long[], Integer[]> splitTripStop(MRoute mRoute, GTrip gTrip, GTripStop gTripStop, ArrayList<MTrip> splitTrips, GSpec routeGTFS) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return SplitUtils.splitTripStop(mRoute, gTrip, gTripStop, routeGTFS, ALL_ROUTE_TRIPS2.get(mRoute.getId()), this);
		}
		return super.splitTripStop(mRoute, gTrip, gTripStop, splitTrips, routeGTFS);
	}

	private static final Pattern VIA = Pattern.compile("((^|\\W){1}(via)(\\W|$){1})", Pattern.CASE_INSENSITIVE);

	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip, GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return; // split
		}
		if (mRoute.getId() == 18L) {
			if (gTrip.getTripHeadsign().toLowerCase(Locale.ENGLISH).endsWith("eastbound")) {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			} else if (gTrip.getTripHeadsign().toLowerCase(Locale.ENGLISH).endsWith("westbound")) {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			}
		}
		String tripHeadsign = gTrip.getTripHeadsign();
		Matcher matcherVIA = VIA.matcher(tripHeadsign);
		if (matcherVIA.find()) {
			String gTripHeadsignBeforeVIA = tripHeadsign.substring(0, matcherVIA.start());
			tripHeadsign = gTripHeadsignBeforeVIA;
		}
		mTrip.setHeadsignString(cleanTripHeadsign(tripHeadsign), gTrip.getDirectionId());
	}

	private static final String TRANSIT_TERMINAL_SHORT = "TT"; // Transit Terminal
	private static final String EAST = "East";
	private static final String GREENE_SHORT = "Grn";
	private static final String HIGH_SCHOOL_SHORT = "HS"; // High School
	private static final String HAMILTON_AIRPORT_SHORT = "Airport"; // Hamilton
	private static final String HAMILTON_WATERFRONT_SHORT = "Waterfront"; // Hamilton
	private static final String HERITAGE = "Heritage";
	private static final String MAC_NAB = "MacNab";
	private static final String WEST = "West";

	@Override
	public boolean mergeHeadsign(MTrip mTrip, MTrip mTripToMerge) {
		List<String> headsignsValues = Arrays.asList(mTrip.getHeadsignValue(), mTripToMerge.getHeadsignValue());
		if (mTrip.getRouteId() == 1L) {
			if (Arrays.asList( //
					"1a University Plz", //
					"Hamilton Go Ctr" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Hamilton Go Ctr", mTrip.getHeadsignId());
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
					"5a Greenhill @ Cochrane", //
					"5e Quigley @ Greenhill", //
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
					"52 To Head St", //
					"52 Pirie @ Governors", //
					"5c Meadowlands", //
					"5c West Hamilton Loop", //
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
					"34a Upper Horning Loop", //
					"Glancaster & Upper Horning Loops", //
					"Glancaster Loop" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Glancaster Loop", mTrip.getHeadsignId());
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
					"41a Chedoke Hosp", //
					"Mohawk @ Garth", //
					"Meadowlands" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Meadowlands", mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					"Mohawk @ Upper James", // <>
					"Mohawk @ Upper Gage", // <>
					"Lime Rdg Mall", //
					"Gage @ Industrial" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Gage @ Industrial", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 43L) {
			if (Arrays.asList( //
					"Lime Rdg Mall", // <>
					"Winterberry @ Paramount Only", //
					"Meadowlands" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Meadowlands", mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					"Lime Rdg Mall", // <>
					"Highland @ Saltfleet HS" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Highland @ Saltfleet HS", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 44L) {
			if (Arrays.asList( //
					"Rymal @ Upper James", //
					"Eastgate Sq" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Eastgate Sq", mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
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
					"55a Levi Loop", //
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
		} else if (mTrip.getRouteId() == RID_STM) {
			if (Arrays.asList( //
					"Scenic Loop", //
					"Upper Paradise @ Mohawk", //
					"Rymal @ Upper James" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Rymal @ Upper James", mTrip.getHeadsignId());
				return true;
			}
			if (Arrays.asList( //
					"St Thomas More", //
					"St Thomas More HS" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("St Thomas More HS", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == RID_TC) {
			if (Arrays.asList( //
					"Ticats Shuttle", //
					"Ticat Post Game Shuttle", //
					"Tim Hortons Field" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Tim Hortons Field", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == RID_CAN) {
			if (Arrays.asList( //
					"Bayfront Pk", //
					"Waterfront" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Waterfront", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == RID_SHER) {
			if (Arrays.asList( //
					"Upper Ottawa @ Fennell", //
					"Upper Gage & Lincoln Alexander South" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Upper Gage & Lincoln Alexander South", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == RID_C_JDB) { // TODO check
			if (Arrays.asList( //
					"John @ Jackson", //
					"Upper Gage @ Rymal", //
					"Rymal @ Upper Gage" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Rymal @ Upper Gage", mTrip.getHeadsignId());
				return true;
			}
		}
		System.out.printf("\nUnexpected trips to merge %s & %s\n", mTrip, mTripToMerge);
		System.exit(-1);
		return false;
	}

	private static final String COMMUNITY_CENTRE_SHORT = "CC"; // Community Center
	private static final Pattern COMMUNITY_CENTRE = Pattern.compile("((^|\\W){1}(community centre|community center)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String COMMUNITY_CENTRE_REPLACEMENT = "$2" + COMMUNITY_CENTRE_SHORT + "$4";

	private static final Pattern HAMILTON_AIRPORT = Pattern.compile("((^|\\W){1}(hamilton airport)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String HAMILTON_AIRPORT_REPLACEMENT = "$2" + HAMILTON_AIRPORT_SHORT + "$4";

	private static final Pattern HAMILTON_WATERFRONT = Pattern.compile("((^|\\W){1}(hamilton waterfront)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String HAMILTON_WATERFRONT_REPLACEMENT = "$2" + HAMILTON_WATERFRONT_SHORT + "$4";

	private static final Pattern MAC_NAB_LC = Pattern.compile("((^|\\W){1}(macnab)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String MAC_NAB_LC_REPLACEMENT = "$2" + MAC_NAB + "$4";

	private static final String PARK_AND_RIDE_SHORT = "P&R"; // Park & Ride
	private static final Pattern PARK_AND_RIDE = Pattern.compile("((^|\\W){1}(park 'n' ride)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String PARK_AND_RIDE_REPLACEMENT = "$2" + PARK_AND_RIDE_SHORT + "$4";

	private static final String POWER_CENTRE_SHORT = "TC"; // Power Center
	private static final Pattern POWER_CENTRE = Pattern.compile("((^|\\W){1}(power centre|power center)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String POWER_CENTRE_REPLACEMENT = "$2" + POWER_CENTRE_SHORT + "$4";

	private static final String SALTFLEET_SCHOOL_SHORT = "Saltfleet HS"; // Saltfleet High School
	private static final Pattern SALTFLEET_SCHOOL = Pattern.compile("((^|\\W){1}(saltfleet high school|saltfleet school)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String SALTFLEET_SCHOOL_REPLACEMENT = "$2" + SALTFLEET_SCHOOL_SHORT + "$4";

	private static final String TRANSIT_CENTRE_SHORT = "TC"; // Transit Center
	private static final Pattern TRANSIT_CENTRE = Pattern.compile("((^|\\W){1}(transit centre|transit center)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String TRANSIT_CENTRE_REPLACEMENT = "$2" + TRANSIT_CENTRE_SHORT + "$4";

	private static final Pattern TRANSIT_TERMINAL = Pattern.compile("((^|\\W){1}(transit terminal)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String TRANSIT_TERMINAL_REPLACEMENT = "$2" + TRANSIT_TERMINAL_SHORT + "$4";

	private static final Pattern HIGH_SCHOOL_ = Pattern.compile("((^|\\W){1}(high school)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String HIGH_SCHOOL_REPLACEMENT = "$2" + HIGH_SCHOOL_SHORT + "$4";

	private static final Pattern GREENE_ = Pattern.compile("((^|\\W){1}(greene)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String GREENE_REPLACEMENT = "$2" + GREENE_SHORT + "$4";

	private static final Pattern HERITAGE_ = Pattern.compile("((^|\\W){1}(Heratige)(\\W|$){1})", Pattern.CASE_INSENSITIVE);
	private static final String HERITAGE_REPLACEMENT = "$2" + HERITAGE + "$4";

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		tripHeadsign = tripHeadsign.toLowerCase(Locale.ENGLISH);
		tripHeadsign = COMMUNITY_CENTRE.matcher(tripHeadsign).replaceAll(COMMUNITY_CENTRE_REPLACEMENT);
		tripHeadsign = HAMILTON_AIRPORT.matcher(tripHeadsign).replaceAll(HAMILTON_AIRPORT_REPLACEMENT);
		tripHeadsign = HAMILTON_WATERFRONT.matcher(tripHeadsign).replaceAll(HAMILTON_WATERFRONT_REPLACEMENT);
		tripHeadsign = MAC_NAB_LC.matcher(tripHeadsign).replaceAll(MAC_NAB_LC_REPLACEMENT);
		tripHeadsign = PARK_AND_RIDE.matcher(tripHeadsign).replaceAll(PARK_AND_RIDE_REPLACEMENT);
		tripHeadsign = POWER_CENTRE.matcher(tripHeadsign).replaceAll(POWER_CENTRE_REPLACEMENT);
		tripHeadsign = SALTFLEET_SCHOOL.matcher(tripHeadsign).replaceAll(SALTFLEET_SCHOOL_REPLACEMENT);
		tripHeadsign = TRANSIT_CENTRE.matcher(tripHeadsign).replaceAll(TRANSIT_CENTRE_REPLACEMENT);
		tripHeadsign = TRANSIT_TERMINAL.matcher(tripHeadsign).replaceAll(TRANSIT_TERMINAL_REPLACEMENT);
		tripHeadsign = GREENE_.matcher(tripHeadsign).replaceAll(GREENE_REPLACEMENT);
		tripHeadsign = HERITAGE_.matcher(tripHeadsign).replaceAll(HERITAGE_REPLACEMENT);
		tripHeadsign = HIGH_SCHOOL_.matcher(tripHeadsign).replaceAll(HIGH_SCHOOL_REPLACEMENT);
		tripHeadsign = CleanUtils.CLEAN_AT.matcher(tripHeadsign).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		tripHeadsign = CleanUtils.CLEAN_AND.matcher(tripHeadsign).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
		tripHeadsign = CleanUtils.SAINT.matcher(tripHeadsign).replaceAll(CleanUtils.SAINT_REPLACEMENT);
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		tripHeadsign = CleanUtils.removePoints(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	@Override
	public String cleanStopName(String gStopName) {
		gStopName = gStopName.toLowerCase(Locale.ENGLISH);
		gStopName = CleanUtils.CLEAN_AT.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}

	@Override
	public int getStopId(GStop gStop) {
		String stopId = gStop.getStopId();
		if (stopId != null && stopId.length() > 0) {
			if (Utils.isDigitsOnly(stopId)) {
				return Integer.valueOf(stopId);
			}
			stopId = CleanUtils.cleanMergedID(stopId);
			if (Utils.isDigitsOnly(stopId)) {
				return Integer.valueOf(stopId);
			}
		}
		System.out.println("Unexpected stop ID for " + gStop + " !");
		System.exit(-1);
		return -1;
	}
}
