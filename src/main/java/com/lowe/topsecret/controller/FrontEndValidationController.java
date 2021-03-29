package com.lowe.topsecret.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;


@RestController
public class FrontEndValidationController {
	
	@RequestMapping(value = "/jsvalidation", method = RequestMethod.GET)
	public ModelAndView csvTranscription() {
		ModelAndView modelAndView = new ModelAndView("jsvalid");
		return modelAndView;
	}
	
	
}
