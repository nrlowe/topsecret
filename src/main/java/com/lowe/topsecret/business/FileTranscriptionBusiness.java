package com.lowe.topsecret.business;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;  
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.core.io.buffer.DefaultDataBuffer;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.lowe.topsecret.model.CustomerModel;

@Component
public class FileTranscriptionBusiness {
	public String csvTranscription(MultipartFile file) {
		String xcel = ".xlsx";
		String word = ".docx";
		if(file.getOriginalFilename().contains(xcel)) {
			return csvExcelTranscription(file);
		} else if(file.getOriginalFilename().contains(word)){
			return csvWordTransacription(file);
		} else {
			return csvTextTransacription(file);
		}
	}
	
	private String csvTextTransacription(MultipartFile file){
		List<String> results = new ArrayList<>();
		try {
			InputStream is = file.getInputStream();
			Scanner sc = new Scanner(is);
			while(sc.hasNext()) {
				results.add(sc.nextLine());
			}
			return populateInsuranceMaps(results);
		} catch(Exception e) {
			return ("Error Reading CSV File, " + e);
		}
	}
	
	private String csvWordTransacription(MultipartFile file){
		List<String> results = new ArrayList<>();
		try {
			InputStream is = file.getInputStream();
			XWPFDocument document = new XWPFDocument(is);
			List<XWPFParagraph> paragraphs = document.getParagraphs();
			for(XWPFParagraph para : paragraphs) {
				results.add(para.getText());
			}
			document.close();
			is.close();
			return populateInsuranceMaps(results);
		} catch(Exception e) {
			return ("Error Reading Word Document, " + e);
		}
	}
	
	
	private String csvExcelTranscription(MultipartFile file) {
		List<String> results = new ArrayList<>();
		try {
			InputStream is = file.getInputStream();
			XSSFWorkbook wb = new XSSFWorkbook(is);
			XSSFSheet xSheet = wb.getSheetAt(0);
			Iterator<Row> itr = xSheet.iterator();
			while(itr.hasNext()) {
				StringBuilder rowBuilder = new StringBuilder();
				Row row = itr.next();
				Iterator<Cell>cellIterator = row.cellIterator();
				while(cellIterator.hasNext()) {
					Cell cell = cellIterator.next();
					switch(cell.getCellType()) {
					case STRING:
						rowBuilder.append(cell.getStringCellValue()).append(",");
						break;
					case NUMERIC:
						int cellValue = (int)cell.getNumericCellValue();
						rowBuilder.append(cellValue).append(",");
						break;
	
					}
				}
				results.add(rowBuilder.toString());
			}	
			wb.close();
		} catch (Exception e) {
			System.out.println(e);
			return "Error Processing File";
		}
		return populateInsuranceMaps(results); 
	}
	
	private String populateInsuranceMaps(List<String> csvList){
		Map<String, List<CustomerModel>> results = new HashMap<>();
		Map<String, Set<String>> companyUniqueIds = new HashMap<>();
		List<CustomerModel> customerList = new ArrayList<>();
		Set<String> companyNames = new HashSet<>();
		for(int i = 0; i < csvList.size(); i++) {
			//userid 0, first name 1, last name 2, version 3, company 4
			String[] customer = csvList.get(i).split(",");
			if(customer.length == 5) {
			CustomerModel customerModel = new CustomerModel();
			customerModel.setUserId(customer[0]);
			customerModel.setName(customer[2] + ", " + customer[1]);
			customerModel.setVersion(Integer.parseInt(customer[3]));
			customerModel.setCompany(customer[4]);
			companyNames.add(customerModel.getCompany());
			customerList.add(customerModel);
			} else {
				break;
			}
		}
		List<CustomerModel> sortedCustomerList = customerList.stream().sorted(Comparator.comparing(CustomerModel::getName)).collect(Collectors.toList());
		Set<String> idSet = null;
		for(CustomerModel model : sortedCustomerList) {
			List<CustomerModel> modelList = null;
			if(!results.containsKey(model.getCompany())) {
				modelList = new ArrayList<>();
				idSet = new HashSet<>();
				modelList.add(model);
				results.put(model.getCompany(), modelList);
				idSet.add(model.getUserId());
				companyUniqueIds.put(model.getCompany(), idSet);
			} else {
				modelList = results.get(model.getCompany());
				idSet = companyUniqueIds.get(model.getCompany());
				if(idSet.contains(model.getUserId())) {
					for(int j = 0; j < modelList.size(); j++) {
						if((modelList.get(j).getUserId().equals(model.getUserId())) && 
								(modelList.get(j).getVersion() < model.getVersion())) {
							modelList.add(j, model);
							modelList.remove(j + 1);
							results.put(model.getCompany(), modelList);
						}
					}
				} else {
					modelList.add(model);
					idSet.add(model.getUserId());
					results.put(model.getCompany(), modelList);
				}
			}
		}
		return printCompanyDocs(results, companyNames);
	}
	
	private String printCompanyDocs(Map<String, List<CustomerModel>> results, Set<String> companyNames) {
		String result = "Succesfully Created Company Files, pleae check root project folder!";
		for(String companyName : companyNames) {
			List<CustomerModel> customerList = results.get(companyName);
			XWPFDocument wordDoc = new XWPFDocument();
			XWPFHeaderFooterPolicy headerPolicy = wordDoc.createHeaderFooterPolicy();
			XWPFHeader header = headerPolicy.createHeader(XWPFHeaderFooterPolicy.DEFAULT);
			XWPFParagraph headerPara = header.createParagraph();
			headerPara.createRun().setText(companyName); 
			for(CustomerModel model : customerList) {
				XWPFParagraph customerLine = wordDoc.createParagraph();
				customerLine.createRun().setText(model.getName() + " -- ID: " + model.getUserId() + " -- Version: " + model.getVersion());
			}
			try {
			wordDoc.write(new FileOutputStream(new File(companyName+".docx")));
			} catch (Exception e) {
				System.out.println(e);
				result = "Issue Creating New Files, " + e;
			} finally {
				try {
					wordDoc.close();
				} catch(Exception e) {
					System.out.println(e);
					result = "Issue closing new file, " + e;
				}
			}
			
		}
		return result;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
