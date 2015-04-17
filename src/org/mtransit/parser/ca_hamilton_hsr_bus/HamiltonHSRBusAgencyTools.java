package org.mtransit.parser.ca_hamilton_hsr_bus;

import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Pattern;

import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MDirectionType;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MSpec;
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
		System.out.printf("Generating HSR bus data...\n");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this);
		super.start(args);
		System.out.printf("Generating HSR bus data... DONE in %s.\n", Utils.getPrettyDuration(System.currentTimeMillis() - start));
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
		return Long.parseLong(gRoute.route_short_name);
	}

	private static final String ROUTE_SN_52A = "52A";
	private static final String ROUTE_ID_52 = "52";

	@Override
	public String getRouteShortName(GRoute gRoute) {
		if (ROUTE_ID_52.equals(gRoute.route_short_name)) {
			return ROUTE_SN_52A;
		}
		return String.valueOf(Integer.valueOf(gRoute.route_short_name));
	}

	@Override
	public String getRouteLongName(GRoute gRoute) {
		return MSpec.cleanLabel(gRoute.route_long_name.toLowerCase(Locale.ENGLISH));
	}

	private static final String AGENCY_COLOR = "F4CB0B"; // YELLOW (flag)

	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	@Override
	public void setTripHeadsign(MRoute route, MTrip mTrip, GTrip gTrip) {
		int directionId = gTrip.direction_id;
		if (route.id == 1l) {
			if (directionId == 0) {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			}
		} else if (route.id == 2l) {
			if (directionId == 0) {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			}
		} else if (route.id == 3l) {
			if (directionId == 0) {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			}
		} else if (route.id == 4l) {
			if (directionId == 0) {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			}
		} else if (route.id == 5l) {
			if (directionId == 0) {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			}
		} else if (route.id == 6l) {
			if (directionId == 0) {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			}
		} else if (route.id == 7l) {
			if (directionId == 0) {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			}
		} else if (route.id == 8l) {
			if (directionId == 0) {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			}
		} else if (route.id == 9l) {
			if (directionId == 0) {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			}
		} else if (route.id == 10l) {
			if (directionId == 0) {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			}
		} else if (route.id == 11l) {
			if (directionId == 0) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			}
		} else if (route.id == 12l) {
			if (directionId == 0) {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			}
		} else if (route.id == 16l) {
			if (directionId == 0) {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			}
		} else if (route.id == 18l) {
			if (directionId == 0) {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			}
		} else if (route.id == 20l) {
			if (directionId == 0) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			}
		} else if (route.id == 21l) {
			if (directionId == 0) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			}
		} else if (route.id == 22l) {
			if (directionId == 0) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			}
		} else if (route.id == 23l) {
			if (directionId == 0) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			}
		} else if (route.id == 24l) {
			if (directionId == 0) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			}
		} else if (route.id == 25l) {
			if (directionId == 0) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			}
		} else if (route.id == 26l) {
			if (directionId == 0) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			}
		} else if (route.id == 27l) {
			if (directionId == 0) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			}
		} else if (route.id == 33l) {
			if (directionId == 0) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			}
		} else if (route.id == 34l) {
			if (directionId == 0) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			}
		} else if (route.id == 35l) {
			if (directionId == 0) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			}
		} else if (route.id == 41l) {
			if (directionId == 0) {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			}
		} else if (route.id == 43l) {
			if (directionId == 0) {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			}
		} else if (route.id == 44l) {
			if (directionId == 0) {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			}
		} else if (route.id == 51l) {
			if (directionId == 0) {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			}
		} else if (route.id == 52l) {
			if (directionId == 0) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			}
		} else if (route.id == 55l) {
			if (directionId == 0) {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			}
		} else if (route.id == 56l) {
			if (directionId == 0) {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			}
		} else if (route.id == 58l) {
			if (directionId == 0) {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			}
		} else if (route.id == 99l) {
			if (directionId == 0) {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			}
		}
		String stationName = cleanTripHeadsign(gTrip.trip_headsign);
		mTrip.setHeadsignString(stationName, directionId);
		System.out.println("Unexpected trip headsign for " + mTrip + " !");
		System.exit(-1);
	}

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		return MSpec.cleanLabel(tripHeadsign.toLowerCase(Locale.ENGLISH));
	}

	private static final Pattern FIRST = Pattern.compile("(first )", Pattern.CASE_INSENSITIVE);
	private static final String FIRST_REPLACEMENT = "1st ";
	private static final Pattern SECOND = Pattern.compile("(second )", Pattern.CASE_INSENSITIVE);
	private static final String SECOND_REPLACEMENT = "2nd ";
	private static final Pattern THIRD = Pattern.compile("(third )", Pattern.CASE_INSENSITIVE);
	private static final String THIRD_REPLACEMENT = "3rd ";
	private static final Pattern FOURTH = Pattern.compile("(fourth )", Pattern.CASE_INSENSITIVE);
	private static final String FOURTH_REPLACEMENT = "4th";
	private static final Pattern FIFTH = Pattern.compile("(fifth )", Pattern.CASE_INSENSITIVE);
	private static final String FIFTH_REPLACEMENT = "5th ";
	private static final Pattern SIXTH = Pattern.compile("(sixth )", Pattern.CASE_INSENSITIVE);
	private static final String SIXTH_REPLACEMENT = "6th ";
	private static final Pattern SEVENTH = Pattern.compile("(seventh )", Pattern.CASE_INSENSITIVE);
	private static final String SEVENTH_REPLACEMENT = "7th ";
	private static final Pattern EIGHTH = Pattern.compile("(eighth )", Pattern.CASE_INSENSITIVE);
	private static final String EIGHTH_REPLACEMENT = "8th ";
	private static final Pattern NINTH = Pattern.compile("(ninth )", Pattern.CASE_INSENSITIVE);
	private static final String NINTH_REPLACEMENT = "9th ";

	private static final Pattern AT = Pattern.compile("( at )", Pattern.CASE_INSENSITIVE);
	private static final String AT_REPLACEMENT = " / ";

	@Override
	public String cleanStopName(String gStopName) {
		gStopName = gStopName.toLowerCase(Locale.ENGLISH);
		gStopName = AT.matcher(gStopName).replaceAll(AT_REPLACEMENT);
		gStopName = FIRST.matcher(gStopName).replaceAll(FIRST_REPLACEMENT);
		gStopName = SECOND.matcher(gStopName).replaceAll(SECOND_REPLACEMENT);
		gStopName = THIRD.matcher(gStopName).replaceAll(THIRD_REPLACEMENT);
		gStopName = FOURTH.matcher(gStopName).replaceAll(FOURTH_REPLACEMENT);
		gStopName = FIFTH.matcher(gStopName).replaceAll(FIFTH_REPLACEMENT);
		gStopName = SIXTH.matcher(gStopName).replaceAll(SIXTH_REPLACEMENT);
		gStopName = SEVENTH.matcher(gStopName).replaceAll(SEVENTH_REPLACEMENT);
		gStopName = EIGHTH.matcher(gStopName).replaceAll(EIGHTH_REPLACEMENT);
		gStopName = NINTH.matcher(gStopName).replaceAll(NINTH_REPLACEMENT);
		return MSpec.cleanLabel(gStopName);
	}

	private static final String STOP_ID_0 = "0";
	private static final String STOP_ID_MERGED_9655 = "_merged_9655";

	@Override
	public int getStopId(GStop gStop) {
		String stopId = gStop.stop_id;
		if (stopId != null && stopId.length() > 0) {
			if (Utils.isDigitsOnly(stopId)) {
				return Integer.valueOf(stopId);
			}
			stopId = stopId.replaceAll(STOP_ID_MERGED_9655, STOP_ID_0);
			if (Utils.isDigitsOnly(stopId)) {
				return Integer.valueOf(stopId);
			}
		}
		System.out.println("Unexpected stop ID for " + gStop + " !");
		System.exit(-1);
		return -1;
	}
}
