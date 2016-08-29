package com.supermap.iserver;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.naming.Context;
import javax.naming.NamingException;

import net.sf.json.JSONObject;

import com.supermap.context.RestServer;
import com.supermap.context.RestService;

public class TiandituImg extends BaseImg {

	@Override
	public Task getImgFromServer(Task task, String result) {
		if (result == null) {
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
		String fileName = System.getProperty("catalina.home") + File.separator
				+ "webapps" + File.separator + "googleImg" + File.separator
				+ super.getFileDir(code);
		File dir = new File(fileName);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		int overviewLevel = service.getOverviewLevel();
		if (mapLevel < overviewLevel) {
			fileName += code + "_1";
		} else {
			fileName += code + "_2";
		}
		fileName += ".jpg";
		task.setFileName(fileName);
		JSONObject r = JSONObject.fromObject(result);
		if (r.containsKey("imageUrl")) {
			String imageUrl = r.getString("imageUrl");
			if (imageUrl.equals("null")) {
				String imageData = r.getString("imageData");
				String[] strBytes = imageData.split(",");
				byte[] bytes = new byte[strBytes.length];
				for (int i = 0; i < strBytes.length; i++) {
					bytes[i] = Byte.parseByte(strBytes[i]);
				}
				BufferedImage bufferedImage = null;
				try {
					bufferedImage = ImageIO
							.read(new ByteArrayInputStream(bytes));
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (null != bufferedImage) {
					BufferedImage jpgBufferedImage = new BufferedImage(
							bufferedImage.getWidth(),
							bufferedImage.getHeight(),
							BufferedImage.TYPE_INT_RGB);
					jpgBufferedImage.createGraphics().drawImage(bufferedImage,
							0, 0, Color.WHITE, null);
					try {
						ImageIO.write(jpgBufferedImage, "jpg", new File(
								fileName));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		// png2jpg，并重绘图片
		if (super.png2jpg(task)) {
			super.drawMapContent(task);
		}
		return task;
	}

}
