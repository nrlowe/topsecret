package com.lowe.topsecret.business;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class BracketsBusiness {
	private static final ArrayList<String> openers = new ArrayList<String>(Arrays.asList("(","{","["));
	private static final ArrayList<String> closers = new ArrayList<String>(Arrays.asList(")","}","]"));
	private static final String result = "RESULT";
	
	public Map<String, String> testBracketClosure(String input){
		Map<String, String> resultMap = new HashMap<>();
		String onlyBrackets = input.replaceAll("[^\\(\\)\\{\\}\\[\\]]", "");
		boolean isValid = true;
		ArrayList<String> arr = new ArrayList<String>(Arrays.asList(onlyBrackets.split("")));
        for(int i = 0; i < arr.size(); i++) {
            if(i + 1 < arr.size() && openers.contains(arr.get(i))) {
                String braceO = arr.get(i);
                int indexC = openers.indexOf(braceO);
                String braceC = closers.get(indexC);
                if(arr.contains(braceC) && braceC.equals(arr.get(i + 1))){
                    arr.remove(i + 1);
                    arr.remove(i);
                    i = -1;
                } else {
                    continue;
                }
            } else {
                resultMap.put(result,"NO : Not a valid LISP segment");
                isValid = false;
                break;
            }
        }
        resultMap.put("input", input);
        if(isValid) {
        	resultMap.put(result, "YES : Valid LISP Segment");
        }
		return resultMap;
	}
}
