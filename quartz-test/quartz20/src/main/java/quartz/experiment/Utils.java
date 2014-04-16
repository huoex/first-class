package quartz.experiment;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * ObjectUtils
 *
 * @author Zemian Deng
 */
public class Utils {
	
	private static Logger logger = LoggerFactory.getLogger(Utils.class);
	
	@SuppressWarnings("unchecked")
	public static <T> T createRecorderProxy(Class<T> interfaceType) {
		ClassLoader loader = interfaceType.getClassLoader();
		Class<T>[] interfaces = new Class[] { interfaceType };
		InvocationHandler handler = new MethodsLoggerHandler();
		return (T)Proxy.newProxyInstance(loader, interfaces , handler);
	}
	
	public static class MethodsLoggerHandler implements InvocationHandler {
		
		protected Logger logger = LoggerFactory.getLogger(getClass());
		
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			logger.info("Method Invoked: " + method);
			
			Class<?> retType = method.getReturnType();
			Object ret = null;
			if (!retType.equals(Void.TYPE)) {
				ret = Mockito.mock(retType); 
			}
			return ret;
		}
		
	}
	
	public static String propsToText(Properties configProps) {
		StringWriter sWriter = new StringWriter();
		PrintWriter pWriter = new PrintWriter(sWriter);
		// print in sorted order.
		List<String> names = new ArrayList<String>(configProps.stringPropertyNames());
		Collections.sort(names);
		for (String name : names)
			pWriter.println(name + " = " + configProps.getProperty(name));
		pWriter.close();
		return sWriter.toString();
	}
	
	public static Properties loadPropertiesFromText(String configPropsText) {
		try {
			Properties config = new Properties();
			// Read in form input.
			StringReader reader = new StringReader(configPropsText);
			config.load(reader);
			return config;
		} catch (Exception e) {
			throw new RuntimeException("Failed to create Properties from input text.", e);			
		}					
	}
	
	public static Map<String, Object> createMap(Object ... objects) {
		if (objects.length % 2 != 0)
			throw new RuntimeException("CreateMap parameters must be in even count. Got " + objects.length + " instead.");
		Map<String, Object> result = new HashMap<String, Object>();	
		int size = objects.length / 2;
		for (int i = 0; i < size; i += 2) {
			result.put(objects[i].toString(), objects[i+1]);
		}
		return result;
	}
	
	/**
	 * Generate a stacktrace with method call stack and log it.
	 */
	public static void stackTraceCheckpoint() {
		try { 
			throw new RuntimeException(); 
		} 
		catch (RuntimeException e) { 
			logger.info("StackTrace Checkpoint.", e); 
		}
	}
	
	public static class Getter {
		private Object object;
		private Method method;
		private String propName;
		
		public String getPropName() {
			return propName;
		}
		public Method getMethod() {
			return method;
		}
		public Object getObject() {
			return object;
		}
	}
	
	/**
	 * Get all the getter methods from an object, including its parent classes, except getClass(). 
	 * A getter method is defined as zero parameter and starts with getX, hasX, or isX.
	 * 
	 * @param object - object to look getters with.
	 * @param upToClass - stop traversing up to this class to look for getters.
	 */
	public static List<Getter> getGetters(Object object, Class<?> upToClass) {
		if (upToClass == null)
			upToClass = Object.class;
		List<Getter> getters = new ArrayList<Getter>();
		Class<?> objCls = object.getClass();
		Class<?> parentCls = objCls;
		Class<?> rootCls = Object.class;
		while (parentCls != null && !parentCls.equals(rootCls)) {
			Method[] methods = parentCls.getMethods();
			for (Method method : methods) {
				//logger.info(method.getName() + ", params.length=" + method.getParameterTypes().length);
				if (method.getParameterTypes().length > 0)
					continue; // skip any non-zero param methods.

				String methodName = method.getName();
				if (methodName.equals("getClass"))
					continue; // skip getClass method.
				
				String propName = null;
				if (methodName.length() >= 4 && methodName.startsWith("get") && Character.isUpperCase(methodName.charAt(3))) {
					propName = methodName.substring(3, 4).toLowerCase() + methodName.substring(4);		
				} else if (methodName.length() >= 4 && methodName.startsWith("has") && Character.isUpperCase(methodName.charAt(3))) {
					propName = methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
				} if (methodName.length() >= 3 && methodName.startsWith("is") && Character.isUpperCase(methodName.charAt(2))) {
					propName = methodName.substring(2, 3).toLowerCase() + methodName.substring(3);
				}
				//logger.info(method.getName() + ", propName=" + propName);
				if (propName != null) {
					Getter getter = new Getter();
					getter.object = object;
					getter.propName = propName;
					getter.method = method;
					getters.add(getter);
				}
			}
			if (parentCls.equals(upToClass)) {
				parentCls = null;
			} else {
				parentCls = parentCls.getSuperclass();
			}
		}
		return getters;
	}
	
	public static List<Getter> getGetters(Object object) {
		return getGetters(object, Object.class);
	}
	
	public static Object getGetterValue(Getter getter) {
		try {
			return getter.method.invoke(getter.object);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String getGetterStrValue(Getter getter) {
		return getGetterValue(getter).toString();
	}
	
	public static String toObjectString(Object object) {
		return object.getClass().getName() + "@" + System.identityHashCode(object);
	}
	
	public static String join(Collection<?> items, String sep) {
		if (items.isEmpty())
			return "";		
		Iterator<?> itr = items.iterator();
		StringBuilder sb = new StringBuilder();
		if (itr.hasNext())
			sb.append(itr.next().toString());
		while (itr.hasNext()) {
			sb.append(sep).append(itr.next().toString());
		}
		return sb.toString();
	}
	
	public static String toString(Object object) { return toString(object, ", "); }
	public static String toString(Object object, String sep) {
		StringBuilder sb = new  StringBuilder(toObjectString(object));
		if (object instanceof CharSequence) {
			return object.toString();
		} else if (object instanceof Number) {
			return object.toString();
		} else if (object.getClass().isArray()) {
			Object[] array = (Object[])object;
			sb.append("{");
			if (array.length > 0)
				sb.append(sep).append(join(Arrays.asList(array), sep)).append(sep);
			sb.append("}").append(", size=").append(array.length);
		}  else if (object instanceof Collection<?>) {
			List<Object> list = new ArrayList<Object>((Collection<?>)object);
			sb.append("{");
			if (list.size() > 0)
				sb.append(sep).append(join(list, sep)).append(sep);
			sb.append("}").append(", size=").append(list.size());
		} else if (object instanceof Map<?,?>) {
			Map<?,?> map = (Map<?,?>)object;
			sb.append("{");
			if (map.size() > 0)
				sb.append(sep).append(join(map.entrySet(), sep)).append(sep);
			sb.append("}").append(", size=").append(map.size());
		} else {
			List<Getter> getters = getGetters(object, null);
			if (getters.size() > 0) {
				sb.append("{");
				List<String> propVals = new ArrayList<String>();
				for (Getter getter : getters) {
					String val = getGetterStrValue(getter);
					propVals.add(getter.propName + "=" + val);
				}
				sb.append(join(propVals, sep));
				sb.append("}");
			}
		}
		return sb.toString();
	}
	
	/** Print object's toString recursively. */
	public static void dump(Object object) {
		logger.info(toString(object, "\n"));
	}
	
}
