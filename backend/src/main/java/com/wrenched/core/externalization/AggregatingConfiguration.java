package com.wrenched.core.externalization;

import com.wrenched.core.externalization.Externalizer.Configuration;
import com.wrenched.core.externalization.Externalizer.Converter;

/**
 * internal class that aggregates Externalizer configurations
 * if there were several found on the classpath
 * @author konkere
 *
 */
class AggregatingConfiguration implements Configuration {
	private final Configuration[] configs;
	
	AggregatingConfiguration() throws InstantiationException {
		try {
			Object[] arg0 = FactoryFinder.find(Configuration.class.getCanonicalName());
			this.configs = new Configuration[arg0.length];
			
			for (int i = 0; i < arg0.length; i++) {
				if (arg0[i] instanceof Configuration) {
					this.configs[i] = (Configuration)arg0[i];
				}
			}
		}
		catch (Exception e) {
			throw new InstantiationException();
		}
	}
	
	@Override
	public boolean useGAS3() {
		boolean result = false;
		
		for (Configuration c : this.configs) {
			result |= c.useGAS3();
		}
		
		return result;
	}

	@Override
	public Converter findConverter(Class clazz) {
		Converter c = null;
		int i = 0;
		
		while ((c == null) && (i < this.configs.length)) {
			if (this.configs[i] != null) {
				c = this.configs[i].findConverter(clazz);
			}
			i++;
		}
		
		return c;
	}
}