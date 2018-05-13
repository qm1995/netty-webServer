package com.qm.netty.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternUtil {
	/**
	 * 解析类似${xxxx}或${xx.xx}
	 * 目前只支持xx.xx格式，不支持xx.xx.xx等格式的解析
	 */
	public static final String REGEX_1="\\$\\{(\\w+(\\w|\\.)*\\w+)\\}";
	
	/**
	 * 判断是否符合xx.xx的格式，如.xx或xx.都属于不合格
	 */
	public static final String REGEX_2="\\w+(\\w|.)*\\w+";
	public static final String REGEX_3="";
	public static final String REGEX_4="";
	public static final String REGEX_5="";
	
	public static Pattern getPattern(String regex){
		if(regex == null || "".equals(regex)){
			return null;
		}
		return Pattern.compile(regex);
	}
	
}
