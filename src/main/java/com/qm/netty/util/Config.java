package com.qm.netty.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 读取配置类
 * @author qiumin
 *
 */
public class Config {
	private static final Properties p = new Properties();
	
	private static volatile Config config;
	
	private String path = "/1.properties";
	
	private Config(){
		
		InputStream stream = this.getClass().getResourceAsStream(path);
		try {
			p.load(stream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static Config getInstances(){
		if(config == null){
			synchronized (new Object()) {
				if(config == null){
					config = new Config();
				}
			}
		}
		return config;
	}
	
	
	public String getString(String key){
		return p.getProperty(key);
	}
	
	public int getInt(String key){
		String value = getString(key);
		if(value == null){
			throw new RuntimeException("error");
		}
		return Integer.valueOf(value);
	}
	
	public Properties getProperties(){
		return p;
	}
}
