package uk.gov.file;

import uk.gov.records.Record;

/**
 * Interface specifying the behaviour of classes responding to Record events.
 * @author regen
 *
 */
public interface RecordConsumer {
	public void acceptResult(Record r);
	public void acceptResult(String problem);
	public boolean processResults();
}
