package com.lowe.topsecret.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.lowe.topsecret.business.BracketsBusiness;

@RestController
public class BracketsController {
	
	@Autowired
	private BracketsBusiness business;
	
	@RequestMapping(value = "/brackets", method = RequestMethod.GET)
	public ModelAndView testBracketClosure(@RequestParam(value = "brackets", required = true) String userInput) {
		ModelAndView modelAndView = new ModelAndView("brackets");
		modelAndView.addObject("response", business.testBracketClosure(userInput));
		return modelAndView;
	}
}
