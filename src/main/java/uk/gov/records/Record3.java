package uk.gov.records;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import uk.gov.records.RecordUtils.FieldSpec;

/**
 * Class holder for Record type 1 and its 1A children
 * @author regen
 *
 */
public class Record3 extends Record {
	private final static Pattern COMP_REG = Pattern.compile(".{1,20}");
	private final static Pattern LN_REG = Pattern.compile(".{1,15}");

	private List<Record3A> products;

	public Record3(final String line) throws Exception {
		super(line);
		this.products = new ArrayList<>();
	}

	@Override
	public List<FieldSpec> getRecordFields() {
		return Arrays.asList(
				new FieldSpec("companyName", COMP_REG),
				new FieldSpec("letterName", LN_REG));
	}

	public List<Record3A> getProducts() {
		return this.products;
	}

	public void addProduct(Record3A record3A) {
		this.products.add(record3A);
	}

	@Override
	public Map<String, List<? extends Record>> getChildren() {
		Map<String, List<? extends Record>> children = super.getChildren();
		children.put("products", this.products);
		return children;
	}

	/**
	 * Perform any inter-record validation once a 'group' has been read
	 */
	public void postValidate() throws Exception {
		if (this.products.size() == 0) {
			throw new Exception("At least one product record must be supplied for Record Type 3");
		}
		else {
			if (this.products.stream().anyMatch(c -> !this.fieldEquivCheck.test(c, "companyName"))) {
				throw new Exception("3A company names must match those of the corresponding 3 record");
			}
		}
	}

	/**
	 * Class holder for record type 3A
	 * @author regen
	 *
	 */
	public static class Record3A extends Record {
		private final static Pattern PNAME_REG = Pattern.compile(".{3,20}");
		private final static Pattern PCOST_REG = Pattern.compile("^[1-9]?[0-9]*(\\.[0-9][0-9]?)?$");

		public Record3A(final String line) throws Exception {
			super(line);
		}
		
		@Override
		public List<FieldSpec> getRecordFields() {
			return Arrays.asList(
					new FieldSpec("companyName", COMP_REG),
					new FieldSpec("productName", PNAME_REG),
					new FieldSpec("productCost", PCOST_REG));
		}
		

		@Override
		public void postValidate() throws Exception {
			// TODO Auto-generated method stub
			
		}
		
	}

	public static void main(String[] args) throws Exception {
		Record3 a = new Record3("3|OneCo|Jeff");
		Record3A a1 = new Record3A("3A|OneCo|This Prod|999.99");
		a.addProduct(a1);
		a1 = new Record3A("3A|OneCo|That Prod|10000.12");
		a.addProduct(a1);
		a.postValidate();

	}

}
