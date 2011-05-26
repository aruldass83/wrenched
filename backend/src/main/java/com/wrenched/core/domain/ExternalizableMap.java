package com.wrenched.core.domain;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

/**
 * a hashmap which is externalizably represented by an array of arrays (map key-value pairs).
 * @author konkere
 *
 * @param <K>
 * @param <V>
 */
public class ExternalizableMap<K, V> extends HashMap<K, V> implements Externalizable {
    public ExternalizableMap() {
		super();
	}

	public ExternalizableMap(Map<K, V> m) {
		super(m);
	}

	@Override
	@SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        Object[] pairs = (Object[])in.readObject();
        if (pairs != null) {
            for (Object pair : pairs) {
                put((K)((Object[])pair)[0], (V)((Object[])pair)[1]);
            }
        }
    }

	@Override
    public void writeExternal(ObjectOutput out) throws IOException {
        Object[] outObjectArray = new Object[size()];

        int index = 0;
        for (Map.Entry<K, V> entry : entrySet())
            outObjectArray[index++] = new Object[]{entry.getKey(), entry.getValue()};

        out.writeObject(outObjectArray);
    }

    public static <T, U> ExternalizableMap<T, U> newInstance(Map<T, U> map) {
        return new ExternalizableMap<T, U>(map);
    }
    
    public Map<K, V> toMap() {
    	return new HashMap<K, V>(this);
    }

    @Override
    public String toString() {
        return getClass().getName() + " " + super.toString();
    }
}
