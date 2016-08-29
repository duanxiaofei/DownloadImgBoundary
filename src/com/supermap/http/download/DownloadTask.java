/**
 * 
 */
package com.supermap.http.download;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * @author ZYWANG
 *
 */
public class DownloadTask {
	private String url = "";
	private int threadCount = 5;
	private String localPath = "";
	
	private boolean acceptRanges = false;
//	private String ranges = "";
	private long contentLength = 0;
	private long receivedCount = 0;
	private Vector<DownloadThread> threads = new Vector<DownloadThread>();
	
	private long lastCount = 0;
	private long beginTime = 0;
	private long endTime = 0;
	private Object object = new Object();
	private long autoCallbackSleep = 1000;
	
	private Vector<DownloadTaskListener> listeners = new Vector<DownloadTaskListener>();
	
	private static boolean DEBUG = false;
	/**
	 * ���������������
	 * @param url Ŀ���ַ
	 */
	@Deprecated
	public DownloadTask(String url) {
		this(url,5);
	}
	/**
	 * ���������������
	 * @param url Ŀ���ַ
	 * @param threadCount �߳�����
	 */
	@Deprecated
	public DownloadTask(String url, int threadCount) {
		this(url,"",threadCount);
	}
	/**
	 * ���������������
	 * @param url Ŀ���ַ
	 * @param localPath ���ر���·��
	 */
	public DownloadTask(String url, String localPath) {
		this(url,localPath,5);
	}
	/**
	 * ���������������
	 * @param url Ŀ���ַ
	 * @param localPath ���ر���·��
	 * @param threadCount �߳�����
	 */
	public DownloadTask(String url, String localPath, int threadCount) {
		this.url = url;
		this.threadCount = threadCount;
		this.localPath = localPath;
	}
	
	public void setAutoCallbackSleep(long autoCallbackSleep) {
		this.autoCallbackSleep = autoCallbackSleep;
	}
	public long getAutoCallbackSleep() {
		return this.autoCallbackSleep;
	}
	
	public static void setDebug(boolean debug){
		DEBUG = debug;
	}
	public static boolean getDebug(){
		return DEBUG;
	}
	
	public void setLocalPath(String localPath){
		this.localPath = localPath;
	}
	
	/**
	 * ��ʼ����
	 * @throws Exception
	 */
	public void startDown() throws Exception{
		HttpClient httpClient = new DefaultHttpClient();
		try {
			//��ȡ�����ļ���Ϣ
			getDownloadFileInfo(httpClient);
			//������������߳�
			startDownloadThread();
			//��ʼ������������
			monitor();
		} catch (Exception e) {
			throw e;
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
	}

	/**
	 * ��ȡ�����ļ���Ϣ
	 */
	private void getDownloadFileInfo(HttpClient httpClient) throws IOException,
			ClientProtocolException, Exception {
		HttpHead httpHead = new HttpHead(url);
		HttpResponse response = httpClient.execute(httpHead);
		//��ȡHTTP״̬��
		int statusCode = response.getStatusLine().getStatusCode();

		if(statusCode != 200) throw new Exception("��Դ������!");
		if(getDebug()){
			for(Header header : response.getAllHeaders()){
				System.out.println(header.getName()+":"+header.getValue());
			}
		}

		//Content-Length
		Header[] headers = response.getHeaders("Content-Length");
		if(headers.length > 0)
			contentLength = Long.valueOf(headers[0].getValue());
		//Accept-Ranges
//		headers = response.getHeaders("Accept-Ranges");
//		if(headers.length > 0)
//			acceptRanges = true;

		httpHead.abort();
		
//		if(!acceptRanges){
			httpHead = new HttpHead(url);
			httpHead.addHeader("Range", "bytes=0-"+contentLength);
			response = httpClient.execute(httpHead);
			if(response.getStatusLine().getStatusCode() == 206){
				acceptRanges = true;
			}
			httpHead.abort();
//		}
	}

	/**
	 * ������������߳�
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private void startDownloadThread() throws IOException,
			FileNotFoundException {
		//���������ļ�
		File file = new File(localPath);
		file.createNewFile();
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		raf.setLength(contentLength);
		raf.close();
		
		//���������߳��¼�ʵ����
		DownloadThreadListener listener = new DownloadThreadListener() {
			public void afterPerDown(DownloadThreadEvent event) {
				//������һ��Ƭ�κ�׷���������ֽ���
				synchronized (object) {
					DownloadTask.this.receivedCount += event.getCount();
				}
			}

			public void downCompleted(DownloadThreadEvent event) {
				//�����߳�ִ����Ϻ�����������Ƴ�
				threads.remove(event.getTarget());
				if(getDebug()){
					System.out.println("ʣ���߳�����"+threads.size());
				}
			}
		};
		
		//��֧�ֶ��߳�����ʱ
		if (!acceptRanges) {
			if(getDebug()){
				System.out.println("�õ�ַ��֧�ֶ��߳�����");
			}
			//������ͨ����
			DownloadThread thread = new DownloadThread(url, 0, contentLength, file, false);
			thread.addDownloadListener(listener);
			thread.start();
			threads.add(thread);
			return;
		}
		
		//ÿ������Ĵ�С
		long perThreadLength = contentLength / threadCount + 1;
		long startPosition = 0;
		long endPosition = perThreadLength;
		//ѭ��������������߳�
		do{
			if(endPosition > contentLength)
				endPosition = contentLength;

			DownloadThread thread = new DownloadThread(url, startPosition, endPosition, file);
			thread.addDownloadListener(listener);
			thread.start();
			threads.add(thread);

			startPosition = endPosition + 1;//�˴��� 1,�ӽ���λ�õ���һ���ط���ʼ����
			endPosition += perThreadLength;
		} while (startPosition < contentLength);
	}
	
	/**
	 * �����������ع���
	 */
	private void monitor() {
		new Thread() {
			public void run() {
				beginTime = System.currentTimeMillis();
				//���������ֽ���>=�����ֽ��� ���� �����̶߳��ѹرյ�ʱ�����ѭ��
				while(receivedCount < contentLength && !threads.isEmpty()) {
					showInfo(false);
					
					try {
						Thread.sleep(autoCallbackSleep);
					} catch (InterruptedException e) { }
				}
				showInfo(true);
			}
		}.start();
	}

	/**
	 * ���������Ϣ�������¼�
	 */
	private void showInfo(boolean complete) {
		long currentTime = System.currentTimeMillis();
		double realTimeSpeed = (receivedCount - lastCount) * 1.0 / ((currentTime - endTime) / 1000.0);
		double globalSpeed = receivedCount * 1.0 / ((currentTime - beginTime) / 1000.0);
		lastCount = receivedCount;
		endTime = currentTime;
		//�������ؽ��Ȼص��¼�
		fireAutoCallback(new DownloadTaskEvent(receivedCount, contentLength, formatSpeed(realTimeSpeed), formatSpeed(globalSpeed),complete));
	};
	
	private void fireAutoCallback(DownloadTaskEvent event){
		if(listeners.isEmpty()) return;
		
		for(DownloadTaskListener listener:listeners){
			listener.autoCallback(event);
		}
	}
	public void addTaskListener(DownloadTaskListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * ���Ա������ļ�������
	 */
	public String guessFileName() throws Exception{
		HttpClient httpClient = new DefaultHttpClient();
		try {
			HttpHead httpHead = new HttpHead(url);
			HttpResponse response = httpClient.execute(httpHead);
			String contentDisposition = null;
			if(response.getStatusLine().getStatusCode() == 200){
				//Content-Disposition
				Header[] headers = response.getHeaders("Content-Disposition");
				if(headers.length > 0)
					contentDisposition = headers[0].getValue();
			}
			httpHead.abort();
			
			if (contentDisposition!=null && contentDisposition.startsWith("attachment")) {
				return contentDisposition.substring(contentDisposition.indexOf("=")+1);
			} else if (Pattern.compile("(/|=)([^/&?]+\\.[a-zA-Z]+)").matcher(url).find()) {
				Matcher matcher = Pattern.compile("(/|=)([^/&?]+\\.[a-zA-Z]+)").matcher(url);
				String s = "";
				while(matcher.find())
					//�����һ��URL�ϵĿ����ļ�����Ϊ���β²�Ľ��
					s = matcher.group(2);
				return s;
			}
		} catch (Exception e) {
			throw e;
		}finally{
			httpClient.getConnectionManager().shutdown();
		}
		return "UnknowName.temp";
	}
	
	/**
	 * ��ʽ�����ؽ��� Ϊ B/s,K/s,M/s,G/s,T/s
	 */
	private String formatSpeed(double speed){
		DecimalFormat format = new DecimalFormat("#,##0.##");
		if(speed<1024){
			return format.format(speed)+" B/s";
		}
		
		speed /= 1024;
		if(speed<1024){
			return format.format(speed)+" K/s";
		}
		
		speed /= 1024;
		if(speed<1024){
			return format.format(speed)+" M/s";
		}
		
		speed /= 1024;
		if(speed<1024){
			return format.format(speed)+" G/s";
		}
		
		speed /= 1024;
		if(speed<1024){
			return format.format(speed)+" T/s";
		}
		
		return format.format(speed) + "B/s";
	}

}
