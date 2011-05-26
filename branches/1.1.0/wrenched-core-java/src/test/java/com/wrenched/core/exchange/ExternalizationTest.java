package com.wrenched.core.exchange;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import com.wrenched.core.externalization.Externalizer;
import com.wrenched.core.externalization.io.ExternalizingObjectInputStream;
import com.wrenched.core.externalization.io.ExternalizingObjectOutputStream;

public class ExternalizationTest extends TestCase {
	public void testClassSubstitution1() {
		Class z = HashSet.class;
		Object o = new ArrayList();
		
		if ((o != null) && !z.isAssignableFrom(o.getClass())) {
			o = new HashSet();
		}
		
		assertTrue(o instanceof Set);
	}
	
	public void testClassSubstitution3() {
		Class z = ArrayList.class;
		Object o = new ArrayList();
		
		if ((o != null) && !z.isAssignableFrom(o.getClass())) {
			o = new HashSet();
		}
		
		assertTrue(o instanceof List);
	}
	
	private void write(Object entity, String fileName) {
		OutputStream fOut = null;
		OutputStream bOut = null;
		ObjectOutput oOut = null;

		try {
			File f = new File(fileName);
			if (f.exists()) {
				f.delete();
			}
			f.createNewFile();
			f.setWritable(true);
			
			fOut = new FileOutputStream(f);
			bOut = new BufferedOutputStream(fOut);
			oOut = new ExternalizingObjectOutputStream(bOut);
//			Externalizer.getInstance().writeExternal(entity, oOut);
			oOut.writeObject(entity);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				oOut.flush();
				oOut.close();
				bOut.close();
				fOut.close();
			}
			catch (IOException e1) {
				e1.printStackTrace();
			}
		}		
	}

	private <T> T read(Class<T> clazz, String fileName)
	throws IllegalAccessException, ClassNotFoundException {
		InputStream fIn = null;
		InputStream bIn = null;
		ObjectInput oIn = null;
		T entity = null;

		try {
			entity = clazz.newInstance();
			fIn = new FileInputStream(fileName);
			bIn = new BufferedInputStream(fIn);
			oIn = new ExternalizingObjectInputStream(bIn);
//			Externalizer.getInstance().readExternal(entity, oIn);
			entity = (T)oIn.readObject();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (InstantiationException e2) {
			e2.printStackTrace();
		}
		catch (Throwable e3) {
			e3.printStackTrace();
		}
		finally {
			try {
				oIn.close();
				bIn.close();
				fIn.close();
			}
			catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		return entity;
	}
	
	public void testWritePrimitives() {
		TestPlainEntity3 entity = new TestPlainEntity3();
		entity.setA1(true);
		entity.setA2(Byte.MAX_VALUE);
		entity.setA3(Short.parseShort("15"));
		entity.setA4(666);
		entity.setA5(666l);
		entity.setA6(10e-5f);
		entity.setA7(2e6d);
		entity.setA8("blahblah".getBytes());
		
		this.write(entity, "target/testExtPrimitive.bin");
	}
	
	public void testReadPrimitives() throws IllegalAccessException, ClassNotFoundException {
		TestPlainEntity3 entity = this.read(TestPlainEntity3.class, "target/testExtPrimitive.bin");
		
		assertEquals(true, entity.isA1());
		assertEquals(Byte.MAX_VALUE, entity.getA2());
		assertEquals(Short.parseShort("15"), entity.getA3());
		assertEquals(666, entity.getA4());
		assertEquals(666l, entity.getA5());
		assertEquals(10e-5f, entity.getA6());
		assertEquals(2e6d, entity.getA7());
		assertTrue(Arrays.equals("blahblah".getBytes(), entity.getA8()));
	}
	
	public void testWrite() {
		TestEntity entity = new TestEntity();
		entity.setA1(666);
		entity.setA2(true);
		entity.setA4("blahblah");
		entity.setA5(new String[] {"1","2","3"});
		entity.setA6(new ArrayList<Integer>());
		
		this.write(entity, "target/testExt.bin");
	}
	
	public void testRead() throws Exception {
		TestEntity entity = this.read(TestEntity.class, "target/testExt.bin");
		
		assertEquals(666, entity.getA1());
		assertEquals(true, entity.isA2());
		assertEquals(TestEnum.TRK, entity.getA3());
		assertEquals("blahblah", entity.getA4());
		assertEquals(3, entity.getA5().length);
		assertEquals(0, entity.getA6().size());
	}
	
	public void testWriteComplex() {
		TestEntity entity = new TestEntity();
		entity.setA1(666);
		entity.setA2(true);
		entity.setA4("blahblah");
		entity.setA5(new String[] {"1","2","3"});
		entity.setA6(new ArrayList<Integer>());
		
		TestEntity2 entity2 = new TestEntity2();
		entity2.setA1(entity);
		
		this.write(entity2, "target/testExt2.bin");
	}
	
	public void testReadComplex() throws Exception {
		TestEntity2 entity = this.read(TestEntity2.class, "target/testExt2.bin");
		
		assertNotNull(entity.getA1());
		
		assertEquals(666, entity.getA1().getA1());
		assertEquals(true, entity.getA1().isA2());
		assertEquals(TestEnum.TRK, entity.getA1().getA3());
		assertEquals("blahblah", entity.getA1().getA4());
		assertEquals(3, entity.getA1().getA5().length);
		assertEquals(0, entity.getA1().getA6().size());
	}

	public void testWriteMap() {
		Map<Integer, Object> test = new HashMap<Integer, Object>();
		
		test.put(1, "blahblah");
		test.put(2, new Object());
		test.put(3, new ArrayList());
		
		this.write(test, "target/testAMFMap.bin");
	}

	public void testReadMap() throws IllegalAccessException, ClassNotFoundException {
		Map<Integer, Object> test = this.read(HashMap.class, "target/testAMFMap.bin");
		
		assertEquals(3, test.size());
		assertEquals("blahblah", test.get(1));
		assertNotNull(test.get(2));
		assertEquals(0, ((List)test.get(3)).size());
	}
}
