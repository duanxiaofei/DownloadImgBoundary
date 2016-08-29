package com.supermap.iserver;

import javax.imageio.ImageIO;
import javax.naming.Context;
import javax.naming.NamingException;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import com.supermap.context.RestServer;
import com.supermap.context.RestService;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by duanxiaofei on 2016/3/16.
 */
public abstract class BaseImg {
    public enum Level {
	    COUNTRY(1),PROVINCE(2),CITY(3),DISTRICT(4),TOWN(5),VILLAGE(6),RQ(7);
	    public int value;
	    public String name;
	    Level(int value){
	        this.value=value;
	    }

	    public int getValue() {
	        return value;
	    }
	}
    
	/**
	 * 从iserver服务器获取聚合地图数据
	 * 
	 * @param fileName
	 *            本地存储的地图文件名
	 * @param result
	 *            iserver返回的数据
	 * @return
	 */
	public abstract Task getImgFromServer(Task task, String result);

	/**
	 * png转jpg
	 * 
	 * @param fileName
	 * @return
	 */
	public boolean png2jpg(Task task) {
		String fileName = task.getFileName();
		boolean r = true;
		try {
			BufferedImage bufferedImage = ImageIO.read(new File(fileName));
			BufferedImage jpgBufferedImage = new BufferedImage(
					bufferedImage.getWidth(), bufferedImage.getHeight(),
					BufferedImage.TYPE_INT_RGB);
			jpgBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0,
					Color.WHITE, null);
			fileName = fileName.replace("png", "jpg");
			ImageIO.write(jpgBufferedImage, "jpg", new File(fileName));
			task.setFileName(fileName);
		} catch (Exception e) {
			r = false;
		}
		return r;
	}

	/**
	 * 地图自定义处理
	 * 
	 * @param fileName
	 * @param name
	 * @param code
	 * @param mapLevel
	 * @return
	 */
	public void drawMapContent(Task task) {
		String fileName = task.getFileName();
		int mapLevel = task.getMapLevel();
		String name = task.getName();
		String code = task.getCode();
		RestServer server = new RestServer();
		Context ctx = server.getContext();
		RestService service = null;
		try {
			service = (RestService) ctx.lookup("RestService");
		} catch (NamingException e) {
			e.printStackTrace();
		}
		String resolutions = service.getGoogleMapsCompatibleResolutions();
		String[] a = resolutions.split(",");
		double[] d = new double[a.length];
		for (int i = 0; i < a.length; i++) {
			d[i] = Double.valueOf(a[i]);
		}
		File map = new File(fileName);
		try {
			// 获取原图
			Image mapImg = ImageIO.read(map);
			int width = mapImg.getWidth(null);
			int height = mapImg.getHeight(null);
			BufferedImage image = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_RGB);
			Graphics g = image.createGraphics();
			g.drawImage(mapImg, 0, 0, width, height, null);
			// 绘制指南针
			InputStream compassIn=this.getClass().getResourceAsStream("compass.png");
			Image compassImg = ImageIO.read(compassIn);
			int compassWidth = compassImg.getWidth(null);
			int compassHeigth = compassImg.getHeight(null);
			g.drawImage(compassImg, width - compassHeigth - 20, 20,
					compassWidth, compassHeigth, null);
			// 绘制标题
			String title = name + "(" + code + ")";
			g.setFont(new Font("default", Font.BOLD, 30));
			g.setColor(Color.RED);
			g.drawString(title, width / 2 - title.length(), 40);
			// 绘制打印日期
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			g.setFont(new Font("default", Font.BOLD, 15));
			g.setColor(Color.BLACK);
			String dateString = "打印日期：" + dateFormat.format(new Date());
			g.drawString(dateString, width - 200, height - 50);
			// 绘制比例尺
			int scaleLength;
			int scale = 20;
			if (mapLevel == 19) {
				scale = 20;
			} else if (mapLevel == 18) {
				scale = 100;
			} else if (mapLevel == 17) {
				scale = 100;
			} else if (mapLevel == 16) {
				scale = 200;
			} else if (mapLevel == 15) {
				scale = 500;
			}
			scaleLength = (int) Math.ceil(scale / d[mapLevel - 1]);
			g.drawLine(40, height - 50, 40 + scaleLength, height - 50);
			g.drawLine(40, height - 50, 40, height - 60);
			g.drawLine(40 + scaleLength, height - 50, 40 + scaleLength,
					height - 60);
			g.setFont(new Font("default", Font.BOLD, 12));
			g.drawString(String.valueOf(scale) + " m", 40 + scaleLength / 4,
					height - 60);

			g.dispose();
			FileOutputStream out = new FileOutputStream(fileName);
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
			encoder.encode(image);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 删除转换之前的PNG
	 * 
	 * @param filePath
	 */
	protected void deleteFile(String filePath) {
		File f = new File(filePath);
		if (f.exists()) {
			f.delete();
		}
	}
	
	/**
	 * 计算图片路径
	 */
    public String getFileDir(String code) {
        String path = "";
        String parCode = this.getParentCode(code);
        int level = this.toLevel(code);
        while(level > 2) {   
        	//2：市区划等级
            path = parCode + File.separator + path;
            parCode = this.getParentCode(parCode);
            level--;
        }
        return path;
    }
    
    /**
     * 计算区划级别
     * @param regionCode
     * @return
     */
    public int toLevel(final String regionCode){
        if(regionCode.length() > 12) {      //小区
            return  Level.RQ.value;
        } else if (regionCode.substring(0, 2).equals("00")){ //全国
            return Level.COUNTRY.value;
        }else if (regionCode.substring(2, 4).equals("00")) { //省
            return Level.PROVINCE.value;
        } else if (regionCode.substring(4).equals("00000000")) {// 市
            return Level.CITY.value;
        } else if (regionCode.substring(6).equals("000000")) {// 区县
            return Level.DISTRICT.value;
        } else if (regionCode.substring(9).equals("000")) {// 乡镇
            return Level.TOWN.value;
        } else { //村
            return Level.VILLAGE.value;
        }
    }
    
    /**
     * 计算上级区划
     * @param code
     * @return
     */
    public  String getParentCode(String code) {
        String parentCode = "";
        int level = this.toLevel(code);
        switch (level) {
            case 1:
                break;
            case 2:
                parentCode = "00000000000";
                break;
            case 3:
                parentCode = code.substring(0, 2) + "0000000000";
                break;
            case 4:
                parentCode = code.substring(0, 4) + "00000000";
                break;
            case 5:
                parentCode = code.substring(0, 6) + "000000";
                break;
            case 6:
                parentCode = code.substring(0, 9) + "000";
                break;
            case 7:
                parentCode = code.substring(0, 12);
                break;
        }
        return parentCode;
    }
}
