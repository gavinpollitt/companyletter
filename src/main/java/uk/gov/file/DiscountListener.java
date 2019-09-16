package uk.gov.file;

import uk.gov.records.Record2;

public class DiscountListener implements LineListener {

	private RecordConsumer recordListener;

	public DiscountListener(final RecordConsumer recordListener) {
		this.recordListener = recordListener;
	}

	public DiscountListener() {
	}

	@Override
	public void acceptCandidate(final int lineNumber, final String recordLine) {
		String rec = LineListener.getRecType(recordLine);

		try {
			if (rec.equals("2")) {
				// Hit a record 2
				this.recordListener.acceptResult(new Record2(recordLine));
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
