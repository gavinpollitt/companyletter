package uk.gov.records;

import java.util.ArrayList;
import java.util.List;

/**
 * Application specific error message
 * @author regen
 *
 */
public class InvalidRecordException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	List<String> fields = new ArrayList<>();
	
	public void addInvalidField(String s) {
		fields.add(s);
	}

	@Override
	public String getMessage() {
		// TODO Auto-generated method stub
		return "Invalid fields supplied: " + fields;
	}
	
}
