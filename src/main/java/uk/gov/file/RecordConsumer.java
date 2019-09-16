package uk.gov.file;

import uk.gov.records.Record;

public interface RecordConsumer {
	public void acceptResult(Record r);
	public void acceptResult(String problem);
	public boolean processResults();
}
