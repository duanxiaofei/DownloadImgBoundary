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
 * ����google�ۺϵ�ͼ Created by duanxiaofei on 2016/3/16.
 */
public class GoogleImg extends BaseImg {

	/**
	 * ��iserver��������ȡ�ۺϵ�ͼ���� ������google��ͼ�����ͼ���ص����ݸ�ʽ��һ������ʵ��������и��Ǵ���
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
		// ����ͼ���ļ���Ϊcode_1;���򣬸���ͼΪcode_2
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
		//png2jpg�����ػ�ͼƬ
		String pngFileName=task.getFileName();
		if(super.png2jpg(task)){
			super.drawMapContent(task);
		}
		//ɾ��png��ֻ����jpg
		super.deleteFile(pngFileName);
		return task;
	}
}
