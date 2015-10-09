package org.mtransit.parser.ca_hamilton_hsr_bus;

import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Pattern;

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
import org.mtransit.parser.CleanUtils;
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

	@Override
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip, GSpec gtfs) {
		if (mRoute.getId() == 1l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			}
		} else if (mRoute.getId() == 2l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			}
		} else if (mRoute.getId() == 3l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			}
		} else if (mRoute.getId() == 4l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			}
		} else if (mRoute.getId() == 5l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			}
		} else if (mRoute.getId() == 6l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			}
		} else if (mRoute.getId() == 7l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			}
		} else if (mRoute.getId() == 8l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			}
		} else if (mRoute.getId() == 9l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			}
		} else if (mRoute.getId() == 10l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			}
		} else if (mRoute.getId() == 11l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			}
		} else if (mRoute.getId() == 12l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			}
		} else if (mRoute.getId() == 16l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			}
		} else if (mRoute.getId() == 18l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			}
		} else if (mRoute.getId() == 20l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			}
		} else if (mRoute.getId() == 21l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			}
		} else if (mRoute.getId() == 22l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			}
		} else if (mRoute.getId() == 23l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			}
		} else if (mRoute.getId() == 24l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			}
		} else if (mRoute.getId() == 25l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			}
		} else if (mRoute.getId() == 26l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			}
		} else if (mRoute.getId() == 27l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			}
		} else if (mRoute.getId() == 33l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			}
		} else if (mRoute.getId() == 34l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			}
		} else if (mRoute.getId() == 35l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			}
		} else if (mRoute.getId() == 41l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			}
		} else if (mRoute.getId() == 43l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			}
		} else if (mRoute.getId() == 44l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			}
		} else if (mRoute.getId() == 51l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			}
		} else if (mRoute.getId() == 52l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			}
		} else if (mRoute.getId() == 55l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			}
		} else if (mRoute.getId() == 56l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			}
		} else if (mRoute.getId() == 58l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.EAST);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.WEST);
				return;
			}
		} else if (mRoute.getId() == 99l) {
			if (gTrip.getDirectionId() == 0) {
				mTrip.setHeadsignDirection(MDirectionType.NORTH);
				return;
			} else {
				mTrip.setHeadsignDirection(MDirectionType.SOUTH);
				return;
			}
		}
		mTrip.setHeadsignString(cleanTripHeadsign(gTrip.getTripHeadsign()), gTrip.getDirectionId());
		System.out.printf("\nUnexpected trip headsign for %s!\n", mTrip);
		System.exit(-1);
	}

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		tripHeadsign = tripHeadsign.toLowerCase(Locale.ENGLISH);
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
