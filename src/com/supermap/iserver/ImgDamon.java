package com.supermap.iserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 创建一个下载图的后台线程，随部署包一起启动
 * 
 * @author duanxiaofei
 * 
 */
public class ImgDamon implements ServletContextListener {

	Logger ok = LoggerFactory.getLogger("OK");
	Logger error = LoggerFactory.getLogger("ERROR");
	Logger console = LoggerFactory.getLogger("console");
	
	private Thread thread;

	public static void main(String[] args) {
		new ImgDamon().process();
	}

	public void process() {
		if (thread == null) {
			thread = new Thread(new Runnable() {
				@Override
				public void run() {
					while (true) {
						String taskPath = System.getProperty("catalina.home") + File.separator
								+ "webapps" + File.separator + "task";
						File taskDir = new File(taskPath);
						if(!taskDir.exists()){
							taskDir.mkdirs();
						}
						String[] tasks = taskDir.list();
						for (String task : tasks) {
							File file = new File(taskPath, task);
							RandomAccessFile raf = null;
							try {
								raf = new RandomAccessFile(file, "rw");
							} catch (FileNotFoundException e1) {
								e1.printStackTrace();
							}
							FileChannel fc = raf.getChannel();
							FileLock fl = null;
							try {
								fl = fc.tryLock();
								if (fl.isValid()) {
									ok.info("-->");
									ok.info("Get the lock successed!");
									ok.info("开始读取任务数据：" + task);

									String content = raf.readLine();
									try {
										fl.release();
										raf.close();
									} catch (IOException e) {
										e.printStackTrace();
									}
									if (content == null
											|| content.length() == 0) {
										error.info(content);
									} else {
										content = new String(content
												.getBytes("ISO-8859-1"),
												"UTF-8");// 编码转换
										ok.info(content);
									}
									file.delete();
									ok.info("release the lock!");
									if (content != null && content.length() > 0) {
										String[] a = content.split("\\|");
										BaseImg img = new ImgDownler();
										Task t = new Task();
										t.setName(a[0]);
										t.setCode(a[1]);
										t.setMapLevel(Integer.valueOf(a[2]));
										t.setAreaLeve(Integer.valueOf(a[3]));
										t.setBounds(a[4]);
										t = img.getImgFromServer(t,
												Resource.getResult(t));

										if (t == null
												|| t.getFileName() == null) {
											error.info("任务失败：" + task);
										} else {
											ok.info("任务完成。");
										}
									} else {
										error.info("任务失败：" + task);
									}
								}
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
			});
			//thread.setDaemon(true);
			thread.start();
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		if (thread != null) {
			thread.stop();
		}
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		console.info("下图插件开始启动。");
		new ImgDamon().process();
	}

}
