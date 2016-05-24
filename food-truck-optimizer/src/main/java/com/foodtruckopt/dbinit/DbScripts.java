package com.foodtruckopt.dbinit;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "DbScripts")
public class DbScripts {

    private List<Scripts> scripts;

	@XmlElement(name = "Scripts", required = true)
	public List<Scripts> getScripts() {
		return scripts;
	}
	
	public void setScripts(List<Scripts> scripts) {
		this.scripts = scripts;
	}

}
