package com.foodtruckopt.dbinit;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class Scripts{
	
	private Long version;

    private List<Script> scriptList = new ArrayList<Script>();

	public void setVersion(Long version) {
		this.version = version;
	}

	@XmlAttribute(name = "Version")
	public Long getVersion() {
		return version;
	}

	public void setScriptList(List<Script> scripts) {
		this.scriptList = scripts;
	}

    @XmlElement(name = "Script", required = true)
	public List<Script> getScriptList() {
		return scriptList;
	}
}
