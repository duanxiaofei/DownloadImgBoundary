package com.supermap.context;

public interface RestService {
	String getVillageRestUrl();
	void setVillageRestUrl(String restUrl);
	String getTownRestUrl();
	void setTownRestUrl(String restUrl);
	String getCountryRestUrl();
	void setCountryRestUrl(String restUrl);
	int getOverviewLevel();
	void setOverviewLevel(int level);
	String getGoogleMapsCompatibleResolutions();
	void setGoogleMapsCompatibleResolutions(String resolutions);
	int getWidthExt();
	void setWidthExt(int ext);
	int getHeightExt();
	void setHeightExt(int ext);
	
}
