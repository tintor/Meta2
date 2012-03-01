package tintor.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class ByteBufferOutputStream extends OutputStream {
	private final ByteBuffer buffer;

	public ByteBufferOutputStream(final ByteBuffer buffer) {
		this.buffer = buffer;
	}

	@Override
	public void write(final int b) throws IOException {
		buffer.put((byte) b);
	}

	@Override
	public void write(final byte[] buf, final int offset, final int length) throws IOException {
		buffer.put(buf, offset, length);
	}
}