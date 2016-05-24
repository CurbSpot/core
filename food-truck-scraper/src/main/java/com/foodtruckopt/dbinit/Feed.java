package com.foodtruckopt.dbinit;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "feed")
public class Feed {

    private List<Entry> entries;

	@XmlElement(name = "entry", required = true)
	public List<Entry> getEntry() {
		return entries;
	}
	
	public void setEntry(List<Entry> entries) {
		this.entries= entries;
	}

}
