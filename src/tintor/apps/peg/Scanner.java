package tintor.apps.peg;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

class Scanner {
	public static void main(final String[] args) throws IOException {
		final String file = "src/" + Scanner.class.getPackage().getName().replace(".", "/") + "/sample.txt";
		final Scanner scanner = new Scanner(new FileInputStream(file));
		scanner.scan();
	}

	static enum Type {
		Begin, End, Separator, Name, Integer, Space, Operator, String, Char
	}

	private final byte[] buffer = new byte[1 << 16];
	private int address; // position of buffer in source file
	private int cursor; // index of next character to read from buffer 
	private int line_offset; // absolute position of start of the line
	private int buffer_size; // number of bytes stored in buffer
	private final static byte EOF = 0;
	private final InputStream is;

	public Scanner(final InputStream is) {
		this.is = is;
	}

	private byte read() throws IOException {
		if (cursor == buffer_size) {
			address += buffer_size;
			buffer_size = is.read(buffer, 0, buffer.length);
			if (buffer_size <= 0) return EOF;
			cursor = 0;
		}
		return buffer[cursor++];
	}

	private int line = 0;

	public void scan() throws IOException {
		int depth = 0;
		byte c = read();

		loop: while (true) {
			line_offset = address + cursor;
			line += 1;

			// calculate indent
			int indent = 0;
			while (c == '\t') {
				indent += 1;
				c = read();
			}

			// update depth
			while (depth < indent) {
				token(Type.Begin);
				depth += 1;
			}
			while (depth > indent) {
				token(Type.End);
				depth -= 1;
			}

			// comment line
			if (c == '#') {
				c = read();
				while (true)
					if (c == '\n') {
						token(Type.Separator);
						c = read();
						continue loop;
					}
					else if (c == EOF)
						break loop;
					else
						c = read();
			}

			while (true)
				if (isLetter(c) || c == '_') {
					do {
						append(c);
						c = read();
					} while (isLetter(c) || c == '_' || isDigit(c));
					token(Type.Name);
				}
				else if (isDigit(c)) {
					do {
						append(c);
						c = read();
					} while (isDigit(c));
					token(Type.Integer);
				}
				else
					switch (c) {
					case EOF:
						break loop;

					case '\n':
						token(Type.Separator);
						c = read();
						continue loop;

					case ' ':
						token(Type.Space);
						c = read();
						break;

					case '+':
					case '-':
					case '.':
					case '=':
					case '(':
					case ')':
					case '[':
					case ']':
					case '*':
					case '/':
					case '%':
					case ',':
					case '{':
					case '}':
						append(c);
						token(Type.Operator);
						c = read();
						break;

					case '"':
						c = read();
						while (c != '"') {
							if (c == '\n' || c == EOF)
								error("expected '\"' before end of line");
							append(c);
							c = read();
						}
						token(Type.String);
						c = read();
						break;

					case '\'':
						c = read();
						while (c != '\'') {
							if (c == '\n' || c == EOF)
								error("expected ' before end of line");
							append(c);
							c = read();
						}
						token(Type.Char);
						c = read();
						break;

					case '\t':
						do
							c = read();
						while (c == '\t');

						if (c != '#') error("expected comment after tab");

						while (true) {
							c = read();
							if (c == '\n') {
								token(Type.Separator);
								c = read();
								continue loop;
							}
							if (c == EOF) break loop;
						}

					default:
						error("unexpected char '" + (char) c + "'");
					}
		}

		while (depth > 0) {
			token(Type.End);
			depth -= 1;
		}
	}

	private void error(final String msg) {
		throw new RuntimeException("line:" + line + " " + msg);
	}

	private void token(final Type type) {
		System.out.printf("%s:%s %s %s\n", line, address + cursor - line_offset - data_size, type, new String(
				data, 0, data_size));
		data_size = 0;
	}

	private static boolean isLetter(final int c) {
		if (c <= 'Z') return 'A' <= c;
		return 'a' <= c && c <= 'z';
	}

	private static boolean isDigit(final int c) {
		return '0' <= c && c <= '9';
	}

	private byte[] data = new byte[128];
	private int data_size = 0;

	private void append(final byte c) {
		try {
			data[data_size] = c;
		}
		catch (final ArrayIndexOutOfBoundsException e) {
			data = Arrays.copyOf(data, data.length * 2);
			data[data_size] = c;
		}
		data_size++;
	}
}