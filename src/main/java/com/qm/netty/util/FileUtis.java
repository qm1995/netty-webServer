package com.qm.netty.util;

import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtis {
	
	//private static final Logger logger = LoggerFactory.getLogger(FileUtis.class);
	private static List<String> fileNames = new ArrayList<String>();
	private static String path = null;
	private static final String ROOT_PATH = "";
	private static final Map<String, Object> DATA_MAP = new HashMap<String, Object>();//封装请求数据的map
	/**
	 * 初始化一些数据
	 * 得到编译后路径的所有html文件全路径
	 */
	static {
		path = FileUtis.class.getClassLoader().getResource("").getPath();
		getAllHtmlFileName(path);
	}
	
	public static Map<String, Object> getDataMap() {
		return DATA_MAP;
	}

	public static void setData(Map<String, Object> data) {
		DATA_MAP.putAll(data);
	}
	
	public static void clear() {
		DATA_MAP.clear();
	}
	/**
	 * 得到文件编译后的根路径
	 * @return
	 */
	public static String getRootPath() {
		if(path == null || "".equals(path)) {
			throw new RuntimeException("init path is error");
		}
		return path;
	}
	
	
	/**
	 * 根据根路径获取所有的html文件路径
	 * @param path
	 * @return
	 */
	private static List<String> getAllHtmlFileName(String path){
		List<String> nameLists = new ArrayList<String>();
		File file = new File(path);
		addFileName(nameLists, file);
		fileNames.addAll(nameLists);
		return nameLists;
	}
	public static List<String> getFileName(){
		return fileNames;
	}
	//添加文件核心
	private static void addFileName(List<String> nameList,File file) {
		if(file.isFile() && (file.getName().endsWith("html") || file.getName().endsWith("htm"))){
			nameList.add(file.getAbsolutePath()+ROOT_PATH);
		}else {
			File[] files = file.listFiles(new FilenameFilter() {
				
				public boolean accept(File dir, String name) {
					// TODO Auto-generated method stub
					return dir.isDirectory() || name.endsWith("html");
				}
			});
			if(files != null) {
				for(int i = 0; i < files.length; i++) {
					addFileName(nameList, files[i]);
				}
			}
		}
	}
	
	//替换${}表达式
	public static String getResolveContent(String fileContent) {
		List<String> html = getExperFromHtml(fileContent);
		for(String s:html) {
			int index = s.indexOf(".");
			if(index == -1){
				Object parameter = DATA_MAP.get(s);
				if(parameter == null) {
					parameter = "";
				}
				fileContent = fileContent.replace("${"+s+"}",parameter.toString());
			}else{
				String[] split = s.split("\\.");
				String string = split[0];
				Object parameter = DATA_MAP.get(string);
				if(parameter == null) {
					parameter = "";
					//throw new RuntimeException("the expression [${"+s+"]} has error,please check it");
				}else if(split.length == 2){
					try {
						Class<?> clazz = parameter.getClass();
						Field field = clazz.getDeclaredField(split[1]);
						PropertyDescriptor descriptor = new PropertyDescriptor(field.getName(), clazz);
						Method method = descriptor.getReadMethod();
						Object value = method.invoke(parameter, new Object[]{});
						fileContent = fileContent.replace("${"+s+"}",value.toString());
					} catch (Exception e) {
						//TODO Auto-generated catch block
						e.printStackTrace();
					} 
				}
			}
		}
		return fileContent;
	}
	
	/**
	 * 根据文件获取到对应内容,只限于文本类型，
	 * 若还有图片、视频类的流，这个类无用
	 * @param file
	 * @return
	 */
	private static String getFileContent(File file) {
		if(file.isHidden() || !file.exists() || !file.isFile()) {
			return "";
		}
		FileReader read = null;
		BufferedReader br = null;
		try {
			read = new FileReader(file);
			br = new BufferedReader(read);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while((line = br.readLine()) != null) {
				sb.append(line);
			}
			return sb.toString();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if(read != null) {
				try {
					read.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(br != null) {
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return "";
	}
	
	/**
	 * 用来解析${****}类似的表达式
	 * @param content
	 * @return
	 */
	private static List<String> getExperFromHtml(String content){
		List<String> finds = new ArrayList<String>();
		Pattern p = Pattern.compile(PatternUtil.REGEX_1);
		Matcher m = p.matcher(content);
		while(m.find()) {
			String group = m.group(1);//参数为1，则取出${}大括号里面的内容，为0则取出包括${}的内容
			if(group == null || !group.matches(PatternUtil.REGEX_2)){
				throw new RuntimeException("the param ["+group+"] is error,please check it");
			}
			finds.add(group);
		}
		return finds;
	}
	
	/**
	 * 获取所有文件内容
	 * @return
	 */
	public static Map<String,String> getFileContent(){
		Map<String,String> fileContents = new HashMap<String, String>();
		for(String name:fileNames) {
			name = name.replaceAll("\\\\","/");
			String content = getFileContent(new File(name));
			fileContents.put(name, content);
		}
		return fileContents;
	}
	
	public static String getContent(String path){
		path = path.replaceAll("\\\\", "/");
		Map<String, String> fileContents = getFileContent();
		for(Entry<String, String> e:fileContents.entrySet()){
			String key = e.getKey();
			if(key.endsWith(path)){
				String value = e.getValue();
				value = getResolveContent(value);
				return value;
			}
			
		}
		
		return "";
	}
	
	public static void main(String[] args) {
		//String path = FileUtis.class.getClassLoader().getResource("").getPath();
		/*String str = "adsf${user.username.age}";
		Pattern pattern = Pattern.compile("\\$\\{(.*)\\}");
		//Pattern p = Pattern.compile("^\\w");
		Matcher matcher = pattern.matcher(str);
		while(matcher.find()){
			String group = matcher.group(1);
			boolean matches = group.matches(PatternUtil.REGEX_2);
			if(matches){
				String[] split = group.split("\\.");
				System.out.println(split[0]+":"+split[1]+":"+split[2]);
			}
		}*/
		
		String str = "${user.name}-->${user.age}";
		Pattern p = Pattern.compile("\\$\\{(\\w+(\\w|\\.)*\\w+)\\}");
		Matcher matcher = p.matcher(str);
		while(matcher.find()){
			String group = matcher.group(1);
			System.out.println(group);
			str = str.replace("${"+group+"}", "zhangsan");
			System.out.println(str);
		}
	}
}
