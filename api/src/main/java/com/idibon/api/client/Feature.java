package com.idibon.api.client;

import java.util.HashMap;

public class Feature {

	/*
	 * private HashMap<String, LinkedTreeMap<String, Object>> parameters;
	 * private String name;
	 * 
	 * public void setName(String name){ this.name = name; }
	 * 
	 * public String getName(){ return this.name; }
	 * 
	 * public void setParameters(String paramname, String type, String
	 * defaultval, boolean required){ LinkedTreeMap<String, Object> map = new
	 * LinkedTreeMap<String, Object>(); map.put("type", type);
	 * map.put("default", defaultval); map.put("required", required);
	 * this.parameters.put(paramname, map); }
	 * 
	 * public void setParameters(HashMap<String, LinkedTreeMap<String, Object>>
	 * map){ this.parameters = map; }
	 * 
	 * public HashMap<String, LinkedTreeMap<String, Object>> getParameters(){
	 * return this.parameters; }
	 * 
	 * public HashMap<String, Object> getAttributes(String paramname){
	 * HashMap<String, Object> map = new HashMap<String, Object>();
	 * map.put("type", this.parameters.get(paramname).get("type"));
	 * map.put("default", this.parameters.get(paramname).get("default"));
	 * map.put("required", this.parameters.get(paramname).get("required"));
	 * return map; }
	 */

	private HashMap<String, FeatureParameter> parameters;
	private String name;

	public void setName(String name) {
		this.name = name;
	}

	public void setParameters(HashMap<String, FeatureParameter> parameters) {
		this.parameters = parameters;
	}

	public HashMap<String, FeatureParameter> getParameters() {
		return this.parameters;
	}

	public String getName() {
		return this.name;
	}
}
