package uk.gov.records;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import uk.gov.records.RecordUtils.FieldSpec;

/**
 * Class holder for Record type 2
 * @author regen
 *
 */
public class Record2 extends Record {
	private final static Pattern COMP_REG = Pattern.compile(".{1,20}");
	private final static Pattern LN_REG = Pattern.compile(".{1,15}");
	private final static Pattern DISC_REG = Pattern.compile("^[1-9]?[0-9](\\.[0-9][0-9]?)?$");

	public Record2(final String line) throws Exception {
		super(line);
	}

	@Override
	public List<FieldSpec> getRecordFields() {
		return Arrays.asList(
				new FieldSpec("companyName", COMP_REG),
				new FieldSpec("letterName", LN_REG),
				new FieldSpec("discount", DISC_REG));

	}

	@Override
	public Map<String, List<? extends Record>> getChildren() {
		return null;
	}

	/**
	 * Perform any inter-record validation once a 'group' has been read
	 */
	public void postValidate() throws Exception {

	}


	public static void main(String[] args) throws Exception {
		Record2 a = new Record2("2|FiveCo|Tina|7.5");
		a.postValidate();
		
		//ConfirmationSource.getInstance().addSource(a);
		
		
		//ConfirmationSource.getInstance().generateLetters();

	}

}
