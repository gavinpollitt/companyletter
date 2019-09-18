package uk.gov.letter;

import uk.gov.records.Record;
import uk.gov.records.Record1;
import uk.gov.records.Record2;

/**
 * LetterSource implementation for the Confirmation Letter.
 * @author regen
 *
 */
public class DiscountSource extends LetterSource<Record2> {

	private final static String TEMPLATE_LOC = "file:///home/regen/Documents/letters/Discount.txt";
	private final static String OUTPUT_DIR = "file:///home/regen/temp/output";

	private final static String OUTPUT_FN = "Discount";
	
	private final static DiscountSource source = new DiscountSource();
	
	
	public static DiscountSource getInstance() {
		return source;
	}
	
	private DiscountSource() {
	}
	
	@Override
	protected String getTemplateURI() {
		return TEMPLATE_LOC;
	}
	
	@Override
	protected String getOutputDir() {
		return OUTPUT_DIR;
	}

	@Override
	protected String getFilename(Record2 letterRecord) {
		return OUTPUT_FN + "_" + letterRecord.getFields().get("companyName").getValue() + ".txt";
	}

	/**
	 * Convenience method to correctly cast record to the type supported by this class.
	 */
	@Override
	public void consumeRecord(Record r) {
		if (r.getClass() == Record2.class) {
			this.addSource(Record2.class.cast(r));
		}
	}

}

