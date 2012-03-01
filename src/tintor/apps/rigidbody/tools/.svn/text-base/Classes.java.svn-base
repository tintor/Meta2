package tintor.apps.rigidbody.tools;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Classes {
	public static List<Class<?>> getClassesInPackage(final String packageName) throws ClassNotFoundException {
		final List<Class<?>> classes = new ArrayList<Class<?>>();

		try {
			final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			if (classLoader == null) throw new ClassNotFoundException("Can't get class loader");

			// Ask for all resources for the path
			final Enumeration<URL> resources = classLoader.getResources(packageName.replace('.', '/'));
			while (resources.hasMoreElements()) {
				final URL res = resources.nextElement();
				if (res.getProtocol().equalsIgnoreCase("jar")) {
					final JarFile jar = ((JarURLConnection) res.openConnection()).getJarFile();
					for (final JarEntry e : Collections.list(jar.entries())) {
						final String fileName = e.getName();
						if (fileName.startsWith(packageName.replace('.', '/'))
								&& fileName.toLowerCase().endsWith(".class") && !fileName.contains("$")) {
							final String name = fileName.replace('/', '.').substring(0,
									fileName.length() - 6);
							classes.add(Class.forName(name));
						}
					}
				} else {
					final File directory = new File(URLDecoder.decode(res.getPath(), "UTF-8"));
					if (!directory.exists())
						throw new ClassNotFoundException(packageName + " (" + directory.getPath()
								+ ") does not appear to be a valid package");

					for (final String file : directory.list())
						if (file.toLowerCase().endsWith(".class")) {
							final String name = packageName + '.' + file.substring(0, file.length() - 6);
							classes.add(Class.forName(name));
						}
				}
			}
		} catch (final NullPointerException e) {
			throw new ClassNotFoundException(packageName
					+ " does not appear to be a valid package (Null pointer exception)");
		} catch (final UnsupportedEncodingException e) {
			throw new ClassNotFoundException(packageName
					+ " does not appear to be a valid package (Unsupported encoding)");
		} catch (final IOException e) {
			throw new ClassNotFoundException("IOException was thrown when trying to get all resources for "
					+ packageName);
		}

		return classes;
	}
}
