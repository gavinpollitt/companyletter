package uk.gov.file;

import uk.gov.records.Record3;
import uk.gov.records.Record3.Record3A;

/** 
 * Listener class to intercept record type 3 and perform the appropriate processing
 * depending on the event received.
 * @author regen
 *
 */
public class InvoiceListener implements LineListener {

	private boolean invActive = false;
	private boolean childActive = false;
	private Record3 inv = null;
	private RecordConsumer recordListener;

	public InvoiceListener(final RecordConsumer recordListener) {
		this.recordListener = recordListener;
	}
	
	public InvoiceListener() {}
	
	@Override
	public void acceptCandidate(final int lineNumber, final String recordLine) {
		String rec = LineListener.getRecType(recordLine);

		try {
			if (rec.equals("3")) {
				//Hit another record three
				if (this.invActive) {
					if (this.childActive) {
						this.recordListener.acceptResult(this.inv);
						this.childActive = false;
					}
					else {
						this.invActive = false;
						throw new Exception("3 must be followed by, at lease, 1 3A");
					}
				}
				this.inv = new Record3(recordLine);
				this.invActive = true;
			} else if (rec.equals("3A")) {
				if (this.invActive) {
					this.childActive = true;
					Record3A prod = new Record3.Record3A(recordLine);
					this.inv.addProduct(prod);
				} else {
					throw new Exception("3A can only exist following valid parent Record 3");
				}
			} else {
				if (this.invActive) {
					this.invActive = false;
					this.childActive = false;
					this.inv.postValidate();
					this.recordListener.acceptResult(this.inv);					
					this.inv = null;
				}
			}
		} catch (Exception e) {
			this.recordListener.acceptResult("Line " + lineNumber + "->" + e.getMessage());
		}
	}

	@Override
	public void setRecordListener(RecordConsumer recordListener) {
		this.recordListener = recordListener;
	}

}
