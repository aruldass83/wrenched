package com.wrenched.core.externalization.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;

import com.caucho.hessian.io.AbstractHessianInput;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.SerializerFactory;

public class HessianInputStream extends InputStream implements ObjectInput {
	private AbstractHessianInput delegate;
	
	public HessianInputStream(AbstractHessianInput os) {
		this.delegate = os;
	}

	public HessianInputStream(InputStream is) {
		this.delegate = new Hessian2Input(is);

		SerializerFactory sf = new SerializerFactory();
		sf.setAllowNonSerializable(true);
		
		this.delegate.setSerializerFactory(sf);
	}

	public void readFully(byte[] b) throws IOException {
		throw new UnsupportedOperationException("can't skip bytes on this stream!");
		/*
		if (b != null) {
			byte[] arr = delegate.readBytes();
			
			if (b.length == arr.length) {
				for (int i = 0; i < b.length; i++) {
					b[i] = arr[i];
				}
			}
		}
		*/
	}

	public void readFully(byte[] b, int off, int len) throws IOException {
		throw new UnsupportedOperationException("can't skip bytes on this stream!");
	}

	public int skipBytes(int n) throws IOException {
		throw new UnsupportedOperationException("can't skip bytes on this stream!");
	}

	public boolean readBoolean() throws IOException {
		return delegate.readBoolean();
	}

	public byte readByte() throws IOException {
		return (byte)this.read();
	}

	public int readUnsignedByte() throws IOException {
		int ch = this.read();
		if (ch < 0) {
		    throw new EOFException();
		}
		return ch;
	}

	public short readShort() throws IOException {
		byte[] ch = delegate.readBytes();
        if ((ch[0] | ch[1]) < 0) {
            throw new EOFException();
        }
		return (short)b2s(ch);
	}

	public int readUnsignedShort() throws IOException {
		byte[] ch = delegate.readBytes();
        if ((ch[0] | ch[1]) < 0) {
            throw new EOFException();
        }
        return b2s(ch);
	}

	public char readChar() throws IOException {
		return (char)b2s(delegate.readBytes());
	}

	public int readInt() throws IOException {
		return delegate.readInt();
	}

	public long readLong() throws IOException {
		return delegate.readLong();
	}

	public float readFloat() throws IOException {
		return Float.intBitsToFloat(delegate.readInt());
	}

	public double readDouble() throws IOException {
		return delegate.readDouble();
	}

	@Deprecated
	public final String readLine() throws IOException {
		throw new UnsupportedOperationException();
	}

	public String readUTF() throws IOException {
		return delegate.readString();
	}

	public Object readObject() throws ClassNotFoundException, IOException {
		return delegate.readObject();
	}

	@Override
	public int read() throws IOException {
		return delegate.readBytes()[0];
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}
	
	private int b2s(byte[] b) {
        return ((b[0] << 8) + (b[1] << 0));
	}
}
