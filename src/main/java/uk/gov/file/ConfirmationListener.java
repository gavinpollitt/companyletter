package uk.gov.file;

import uk.gov.records.Record1;
import uk.gov.records.Record1.Record1A;

public class ConfirmationListener implements LineListener {

	private boolean confActive = false;
	private Record1 conf = null;
	private RecordConsumer recordListener;

	public ConfirmationListener(final RecordConsumer recordListener) {
		this.recordListener = recordListener;
	}
	
	public ConfirmationListener() {}
	
	@Override
	public void acceptCandidate(final int lineNumber, final String recordLine) {
		String rec = LineListener.getRecType(recordLine);

		try {
			if (rec.equals("1")) {
				//Hit another record one
				if (this.confActive) {
					this.recordListener.acceptResult(this.conf);
				}
				this.conf = new Record1(recordLine);
				this.confActive = true;
			} else if (rec.equals("1A")) {
				if (this.confActive) {
					Record1A cont = new Record1.Record1A(recordLine);
					this.conf.addContact(cont);
				} else {
					throw new Exception("1A can only exist following valid parent Record 1");
				}
			} else {
				if (this.confActive) {
					this.confActive = false;
					this.conf.postValidate();
					this.recordListener.acceptResult(this.conf);					
					this.conf = null;
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
