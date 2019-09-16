package uk.gov.letter;

import uk.gov.records.Record;
import uk.gov.records.Record1;

/**
 * LetterSource implementation for the Confirmation Letter.
 * @author regen
 *
 */
public class ConfirmationSource extends LetterSource<Record1> {

	private final static String TEMPLATE_LOC = "file:///home/regen/Documents/letters/Confirmation.txt";
	private final static String OUTPUT_DIR = "file:///home/regen/temp/output";

	private final static String OUTPUT_FN = "Confirmation";
	
	private final static ConfirmationSource source = new ConfirmationSource();
	
	
	public static ConfirmationSource getInstance() {
		return source;
	}
	
	private ConfirmationSource() {
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
	protected String getFilename(Record1 letterRecord) {
		return OUTPUT_FN + "_" + letterRecord.getFields().get("companyName").getValue() + ".txt";
	}

	public void consumeRecord(Record r) {
		if (r.getClass() == Record1.class) {
			this.addSource(Record1.class.cast(r));
		}
	}

}

