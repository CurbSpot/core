package com.foodtruckopt.dto;

import lombok.Data;

@Data
public class UiForm {
	private final int id;
	
	private final String formTableName;

	private final String displayName;
	
	private final int orderBy;
	
	private final String nameColumnDisplayName;
	
	private final String groupBy;
}
