package uk.gov.letter;

import uk.gov.records.Record;
import uk.gov.records.Record3;

/**
 * LetterSource implementation for the Invoice Letter.
 * @author regen
 *
 */
public class InvoiceSource extends LetterSource<Record3> {

	private final static String TEMPLATE_LOC = "file:///home/regen/Documents/letters/Invoice.txt";
	private final static String OUTPUT_DIR = "file:///home/regen/temp/output";

	private final static String OUTPUT_FN = "Invoice";
	
	private final static InvoiceSource source = new InvoiceSource();
	
	
	public static InvoiceSource getInstance() {
		return source;
	}
	
	private InvoiceSource() {
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
	protected String getFilename(Record3 letterRecord) {
		return OUTPUT_FN + "_" + letterRecord.getFields().get("companyName").getValue() + ".txt";
	}

	/**
	 * Convenience method to correctly cast record to the type supported by this class.
	 */
	@Override
	public void consumeRecord(Record r) {
		if (r.getClass() == Record3.class) {
			this.addSource(Record3.class.cast(r));
		}
	}

}

