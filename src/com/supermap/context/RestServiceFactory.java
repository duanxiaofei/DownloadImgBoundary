package com.supermap.context;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

public class RestServiceFactory implements ObjectFactory {
	// 根据Reference中存储的信息创建出RestService实例
	@Override
	public Object getObjectInstance(Object obj, Name arg1, Context arg2,
			Hashtable<?, ?> arg3) throws Exception {
		if (obj instanceof Reference) {
			Reference ref = (Reference) obj;
			String villageRestUrl = (String) ref.get("villageRestUrl").getContent();
			String townRestUrl = (String) ref.get("townRestUrl").getContent();
			String countryRestUrl = (String) ref.get("countryRestUrl").getContent();
			int overviewLevel = Integer.valueOf(ref.get("overviewLevel").getContent().toString());
			String resolutions=(String) ref.get("googleMapsCompatibleResolutions").getContent();
			int widthExt=Integer.valueOf(ref.get("widthExt").getContent().toString());
			int heightExt=Integer.valueOf(ref.get("widthExt").getContent().toString());
			SimpleRestService service = new SimpleRestService();
			service.setVillageRestUrl(villageRestUrl);
			service.setTownRestUrl(townRestUrl);
			service.setCountryRestUrl(countryRestUrl);
			service.setOverviewLevel(overviewLevel);
			service.setGoogleMapsCompatibleResolutions(resolutions);
			service.setWidthExt(widthExt);
			service.setHeightExt(heightExt);
			return service;
		}
		return null;
	}

}
