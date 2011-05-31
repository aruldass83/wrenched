package com.wrenched.core.externalization.io;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.OutputStream;

import com.caucho.hessian.io.AbstractHessianOutput;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;

/**
 * convenience Hessian adapter to java serialization API
 * @author konkere
 *
 */
public class HessianOutputStream extends OutputStream implements ObjectOutput {
	private AbstractHessianOutput delegate;
	
	public HessianOutputStream(AbstractHessianOutput os) {
		this.delegate = os;
	}

	public HessianOutputStream(OutputStream os) {
		this.delegate = new Hessian2Output(os);

		SerializerFactory sf = new SerializerFactory();
		sf.setAllowNonSerializable(true);
		
		this.delegate.setSerializerFactory(sf);
	}
	
	public void writeBoolean(boolean v) throws IOException {
		delegate.writeBoolean(v);
	}

	public void writeByte(int v) throws IOException {
		this.write(v);
	}

	public void writeShort(int v) throws IOException {
		delegate.writeBytes(s2b(v));
	}

	public void writeChar(int v) throws IOException {
		delegate.writeBytes(s2b(v));
	}

	public void writeInt(int v) throws IOException {
		delegate.writeInt(v);
	}

	public void writeLong(long v) throws IOException {
		delegate.writeLong(v);
	}

	public void writeFloat(float v) throws IOException {
		delegate.writeInt(Float.floatToIntBits(v));
	}

	public void writeDouble(double v) throws IOException {
		delegate.writeDouble(v);
	}

	public void writeBytes(String s) throws IOException {
		for (int i = 0 ; i < s.length() ; i++) {
		    this.writeByte((byte)s.charAt(i));
		}
	}

	public void writeChars(String s) throws IOException {
		for (int i = 0 ; i < s.length() ; i++) {
			this.writeChar(s.charAt(i));
		}
	}

	public void writeUTF(String s) throws IOException {
		delegate.writeString(s);
	}

	public void writeObject(Object obj) throws IOException {
		delegate.writeObject(obj);
	}

	@Override
	public void write(int v) throws IOException {
		delegate.writeBytes(new byte[]{(byte)v});
	}
	
	@Override
	public void flush() throws IOException {
		delegate.flush();
	}
	
	@Override
	public void close() throws IOException {
		delegate.close();
	}
	
	private byte[] s2b(int v) {
		byte[] result = new byte[2];
		result[0] = (byte)(0xff & (v >>> 8));
		result[1] = (byte)(0xff & (v >>> 0));
		return result;
	}
}
