package com.foodtruckopt.dbinit;

import java.net.URL;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class SeattleGovScrap {
	private static final Logger logger = Logger.getLogger(SeattleGovScrap.class);

	public static void main(String[] args) throws Exception {
		// or if you prefer DOM:
		URL url = new URL("http://www.trumba.com/calendars/seattle-events-weekend-events.xml");

		XmlReader reader = null;

		try {
			reader = new XmlReader(url);
			SyndFeed feed = new SyndFeedInput().build(reader);
			System.out.println("Feed Title: " + feed.getAuthor());

			for (Iterator i = feed.getEntries().iterator(); i.hasNext();) {
				SyndEntry entry = (SyndEntry) i.next();
				System.out.println(entry.getTitle());
				for(Object st: entry.getContents()){
					SyndContentImpl data = (SyndContentImpl)st;
					String val = data.getValue();
					String[] split = val.split("<br />");
					System.out.println("***start");
					for (String sp : split){
						//System.out.println(sp);
						
						String[] split2 = sp.split("<br/>");
						for (String sp1 : split2){
							System.out.println(sp1);
						}
						
					}
					System.out.println("***end");
				}
			}
		} finally {
			if (reader != null)
				reader.close();
		}
	}
}
