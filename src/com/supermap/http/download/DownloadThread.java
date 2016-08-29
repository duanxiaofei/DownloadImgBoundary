/**
 * 
 */
package com.supermap.http.download;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Vector;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * �ֶ������߳�
 * @author ZYWANG
 */
public class DownloadThread extends Thread {
	private String url = "";
	private long startPosition = 0;
	private long endPosition = 0;
	private File file = null;
	private boolean isRange = true;
	
	private Vector<DownloadThreadListener> listeners = new Vector<DownloadThreadListener>();
	
	/**
	 * ���������߳�
	 * @param url Ŀ��URL
	 * @param startPosition ��ʼ�ֽ�λ��
	 * @param endPosition �����ֽ�λ��
	 * @param file �����ļ�λ��
	 */
	public DownloadThread(String url, long startPosition, long endPosition, File file) {
		this(url,startPosition,endPosition,file,true);
	}
	/**
	 * ���������߳�
	 * @param url Ŀ��URL
	 * @param startPosition ��ʼ�ֽ�λ��
	 * @param endPosition �����ֽ�λ��
	 * @param file �����ļ�λ��
	 * @param isRange �Ƿ�ʹ�÷ֶ�����
	 */
	public DownloadThread(String url, long startPosition, long endPosition, File file, boolean isRange) {
		this.url = url;
		this.startPosition = startPosition;
		this.endPosition = endPosition;
		this.file = file;
		this.isRange = isRange;
	}
	
	/**
	 * ���ڹ��̴���
	 */
	public void run() {
		if(DownloadTask.getDebug()){
			System.out.println("Start:" + startPosition + "-" +endPosition);
		}
		HttpClient httpClient = new DefaultHttpClient();
		try {
			HttpGet httpGet = new HttpGet(url);
			if(isRange){//���߳�����
				httpGet.addHeader("Range", "bytes="+startPosition+"-"+endPosition);
			}
			HttpResponse response = httpClient.execute(httpGet);
			int statusCode = response.getStatusLine().getStatusCode();
			if(DownloadTask.getDebug()){
				for(Header header : response.getAllHeaders()){
					System.out.println(header.getName()+":"+header.getValue());
				}
				System.out.println("statusCode:" + statusCode);
			}
			if(statusCode == 206 || (statusCode == 200 && !isRange)){
				InputStream inputStream = response.getEntity().getContent();
				//���������д��
				RandomAccessFile outputStream = new RandomAccessFile(file, "rw");
				//����ָ��λ��
				outputStream.seek(startPosition);
				int count = 0;byte[] buffer=new byte[1024];
				while((count = inputStream.read(buffer, 0, buffer.length))>0){
					outputStream.write(buffer, 0, count);
					//���������¼�
					fireAfterPerDown(new DownloadThreadEvent(this,count));
				}
				outputStream.close();
			}
			httpGet.abort();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			//������������¼�
			fireDownCompleted(new DownloadThreadEvent(this, endPosition));
			if(DownloadTask.getDebug()){
				System.out.println("End:" + startPosition + "-" +endPosition);
			}
			httpClient.getConnectionManager().shutdown();
		}
	}
	
	private void fireAfterPerDown(DownloadThreadEvent event){
		if(listeners.isEmpty()) return;
		 
		for(DownloadThreadListener listener:listeners){
			listener.afterPerDown(event);
		}
	}
	private void fireDownCompleted(DownloadThreadEvent event){
		if(listeners.isEmpty()) return;
		 
		for(DownloadThreadListener listener:listeners){
			listener.downCompleted(event);
		}
	}
	public void addDownloadListener(DownloadThreadListener listener){
		listeners.add(listener);
	}
}
