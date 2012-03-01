package tintor.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

// Class for sending/receiving Java objects over UDP protocol
public class Host {
	protected final DatagramChannel channel;
	private final InetSocketAddress serverAddress;
	private final ByteBuffer buffer = ByteBuffer.allocateDirect(64 << 10);

	private final ByteBufferInputStream in;
	private final ByteBufferOutputStream out;

	protected Host(final InetSocketAddress serverAddress) {
		this.serverAddress = serverAddress;
		try {
			channel = DatagramChannel.open();
			channel.socket().setReceiveBufferSize(buffer.capacity());
			channel.socket().setSendBufferSize(buffer.capacity());
			in = new ByteBufferInputStream(buffer);
			out = new ByteBufferOutputStream(buffer);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static Host bind(final int port) {
		final Host host = new Host(null);
		try {
			host.channel.socket().bind(new InetSocketAddress(port));
			host.channel.configureBlocking(false);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		return host;
	}

	public static Host connect(final String address, final int port, final boolean blocking) {
		final Host host = new Host(new InetSocketAddress(address, port));
		try {
			host.channel.socket().connect(host.serverAddress);
			host.channel.configureBlocking(blocking);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		return host;
	}

	public Object receiveObject() {
		while (true) {
			final InetSocketAddress address = receive();
			if (address == null) return null;
			if (!address.equals(serverAddress)) continue;
			return read();
		}
	}

	public void sendObject(final Object object) {
		write(object);
		send(serverAddress);
	}

	public InetSocketAddress receive() {
		buffer.clear();
		try {
			return (InetSocketAddress) channel.receive(buffer);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Object read() {
		buffer.flip();
		try {
			final ObjectInputStream stream = new ObjectInputStream(in);
			try {
				return stream.readObject();
			} finally {
				stream.close();
			}
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void write(final Object object) {
		buffer.clear();
		try {
			final ObjectOutputStream stream = new ObjectOutputStream(out);
			try {
				stream.writeObject(object);
			} finally {
				stream.close();
			}
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
		buffer.flip();
	}

	public void send(final InetSocketAddress address) {
		try {
			buffer.mark();
			assert buffer.remaining() > 0;
			channel.send(buffer, address);
			buffer.reset();
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
}
