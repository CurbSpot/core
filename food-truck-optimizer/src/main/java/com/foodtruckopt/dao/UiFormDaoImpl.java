package com.foodtruckopt.dao;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.foodtruckopt.dto.ColumnDTO;
import com.foodtruckopt.dto.UiForm;
import com.foodtruckopt.dto.UiFormLink;
import com.foodtruckopt.util.Page;
import com.foodtruckopt.util.PaginationHelper;

import lombok.val;

@Component
public class UiFormDaoImpl implements UiFormDao {
	private static Logger logger = Logger.getLogger(UiFormDaoImpl.class);
	SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");

	private JdbcTemplate jdbcTemplate;
	private PlatformTransactionManager transactionManager;

	@Autowired
	public UiFormDaoImpl(JdbcTemplate jdbcTemplate, PlatformTransactionManager transactionManager) {
		logger.debug(
				"########################   Initiated......      #####################################################");
		this.jdbcTemplate = jdbcTemplate;
		this.transactionManager = transactionManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cs.dao.UiFormDao#getFormList()
	 */
	@Override
	public List<UiForm> getFormList(String appName) {
		String sql = "select * from ui_form order by order_by";
		return jdbcTemplate.query(sql, (rs, rowNum) -> getUiForm(rs));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cs.dao.UiFormDao#getFormData(int,
	 * com.cs.dto.UiForm, java.util.List)
	 */
	@Override
	public Map getFormData(String appName, int dataId, UiForm form, List<UiFormLink> formLinks) throws SQLException {
		val json = new LinkedHashMap();
		getFormDataHeader(dataId, form, json);

		formLinks.forEach(x -> getFormLinkData(dataId, form, x, json));

		Map json2 = null;
		if (dataId != 0) {
			json2 = new LinkedHashMap();
			json2.put("type", "checkbox");
			json2.put("label", "Save As New");
			json2.put("val", false);
			json.put("save_as_new", json2);
		}

		json2 = new LinkedHashMap();
		json2.put("type", "submit");
		json2.put("label", "Submit");

		json.put("submit", json2);
		return json;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cs.dao.UiFormDao#getFormInfo(int)
	 */
	@Override
	public UiForm getFormInfo(String appName, int formId) {
		val sql = "select * from ui_form where id=?";
		return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> getUiForm(rs), formId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cs.dao.UiFormDao#getFormLinkInfo(int)
	 */
	@Override
	public List<UiFormLink> getFormLinkInfo(String appName, int formId) {
		val sql = "select * from ui_form_link where ui_form_id=?";
		return jdbcTemplate.query(sql, (rs, rowNum) -> getUiFormLink(rs), formId);
	}

	private UiForm getUiForm(ResultSet rs) throws SQLException {
		return new UiForm(rs.getInt("id"), rs.getString("form_table_name"), rs.getString("display_name"),
				rs.getInt("order_by"), rs.getString("name_column_display_name"), rs.getString("group_by"));
	}

	private String getDisplayName(String name) {
		// TODO Auto-generated method stub
		val split = name.split("_");
		val res = new StringBuffer();

		for (String str : split) {
			char[] stringArray = str.trim().toCharArray();
			if (stringArray.length > 0) {
				stringArray[0] = Character.toUpperCase(stringArray[0]);
				str = new String(stringArray);
			}
			res.append(str).append(" ");
		}

		return res.toString();
	}

	private UiFormLink getUiFormLink(ResultSet rs) throws SQLException {
		return new UiFormLink(rs.getInt("ui_form_id"), rs.getInt("ui_form_link_id"), rs.getString("link_name"),
				rs.getBoolean("single_select"));
	}

	private void getFormLinkData(int dataId, UiForm form, UiFormLink formLink, Map json) {
		val sql = "select * from ui_form where id=?";

		val list = jdbcTemplate.query(sql,
				(rs, rowNum) -> rs.getString("form_table_name") + "#" + rs.getString("display_name"),
				formLink.getUiFormLinkId());

		val list1 = new ArrayList<LinkedHashMap>();
		list.forEach(x -> {
			String[] st = x.split("#");
			System.out.println("*********x=" + x + " formLink.getLinkName()" + formLink.getLinkName()
					+ " formLink.getSingleSelect()" + formLink.getSingleSelect());

			getSelectedData(jdbcTemplate, form, st[0], dataId, st[1], formLink.getLinkName(),
					formLink.getSingleSelect(), json);
		});
	}

	private void getSelectedData(JdbcTemplate jdbcTemplate, UiForm form, String joinTableName, int dataId,
			String displayName, String relationshipName, Boolean singleSelect, Map root) {
		// Get Entire list
		String relationshipName1 = (relationshipName == null || relationshipName.isEmpty()) ? joinTableName
				: relationshipName;
		String displayName1 = (relationshipName == null || relationshipName.isEmpty()) ? displayName
				: convertToDisplayValue(relationshipName);
		val relnTableList = getRelationsshipDataList(jdbcTemplate, joinTableName);

		val sql = String.format(
				"select x.id, x.name from %s as x, %s_%s_relationship as y where y.%s_id=x.id and y.%s_id=?",
				joinTableName, form.getFormTableName(), relationshipName1, joinTableName, form.getFormTableName());
		System.out.println("*********sql=" + sql);
		List<String> relnList1 = new ArrayList<String>();
		if (dataId != 0) {
			relnList1 = jdbcTemplate.query(sql, (rs, rowNum) -> {
				return rs.getObject(1) + "#" + rs.getObject(2);
			}, dataId);
		}

		val relnList = relnList1;
		val optionsJsonList = new ArrayList<LinkedHashMap>();
		// None selected
		val json1 = new LinkedHashMap();
		val selData = new ArrayList<String>();
		relnTableList.forEach(x -> {
			String split[] = x.split("#");

			Map jsonLoc = new LinkedHashMap();
			jsonLoc.put("label", split[1]);
			if (!relnList.isEmpty() && relnList.contains(x)) {
				// TODO: currently we assume it is always returing one rows. we
				// need to change this
				// One value selected
				selData.add(x);
			}

			json1.put(x, jsonLoc);
		});

		val relnTableJson = new LinkedHashMap();
		relnTableJson.put("type", "select");
		relnTableJson.put("label", displayName1);
		relnTableJson.put("options", json1);
		relnTableJson.put("multiple", !singleSelect);
		if (!selData.isEmpty()) {
			if (singleSelect) {
				relnTableJson.put("val", selData.get(0));
			} else {
				relnTableJson.put("val", selData);
			}
		}
		root.put(relationshipName1, relnTableJson);
	}

	private String convertToDisplayValue(String relationshipName) {
		// TODO Auto-generated method stub
		String[] split = relationshipName.split("_");
		String out = "";
		for (String st : split) {
			out += (upperCaseFirst(st) + " ");
		}
		return out.trim();
	}

	public static String upperCaseFirst(String value) {

		char[] array = value.toCharArray();
		array[0] = Character.toUpperCase(array[0]);
		return new String(array);
	}

	private List<String> getRelationsshipDataList(JdbcTemplate jdbcTemplate, String tableName) {
		// String sql2 = "select name from " + tableName;
		val sql = String.format("select id, name from %s order by name ASC", tableName);
		List<String> list = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString(1) + "#" + rs.getString(2));

		list.sort((o1, o2) -> getByIndex(o1, "#", 1).compareTo(getByIndex(o2, "#", 1)));

		return list;
	}

	private void getFormDataHeader(int dataId, UiForm form, Map json) throws SQLException {
		if (dataId != 0) {
			val sql = String.format("select * from %s where id=?", form.getFormTableName());
			jdbcTemplate.query(sql, (rs, rowNum) -> {
				ResultSetMetaData rsmd = rs.getMetaData();
				for (int i = 1; i < rsmd.getColumnCount() + 1; i++) {
					String name = rsmd.getColumnName(i).toLowerCase();
					if (!name.equals("id")) {
						Map innerJson = new LinkedHashMap();
						if (!name.contains("_id")) {
							innerJson.put("type", getType(rsmd.getColumnTypeName(i).toLowerCase(), name));
							innerJson.put("required", rsmd.isNullable(i) == ResultSetMetaData.columnNoNulls);

							if (String.valueOf(getType(rsmd.getColumnTypeName(i).toLowerCase(), name))
									.equalsIgnoreCase("date")) {
								innerJson.put("val", rs.getObject(i) == null ? "" : sdf.format(rs.getObject(i)));
							} else {
								innerJson.put("val", rs.getObject(i));
							}

							if (name.equals("name")) {
								innerJson.put("label", getNameColumnDisplayName(form));
							} else {
								innerJson.put("label", getDisplayName(name));

							}
						} else {
							// Check if the column name is same as Table name
							innerJson.put("val", rs.getObject(i));
							fillList(name, innerJson, form);
							innerJson.put("required", rsmd.isNullable(i) == ResultSetMetaData.columnNoNulls);
						}
						json.put(name, innerJson);
					}
				}
				return "";
			}, dataId);
		} else {
			val sql = String.format("select * from %s", form.getFormTableName());
			generateCreateJson(sql, json, form);
		}
	}

	private String getNameColumnDisplayName(UiForm form) {
		if (form.getNameColumnDisplayName() != null && !form.getNameColumnDisplayName().isEmpty()) {
			return form.getNameColumnDisplayName();
		} else {
			return "Name";
		}
	}

	private void generateCreateJson(String sql, Map json, UiForm form) throws SQLException {
		try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
			val stmt = connection.createStatement();
			val rs = stmt.executeQuery(sql);
			val rsmd = rs.getMetaData();

			for (int i = 1; i < rsmd.getColumnCount() + 1; i++) {
				String name = rsmd.getColumnName(i).toLowerCase();
				if (!name.equals("id")) {
					Map innerJson = new LinkedHashMap();
					logger.debug("%%%%%%%%%%%%columnname=" + name);
					if (!name.contains("_id")) {
						innerJson.put("type", getType(rsmd.getColumnTypeName(i).toLowerCase(), name));
						if (name.equals("name")) {
							innerJson.put("label", getNameColumnDisplayName(form));
						} else {
							innerJson.put("label", getDisplayName(name));
						}
						innerJson.put("placeholder", getDefaultValue(rsmd));
						innerJson.put("required", rsmd.isNullable(i) == ResultSetMetaData.columnNoNulls);
					} else {
						// Check if the column name is same as Table name
						String val = fillList(name, innerJson, form);
						if (rsmd.isNullable(i) == ResultSetMetaData.columnNoNulls) {
							if (val == null) {
								throw new SQLException("values in table " + name
										+ " is not present, please fill the values and comeback and fill these values");
							}
							innerJson.put("required", true);
							innerJson.put("val", val);
						}
					}
					json.put(name, innerJson);
				}
			}
		}
	}

	private Object getType(String columnTypeName, String name) {
		// TODO Auto-generated method stub
		if (name.equals("id")) {
			return "label";
		}
		switch (columnTypeName) {
		case "varchar":
		case "varchar_ignorecase":
			return "text";
		case "date":
			return columnTypeName;
		case "integer":
		case "int4":
			return "number";
		}
		return columnTypeName;
	}

	private String getDefaultValue(ResultSetMetaData rsmd) {
		// TODO Auto-generated method stub
		return "whatever";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cs.dao.UiFormDao#getFormDataList(int)
	 */
	@Override
	public Page<Map> getFormDataList(String appName, int formId, long pageNo) {
		val form = getFormInfo(appName, formId);

		if (form.getFormTableName().equals("authority_rule_lookup")) {
			return getAuth(pageNo);
		}
		val sql = String.format("select * from %s", form.getFormTableName());
		List<Map> list = jdbcTemplate.query(sql, (rs, rowNum) -> {
			ResultSetMetaData rsmd = rs.getMetaData();

			Map<String, Object> json1 = new LinkedHashMap<String, Object>();
			if (nameColumnExists(rsmd)) {
				json1.put(getNameColumnDisplayName(form), rs.getString("name"));
			}
			json1.put("id", rs.getObject("id"));
			for (int i = 1; i < rsmd.getColumnCount() + 1; i++) {
				String name = rsmd.getColumnName(i).toLowerCase();
				if (!name.equals("id") && !name.equals("name")) {
					logger.debug("*****column name=" + name);
					if (!name.contains("_id")) {
						json1.put(getDisplayName(name), rs.getObject(name));
					} else {
						fillNameInfoIfTableExists(rs, json1, name);
					}
				}
			}
			return json1;
		});

		Page<Map> page1 = new Page<>();
		page1.setPageItems(list);
		page1.setPagesAvailable(1);
		return page1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cs.dao.UiFormDao#saveFormData(int,
	 * org.json.simple.JSONObject)
	 */
	@Override
	public void saveFormData(String appName, int formId, int dataId, JSONObject input) throws Exception {
		logger.debug(input);

		UiForm formInfo = getFormInfo(appName, formId);
		List<ColumnDTO> columnNames = getColumnNamesFromQuery(formInfo.getFormTableName());

		Boolean saveAsNew = (Boolean) input.get("save_as_new");
		Integer idn = (saveAsNew != null && saveAsNew) ? 0 : dataId;

		if (!isNamePresent(input, formInfo, idn)) {
			int query = doInsert(appName, formId, idn, formInfo.getFormTableName(), columnNames, input);

			logger.debug("result " + query);
		} else {
			throw new Exception("Name already present");
		}
	}

	private boolean isNamePresent(JSONObject input, UiForm formInfo, Integer dataId) {
		if (dataId == 0 && input.get("name") != null) {
			String sql = String.format("select count(*) from %s where name='%s'", formInfo.getFormTableName(),
					input.get("name"));

			logger.debug("namepresent sql=" + sql);

			Integer count = jdbcTemplate.queryForInt(sql);

			return count > 0;
		} else {
			return false;
		}
	}

	private int doInsert(String appName, int formid, int dataId, String tableName, List<ColumnDTO> columnNames,
			JSONObject input) throws ParseException, SQLException {

		Iterator iterator = input.keySet().iterator();
		String columns = "";
		String values = "";
		String id = "";
		String updateParams = "";
		Object[] obj = null;

		id = dataId == 0 ? String.valueOf(getMaxId(tableName) + 1) : String.valueOf(dataId);
		input.put("id", id);
		if (dataId == 0)
			obj = new Object[columnNames.size()];
		else
			obj = new Object[columnNames.size() - 1];

		int i = 0;
		for (ColumnDTO column : columnNames) {

			columns = columns + "," + column.getColumnName();
			values = values + "," + "?";
			if (column.getColumnName().equalsIgnoreCase("id") && dataId > 0)
				continue;

			updateParams = updateParams + "," + column.getColumnName() + " = ?";

			switch (column.getColumnType()) {

			case 2:
			case 3:
			case 4:
				String data = String.valueOf(input.get(column.getColumnName().toLowerCase()));
				obj[i] = data == null || data.trim().equals("null") || data.trim().equals("") ? null
						: new Integer(data);
				break;
			case 12:
				obj[i] = String.valueOf(input.get(column.getColumnName().toLowerCase()));
				break;
			case 91:
				data = (String) input.get(column.getColumnName().toLowerCase());
				obj[i] = data == null ? null : sdf.parse(data);
				break;

			}

			i++;
		}

		String sql = null;
		if (dataId == 0)
			sql = String.format("insert into %s(%s) values(%s)", tableName, columns.substring(1), values.substring(1));
		else
			sql = String.format("update  %s set %s where id=%s", tableName, updateParams.substring(1), id);

		logger.debug(sql);

		List<UiFormLink> tableRelations = getFormLinkInfo(appName, formid);

		Map<String, List<Object[]>> queryValueMap = new HashMap<String, List<Object[]>>();
		List<String> deleteQueryList = new ArrayList<String>();

		for (UiFormLink uiFormLink : tableRelations) {

			UiForm formInfo = getFormInfo(appName, uiFormLink.uiFormLinkId);
			logger.debug("formInfo=" + formInfo + " uiFormLink=" + uiFormLink);

			String deleteQuery = null;

			String relationQuery = null;

			List map = null;

			if (uiFormLink.linkName != null && uiFormLink.linkName.trim().length() == 0) {
				logger.debug("in uiFormLink.linkName=" + uiFormLink.linkName);
				deleteQuery = String.format("delete from %s where %s_id=%s",
						(tableName + "_" + uiFormLink.linkName + "_relationship"), tableName, id);

				relationQuery = String.format("insert into %s(%s_id,%s_id) values(?,?)",
						(tableName + "_" + uiFormLink.linkName + "_relationship"), tableName, uiFormLink.linkName);

				map = (List) input.get(uiFormLink.linkName);
			} else {
				logger.debug("in uiFormLink.linkName=" + uiFormLink.linkName);
				deleteQuery = String.format("delete from %s where %s_id=%s",
						(tableName + "_" + formInfo.getFormTableName() + "_relationship"), tableName, id);

				relationQuery = String.format("insert into %s(%s_id,%s_id) values(?,?)",
						(tableName + "_" + formInfo.getFormTableName() + "_relationship"), tableName,
						formInfo.getFormTableName());

				if (uiFormLink.singleSelect == false)
					map = (List) input.get(formInfo.getFormTableName());
				else {
					String data = (String) input.get(formInfo.getFormTableName());
					if (data != null) {
						map = new ArrayList();
						map.add(data);
					}
				}

			}
			logger.debug("deleteQuery=" + deleteQuery);
			logger.debug("relationQuery=" + relationQuery);

			if (map != null) {
				Iterator iter = map.iterator();
				List<Object[]> valueList = new ArrayList<Object[]>();
				while (iter.hasNext()) {

					String key = String.valueOf(iter.next());
					String[] split = key.split("#");
					Object mapObj[] = new Object[2];
					mapObj[0] = new Integer(String.valueOf(input.get("id")));
					mapObj[1] = new Integer(split[0]);
					valueList.add(mapObj);
				}
				if (valueList.size() > 0) {

					queryValueMap.put(relationQuery, valueList);
				}
				deleteQueryList.add(deleteQuery);
			}
			logger.debug("deleteQueryList size=" + deleteQueryList.size() + " map=" + map);
		}
		logger.debug("queryValueMap size=" + queryValueMap.size());

		TransactionDefinition def = new DefaultTransactionDefinition(
				DefaultTransactionDefinition.ISOLATION_READ_UNCOMMITTED);

		TransactionStatus status = transactionManager.getTransaction(def);
		int rs = -1;
		try {
			// try(Connection conn =
			// jdbcTemplate.getDataSource().getConnection()){
			// conn.setAutoCommit(false);
			// update(conn, sql, obj);
			rs = jdbcTemplate.update(sql, obj);
			for (String deleteQuery : deleteQueryList) {
				logger.debug("deletequery = " + deleteQuery);
				jdbcTemplate.update(deleteQuery);
				// update(conn, deleteQuery, null);
			}

			Iterator iter = queryValueMap.keySet().iterator();
			while (iter.hasNext()) {
				String key = String.valueOf(iter.next());
				List<Object[]> value = queryValueMap.get(key);
				for (Object valObj[] : value) {
					logger.debug("update key = " + key);
					jdbcTemplate.update(key, valObj);
					// update(conn, key, valObj);
				}

			}
			// conn.commit();
			// conn.close();
			transactionManager.commit(status);
		} catch (Exception ex) {
			transactionManager.rollback(status);
			throw ex;
		} finally {
			// transactionManager.getDataSource().getConnection().close();
		}

		return rs;

	}

	private int update(Connection conn, String sql, Object[] objs) throws SQLException {
		// TODO Auto-generated method stub
		try (PreparedStatement p = conn.prepareStatement(sql)) {

			int i = 1;
			if (objs != null) {
				for (Object obj : objs) {
					if (obj instanceof Date) {
						java.sql.Date sqlDate = new java.sql.Date(((Date) obj).getTime());
						p.setDate(i++, sqlDate);
					} else {
						p.setObject(i++, obj);
					}
				}
			}
			int x = p.executeUpdate();
			p.close();
			return x;
		}
	}

	private List<String> getColumnNames(String tableName) throws SQLException {

		List<String> list = new ArrayList<String>();

		try (Connection conn = jdbcTemplate.getDataSource().getConnection()) {
			ResultSet resultSet = conn.getMetaData().getColumns(null, null, tableName, null);
			while (resultSet.next()) {
				String colName = resultSet.getString(4);
				list.add(colName);
			}
		}
		return list;

	}

	private List<ColumnDTO> getColumnNamesFromQuery(String tableName) {

		String sql = String.format("select * from %s", tableName);
		List<ColumnDTO> list = jdbcTemplate.query(sql, rs -> {
			int colCount = rs.getMetaData().getColumnCount();
			List<ColumnDTO> columns = new ArrayList<ColumnDTO>();

			for (int i = 1; i <= colCount; i++) {
				if (rs.getMetaData().getColumnName(i).contains("*"))
					continue;
				ColumnDTO col = new ColumnDTO(rs.getMetaData().getColumnName(i), rs.getMetaData().getColumnType(i));
				columns.add(col);
			}

			return columns;
		});

		return list;

	}

	private int getMaxId(String tableName) {

		String sql = String.format("select max(id) from %s", tableName);

		int count = jdbcTemplate.query(sql, rs -> {
			rs.next();
			Integer cnt = rs.getInt(1);
			return cnt;
		});

		return count;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.cs.dao.UiFormDao#diaplsyValidate()
	 */
	@Override
	public Map displayValidate() {
		val rootMap = new LinkedHashMap();

		val ldapUsersJson = addDropdown("ldap_users");
		rootMap.put("LDAP Users", ldapUsersJson);

		val programJson = addDropdown("program");
		rootMap.put("Program", programJson);

		val exportControlValueJson = addDropdown("export_control_value");
		rootMap.put("Export Control Value", exportControlValueJson);

		val contTypeJson = addDropdown("cont_type");
		rootMap.put("Content Type", contTypeJson);

		val productJson = addDropdown("product");
		rootMap.put("Product", productJson);

		val sowActivityJson = addDropdown("sow_activity");
		rootMap.put("Sow Activity", sowActivityJson);

		val locationJson = addDropdown("country", "Location");
		rootMap.put("Location", locationJson);

		val citizenJson = addDropdown("country", "Citizen");
		rootMap.put("Citizen", citizenJson);

		val authorityMultipleCountryJson = addDropdown("authority_multiple_country");
		rootMap.put("Authority Multiple Country", authorityMultipleCountryJson);

		val json3 = new LinkedHashMap();
		json3.put("type", "submit");
		json3.put("label", "Validate for Supplier and Program Information");
		rootMap.put("submitVSP", json3);

		val json4 = new LinkedHashMap();
		json4.put("type", "submit");
		json4.put("label", ">>Validate for ECV, Product and Content type");
		rootMap.put("submitEPC", json4);

		val json5 = new LinkedHashMap();
		json5.put("type", "submit");
		json5.put("label", ">>Validate for SOW");
		rootMap.put("submitSOW", json5);

		val json6 = new LinkedHashMap();
		json6.put("type", "submit");
		json6.put("label", ">>Validate for User Location");
		rootMap.put("submitLocation", json6);

		val json7 = new LinkedHashMap();
		json7.put("type", "submit");
		json7.put("label", ">>Validate for User Citizenship");
		rootMap.put("submitUC", json7);

		return rootMap;
	}

	private LinkedHashMap addDropdown(String name) {
		String displayName = getDisplayName(name);
		return addDropdown(name, displayName);
	}

	private LinkedHashMap addDropdown(String name, String displayName) {
		String sql = String.format("select name from %s", name);

		val relnList = jdbcTemplate.query(sql, (rs, rowNum) -> {
			return rs.getObject(1);
		});

		// None selected
		val json1 = new LinkedHashMap();
		relnList.forEach(x -> {

			Map jsonLoc = new LinkedHashMap();
			jsonLoc.put("label", x);
			Boolean checked = false;
			jsonLoc.put("val", Boolean.toString(checked));
			jsonLoc.put("slaveTo", Boolean.toString(checked));
			json1.put(x, jsonLoc);
		});

		val relnTableJson = new LinkedHashMap();
		relnTableJson.put("type", "select");
		relnTableJson.put("label", displayName);
		relnTableJson.put("options", json1);
		return relnTableJson;
	}

	public int deleteRow(String appName, int rowId, int formId) throws SQLException {

		UiForm uiform = getFormInfo(appName, formId);
		String sql = String.format("delete from %s where id=%s", uiform.getFormTableName(), rowId);
		List<UiFormLink> tableRelations = getFormLinkInfo(appName, formId);
		int result = -1;

		TransactionDefinition def = new DefaultTransactionDefinition(
				DefaultTransactionDefinition.ISOLATION_READ_UNCOMMITTED);
		TransactionStatus status = transactionManager.getTransaction(def);
		// try(Connection conn = jdbcTemplate.getDataSource().getConnection()){
		try {
			// conn.setAutoCommit(false);
			for (UiFormLink uiFormLink : tableRelations) {
				UiForm formInfo = getFormInfo(appName, uiFormLink.uiFormLinkId);
				String relationshipName1 = (uiFormLink.getLinkName() == null || uiFormLink.getLinkName().isEmpty())
						? formInfo.getFormTableName() : uiFormLink.getLinkName();

				String deleteQuery = String.format("delete from %s where %s_id=%s",
						(uiform.getFormTableName() + "_" + relationshipName1 + "_relationship"),
						uiform.getFormTableName(), rowId);
				jdbcTemplate.update(deleteQuery);
				// update(conn, deleteQuery, null);
			}

			result = jdbcTemplate.update(sql);
			// result=update(conn, sql, null);
			transactionManager.commit(status);
			// conn.close();
			// conn.commit();
		} catch (Exception ex) {
			transactionManager.rollback(status);
			throw ex;
		} finally {
			// transactionManager.getDataSource().getConnection().close();
		}
		return result;
	}

	/**
	 * @description Below are the Methods to support soft key to table relations
	 *              for Rule tables
	 * @param name
	 * @param innerJson
	 * @param form
	 * @throws SQLException
	 */
	private String fillList(String name, Map innerJson, UiForm form) throws SQLException {
		// TODO Auto-generated method stub
		String tableName = getTableName(name);
		logger.debug("tableName=" + tableName);
		String retVal = null;
		if (!tableExists(tableName)) {
			innerJson.put("label", getDisplayName(name));
			logger.debug("tableName does not exist" + tableName);

			innerJson.put("type", "number");
			innerJson.put("placeholder", "id");
		} else {
			innerJson.put("label", getColumnDisplayName(name));
			logger.debug("tableName does exist" + tableName);
			val sql = String.format("select id, name from %s order by name ASC", tableName);

			Map<String, Map<String, String>> json1 = new LinkedHashMap<String, Map<String, String>>();

			List<String> list = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt(1) + "#" + rs.getString(2));

			list.sort((o1, o2) -> getByIndex(o1, "#", 1).compareTo(getByIndex(o2, "#", 1)));

			list.forEach(x -> {
				Map<String, String> json2 = new LinkedHashMap<String, String>();
				json2.put("label", getByIndex(x, "#", 1));
				json1.put(getByIndex(x, "#", 0), json2);
			});

			innerJson.put("type", "select");
			innerJson.put("options", json1);
			retVal = getByIndex(list.get(0), "#", 0);
		}
		return retVal;
	}

	private String getByIndex(String data, String sep, int idx) {
		String[] split = data.split(sep);
		if (idx < split.length) {
			return split[idx];
		}
		return "";
	}

	private boolean tableExists(String name) throws SQLException {
		try (Connection conn = jdbcTemplate.getDataSource().getConnection()) {
			DatabaseMetaData md = conn.getMetaData();
			ResultSet rs = md.getTables(null, null, name, null);
			if (rs.next()) {
				return true;
			}
		}
		return false;
	}

	private void fillNameInfoIfTableExists(ResultSet rs, Map<String, Object> json1, String name) throws SQLException {
		// TODO Auto-generated method stub
		String tableName = getTableName(name);
		logger.debug("tableName=" + tableName);
		if (tableExists(tableName)) {
			String columnDisplayName = getColumnDisplayName(name);
			Integer id = rs.getInt(name);
			if (id != null) {
				val sql = "select id, name from " + tableName + " where id=" + rs.getInt(name);

				jdbcTemplate.query(sql, (rs1, rowNum) -> {
					json1.put(columnDisplayName, rs1.getObject("name"));
					return "";
				});
			}
			if (json1.get(columnDisplayName) == null) {
				json1.put(columnDisplayName, "");
			}
		} else {
			json1.put(getDisplayName(name), rs.getObject(name));
		}
	}

	private String getColumnDisplayName(String name) {
		// TODO Auto-generated method stub
		String columnDisplay = name.substring(0, name.length() - 3);
		return getDisplayName(columnDisplay) + " Name";
	}

	private String getTableName(String name) {
		String[] split = name.split("__");

		String tabName = split[0];
		if (split.length == 2) {
			tabName = split[1];
		}
		return tabName.substring(0, tabName.length() - 3);
	}

	private boolean nameColumnExists(ResultSetMetaData rsmd) throws SQLException {
		// TODO Auto-generated method stub
		for (int i = 1; i < rsmd.getColumnCount() + 1; i++) {
			String name = rsmd.getColumnName(i).toLowerCase();
			if (name.equals("name")) {
				return true;
			}
		}
		return false;
	}

	/************* End rule tables display ***************/

	@Override
	public String getApplicationDisplayName(String appName) {
		val sql = String.format("select * from ui_app where name='%s'", appName);

		return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> rs.getString("display_name"));
	}

	private Page<Map> getAuth(long pageNo) {
		String countSql = "select count(arl.id)"
				+ "from authority_rule_lookup as arl, auth as a, supplier as s, sow_activity as sow, program as p, cont_type as ct, "
				+ "export_control_value as e, product as pd, item as i, country as c, country as l "
				+ "where arl.auth_id=a.id and arl.supplier_id=s.id and arl.sow_activity_id=sow.id and p.id=arl.program_id and arl.cont_type_id=ct.id "
				+ "and pd.id=arl.product_id and i.id=arl.item_id and arl.citizen__country_id=c.id and arl.location__country_id=l.id "
				+ "and arl.export_control_value_id=e.id and arl.product_id=pd.id";

		String sql = "select arl.id as id, a.name as \"Authority Name\", s.name as \"Supplier Name\", sow.name as \"Sow Name\", "
				+ "p.name as \"Program Name\", ct.name as \"Cont Type Name\", "
				+ "e.name as \"ECV Name\", i.name as \"Item Name\", pd.name as \"Product Name\", "
				+ "c.name as \"Citizen Name\", l.name as \"Location Name\" "
				+ "from authority_rule_lookup as arl, auth as a, supplier as s, sow_activity as sow, program as p, cont_type as ct, "
				+ "export_control_value as e, product as pd, item as i, country as c, country as l "
				+ "where arl.auth_id=a.id and arl.supplier_id=s.id and arl.sow_activity_id=sow.id and p.id=arl.program_id and arl.cont_type_id=ct.id "
				+ "and pd.id=arl.product_id and i.id=arl.item_id and arl.citizen__country_id=c.id and arl.location__country_id=l.id "
				+ "and arl.export_control_value_id=e.id and arl.product_id=pd.id";

		PaginationHelper<Map> ph = new PaginationHelper<Map>();
		Page<Map> page = ph.fetchPage(jdbcTemplate, countSql, sql, new Object[] {}, (int) pageNo, 100,
				new ParameterizedRowMapper<Map>() {
					public Map mapRow(ResultSet rs, int i) throws SQLException {
						LinkedHashMap<String, Object> json = new LinkedHashMap<String, Object>();

						json.put("Authority Name", rs.getString("Authority Name"));
						json.put("Supplier Name", rs.getString("Supplier Name"));
						json.put("Sow Name", rs.getString("Sow Name"));
						json.put("Program Name", rs.getString("Program Name"));
						json.put("Cont Type Name", rs.getString("Cont Type Name"));
						json.put("ECV Name", rs.getString("ECV Name"));
						json.put("Item Name", rs.getString("Item Name"));
						json.put("Product Name", rs.getString("Product Name"));
						json.put("Citizen Name", rs.getString("Citizen Name"));
						json.put("Location Name", rs.getString("Location Name"));
						json.put("id", rs.getInt("id"));

						return json;
					}
				});
		return page;
	}
}
