package com.supermap.context;

import javax.naming.Context;


public class RestClient {
	public static void main(String[] args) {
		try {
			RestServer server = new RestServer();
			Context ctx = server.getContext();
			RestService service = (RestService) ctx.lookup("RestService");
			System.out.println("villageRestUrl£º"+service.getVillageRestUrl());
			System.out.println("townRestUrl£º"+service.getTownRestUrl());
			System.out.println("countryRestUrl£º"+service.getCountryRestUrl());
			System.out.println("overviewLevel£º"+service.getOverviewLevel());
			System.out.println("googleMapsCompatibleResolutions£º"+service.getGoogleMapsCompatibleResolutions());
			System.out.println("widthExt£º"+service.getWidthExt());
			System.out.println("heightExt£º"+service.getHeightExt());
			server.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
