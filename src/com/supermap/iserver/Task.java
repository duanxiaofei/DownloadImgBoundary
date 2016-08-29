package com.supermap.iserver;

public class Task {
	int id;
	String code;
	String name;
	int mapLevel;
	int areaLeve;
	String bounds;
	String fileName;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getMapLevel() {
		return mapLevel;
	}
	public void setMapLevel(int mapLevel) {
		this.mapLevel = mapLevel;
	}
	public int getAreaLeve() {
		return areaLeve;
	}
	public void setAreaLeve(int areaLeve) {
		this.areaLeve = areaLeve;
	}
	public String getBounds() {
		return bounds;
	}
	public void setBounds(String bounds) {
		this.bounds = bounds;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
}
