package org.mtransit.parser.ca_hamilton_hsr_bus;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MDirectionType;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;

// http://www.hamilton.ca/ProjectsInitiatives/OpenData/
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

	private static final long RID_STM = 100001L;

	@Override
	public long getRouteId(GRoute gRoute) {
		if (RSN_STM.equals(gRoute.getRouteShortName())) {
			return RID_STM;
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
	public String getRouteLongName(GRoute gRoute) {
		return CleanUtils.cleanLabel(gRoute.getRouteLongName().toLowerCase(Locale.ENGLISH));
	}

	private static final String AGENCY_COLOR = "F4CB0B"; // YELLOW (flag)

	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	private static final Pattern VIA = Pattern.compile("((^|\\W){1}(via)(\\W|$){1})", Pattern.CASE_INSENSITIVE);

	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip, GSpec gtfs) {
		if (mRoute.getId() == 18l) {
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

	private static final String AT = " @ ";
	private static final String TRANSIT_TERMINAL_SHORT = "TT"; // Transit Terminal
	private static final String ANCASTER_BUSINESS_PK = "Ancaster Business Pk";
	private static final String DOWNTOWN = "Downtown";
	private static final String EAST = "East";
	private static final String EASTGATE_SQUARE = "Eastgate Sq";
	private static final String FIESTA_MALL = "Fiesta Mall";
	private static final String GAGE = "Gage";
	private static final String GLANCASTER_LOOP = "Glancaster Loop";
	private static final String GREENE_SHORT = "Grn";
	private static final String HAMILTON_GO_CENTER = "Hamilton GO Ctr";
	private static final String HIGH_SCHOOL_SHORT = "HS"; // High School
	private static final String INDUSTRIAL = "Industrial";
	private static final String HAMILTON_AIRPORT_SHORT = "Airport"; // Hamilton
	private static final String HAMILTON_WATERFRONT_SHORT = "Waterfront"; // Hamilton
	private static final String HERITAGE = "Heritage";
	private static final String HIGHWAY_8 = "Hwy 8";
	private static final String JONES = "Jones";
	private static final String PLEASANT = "Pleasant";
	private static final String MAC_NAB = "MacNab";
	private static final String MAC_NAB_TRANSIT_TERMINAL = MAC_NAB + " " + TRANSIT_TERMINAL_SHORT;
	private static final String MEADOWLANDS = "Meadowlands";
	private static final String ORCHARD = "Orchard";
	private static final String PIER_8 = "Pier 8";
	private static final String RYMAL = "Rymal";
	private static final String ST_ELIZABETH_VILLAGE = "St Elizabeth Vlg";
	private static final String UPPER_OTTAWA = "Upper Ottawa";
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
					"Downtown", // same
					"5a Greenhill @ Cochrane", //
					"5e Quigley @ Greenhill", //
					"Jones @ King", //
					EAST //
					).containsAll(headsignsValues)) {
				mTrip.setHeadsignString(EAST, mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					// 52 Head St / 52 Pirie @ Governors / West Hamilton Loop / 5c Meadowlands / 5c West Hamilton Loop / Downtown
					"Downtown", // same
					"52 Head St", //
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
					"MacNab TT" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("MacNab TT", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 23L) {
			if (Arrays.asList( //
					"Upper Gage @ Mohawk", //
					"MacNab TT" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("MacNab TT", mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					"Upper Gage @ Rymal", //
					"Rymal @ Upper Gage" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Rymal @ Upper Gage", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 24L) {
			if (Arrays.asList( //
					"Upper Sherman @ Mohawk", //
					"MacNab TT" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("MacNab TT", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 33L) {
			if (Arrays.asList( //
					"Fennell @ West 5th", //
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
				mTrip.setHeadsignString("Glancaster Loop", mTrip.getHeadsignId()); // TODO ?
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
					"41a Chedoke Hosp", //
					"Mohawk @ Garth", //
					"Meadowlands" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Meadowlands", mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					"Mohawk @ Upper James", //
					"Mohawk @ Upper Gage", //
					"Lime Rdg Mall", //
					"Gage @ Industrial" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Gage @ Industrial", mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 43L) {
			if (Arrays.asList( //
					"Lime Rdg Mall", // same
					"Meadowlands" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("Meadowlands", mTrip.getHeadsignId());
				return true;
			} else if (Arrays.asList( //
					"Lime Rdg Mall", // same
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
		} else if (mTrip.getRouteId() == RID_STM) {
			if (Arrays.asList( //
					"Rymal @ Upper James", //
					"Scenic Loop", //
					"Upper Paradise @ Mohawk", //
					"St Thomas More HS PM" //
			).containsAll(headsignsValues)) {
				mTrip.setHeadsignString("St Thomas More HS PM", mTrip.getHeadsignId());
				return true;
			}
		}
		if (isGoodEnoughAccepted()) {
			return super.mergeHeadsign(mTrip, mTripToMerge);
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
