package com.supermap.context;

import javax.naming.Context;


public class RestClient {
	public static void main(String[] args) {
		try {
			RestServer server = new RestServer();
			Context ctx = server.getContext();
			RestService service = (RestService) ctx.lookup("RestService");
			System.out.println("villageRestUrl��"+service.getVillageRestUrl());
			System.out.println("townRestUrl��"+service.getTownRestUrl());
			System.out.println("countryRestUrl��"+service.getCountryRestUrl());
			System.out.println("overviewLevel��"+service.getOverviewLevel());
			System.out.println("googleMapsCompatibleResolutions��"+service.getGoogleMapsCompatibleResolutions());
			System.out.println("widthExt��"+service.getWidthExt());
			System.out.println("heightExt��"+service.getHeightExt());
			server.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
