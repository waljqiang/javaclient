package lib.ssl;

import java.net.URL;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ClassloadingUtil{
	private static final Log log = LogFactory.getLog(ClassloadingUtil.class);
	private static final String INSTANTIATION_EXCEPTION_MESSAGE = "Your class must have a constructor without arguments. If it is an inner class, it must be static!";
	
	public static Object newInstanceFromClassLoader(final String className){
		return newInstanceFromClassLoader(ClassloadingUtil.class,className);
	}
	
	public static Object newInstanceFromClassLoader(final String className,Object... objs){
		return newInstanceFromClassLoader(ClassloadingUtil.class,className,objs); 
	}

	public static Object newInstanceFromClassLoader(final Class<?> classOwner,final String className){
		ClassLoader loader = classOwner.getClassLoader();
		try{
			Class<?> clazz = loader.loadClass(className);
			return clazz.newInstance();
		}catch(Throwable t){
			if(t instanceof InstantiationException){
				log.debug(INSTANTIATION_EXCEPTION_MESSAGE);
			}
			loader = Thread.currentThread().getContextClassLoader();
			if(loader == null){
				throw new RuntimeException("No local context classloader",t);
			}
			try{
				return loader.loadClass(className).newInstance();
			}catch(InstantiationException e){
				throw new RuntimeException(INSTANTIATION_EXCEPTION_MESSAGE+" "+className,e);
			}catch(ClassNotFoundException e){
				throw new IllegalStateException(e);
			}catch(IllegalAccessException e){
				throw new RuntimeException(e);
			}
		}
	}
	
	public static Object newInstanceFromClassLoader(final Class<?> classOwner,final String className,Object... objs){
		ClassLoader loader = classOwner.getClassLoader();
		try{
			Class<?>[] parametersType = new Class<?>[objs.length];
			for(int i = 0;i < objs.length;i++){
				parametersType[i] = objs[i].getClass();
			}
			Class<?> clazz = loader.loadClass(className);
			return clazz.getConstructor(parametersType).newInstance(objs);
		}catch(Throwable t){
			if(t instanceof InstantiationException){
				log.debug(INSTANTIATION_EXCEPTION_MESSAGE);
			}
			loader = Thread.currentThread().getContextClassLoader();
			if(loader == null){
				throw new RuntimeException("No local context classloader",t);
			}
			try{
				return loader.loadClass(className).newInstance();
			}catch(InstantiationException e){
				throw new RuntimeException(INSTANTIATION_EXCEPTION_MESSAGE+" "+className,e);
			}catch(ClassNotFoundException e){
				throw new IllegalStateException(e);
			}catch(IllegalAccessException e){
				throw new RuntimeException(e);
			}
		}
	}
	
	public static URL findResource(final String resourceName){
		return findResource(ClassloadingUtil.class.getClassLoader(),resourceName);
	}
	
	public static URL findResource(ClassLoader loader,final String resourceName){
		try{
			URL resource = loader.getResource(resourceName);
			if(resource != null)
				return resource;
		}catch(Throwable t){
			
		}
		loader = Thread.currentThread().getContextClassLoader();
		if(loader == null)
			return null;
		return loader.getResource(resourceName);
	}
	
	public static String loadProperty(ClassLoader loader,String propertiesFile,String name){
		Properties properties = loadProperties(loader,propertiesFile);
		return (String)properties.get(name);
	}
	
	public static Properties loadProperties(String propertiesFile){
		return loadProperties(ClassloadingUtil.class.getClassLoader(),propertiesFile);
	}
	
	public static Properties loadProperties(ClassLoader loader,String propertiesFile){
		Properties properties = new Properties();
		try{
			URL url = findResource(loader,propertiesFile);
			if(url != null)
				properties.load(url.openStream());
		}catch(Throwable ignored){
			log.warn(ignored.getMessage(),ignored);
		}
		return properties;
	}
	
}