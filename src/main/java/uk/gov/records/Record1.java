package uk.gov.records;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import uk.gov.letter.ConfirmationSource;
import uk.gov.records.RecordUtils.Field;
import uk.gov.records.RecordUtils.FieldSpec;

/**
 * Class holder for Record type 1 and its 1A children
 * @author regen
 *
 */
public class Record1 extends Record {
	private final static Pattern COMP_REG = Pattern.compile(".{1,20}");
	private final static Pattern ADD_REG = Pattern.compile("[\\w -]{1,15}");
	private final static Pattern PC_REG = Pattern.compile(
			"([Gg][Ii][Rr] 0[Aa]{2})|((([A-Za-z][0-9]{1,2})|(([A-Za-z][A-Ha-hJ-Yj-y][0-9]{1,2})|(([A-Za-z][0-9][A-Za-z])|([A-Za-z][A-Ha-hJ-Yj-y][0-9][A-Za-z]?))))\\s?[0-9][A-Za-z]{2})");
	private final static Pattern LN_REG = Pattern.compile(".{1,15}");

	private List<Record1A> contacts;

	public Record1(final String line) throws Exception {
		super(line);
		this.contacts = new ArrayList<>();
	}

	@Override
	public List<FieldSpec> getRecordFields() {
		return Arrays.asList(
				new FieldSpec("companyName", COMP_REG),
				new FieldSpec("address1", ADD_REG),
				new FieldSpec("postcode", PC_REG),
				new FieldSpec("letterName", LN_REG));
	}

	public List<Record1A> getContacts() {
		return contacts;
	}

	public void addContact(Record1A record1A) {
		this.contacts.add(record1A);
	}

	@Override
	public Map<String, List<? extends Record>> getChildren() {
		Map<String, List<? extends Record>> children = super.getChildren();
		children.put("contacts", this.contacts);
		return children;
	}

	/**
	 * Perform any inter-record validation once a 'group' has been read
	 */
	public void postValidate() throws Exception {
		if (this.contacts.size() == 0) {
			throw new Exception("At least one contact record must be supplied for Record Type 1");
		}
		else {
			if (this.contacts.stream().anyMatch(c -> !this.fieldEquivCheck.test(c, "companyName"))) {
				throw new Exception("1A company names must match those of the corresponding 1 record");
			}
		}
	}

	/**
	 * Class holder for record type 1A
	 * @author regen
	 *
	 */
	public static class Record1A extends Record {
		private final static Pattern CNAME_REG = Pattern.compile(".{1,20}");
		private final static Pattern CNUM_REG = Pattern.compile("[\\d ]{1,14}");
		private Map<String,Field> fields;

		public Record1A(final String line) throws Exception {
			super(line);
		}
		
		@Override
		public List<FieldSpec> getRecordFields() {
			return Arrays.asList(
					new FieldSpec("companyName", COMP_REG),
					new FieldSpec("contactName", CNAME_REG),
					new FieldSpec("contactNumber", CNUM_REG));
		}
		

		@Override
		public void postValidate() throws Exception {
			// TODO Auto-generated method stub
			
		}
		
	}

	public static void main(String[] args) throws Exception {
		Record1 a = new Record1("1|OneCo|10 One Road|TF1 6SO|Jeff");
		Record1A a1 = new Record1A("1A|OneCo|Mark Shark|01952 234543");
		a.addContact(a1);
		a1 = new Record1A("1A|OneCo|Jeff Wing|01952 277354");
		a.addContact(a1);
		a1 = new Record1A("1A|OneCo|Gary Grunt|01952 742354");
		a.addContact(a1);
		a.postValidate();
		
		ConfirmationSource.getInstance().addSource(a);
		
		a = new Record1("1|SixCo|888 Eights Road|NN11 5TN|Samit");
		a1 = new Record1A("1A|SixCo|Mary Contrary|01204 324445");
		a.addContact(a1);
		a.postValidate();

		ConfirmationSource.getInstance().addSource(a);
		
		ConfirmationSource.getInstance().generateLetters();

	}

}
