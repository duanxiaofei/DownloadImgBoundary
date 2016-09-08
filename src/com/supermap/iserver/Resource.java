package com.supermap.iserver;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.ws.rs.core.MultivaluedMap;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.supermap.context.RestServer;
import com.supermap.context.RestService;

/**
 * 从iserver获取图片资源 通过边界位置计算图片的范围
 * 
 * @author duanxiaofei
 * 
 */
public class Resource {

	public static String getResult(Task task) {
		RestServer server = new RestServer();
		Context ctx = server.getContext();
		RestService service = null;
		try {
			service = (RestService) ctx.lookup("RestService");
		} catch (NamingException e) {
			e.printStackTrace();
		}

		int mapLevel = task.getMapLevel();
		int widthExt = service.getWidthExt();
		int heightExt = service.getHeightExt();
		int overviewLevel=service.getOverviewLevel();
		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);

		int areaLevel = task.getAreaLeve();
		String restUrl = "";
		if (areaLevel == 6 || areaLevel == 7) {
			restUrl = service.getVillageRestUrl();
		} else if (areaLevel == 5) {
			restUrl = service.getTownRestUrl();
		} else if (areaLevel == 4) {
			restUrl = service.getCountryRestUrl();
		}
		boolean tianditu = service.isTianditu();

		String bounds = task.getBounds();
		WebResource imgResource = client.resource(restUrl);
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
		String[] temp = bounds.split(",");
		double left = Double.valueOf(temp[0]);
		double top = Double.valueOf(temp[1]);
		double right = Double.valueOf(temp[2]);
		double bottom = Double.valueOf(temp[3]);

		String resolutions = service.getGoogleMapsCompatibleResolutions();
		String[] a = resolutions.split(",");
		double[] d = new double[a.length];
		for (int i = 0; i < a.length; i++) {
			d[i] = Double.valueOf(a[i]);
		}

		int width = 0, height = 0;
		int level=0;
		if (tianditu) {
			if(mapLevel>overviewLevel){
				level=mapLevel-1;
				widthExt=2*widthExt;
				heightExt=2*heightExt;
			}else{
				level=mapLevel;
			}
			width = (int)(Math.ceil(getDistanceByWGS84(left, top, right, top) / d[(level)] / 256.0D) * 256.0D) +widthExt;
		    height = (int)(Math.ceil(getDistanceByWGS84(left, top, left, bottom) / d[(level)] / 256.0D) * 256.0D) +heightExt;
		} else {
			width = (int) (Math.ceil((right - left + widthExt) / d[mapLevel - 1] / 256) * 256);
			height = (int) (Math.ceil((top - bottom + heightExt) / d[mapLevel - 1] / 256) * 256);
			while ((width <= 1024 || height <= 1024) && mapLevel <= 18) {
				mapLevel = mapLevel + 1;
				width = (int) (Math.ceil((right - left) / d[mapLevel - 1] / 256) * 256);
				height = (int) (Math.ceil((top - bottom) / d[mapLevel - 1] / 256) * 256);
			}
			while (width >= 8192 || height >= 8192) {
				mapLevel = mapLevel - 1;
				width = (int) (Math.ceil((right - left) / d[mapLevel - 1] / 256) * 256);
				height = (int) (Math.ceil((top - bottom) / d[mapLevel - 1] / 256) * 256);
			}
		}
		System.out.println("width：" + width);
		System.out.println("height：" + height);

		double centerX = (left + right) / 2;
		double centerY = (top + bottom) / 2;
		String center = "{x:" + double2String(centerX) + ",y:" + double2String(centerY) + "}";
		double scale = 0.0254 / (d[level] * 96);
		queryParams.add("width", String.valueOf(width));
		queryParams.add("height", String.valueOf(height));
		queryParams.add("center", center);
		queryParams.add("scale", double2String(scale));
		queryParams.add("antialias", "true");
		WebResource resource = imgResource.queryParams(queryParams);
		String result = null;
		try {
			result = resource.get(String.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;

	}

	protected static String double2String(double d) {
		return String.format("%.16f", d);
	}

	private static double getDistanceByWGS84(double x1, double y1, double x2, double y2) {
		double EARTH_RADIUS = 6378.137;  
		double radLat1 = rad(x1);    
        double radLat2 = rad(x2);    
        double a = radLat1 - radLat2;    
        double b = rad(y1) - rad(y2);    
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)    
                + Math.cos(radLat1) * Math.cos(radLat2)    
                * Math.pow(Math.sin(b / 2), 2)));    
        s = s * EARTH_RADIUS;    
        s = Math.round(s * 10000d) / 10000d;    
        s = s*1000;    
        return s;    
	}
	private static double rad(double d) {    
        return d * Math.PI / 180.0;    
    }   
}
