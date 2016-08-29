package com.supermap.context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class RestServer {
	private Context ctx=null;
	public RestServer(){
		Hashtable<String, String> env=new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.fscontext.RefFSContextFactory");
		env.put(Context.PROVIDER_URL, "file:/");
		try {
			ctx=new InitialContext(env);
		} catch (NamingException e) {
			e.printStackTrace();
		}
		loadRs();
	}
	
	private void loadRs(){
		if(ctx!=null){
			InputStream in = getClass().getResourceAsStream(
					"config.properties");
			Properties props = new Properties();
			try {
				props.load(in);
			} catch (IOException e) {
				e.printStackTrace();
			}
			String villageRestUrl=props.getProperty("villageRestUrl");
			String townRestUrl=props.getProperty("townRestUrl");
			String countryRestUrl=props.getProperty("countryRestUrl");
			int overviewLevel=Integer.valueOf(props.getProperty("overviewLevel"));
			String googleMapsCompatibleResolutions=props.getProperty("googleMapsCompatibleResolutions");
			int widthExt=Integer.valueOf(props.getProperty("widthExt"));
			int heightExt=Integer.valueOf(props.getProperty("heightExt"));
			RestService service=new SimpleRestService();
			service.setVillageRestUrl(villageRestUrl);
			service.setTownRestUrl(townRestUrl);
			service.setCountryRestUrl(countryRestUrl);
			service.setOverviewLevel(overviewLevel);
			service.setGoogleMapsCompatibleResolutions(googleMapsCompatibleResolutions);
			service.setWidthExt(widthExt);
			service.setHeightExt(heightExt);
			try {
				this.ctx.rebind("RestService", service);
			} catch (NamingException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void close() throws NamingException {
		ctx.close();
	}

	public Context getContext() {
		return ctx;
	}
}
