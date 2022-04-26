package org.mtransit.parser.ca_hamilton_hsr_bus;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.commons.CharUtils;
import org.mtransit.commons.CleanUtils;
import org.mtransit.commons.StringUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.mt.data.MAgency;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import static org.mtransit.commons.StringUtils.EMPTY;

// https://www.hamilton.ca/city-initiatives/strategies-actions/open-data-program
// https://googlehsr.hamilton.ca/latest/google_transit.zip
public class HamiltonHSRBusAgencyTools extends DefaultAgencyTools {

	public static void main(@NotNull String[] args) {
		new HamiltonHSRBusAgencyTools().start(args);
	}

	@NotNull
	@Override
	public String getAgencyName() {
		return "HSR";
	}

	public boolean defaultExcludeEnabled() {
		return true;
	}

	@Nullable
	@Override
	public List<Locale> getSupportedLanguages() {
		return LANG_EN;
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	@Override
	public boolean defaultRouteIdEnabled() {
		return true;
	}

	@Override
	public boolean useRouteShortNameForRouteId() {
		return true;
	}

	@Nullable
	@Override
	public Long convertRouteIdFromShortNameNotSupported(@NotNull String routeShortName) {
		if ("ROCKTON".equalsIgnoreCase(routeShortName)) {
			return 4528L;
		} else if ("TC".equalsIgnoreCase(routeShortName)) {
			return 4531L;
		} else if ("SHER".equalsIgnoreCase(routeShortName)) {
			return 4530L;
		} else if ("BLACK".equalsIgnoreCase(routeShortName)) {
			return 4619L;
		} else if ("GOLD".equalsIgnoreCase(routeShortName)) {
			return 4618L;
		} else if ("GREEN".equalsIgnoreCase(routeShortName)) {
			return 4621L;
		} else if ("RED".equalsIgnoreCase(routeShortName)) {
			return 4620L;
		}
		return super.convertRouteIdFromShortNameNotSupported(routeShortName);
	}

	@Nullable
	@Override
	public Long convertRouteIdPreviousChars(@NotNull String previousChars) {
		if ("TC".equalsIgnoreCase(previousChars)) {
			return 99_000L;
		}
		return null;
	}

	private static final Pattern STARTS_WITH_0_ = Pattern.compile("(^0*)");

	private static final Pattern DASH_ = Pattern.compile("(\\-+)");

	@NotNull
	@Override
	public String cleanRouteShortName(@NotNull String routeShortName) {
		routeShortName = STARTS_WITH_0_.matcher(routeShortName).replaceAll(EMPTY);
		routeShortName = DASH_.matcher(routeShortName).replaceAll(EMPTY);
		return super.cleanRouteShortName(routeShortName);
	}

	@Override
	public boolean defaultRouteLongNameEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String cleanRouteLongName(@NotNull String routeLongName) {
		routeLongName = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, routeLongName, getIgnoredWords());
		return super.cleanRouteLongName(routeLongName);
	}

	@Override
	public boolean defaultAgencyColorEnabled() {
		return true;
	}

	private static final String AGENCY_COLOR = "F4CB0B"; // YELLOW (flag)

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
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

	private static final Pattern FIX_BURLINGTON_ = CleanUtils.cleanWords("burlinton");
	private static final String FIX_BURLINGTON_REPLACEMENT = CleanUtils.cleanWordsReplacement("Burlington");

	private static final Pattern HAMILTON_AIRPORT = CleanUtils.cleanWords("hamilton airport");
	private static final String HAMILTON_AIRPORT_REPLACEMENT = CleanUtils.cleanWordsReplacement("Airport");

	private static final Pattern HAMILTON_WATERFRONT = CleanUtils.cleanWords("hamilton waterfront");
	private static final String HAMILTON_WATERFRONT_REPLACEMENT = CleanUtils.cleanWordsReplacement("Waterfront");

	private static final String POWER_CENTRE_SHORT = "PC"; // Power Center
	private static final Pattern POWER_CENTRE = CleanUtils.cleanWords("power centre", "power center");
	private static final String POWER_CENTRE_REPLACEMENT = CleanUtils.cleanWordsReplacement(POWER_CENTRE_SHORT);

	private static final Pattern FIX_HERITAGE_ = CleanUtils.cleanWords("Heratige");
	private static final String FIX_HERITAGE_REPLACEMENT = CleanUtils.cleanWordsReplacement("Heritage");

	private static final Pattern STARTS_WITH_ALDER_SHOT_GO_DASH_ = Pattern.compile("(^(aldershot go - ))", Pattern.CASE_INSENSITIVE); // route 18

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, tripHeadsign, getIgnoredWords());
		tripHeadsign = CleanUtils.keepToAndRemoveVia(tripHeadsign);
		tripHeadsign = FIX_BURLINGTON_.matcher(tripHeadsign).replaceAll(FIX_BURLINGTON_REPLACEMENT);
		tripHeadsign = HAMILTON_AIRPORT.matcher(tripHeadsign).replaceAll(HAMILTON_AIRPORT_REPLACEMENT);
		tripHeadsign = HAMILTON_WATERFRONT.matcher(tripHeadsign).replaceAll(HAMILTON_WATERFRONT_REPLACEMENT);
		tripHeadsign = POWER_CENTRE.matcher(tripHeadsign).replaceAll(POWER_CENTRE_REPLACEMENT);
		tripHeadsign = FIX_HERITAGE_.matcher(tripHeadsign).replaceAll(FIX_HERITAGE_REPLACEMENT);
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
		gStopName = CleanUtils.CLEAN_AND.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AND_REPLACEMENT);
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
			if (CharUtils.isDigitsOnly(stopId)) {
				return Integer.parseInt(stopId);
			}
			stopId = CleanUtils.cleanMergedID(stopId);
			if (CharUtils.isDigitsOnly(stopId)) {
				return Integer.parseInt(stopId);
			}
		}
		throw new MTLog.Fatal("Unexpected stop ID for %s!", gStop);
	}
}
