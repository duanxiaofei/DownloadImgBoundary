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
 * 下载google聚合地图 Created by duanxiaofei on 2016/3/16.
 */
public class GoogleImg extends BaseImg {

	/**
	 * 从iserver服务器获取聚合地图数据 抽象处理，google地图、天地图返回的数据格式不一样，据实际情况进行覆盖处理。
	 * 
	 * @param fileName
	 * @param result
	 * @return
	 */
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
		// 概览图，文件名为code_1;否则，高清图为code_2
		String fileName =  System.getProperty("user.home") + File.separator + "webapps" + File.separator 
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
		//png2jpg，并重绘图片
		String pngFileName=task.getFileName();
		if(super.png2jpg(task)){
			super.drawMapContent(task);
		}
		//删除png，只保留jpg
		super.deleteFile(pngFileName);
		return task;
	}
}
