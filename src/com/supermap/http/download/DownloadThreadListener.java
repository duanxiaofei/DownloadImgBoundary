/**
 * 
 */
package com.supermap.http.download;

/**
 * �����߳��¼�
 * @author ZYWANG
 */
public interface DownloadThreadListener {

	/**
	 * ÿ��������һ���ֽ�����󴥷�
	 * @param event
	 */
	public void afterPerDown(DownloadThreadEvent event);
	
	/**
	 * �������ʱ����
	 * @param event
	 */
	public void downCompleted(DownloadThreadEvent event);
}
