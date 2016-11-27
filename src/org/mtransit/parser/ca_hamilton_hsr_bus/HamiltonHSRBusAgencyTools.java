package org.mtransit.parser.ca_hamilton_hsr_bus;

import java.util.HashSet;
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

	@Override
	public long getRouteId(GRoute gRoute) {
		return Long.parseLong(gRoute.getRouteShortName());
	}

	private static final String ROUTE_SN_52A = "52A";
	private static final String ROUTE_ID_52 = "52";

	@Override
	public String getRouteShortName(GRoute gRoute) {
		if (ROUTE_ID_52.equals(gRoute.getRouteShortName())) {
			return ROUTE_SN_52A;
		}
		return String.valueOf(Integer.valueOf(gRoute.getRouteShortName()));
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
	private static final String HAMILTON_GO_CENTER = "Hamilton GO Ctr";
	private static final String INDUSTRIAL = "Industrial";
	private static final String HAMILTON_AIRPORT_SHORT = "Airport"; // Hamilton
	private static final String HAMILTON_WATERFRONT_SHORT = "Waterfront"; // Hamilton
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
		if (mTrip.getRouteId() == 1l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(FIESTA_MALL, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(HAMILTON_GO_CENTER, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 4l) {
			if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(DOWNTOWN, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 5l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(WEST, mTrip.getHeadsignId()); // TODO?
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(EAST, mTrip.getHeadsignId()); // TODO?
				return true;
			}
		} else if (mTrip.getRouteId() == 10l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(EASTGATE_SQUARE, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 20l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(HAMILTON_AIRPORT_SHORT, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(HAMILTON_WATERFRONT_SHORT + AT + PIER_8, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 22l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(UPPER_OTTAWA + AT + RYMAL, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 34l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(GLANCASTER_LOOP, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 35l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(ST_ELIZABETH_VILLAGE, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(MAC_NAB_TRANSIT_TERMINAL, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 41l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(MEADOWLANDS, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(GAGE + AT + INDUSTRIAL, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 43l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString("Highland @ Saltfleet HS", mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(MEADOWLANDS, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 44l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(ANCASTER_BUSINESS_PK, mTrip.getHeadsignId());
				return true;
			} else if (mTrip.getHeadsignId() == 1) {
				mTrip.setHeadsignString(EASTGATE_SQUARE, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 52l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(ORCHARD + AT + PLEASANT, mTrip.getHeadsignId());
				return true;
			}
		} else if (mTrip.getRouteId() == 55l) {
			if (mTrip.getHeadsignId() == 0) {
				mTrip.setHeadsignString(JONES + AT + HIGHWAY_8, mTrip.getHeadsignId());
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
		tripHeadsign = CleanUtils.CLEAN_AT.matcher(tripHeadsign).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
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

	private static final Pattern STOP_ID_MERGED = Pattern.compile("(([0-9]*)_merged_([0-9]*))", Pattern.CASE_INSENSITIVE);
	private static final String STOP_ID_MERGED_REPLACEMENT = "$2";

	@Override
	public int getStopId(GStop gStop) {
		String stopId = gStop.getStopId();
		if (stopId != null && stopId.length() > 0) {
			if (Utils.isDigitsOnly(stopId)) {
				return Integer.valueOf(stopId);
			}
			stopId = STOP_ID_MERGED.matcher(stopId).replaceAll(STOP_ID_MERGED_REPLACEMENT);
			if (Utils.isDigitsOnly(stopId)) {
				return Integer.valueOf(stopId);
			}
		}
		System.out.println("Unexpected stop ID for " + gStop + " !");
		System.exit(-1);
		return -1;
	}
}
