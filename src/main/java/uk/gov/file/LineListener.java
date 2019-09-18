package uk.gov.file;

/**
 * Interface defining the behaviour of listeners awaiting events following a successful read of a file line
 * @author regen
 *
 */
public interface LineListener {
	public void acceptCandidate(final int lineNumber, final String recordLine);
	public void setRecordListener(final RecordConsumer recordListener);
	
	static String getRecType(String line) {
		return line.substring(0, line.indexOf("|"));
	}
}
