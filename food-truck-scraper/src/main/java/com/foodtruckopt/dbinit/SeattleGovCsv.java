package com.foodtruckopt.dbinit;

import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;

public class SeattleGovCsv {
	private static final Logger logger = Logger.getLogger(SeattleGovCsv.class);

	public static void main(String[] args) throws Exception {
		String flag = "plain";

		if (args.length == 1) {
			flag = args[0];
		}
		if (flag.equals("plain")) {
			Reader in = new FileReader("eventsdata.csv");
			Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);
			for (CSVRecord record : records) {
				if (!record.get(2).trim().isEmpty()) {
					String name = record.get(2).replace("'", "''");
					String date = record.get(0).replace("'", "''");
					String time = record.get(1).replace("'", "''");
					String address = record.get(4).replace("'", "''");
					System.out.println(String.format(
							"insert into event (name, date, time, address) values('%s', '%s', '%s', '%s');", name, date,
							time, address));
				}
			}
		} else if (flag.equals("complex")) {
			Reader in = new FileReader("eventsdata1.csv");
			Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);
			String currentStr = "";
			String name = "";
			String date = "";
			String time = "";
			String address = "";
			for (CSVRecord record : records) {
				if (!record.get(2).trim().isEmpty()) {
					name = record.get(2).replace("'", "''");
					date = record.get(0).replace("'", "''");
					time = record.get(1).replace("'", "''");
					address = record.get(4).replace("'", "''");
				} else if (!record.get(4).trim().isEmpty()) {
					address = address + "," + record.get(4);

				} else {
					if (!name.isEmpty()) {
						System.out.println(String.format(
								"insert into event (name, date, time, address) values('%s', '%s', '%s', '%s');", name,
								date, time, address));
						name = "";
						date = "";
						time = "";
						address = "";
					}
				}
			}
		} else if (flag.equals("foodtruck")) {
			Reader in = new FileReader("foodtruck-locations.csv");
			Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);

			for (CSVRecord record : records) {
				// System.out.println("record=" + record);
				if (!record.get(0).trim().isEmpty()) {
					String[] split = record.get(1).trim().split("\\@");
					String time = "";
					String address = "";
					if (split.length > 1) {
						time = split[0].trim().replace("'", "''");
						address = split[1].trim().replace("'", "''");
					}
					String name = record.get(2).trim().replace("'", "''");
					String date = record.get(0).replace("'", "''");
					;
					String type = record.get(3).trim().replace("'", "''");
					System.out.println(String.format(
							"insert into foodtruck (name, date, time, address, type) values('%s', '%s', '%s', '%s', '%s');",
							name, date, time, address, type));
					continue;
				}
			}
		} else if (flag.equals("prediction")) {
			Reader in = new FileReader("prediction.csv");
			Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);

			/*
			 * name character varying(100), "time" character varying(100),
			 * location character varying(100), footTraffic character
			 * varying(100), month character varying(100), longitude character
			 * varying(100), latitude character varying(100),
			 * predicted_customers character varying(100), predicted_if_raining
			 * character varying(100), predicted_if_event character varying(100)
			 */
			for (CSVRecord record : records) {
				if (!record.get(0).trim().isEmpty()) {
					System.out.println(String.format(
							"insert into predictions (name, time, location, footTraffic, month, longitude, latitude, predicted_customers, predicted_if_raining, predicted_if_event) values('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s');",
							record.get(0), record.get(1), record.get(2), record.get(3), record.get(4), record.get(5),
							record.get(6), record.get(7), record.get(8), record.get(9)));
				}
			}

		} else if (flag.equals("sample-data")) {
			Reader in = new FileReader("prediction.csv");
			Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);

			ArrayList<NameValue> nameValues = new ArrayList<NameValue>();

			for (CSVRecord record : records) {
				if (!record.get(0).trim().isEmpty()) {
					if (!record.get(1).trim().equals("Lunchtime")) {
						// System.out.println(record.get(2) + "," +
						// record.get(7));
						/*
						 * if(map.get(record.get(2)) == null){
						 * map.put(record.get(2),
						 * Integer.parseInt(record.get(7))); } else { int x =
						 * map.get(record.get(2)) +
						 * Integer.parseInt(record.get(7));
						 * map.put(record.get(2), x); }
						 */
						NameValue nameValue = new NameValue();
						nameValue.setName(record.get(2));
						nameValue.setValue(Integer.parseInt(record.get(7)));
						nameValues.add(nameValue);
					}
				}
			}

			Map<String, Double> map = nameValues.stream()
					.collect(Collectors.groupingBy(p -> p.getName(), Collectors.averagingInt(p -> p.getValue())));
			
			for (String key : map.keySet()) {
				System.out.println(key + "," + map.get(key));
			}
		}
	}
}
