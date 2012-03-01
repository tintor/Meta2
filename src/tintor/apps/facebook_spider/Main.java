package tintor.apps.facebook_spider;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tintor.util.Stream;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

public class Main {
	final static Multimap<String, String> network = Multimaps.newHashMultimap();

	static Set<String> guide = new HashSet<String>();

	final static Pattern m = Pattern.compile("<a.*?>");
	final static Pattern h = Pattern.compile("href=\"(.*?)\"");
	static PrintWriter writer;

	public static void main(final String[] args) throws Exception {
		load("c:/log");
		//guide.add("http://www.facebook.com/people/Ivan-Ljuba/565145338");
		guide.add("http://www.facebook.com/people/Danica-Porobic/1138126743");

		// TODO breadth first search (not DFS) - search around Danica and Ljuba!
		// TODO print number of unconnected components!

		final Random rand = new Random();
		while (true) {
			System.out.printf("nodes %d edges %d guide %d\n", network.keySet().size(), network.size(), guide
					.size());
			final Set<String> set = guide.isEmpty() ? network.keySet() : guide;
			final String next = set.toArray()[rand.nextInt(set.size())].toString();
			guide.remove(next);
			parse(next);
		}
	}

	static void load(final String filename) throws Exception {
		if (!new File(filename).exists())
			return;
		final BufferedReader reader = new BufferedReader(new FileReader(filename));
		try {
			while (true) {
				final String line = reader.readLine();
				if (line == null) {
					break;
				}
				final int tab = line.indexOf('\t');
				final String key = line.substring(0, tab);
				final String value = line.substring(tab + 1);
				network.put(key, value);
			}
		} finally {
			reader.close();
		}
	}

	static void parse(final String key) throws Exception {
		Thread.sleep(900);
		System.out.printf("parsing %s\n", key);
		try {
			final String page = Stream.read(Stream.input(new URL(key)));
			final Matcher matcher = m.matcher(page);
			while (matcher.find()) {
				final String link = matcher.group();
				if (!link.contains("rel=\"friend\"")) {
					continue;
				}

				final Matcher href = h.matcher(link);
				if (href.find()) {
					final String value = href.group(1).intern();
					if (!network.containsEntry(key, value)) {
						writer = new PrintWriter(new BufferedWriter(new FileWriter("c:/log")));
						try {
							writer.printf("%s\t%s\n%s\t%s\n", key, value, value, key);
							writer.flush();
						} finally {
							writer.close();
						}
						network.put(key, value);
						network.put(value, key);
						guide.add(key);
						guide.add(value);
					}
				}
			}
		} catch (final RuntimeException e) {
			System.out.println(e);
		}
	}
}
