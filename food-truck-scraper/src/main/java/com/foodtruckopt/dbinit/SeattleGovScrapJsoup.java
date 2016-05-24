package com.foodtruckopt.dbinit;

import java.net.URL;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class SeattleGovScrapJsoup {
	private static final Logger logger = Logger.getLogger(SeattleGovScrapJsoup.class);

	public static void main(String[] args) throws Exception {
		// or if you prefer DOM:
		URL url = new URL("http://www.seattle.gov/event-calendar");

		Document doc = Jsoup.parse(url, 50000);
		
		System.out.println("doc=" + doc.html());
		Element elm = doc.select("table").first();
		System.out.println("elm=" + elm.html());
		for (Element row : elm.select("tr")) {
			System.out.println("row=" + row.html());
			
		}
	}
}
