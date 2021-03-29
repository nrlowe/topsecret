package com.lowe.topsecret.controller;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.lowe.topsecret.business.FileTranscriptionBusiness;

@RestController
public class FileTranscriptionController {
	
	@Autowired
	private FileTranscriptionBusiness business;
	
	@RequestMapping(value = "/csvTranscript", method = RequestMethod.POST)
	public ModelAndView csvTranscription(@RequestParam(value = "csvFile", required = true) MultipartFile  csvFile) {
		System.out.println("Controller CSV Process Started " + LocalDateTime.now());
		ModelAndView modelAndView = new ModelAndView("csvtranscript");
		String transcriptionMessage = business.csvTranscription(csvFile);
		modelAndView.addObject("response", transcriptionMessage);
		System.out.println("Controller CSV Process Finished " + LocalDateTime.now());
		return modelAndView;
	}
	

}
