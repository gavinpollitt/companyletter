package uk.gov.file;

import uk.gov.letter.ConfirmationSource;
import uk.gov.records.Record;
import uk.gov.records.Record1;

/**
 * Simple utility class to hold, in memory, the successful records used to issue
 * to the letter generation.
 * @author regen
 *
 */
public class SourceManager {
	private final static SourceManager sourceManager = new SourceManager();

	public static SourceManager getInstance() {
		return sourceManager;
	}

	private SourceManager() {
		this.reset();
	}

	public void addRecord(Record1 r) {
		ConfirmationSource.getInstance().addSource(r);
	}

	public void dumpSources() throws Exception {
		ConfirmationSource.getInstance().generateLetters();
	}
	
	public void reset() {
		ConfirmationSource.getInstance().reset();
	}
}
