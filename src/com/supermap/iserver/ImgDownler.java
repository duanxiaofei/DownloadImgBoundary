package com.supermap.iserver;

import javax.naming.Context;
import javax.naming.NamingException;

import net.sf.json.JSONObject;

import com.supermap.context.RestServer;
import com.supermap.context.RestService;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * 下载聚合地图
 * 通过解析imageUrl值进行二进制流输出
 * 1表示概览图，2表示高清图
 * iserver返回的是png格式，进行转换成jpg，最后做二次绘制，删除png
 * @author duanxiaofei
 *
 */
public class ImgDownler extends BaseImg {

	@Override
	public Task getImgFromServer(Task task, String result) {
		if(result==null){
			return null;
		}
		RestServer server = new RestServer();
		Context ctx = server.getContext();
		RestService service = null;
		try {
			service = (RestService) ctx.lookup("RestService");
		} catch (NamingException e) {
			e.printStackTrace();
		}
		int mapLevel = task.getMapLevel();
		String code = task.getCode();
		String fileName =  System.getProperty("catalina.home") + File.separator + "webapps" + File.separator 
				+ "googleImg"+File.separator+super.getFileDir(code);
		File dir=new File(fileName);
		if(!dir.exists()){
			dir.mkdirs();
		}
		int overviewLevel = service.getOverviewLevel();
		if (mapLevel < overviewLevel) {
			fileName += code + "_1";
		} else {
			fileName += code + "_2";
		}
		fileName+=".png";
		task.setFileName(fileName);
		JSONObject r=JSONObject.fromObject(result);
		if(r.containsKey("imageUrl")){
			String imageUrl=r.getString("imageUrl");
			URL url = null;
			try {
				url = new URL(imageUrl);
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (null != url) {
					int byteRead = 0;
					URLConnection conn = url.openConnection();
					InputStream inStream = conn.getInputStream();
					FileOutputStream fs = new FileOutputStream(fileName);
					byte[] buffer = new byte[1024];
					while ((byteRead = inStream.read(buffer)) != -1) {
						fs.write(buffer, 0, byteRead);
					}
					fs.close();
					inStream.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		String pngFileName=task.getFileName();
		if(super.png2jpg(task)){
			super.drawMapContent(task);
		}
		super.deleteFile(pngFileName);
		return task;
	}
}
