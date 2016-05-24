package com.foodtruckopt.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import com.foodtruckopt.dto.UiForm;
import com.foodtruckopt.dto.UiFormLink;
import com.foodtruckopt.util.Page;

public interface UiFormDao {

	public abstract List<UiForm> getFormList(String appName);

	public abstract Map getFormData(String appName, int dataId, UiForm form, List<UiFormLink> formLinks) throws SQLException;

	public abstract UiForm getFormInfo(String appName, int formId);

	public abstract List<UiFormLink> getFormLinkInfo(String appName, int formId);

	public abstract Page<Map> getFormDataList(String appName, int formId, long pageNo);

	public abstract void saveFormData(String appName, int formId,int dataId, JSONObject input) throws Exception;

	public abstract Map displayValidate();
	
	public int deleteRow(String appName, int rowId,int formId) throws SQLException;

	public abstract String getApplicationDisplayName(String appName);

}