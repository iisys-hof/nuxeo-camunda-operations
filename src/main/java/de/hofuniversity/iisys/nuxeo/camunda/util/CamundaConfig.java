package de.hofuniversity.iisys.nuxeo.camunda.util;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class CamundaConfig
{
	public static final String CAMUNDA_URL = "camunda.url";
	
	public static final String CONC_WORKFLOWS = "nuxeo.concurrent_workflows";
	public static final String BLOCK_CONC_WORKFLOWS = "nuxeo.block_concurrent_workflows";
	public static final String CLEAR_STALE_WFS = "nuxeo.clear_stale_workflows";
	
	private static final String PROPERTIES = "camunda-operations";
	
	private static CamundaConfig fInstance;
	
	private final Map<String, String> fConfig;
	
	public static synchronized CamundaConfig getInstance()
	{
		if(fInstance == null)
		{
			fInstance = new CamundaConfig();
		}
		
		return fInstance;
	}
	
	public CamundaConfig()
	{
		fConfig = new HashMap<String, String>();
		
		readConfig();
	}
	
	private void readConfig()
	{
		try
		{
	        final ClassLoader loader = Thread.currentThread()
	            .getContextClassLoader();
	        ResourceBundle rb = ResourceBundle.getBundle(PROPERTIES,
	            Locale.getDefault(), loader);
	        
	        String key = null;
	        String value = null;
	        
	        Enumeration<String> keys = rb.getKeys();
	        while(keys.hasMoreElements())
	        {
	        	key = keys.nextElement();
	        	value = rb.getString(key);
	        	
	        	fConfig.put(key, value);
	        }
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public Map<String, String> getConfiguration()
	{
		return fConfig;
	}
}
