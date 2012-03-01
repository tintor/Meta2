package tintor.apps.search_engine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class Scanner {
	private final Reader reader;

	public Scanner(final Reader reader) {
		this.reader = reader;
	}

	private final StringBuilder builder = new StringBuilder();
	public String word;
	public int index = -1;
	public int offset;

	public boolean hasNext() throws IOException {
		while (hasNextChar())
			if (Character.isJavaIdentifierPart(character)) {
				offset = position;
				index += 1;
				do
					builder.append(character);
				while (hasNextChar() && Character.isJavaIdentifierPart(character));
				word = builder.toString();
				builder.delete(0, builder.length());
				return true;
			}
		return false;
	}

	private int position = -1;
	private char character;

	private boolean hasNextChar() throws IOException {
		final int b = reader.read();
		if (b == -1) return false;
		character = Character.toLowerCase((char) b);
		position += 1;
		return true;
	}
}

class WordInfo implements Comparable<WordInfo> {
	final File file;
	final int[] offsets;

	WordInfo(final File file, final int[] offsets) {
		this.file = file;
		this.offsets = offsets;
	}

	@Override
	public int compareTo(final WordInfo w) {
		return file.compareTo(w.file);
	}
}

class Result {
	File file;
	Map<String, int[]> words = new LinkedHashMap<String, int[]>();
}

class Merger {
	private static class IndexIterator {
		final String word;
		final Iterator<WordInfo> iterator;
		WordInfo value;

		IndexIterator(final String word, final Iterator<WordInfo> iterator) {
			this.word = word;
			this.iterator = iterator;
		}
	}

	private final List<IndexIterator> iterators = new ArrayList<IndexIterator>();

	void add(final String word, final List<WordInfo> list) {
		iterators.add(new IndexIterator(word, list.iterator()));
	}

	public Result result;

	boolean hasNext() {
		result = null;
		if (iterators.size() == 0) return false;

		for (final IndexIterator i : iterators)
			if (!i.iterator.hasNext()) return false;
		for (final IndexIterator i : iterators)
			i.value = i.iterator.next();

		IndexIterator a = null;
		for (final IndexIterator b : iterators) {
			if (a != null) if (!hasNext(a, b)) return false;
			a = b;
		}

		result = new Result();
		result.file = iterators.get(0).value.file;
		for (final IndexIterator i : iterators)
			result.words.put(i.word, i.value.offsets.clone());
		return true;
	}

	boolean hasNext(final IndexIterator a, final IndexIterator b) {
		while (true) {
			final int cmp = a.value.compareTo(b.value);
			if (cmp == 0) return true;
			if (cmp < 0) {
				if (!a.iterator.hasNext()) return false;
				a.value = a.iterator.next();
			}
			if (cmp > 0) {
				if (!b.iterator.hasNext()) return false;
				b.value = b.iterator.next();
			}
		}
	}
}

public class SearchEngine {
	final List<File> roots = new ArrayList<File>();

	private final Map<String, List<WordInfo>> index = new HashMap<String, List<WordInfo>>();

	//	private static <A, B> List<Map.Entry<A, B>> sort(final Map<A, B> map,
	//			final Comparator<? super Map.Entry<A, B>> comparator) {
	//		return sort(map.entrySet(), comparator);
	//	}

	//	private static <T> List<T> sort(final Set<T> set, final Comparator<? super T> comparator) {
	//		return sort(new ArrayList<T>(set), comparator);
	//	}

	//	private static <T> List<T> sort(final List<T> list, final Comparator<? super T> comparator) {
	//		Collections.sort(list, comparator);
	//		return list;
	//	}

	void crawl() {
		final long start1 = System.nanoTime();
		for (final File root : roots)
			crawl(root);
		final long time1 = System.nanoTime() - start1;
		System.out.printf("Crawling and scanning %dms\n", (time1 + 500000) / 1000000);
	}

	void index() {
		System.out.printf("Sorting %d indexes\n", index.size());
		final long start2 = System.nanoTime();
		for (final Map.Entry<String, List<WordInfo>> entry : index.entrySet())
			Collections.sort(entry.getValue());
		final long time2 = System.nanoTime() - start2;

		System.out.printf("Sorting %d indexes %dms\n", index.size(), (time2 + 500000) / 1000000);
	}

	private void crawl(final File file) {
		if (file.isDirectory())
			for (final File f : file.listFiles())
				crawl(f);
		else
			try {
				crawl(file, new FileInputStream(file));
			} catch (final FileNotFoundException e) {
				System.err.println(e.toString());
			}
	}

	static class XInputStream extends InputStream {
		private final InputStream is;
		private ByteArrayOutputStream baos = new ByteArrayOutputStream();
		private ByteArrayInputStream bais;

		XInputStream(final InputStream is) {
			this.is = is;
		}

		void revert() {
			bais = new ByteArrayInputStream(baos.toByteArray());
			baos = null;
		}

		@Override
		public int read() throws IOException {
			if (bais == null) {
				final int b = is.read();
				if (b != -1) baos.write(b);
				return b;
			}
			final int b = bais.read();
			return b == -1 ? is.read() : b;
		}
	}

	private void crawl(final File file, final InputStream istream) {
		final XInputStream xistream = new XInputStream(istream);
		try {
			boolean isComposite = false;
			final ZipInputStream zis = new ZipInputStream(xistream);
			while (true) {
				final ZipEntry e = zis.getNextEntry();
				if (e == null) break;

				isComposite = true;
				if (!e.isDirectory()) crawl(new File(file, e.getName()), zis);
			}

			if (!isComposite) {
				xistream.revert();
				scan(file, xistream);
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void scan(final File file, final InputStream istream) {
		try {
			System.out.printf("Scanning '%s'\n", file);

			final Scanner scanner = new Scanner(new InputStreamReader(istream));
			final Map<String, List<Integer>> map = new HashMap<String, List<Integer>>();
			while (scanner.hasNext()) {
				if (!map.containsKey(scanner.word)) map.put(scanner.word, new ArrayList<Integer>());
				map.get(scanner.word).add(scanner.offset);
			}

			// add map to index
			for (final Map.Entry<String, List<Integer>> entry : map.entrySet()) {
				if (!index.containsKey(entry.getKey())) index.put(entry.getKey(), new ArrayList<WordInfo>());
				index.get(entry.getKey()).add(new WordInfo(file, toIntArray(entry.getValue())));
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static int[] toIntArray(final List<Integer> list) {
		final int[] array = new int[list.size()];
		int i = 0;
		for (final Integer v : list)
			array[i++] = v;
		return array;
	}

	List<Result> search(final String query, final int maxResults) {
		final long start = System.nanoTime();
		final Merger merger = new Merger();
		for (final String word : scanQuery(query))
			merger.add(word, index.get(word));

		final List<Result> results = new ArrayList<Result>(maxResults);
		while (results.size() < maxResults && merger.hasNext())
			results.add(merger.result);
		final long end = System.nanoTime() - start;
		System.out.printf("Searching %dms\n", (end + 500000) / 1000000);
		return results;
	}

	private static String[] scanQuery(final String query) {
		final Scanner scanner = new Scanner(new StringReader(query));
		final Set<String> words = new LinkedHashSet<String>();
		try {
			while (scanner.hasNext())
				words.add(scanner.word);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		System.out.printf("Query '%s': %s\n", query, words);
		return words.toArray(new String[words.size()]);
	}

	public static void main(final String[] args) {
		final SearchEngine engine = new SearchEngine();
		engine.roots.add(new File("d:/shakespeare"));
		engine.crawl();
		engine.index();
		for (final Result result : engine.search("bertram virtue name", 10)) {
			System.out.printf("%s\n", result.file);
			for (final Map.Entry<String, int[]> entry : result.words.entrySet())
				System.out.printf("\t%s [%s]\n", entry.getKey(), arrayToString(entry.getValue(), ", "));
		}
	}

	private static String arrayToString(final int[] array, final String separator) {
		final StringBuilder builder = new StringBuilder();
		for (final int a : array) {
			builder.append(a);
			builder.append(separator);
		}
		return builder.substring(0, builder.length() - separator.length());
	}
}