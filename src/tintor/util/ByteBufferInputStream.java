package tintor.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ByteBufferInputStream extends InputStream {
	private final ByteBuffer buffer;

	public ByteBufferInputStream(final ByteBuffer buffer) {
		this.buffer = buffer;
	}

	@Override
	public int read() throws IOException {
		if (buffer.remaining() == 0) return -1;
		final int b = buffer.get();
		return b >= 0 ? b : b + 128;
	}

	@Override
	public int available() throws IOException {
		return buffer.remaining();
	}

	@Override
	public int read(final byte[] buf, final int offset, final int length) throws IOException {
		final int bytes = Math.min(length, buffer.remaining());
		buffer.get(buf, offset, bytes);
		return bytes;
	}
}