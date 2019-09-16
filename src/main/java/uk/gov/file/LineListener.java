package uk.gov.file;

public interface LineListener {
	public void acceptCandidate(final int lineNumber, final String recordLine);
	public void setRecordListener(final RecordConsumer recordListener);
	
	static String getRecType(String line) {
		return line.substring(0, line.indexOf("|"));
	}
}
