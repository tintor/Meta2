package tintor.apps.weather;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TAF {
	static interface Element {

	}

	static class Condition implements Element {
		static final Pattern pattern = Pattern.compile("\\A(\\+|\\-)?([A-Z][A-Z])+\\z");

		enum Symbol {
			mist("BR"), thunderstorm("TS"), showers("SH"), freezing("FZ"), rain("RA"), snow("SN"), shallow("MI"), haze(
					"HZ"), fog("FG"), drizzle("DZ"), smoke("FU"), drifting("DR"), blowing("BL"), small_hail(
					"GS"), hail("GR"), vicinity("VC"), duststorm("DS"), widespread_dust("DU"), ice_pellets(
					"PL"), dust_sand_whirls("PO"), spray("PY"), sand("SA"), snow_grains("SG"), snow_pellets(
					"SP"), sandstorm("SS"), snow_shower("SW"), unknown_percipitation("UP"), volcanic_ash("VA");

			final String code;

			Symbol(final String code) {
				this.code = code;
			}
		}

		public final int strength;
		public final Symbol[] symbol;

		public Condition(final Matcher m) {
			strength = "+".equals(m.group(1)) ? 1 : "-".equals(m.group(1)) ? -1 : 0;

			final String code = m.group(2);
			symbol = new Symbol[code.length() / 2];
			for (int i = 0; i < symbol.length; i++) {
				symbol[i] = decode(code.substring(i * 2, i * 2 + 2));
			}
		}

		private static Symbol decode(final String code) {
			for (final Symbol s : Symbol.values()) {
				if (s.code.equals(code))
					return s;
			}
			return null;
		}

		@Override
		public String toString() {
			final StringBuilder b = new StringBuilder();
			if (strength < 0) {
				b.append("light ");
			} else if (strength > 0) {
				b.append("heavy ");
			}
			for (final Symbol element : symbol) {
				b.append(element);
				b.append(' ');
			}
			return b.toString().trim();
		}
	}

	static class Wind implements Element {
		static final Pattern pattern = Pattern.compile("\\A((VRB)|\\d\\d\\d)(\\d\\d)(G(\\d\\d))?(KT|MPS)\\z");

		public final boolean variable;
		public final int direction;
		public final int speed;
		public final int gusting;
		public final boolean knots;
		public final boolean mps;

		public Wind(final Matcher m) {
			variable = m.group(2) != null;
			direction = variable ? -1 : intGroup(m, 1);
			speed = intGroup(m, 3);
			gusting = m.group(5) != null ? intGroup(m, 5) : -1;
			knots = "KT".equals(m.group(6));
			mps = "MPS".equals(m.group(6));
		}

		@Override
		public String toString() {
			if (!variable && direction == 0 && speed == 0 && gusting == -1)
				return "no wind";

			final StringBuilder b = new StringBuilder();

			b.append("wind ");
			if (variable) {
				b.append("variable");
			} else {
				b.append(String.format("%s (%s degrees)", encode(direction), direction));
			}

			if (knots) {
				b.append(String.format(" %.1f m/s (%d knots)", speed * 0.514, speed));
				if (gusting != -1) {
					b.append(String.format(" gusting %.1f m/s (%d knots)", gusting * 0.514, gusting));
				}
			}
			if (mps) {
				b.append(String.format(" %d m/s (%.0f knots)", speed, speed / 0.514));
				if (gusting != -1) {
					b.append(String.format(" gusting %d m/s (%.0f knots)", gusting, gusting / 0.514));
				}
			}

			return b.toString().trim();
		}

		static String encode(final int dir) {
			if (22 < dir && dir <= 67)
				return "NE";
			if (67 < dir && dir <= 112)
				return "E";
			if (112 < dir && dir <= 157)
				return "SE";
			if (157 < dir && dir <= 202)
				return "S";
			if (202 < dir && dir <= 247)
				return "SW";
			if (247 < dir && dir <= 292)
				return "W";
			if (292 < dir && dir <= 337)
				return "NW";
			return "N";
		}
	}

	static class Temperature implements Element {
		static final Pattern pattern = Pattern.compile("\\AT(X|N)(M)?(\\d\\d)/(\\d\\d)(\\d\\d)Z\\z");

		public final boolean max;
		public final int degrees;
		public final Date date;

		public Temperature(final int year, final int month, final Matcher m) {
			max = "X".equals(m.group(1));
			degrees = ("M".equals(m.group(2)) ? -1 : 1) * intGroup(m, 3);
			date = new Date(year, month, intGroup(m, 4), intGroup(m, 5), 0); // TODO TimeZone!
		}

		@Override
		public String toString() {
			return String.format("%s temp %s degrees at %s TODO TimeZone", max ? "max" : "min", degrees, date);
		}
	}

	static class Clouds implements Element {
		static final Pattern pattern = Pattern.compile("\\A(FEW|SCT|BKN|OVC)((\\d\\d\\d)|///)(CB|TCU)?\\z");

		enum Type {
			few, scatered, broken, overcast
		}

		public final Type type;
		public final int altitudeFt;
		public final boolean cumulonimbus;
		public final boolean towering_cumulus;

		public Clouds(final Matcher m) {
			type = "FEW".equals(m.group(1)) ? Type.few : "SCT".equals(m.group(1)) ? Type.scatered : "BKN"
					.equals(m.group(1)) ? Type.broken : Type.overcast;
			altitudeFt = m.group(3) != null ? intGroup(m, 3) * 100 : -1;
			cumulonimbus = m.group(4) != null && "CB".equals(m.group(4));
			towering_cumulus = m.group(4) != null && "TCU".equals(m.group(4));
		}

		@Override
		public String toString() {
			final StringBuilder b = new StringBuilder();
			b.append(String.format("%s clouds", type));
			b.append(String.format(" altitude %.0f m (%s feet)", altitudeFt / 0.3048, altitudeFt));
			if (cumulonimbus) {
				b.append(" cumulonimbus");
			}
			if (towering_cumulus) {
				b.append(" towering cumulus");
			}
			return b.toString();
		}
	}

	static class Interval implements Element {
		static final Pattern pattern = Pattern.compile("\\A(\\d\\d)(\\d\\d)((/|//)(\\d\\d))?(\\d\\d)\\z");

		public final Date start, end;

		public Interval(final int year, final int month, final Matcher m) {
			final int sday = intGroup(m, 1);
			final int shour = intGroup(m, 2);
			final int eday = m.group(1) != null ? intGroup(m, 5) : sday;
			final int ehour = intGroup(m, 6);
			// TODO cound be on year or month boundary 
			start = new Date(year, month, sday, shour, 0);
			end = new Date(year, month, eday, ehour, 0);
		}

		@Override
		public String toString() {
			return String.format("from %s to %s (TODO timezone!)", start, end);
		}
	}

	static class Event {
		enum Type {
			Initial, Becomming, Temporary
		}

		public Type type;
		public Date start, end;
		public String wind;
		public boolean cavok;
		public Temperature maxTemp;
		public Temperature minTemp;
		public int visibility;
		public List<Condition> conditions;
		public List<Clouds> clouds;
		public List<String> unknown;

		public List<Element> elements;
	}

	public String station;
	public boolean amendment;
	public Date date;
	public List<Event> events;

	private static int intGroup(final Matcher m, final int g) {
		return Integer.parseInt(m.group(g));
	}

	// <taf> := TAF <station_id> <time_of_report> <event> { (TEMPO | BECMG) <event> }
	public static void parse(final int year, final int month, final String raw) throws Exception {
		final String[] tokens = raw.split("\\s+");
		int pos = 0;

		while (pos < tokens.length && tokens[pos].matches("\\ATAF\\w*|AMD|COR\\z")) {
			if (tokens[pos].equals("AMD")) {
				//System.out.printf("Amendment\n");
			}

			if (tokens[pos].equals("COR")) {
				//System.out.printf("Correction\n");
			}

			pos++;
		}

		if (!tokens[pos].matches("(\\w\\w\\w\\w?)|(MF.760)"))
			throw new Exception("Invalid station '" + tokens[pos] + "'");
		//System.out.printf("Station '%s'\n", tokens[pos]);
		pos++;

		if (pos < tokens.length && tokens[pos].equals("WIII")) {
			pos++;
		}

		if (pos < tokens.length) {
			final Matcher m = reportedOn.matcher(tokens[pos]);
			if (m.matches()) {
				final Date date = new Date(year, month, intGroup(m, 1), intGroup(m, 2), intGroup(m, 3));
				//System.out.printf("Report date %s (TODO timezone!)\n", date);
				pos++;
			}
		}

		// Events
		final List<String> eventTokens = new ArrayList<String>();
		Event.Type eventType = Event.Type.Initial;
		for (; pos < tokens.length; pos++) {
			final String token = tokens[pos];

			if (token.equals("RMK") || token.startsWith("RMK/")) {
				break;
			}

			// TODO FM171200 (nesto kao promena vremena)
			if (!token.startsWith("BECMG") && !token.startsWith("TEMPO")) {
				if (token.matches("\\A(\\d?\\d)/(\\d?\\d)SM\\z") && eventTokens.size() > 0
						&& eventTokens.get(eventTokens.size() - 1).matches("\\A\\d\\z")) {
					eventTokens.set(eventTokens.size() - 1, eventTokens.get(eventTokens.size() - 1) + " "
							+ token);
				} else {
					eventTokens.add(token);
				}
				continue;
			}

			parseEvent(year, month, eventType, eventTokens);
			eventTokens.clear();

			if (token.equals("BECMG")) {
				eventType = Event.Type.Becomming;
			} else if (token.equals("TEMPO")) {
				eventType = Event.Type.Temporary;
			} else if (token.startsWith("BECMG")) {
				eventType = Event.Type.Becomming;
				eventTokens.add(token.substring(5));
			} else if (token.startsWith("TEMPO")) {
				eventType = Event.Type.Temporary;
				eventTokens.add(token.substring(5));
			}
		}
		parseEvent(year, month, eventType, eventTokens);
	}

	private static final Pattern reportedOn = Pattern.compile("\\A(\\d\\d)(\\d\\d)(\\d\\d)Z\\z");

	// <event> := <validity> <element>+
	// <element> := <wind> | CAVOK | <max_temp> | <min_temp> | <visibility> | <condition> | <clouds>
	// <wind> := (VRB | <direction d3>) <speed in knots d2> [G <speed in knots d2] KT
	static Event parseEvent(final int year, final int month, final Event.Type type, final List<String> tokens) {
		final Event event = new Event();
		//System.out.printf("%s\n", type);

		for (String token : tokens) {
			if (token.endsWith("=")) {
				token = token.substring(0, token.length() - 1);
			}

			final Matcher intervalm = Interval.pattern.matcher(token);
			if (intervalm.matches()) {
				//System.out.printf("\t%s\n", new Interval(year, month, intervalm));
				continue;
			}

			// METAR only
			if (token.equals("NOSIG")) {
				//System.out.println("\tno significant change in next 2 hours");
				continue;
			}

			if (token.matches("\\A/+\\z") || token.matches("\\AR\\d\\d/")) {
				continue;
			}

			final Matcher windm = Wind.pattern.matcher(token);
			if (windm.matches()) {
				//System.out.printf("\t%s\n", new Wind(windm));
				continue;
			}

			final Matcher probabilitym = Pattern.compile("\\APROB(\\d\\d)\\z").matcher(token);
			if (probabilitym.matches()) {
				//System.out.printf("\tprobability %d%%\n", intGroup(probabilitym, 1));
				continue;
			}

			final Matcher varyingWindM = Pattern.compile("\\A(\\d\\d\\d)V(\\d\\d\\d)\\z").matcher(token);
			if (varyingWindM.matches()) {
				//System.out.printf("\tvarying wind direction from %d to %d\n", intGroup(varyingWindM, 1),
				//		intGroup(varyingWindM, 2));
				continue;
			}

			// METAR only
			final Matcher pressurem = Pattern.compile("\\A(A|Q)(\\d\\d\\d\\d)\\z").matcher(token);
			if (pressurem.matches()) {
				//System.out.printf("\tpressure %d %s\n", intGroup(pressurem, 2), "A".equals(pressurem.group(1)) ? "inches Hg" : "hPa");
				continue;
			}

			if (token.equals("CAVOK")) {
				event.cavok = true;
				//System.out.println("\tclouds and visibility OK");
				continue;
			}
			if (token.equals("CLR")) {
				//System.out.println("\tclear");
				continue;
			}

			if (token.matches("\\A\\d\\d\\d\\d\\z")) {
				//System.out.printf("\tvisibility %s meters\n", Integer.parseInt(token));
				continue;
			}
			if (token.matches("\\A(\\d\\d\\d\\d)FT\\z")) {
				//System.out.printf("\tvisibility %s feet\n", intGroup(m, 1));
				continue;
			}
			final Matcher visibilitySMm = Pattern.compile("\\A((\\d) )?(\\d?\\d)(/(\\d?\\d))?SM\\z").matcher(
					token);
			if (visibilitySMm.matches()) {
				final int visibilityM = (visibilitySMm.group(2) != null ? intGroup(visibilitySMm, 2) : 0)
						* 1600 + intGroup(visibilitySMm, 3) * 1600
						/ (visibilitySMm.group(5) != null ? intGroup(visibilitySMm, 5) : 1);
				//System.out.printf("\tvisibility %d meters\n", visibilityM);
				continue;
			}

			final Matcher vvm = Pattern.compile("\\AVV((///)|\\d\\d\\d)\\z").matcher(token);
			if (vvm.matches()) {
				if (vvm.group(2) != null) {
					//System.out.printf("\tvertical visibility ///\n");
				} else {
					//System.out.printf("\tvertical visibility %d something!\n", intGroup(vvm, 1));
				}
				continue;
			}

			final Matcher cloudsm = Clouds.pattern.matcher(token);
			if (cloudsm.matches()) {
				//System.out.printf("\t%s\n", new Clouds(cloudsm));
				continue;
			}

			// METAR only
			final Matcher temperature2m = Pattern.compile("\\A(M)?(\\d\\d)/((M)?(\\d?\\d)|XX)?\\z")
					.matcher(token);
			if (temperature2m.matches()) {
				final int temp = (temperature2m.group(1) != null ? -1 : 1) * intGroup(temperature2m, 2);
				final int dewpoint = temperature2m.group(5) != null ? (temperature2m.group(4) != null ? -1 : 1)
						* intGroup(temperature2m, 5) : Integer.MIN_VALUE;
				//System.out.printf("\ttemperature %d deg C, dewpoint %d deg C\n", temp, dewpoint);
				continue;
			}

			final Matcher temperaturem = Temperature.pattern.matcher(token);
			if (temperaturem.matches()) {
				//System.out.printf("\t%s\n", new Temperature(year, month, temperaturem));
				continue;
			}

			// TODO merge with condition
			if (token.equals("NSW")) {
				//System.out.println("\tno significant weather");
				continue;
			}

			final Matcher conditionm = Condition.pattern.matcher(token);
			if (conditionm.matches()) {
				//System.out.printf("\t%s\n", new Condition(conditionm));
				continue;
			}
			System.out.printf("\tunknown %s\n", token);
		}
		return event;
	}
}

class Chopper {
	private final BufferedReader reader;
	private Date date;
	private String chop;
	private final StringBuilder builder = new StringBuilder();

	public Chopper(final Reader _reader) {
		reader = new BufferedReader(_reader);
	}

	public boolean hasMore() throws IOException {
		final Date prevDate = date;
		final String prevChop = chop;

		date = null;
		builder.setLength(0);
		while (true) {
			final String line = reader.readLine();
			if (line == null) {
				if (builder.length() > 0) {
					chop = builder.toString();
					return !date.equals(prevDate) || !chop.equals(prevChop);
				}
				return false;
			}

			final String trimmed = line.trim();
			if (trimmed.length() == 0) {
				if (builder.length() > 0) {
					chop = builder.toString();
					if (!date.equals(prevDate) || !chop.equals(prevChop))
						return true;
					date = null;
					builder.setLength(0);
				}
				continue;
			}

			if (date == null) {
				try {
					date = dateFormat.parse(trimmed);
				} catch (final ParseException e) {
					date = new Date(0);
				}
			} else {
				builder.append(trimmed);
				builder.append('\n');
			}
		}
	}

	private static final DateFormat dateFormat = getDateFormat();

	private static DateFormat getDateFormat() {
		final SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		format.setTimeZone(TimeZone.getTimeZone("GMT+0:00"));
		return format;
	}

	public Date date() {
		return date;
	}

	public String read() {
		return chop;
	}
}

public class Main {
	public static void main(final String[] args) throws IOException, ParseException, Exception {
		final Chopper chopper = new Chopper(new FileReader("src/tintor/apps/weather/METAR_05Z.txt"));
		int i = 0;
		while (chopper.hasMore()) {
			System.out.printf("Entry %s [%s]\n", ++i, chopper.date());
			System.out.print(chopper.read());
			// NOTE may cross year / month boundary
			try {
				TAF.parse(chopper.date().getYear(), chopper.date().getMonth(), chopper.read());
			} catch (final Exception e) {
				System.out.println(e);
				e.printStackTrace();
			}
		}
	}
}