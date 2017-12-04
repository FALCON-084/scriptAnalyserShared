package com.ust.rest.controller;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.ust.model.ResponseModel;
import com.ust.parser.ScriptParser;

@RestController
@Scope("session")
public class MainController {
	@Autowired
	ServletContext context;
	@Autowired
	ScriptParser scriptParser;
	String json;

	@RequestMapping(value = "/fileprocess",headers=("content-type=multipart/*"), method = RequestMethod.POST)
	public ModelAndView renderFooList(@RequestParam("file") MultipartFile inputFile, HttpSession session) throws Exception {
		
		ModelAndView mav = new ModelAndView("analysisdetails");
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String json = ow.writeValueAsString(getResponse(inputFile));
		mav.addObject("data", json);
		session.setAttribute("data", json);
		return mav;
	}
	
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	public ModelAndView list(HttpSession session) throws JsonProcessingException {
		
		String strData=(String) session.getAttribute("data");
		ModelAndView mav = new ModelAndView("analysisdetails");
		mav.addObject("data", strData);	
		return mav;
	}
	
	@RequestMapping(value = "/showdata", method = RequestMethod.POST)
	public ModelAndView showData( HttpSession session) throws JsonProcessingException {

		String strData=(String) session.getAttribute("data");
		ModelAndView mav = new ModelAndView("displayTable");
		mav.addObject("data", strData);	
		return mav;
	}
	@RequestMapping(value = "/mainupload",headers=("content-type=multipart/*"), method = RequestMethod.POST)
	@ResponseBody
	public ResponseModel getCodeBlockDetails(@RequestParam("file") MultipartFile inputFile) throws Exception {
		return getResponse(inputFile);
	}


	private ResponseModel getResponse(MultipartFile inputFile) throws Exception {
		
		return scriptParser.parse(inputFile);
		
			}


}
