package com.foodtruckopt.controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartResolver;

import com.foodtruckopt.dao.UiFormDaoImpl;
import com.foodtruckopt.dto.AppUser;
import com.foodtruckopt.dto.UiForm;
import com.foodtruckopt.repositories.UserRepository;
import com.foodtruckopt.util.Page;

import lombok.val;

@RestController
@EnableAutoConfiguration
public class DynamicController {

	@Autowired
	UiFormDaoImpl dao;

	private static Logger logger = Logger.getLogger(DynamicController.class);

	@RequestMapping(value = "/saveForm", method = RequestMethod.POST)
	public JSONObject saveForm(@RequestBody JSONObject input, @RequestParam("app_name") String appName, @RequestParam("formid") int formId,
			@RequestParam("dataid") int dataId) throws Exception {
		logger.debug("test " + input);
		// TODO: Implement save
		JSONObject json = new JSONObject();
		dao.saveFormData(appName, formId, dataId, input);

		json.put("success", "success");
		json.put("outcomeList", "outcome");
		return json;
	}

	@RequestMapping("/getApplicationDisplayName/{app_name}")
	public String getApplicationDisplayName(@PathVariable("app_name") String appName) {
		return "\"" + dao.getApplicationDisplayName(appName) + "\"";
	}

	@RequestMapping("/getFormList/{app_name}")
	public List<Map> getFormList(@PathVariable("app_name") String appName) {

		return dao.getFormList(appName).stream().collect(Collectors.groupingBy(UiForm::getGroupBy)).entrySet().stream().map(x -> {
			Map map1 = new LinkedHashMap();
			map1.put("group", x.getKey());
			map1.put("tableList", x.getValue());
			return map1;
		}).collect(Collectors.toList());
	}

	@RequestMapping("/getFormDataList/{app_name}/{form_id}/{page_no}")
	public Map<String, Object> getFormDataList(@PathVariable("app_name") String appName, @PathVariable("form_id") int formId,
			@PathVariable("page_no") long pageNum) {
		val formInfo = dao.getFormInfo(appName, formId);

		Page<Map> page = dao.getFormDataList(appName, formId, pageNum);

		val map = new LinkedHashMap();
		map.put("formName", formInfo.getDisplayName());
		map.put("formList", page.getPageItems());
		map.put("pagesAvailable", page.getPagesAvailable());

		return map;
	}

	@RequestMapping("/getFormData/{app_name}/{form_id}/{data_id}")
	public JSONObject getFormData(@PathVariable("app_name") String appName, @PathVariable("form_id") int formId,
			@PathVariable("data_id") int dataId) throws SQLException {
		val form = dao.getFormInfo(appName, formId);
		val formLinks = dao.getFormLinkInfo(appName, formId);

		val json = dao.getFormData(appName, dataId, form, formLinks);
		val json1 = new LinkedHashMap();
		json1.put("fields", json);
		json1.put("type", "fieldset");
		json1.put("label", form.getDisplayName());
		val json2 = new LinkedHashMap();
		json2.put(form.getFormTableName(), json1);

		JSONObject json3 = new JSONObject(json2);

		return json3;
	}
	
	@RequestMapping(value = "/deleteRecord/{app_name}/{record_id}/{form_id}", method = RequestMethod.POST)
	public int deleteRecord(@PathVariable("app_name") String appName, @PathVariable("record_id") int rowId, @PathVariable("form_id") int formId)
			throws SQLException {
		JSONObject json = new JSONObject();
		logger.debug("test delete");
		logger.debug("Table id " + formId);
		logger.debug("row id " + rowId);
		return dao.deleteRow(appName, rowId, formId);
	}

	private Object addAll(List<Map> list1) {
		ArrayList<Map> list = new ArrayList<>();

		list1.sort((x1, x2) -> x1.get("name").toString().compareTo(x1.get("name").toString()));
		LinkedHashMap map = new LinkedHashMap();
		map.put("id", -1);
		map.put("name", "All");
		list.add(map);
		list.addAll(list1);
		return list;
	}

	@Autowired
	UserRepository userRepository;

	@RequestMapping(value = "/user", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<AppUser> findAll() {
		final List<AppUser> resultList = new ArrayList<>();
		final Iterable<AppUser> all = userRepository.findAll();
		for (AppUser u : all) {
			System.out.println("u=" + u.getUsername());
			resultList.add(u);
		}
		return resultList;
	}

	@RequestMapping(value = "/user/{username}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public AppUser create(@PathVariable String username) {
		return userRepository.save(new AppUser(username));
	}

}
