package com.supermap.context;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;


public class SimpleRestService implements Referenceable, RestService {

	private String villageRestUrl;
	private String townRestUrl;
	private String countryRestUrl;
	private int overviewLevel;
	private String googleMapsCompatibleResolutions;
	private int widthExt;
	private int heightExt;
	private boolean tianditu;
	
	@Override
	public Reference getReference() throws NamingException {
		  Reference ref=new Reference(getClass().getName(),RestServiceFactory.class.getName(),null);
		  ref.add(new StringRefAddr("villageRestUrl",this.villageRestUrl));
		  ref.add(new StringRefAddr("townRestUrl",this.townRestUrl));
		  ref.add(new StringRefAddr("countryRestUrl",this.countryRestUrl));
		  ref.add(new StringRefAddr("overviewLevel",String.valueOf(this.overviewLevel)));
		  ref.add(new StringRefAddr("googleMapsCompatibleResolutions",this.googleMapsCompatibleResolutions));
		  ref.add(new StringRefAddr("widthExt",String.valueOf(this.widthExt)));
		  ref.add(new StringRefAddr("heightExt",String.valueOf(this.heightExt)));
		  ref.add(new StringRefAddr("tianditu",String.valueOf(this.tianditu)));
		  return ref;
	}


	@Override
	public String getVillageRestUrl() {
		return this.villageRestUrl;
	}


	@Override
	public void setVillageRestUrl(String restUrl) {
		this.villageRestUrl=restUrl;
	}


	@Override
	public String getTownRestUrl() {
		return this.townRestUrl;
	}


	@Override
	public void setTownRestUrl(String restUrl) {
		this.townRestUrl=restUrl;
	}


	@Override
	public String getCountryRestUrl() {
		return this.countryRestUrl;
	}


	@Override
	public void setCountryRestUrl(String restUrl) {
		this.countryRestUrl=restUrl;
	}


	@Override
	public int getOverviewLevel() {
		return this.overviewLevel;
	}


	@Override
	public void setOverviewLevel(int level) {
		this.overviewLevel=level;
	}


	@Override
	public String getGoogleMapsCompatibleResolutions() {
		return this.googleMapsCompatibleResolutions;
	}


	@Override
	public void setGoogleMapsCompatibleResolutions(String resolutions) {
		this.googleMapsCompatibleResolutions=resolutions;
	}


	@Override
	public int getWidthExt() {
		return this.widthExt;
	}


	@Override
	public void setWidthExt(int ext) {
		this.widthExt=ext;
	}


	@Override
	public int getHeightExt() {
		return this.heightExt;
	}


	@Override
	public void setHeightExt(int ext) {
		this.heightExt=ext;
	}

	@Override
	public boolean isTianditu() {
		return tianditu;
	}

	@Override
	public void setTianditu(boolean tianditu) {
		this.tianditu = tianditu;
	}

}
