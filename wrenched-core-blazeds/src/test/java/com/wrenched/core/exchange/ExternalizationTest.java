package com.wrenched.core.exchange;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import com.wrenched.core.domain.EnumHolder;
import com.wrenched.core.domain.ExternalizableDecorator;
import com.wrenched.core.externalization.BlazeDSConfiguration;
import com.wrenched.core.messaging.io.EnumPropertyProxy;
import com.wrenched.core.messaging.io.amf.J5AmfMessageDeserializer;
import com.wrenched.core.messaging.io.amf.J5AmfMessageSerializer;

import flex.messaging.io.MessageDeserializer;
import flex.messaging.io.MessageIOConstants;
import flex.messaging.io.MessageSerializer;
import flex.messaging.io.PropertyProxyRegistry;
import flex.messaging.io.SerializationContext;

public class ExternalizationTest extends AbstractDependencyInjectionSpringContextTests {
	private static final SerializationContext context = new SerializationContext();
	
	static {
		BlazeDSConfiguration.registerDecoratorFor(ExternalizableDecorator.class);
		PropertyProxyRegistry.getRegistry().register(EnumHolder.class,
				new EnumPropertyProxy());
		PropertyProxyRegistry.getRegistry().register(Enum.class,
				new EnumPropertyProxy());

		context.setDeserializerClass(J5AmfMessageDeserializer.class);
    	context.setSerializerClass(J5AmfMessageSerializer.class);
	}
	protected String[] getConfigLocations() {
		return new String[] {"classpath:testExternalizationContext.xml"};
	}
	

	private void writeAMF(Object entity, String fileName) {
		FileOutputStream fOut = null;

		try {
			File f = new File(fileName);
			if (f.exists()) {
				f.delete();
			}
			f.createNewFile();
			
			fOut = new FileOutputStream(f);
			
			MessageSerializer ms = context.newMessageSerializer();
			ms.setVersion(MessageIOConstants.AMF3);
			ms.initialize(context, fOut, null);
			ms.writeObject(entity);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				fOut.close();
			}
			catch (IOException e1) {
				e1.printStackTrace();
			}
		}		
	}
	
	private <T> T readAMF(Class<T> clazz, String fileName)
	throws IllegalAccessException, ClassNotFoundException {
		FileInputStream fIn = null;
		T entity = null;

		try {
			entity = clazz.newInstance();
			fIn = new FileInputStream(fileName);
			MessageDeserializer md = context.newMessageDeserializer();
			md.initialize(context, fIn, null);
			entity = clazz.cast(md.readObject());
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (InstantiationException e2) {
			e2.printStackTrace();
		}
		finally {
			try {
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
		
		this.writeAMF(entity, "target/testExtPrimitive.bin");
	}
	
	public void testReadPrimitives() throws IllegalAccessException, ClassNotFoundException {
		TestPlainEntity3 entity = this.readAMF(TestPlainEntity3.class, "target/testExtPrimitive.bin");
		
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
		
		this.writeAMF(entity, "target/testExt.bin");
	}
	
	public void testRead() throws Exception {
		TestEntity entity = this.readAMF(TestEntity.class, "target/testExt.bin");
		
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
		
		this.writeAMF(entity2, "target/testExt2.bin");
	}
	
	public void testReadComplex() throws Exception {
		TestEntity2 entity = this.readAMF(TestEntity2.class, "target/testExt2.bin");
		
		assertNotNull(entity.getA1());
		
		assertEquals(666, entity.getA1().getA1());
		assertEquals(true, entity.getA1().isA2());
		assertEquals(TestEnum.TRK, entity.getA1().getA3());
		assertEquals("blahblah", entity.getA1().getA4());
		assertEquals(3, entity.getA1().getA5().length);
		assertEquals(0, entity.getA1().getA6().size());
	}
	
	public void testWritePlain() {
		TestPlainEntity entity = new TestPlainEntity();
		entity.setA1(666);
		entity.setA2(true);
		entity.setA4("blahblah");
		entity.setA5(new String[] {"1","2","3"});
		entity.setA6(new ArrayList<Integer>());
		
		this.writeAMF(entity, "target/testAMFExt.bin");
	}

	public void testReadPlain() throws IllegalAccessException, ClassNotFoundException {
		TestPlainEntity entity = this.readAMF(TestPlainEntity.class, "target/testAMFExt.bin");

		assertEquals(666, entity.getA1());
		assertEquals(true, entity.isA2());
		assertEquals(TestEnum.TRK, entity.getA3());
		assertEquals("blahblah", entity.getA4());
		assertEquals(3, entity.getA5().length);
		assertEquals(0, entity.getA6().size());
	}
	
	public void testWritePlain2() {
		Map<Integer, Object> test = new HashMap<Integer, Object>();
		
		test.put(1, "blahblah");
		test.put(2, new Object());
//		test.put(3, new ArrayList());
		test.put(3, new TestPlainEntity3());

		TestPlainEntity2 entity = new TestPlainEntity2();
		entity.setA1(666);
		entity.setA2(true);
		entity.setA4("blahblah");
		entity.setA5(new String[] {"1","2","3"});
		entity.setA6(new ArrayList<Integer>());
		entity.setA7(test);
		
		this.writeAMF(entity, "target/testAMFExt.bin");
	}

	public void testReadPlain2() throws IllegalAccessException, ClassNotFoundException {
		TestPlainEntity2 entity = this.readAMF(TestPlainEntity2.class, "target/testAMFExt.bin");

		assertEquals(666, entity.getA1());
		assertEquals(true, entity.isA2());
		assertEquals(TestEnum.TRK, entity.getA3());
		assertEquals("blahblah", entity.getA4());
		assertEquals(3, entity.getA5().length);
		assertEquals(0, entity.getA6().size());

		Map<Integer, Object> test = entity.getA7();
		
		assertNotNull(test);
		assertEquals(3, test.size());
		assertEquals("blahblah", test.get(1));
		assertNotNull(test.get(2));
//		assertEquals(0, ((List)test.get(3)).size());
		assertTrue(test.get(3) instanceof TestPlainEntity3);
	}

	public void testWriteMap() {
		Map<Integer, Object> test = new HashMap<Integer, Object>();
		
		test.put(1, "blahblah");
		test.put(2, new Object());
		test.put(3, new ArrayList());
		
		this.writeAMF(test, "target/testAMFMap.bin");
	}

	public void testReadMap() throws IllegalAccessException, ClassNotFoundException {
		Map<Integer, Object> test = this.readAMF(HashMap.class, "target/testAMFMap.bin");
		
		assertEquals(3, test.size());
		assertEquals("blahblah", test.get(1));
		assertNotNull(test.get(2));
		assertEquals(0, ((List)test.get(3)).size());
	}
}
