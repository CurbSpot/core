package com.foodtruckopt.controller;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.val;

@RestController
@EnableAutoConfiguration
@RequestMapping("/api")
public class PostController {

	@RequestMapping(value = "/posts", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<String> posts() {
		ArrayList<String> list = new ArrayList<String>();
		list.add("Krishna");
		list.add("Aditya");
		return list;
	}
	
	@RequestMapping(value = "/posts/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public JSONObject post(@PathVariable("id") String id) {
		System.out.println("in the put message=" + id);
		val json = new JSONObject();
		json.put("val", "kkkk");
		return json;
	}
	
	@RequestMapping(value = "/posts", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public void save(@RequestBody String name) {
		System.out.println("in the put message=" + name);
	}

	@RequestMapping(value = "/posts/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody int  delete(@PathVariable("id") String name) {
		System.out.println("in the delete message=" + name);
		return 1;
	}

	@RequestMapping(value = "/posts", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public void create(@RequestBody String name) {
		System.out.println("in the create message=" + name);
	}
}
