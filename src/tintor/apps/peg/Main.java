package tintor.apps.peg;

import java.util.HashMap;
import java.util.Map;

// date := "(" date ")" / number {"-" number}
// number := [0-9]+
public class Main {
	public static void main(final String[] args) {
		final Grammar g = new Grammar();
		g.put("date", "(", g.get("date"), ")");
		g.put("date", g.get("number"), Matcher.any("-", g.get("number")));
		g.put("number", Matcher.range('0', '9').repeat());
		System.out.println(g.get("date").search("danas je (((1984-9-30)).", 0));
	}
}

class Grammar {
	private final Map<String, Matcher> matchers = new HashMap<String, Matcher>();
	private final Map<String, Matcher> references = new HashMap<String, Matcher>();

	void put(final String name, final Object... m) {
		final Matcher a = Matcher.sequence(m);
		matchers.put(name, matchers.containsKey(name) ? matchers.get(name).or(a) : a);
	}

	Matcher get(final String name) {
		if (!references.containsKey(name)) references.put(name, new Matcher() {
			@Override
			public int match(final Object text, final int position, final boolean forward) {
				final Matcher matcher = matchers.get(name);
				if (matcher == null) throw new RuntimeException("unknown rule: " + name);
				return matcher.match(text, position, forward);
			}
		});
		return references.get(name);
	}
}

class Match {
	final String text;
	final int start, count;

	Match(final String text, final int start, final int count) {
		if (start < 0 || count < 0 || start + count > text.length()) throw new IllegalArgumentException();
		this.text = text;
		this.start = start;
		this.count = count;
	}

	@Override
	public String toString() {
		return String.format("start=%s, count=%s, match='%s', prematch='%s', postmatch='%s'", start, count,
				text.substring(start, start + count), text.substring(0, start), text.substring(start
						+ count));
	}
}

abstract class Matcher {
	protected final static int Fail = Integer.MIN_VALUE;

	public Match search(final String text, final int start) {
		for (int position = start; position <= text.length(); position++) {
			final int p = match(text, position, true);
			if (p != Fail) return new Match(text, position, p - position);
		}
		return null;
	}

	abstract public int match(Object text, int position, boolean forward);

	// Singleton matchers

	public static final Matcher Start = new Matcher() {
		@Override
		public int match(@SuppressWarnings("unused") final Object text, final int position,
				final boolean forward) {
			assert forward;
			return position == 0 ? position : Fail;
		}
	};

	public static final Matcher End = new Matcher() {
		@Override
		public int match(final Object text, final int position, final boolean forward) {
			assert forward;
			final int length = ((String) text).length();
			return position == length ? position : Fail;
		}
	};

	// Base matchers

	public static Matcher range(final char low, final char high) {
		return new Matcher() {
			@Override
			public int match(final Object text, final int position, final boolean forward) {
				return forward ? match((String) text, position, position + 1) : match((String) text,
						position - 1, position - 1);
			}

			private int match(final String text, final int index, final int result) {
				return index >= 0 && index < text.length() && low <= text.charAt(index)
						&& text.charAt(index) <= high ? result : Fail;
			}
		};
	}

	public static Matcher string(final String str) {
		return new Matcher() {
			@Override
			public int match(final Object text, final int position, final boolean forward) {
				return forward ? match((String) text, position, position + str.length()) : match(
						(String) text, position - str.length(), position - str.length());
			}

			private int match(final String text, final int start, final int result) {
				return start >= 0 && start + str.length() <= text.length()
						&& text.substring(start).startsWith(str) ? result : Fail;
			}
		};
	}

	// Unary matchers

	public Matcher optional() {
		return new Matcher() {
			@Override
			public int match(final Object text, final int position, final boolean forward) {
				final int p = Matcher.this.match(text, position, forward);
				return p == Fail ? position : p;
			}
		};
	}

	public Matcher any() {
		return new Matcher() {
			@Override
			public int match(final Object text, final int position, final boolean forward) {
				int p = position;
				while (true) {
					final int a = Matcher.this.match(text, p, forward);
					if (a == Fail) break;
					p = a;
				}
				return p;
			}
		};
	}

	public Matcher repeat() {
		return new Matcher() {
			@Override
			public int match(final Object text, final int position, final boolean forward) {
				int p = Matcher.this.match(text, position, forward);
				if (p == Fail) return Fail;
				while (true) {
					final int a = Matcher.this.match(text, p, forward);
					if (a == Fail) break;
					p = a;
				}
				return p;
			}
		};
	}

	public Matcher and() {
		return new Matcher() {
			@Override
			public int match(final Object text, final int position, final boolean forward) {
				return Matcher.this.match(text, position, forward) != Fail ? position : Fail;
			}
		};
	}

	public Matcher not() {
		return new Matcher() {
			@Override
			public int match(final Object text, final int position, final boolean forward) {
				return Matcher.this.match(text, position, forward) != Fail ? Fail : position;
			}
		};
	}

	public Matcher reverse() {
		return new Matcher() {
			@Override
			public int match(final Object text, final int position, final boolean forward) {
				return Matcher.this.match(text, position, !forward);
			}
		};
	}

	// Binary matchers

	public Matcher then(final Matcher second) {
		return new Matcher() {
			@Override
			public int match(final Object text, final int position, final boolean forward) {
				final int p = Matcher.this.match(text, position, forward);
				return p == Fail ? Fail : second.match(text, p, forward);
			}
		};
	}

	public Matcher or(final Matcher second) {
		return new Matcher() {
			@Override
			public int match(final Object text, final int position, final boolean forward) {
				final int p = Matcher.this.match(text, position, forward);
				return p != Fail ? p : second.match(text, position, forward);
			}
		};
	}

	// Multi matchers

	public static Matcher optional(final Object... matchers) {
		return sequence(matchers).optional();
	}

	public static Matcher any(final Object... matchers) {
		return sequence(matchers).any();
	}

	public static Matcher repeat(final Object... matchers) {
		return sequence(matchers).repeat();
	}

	public static Matcher and(final Object... matchers) {
		return sequence(matchers).and();
	}

	public static Matcher not(final Object... matchers) {
		return sequence(matchers).not();
	}

	public static Matcher sequence(final Object... matchers) {
		if (matchers.length == 0) throw new IllegalArgumentException();
		return sequence(matchers, 0);
	}

	public static Matcher choice(final Object... matchers) {
		if (matchers.length == 0) throw new IllegalArgumentException();
		return choice(matchers, 0);
	}

	private static Matcher sequence(final Object[] matchers, final int start) {
		assert start < matchers.length;
		if (start + 1 == matchers.length) return valueOf(matchers[start]);
		return valueOf(matchers[start]).then(sequence(matchers, start + 1));
	}

	private static Matcher choice(final Object[] matchers, final int start) {
		assert start < matchers.length;
		if (start + 1 == matchers.length) return valueOf(matchers[start]);
		return valueOf(matchers[start]).or(choice(matchers, start + 1));
	}

	private static Matcher valueOf(final Object m) {
		return m instanceof String ? string(m.toString()) : (Matcher) m;
	}
}