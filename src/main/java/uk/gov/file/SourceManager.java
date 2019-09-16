package uk.gov.file;

import java.util.List;

import uk.gov.letter.LetterSource;
import uk.gov.records.Record;

/**
 * Simple utility class to hold, in memory, the successful records used to issue
 * to the letter generation.
 * @author regen
 *
 */
public class SourceManager {

	private final static SourceManager sourceManager = new SourceManager();
	private List <LetterSource<? extends Record>> sources;

	public static SourceManager getInstance() {
		return sourceManager;
	}

	private SourceManager() {
	}

	public void addLetterSources(List <LetterSource<? extends Record>> sources) {
		this.sources = sources;
	}

	public void addRecord(Record r) {
		this.sources.stream().forEach(ls -> ls.consumeRecord(r));
	}

	public void dumpSources() {
		this.sources.stream().forEach(this::gl);
	}
	
	public void reset() {
		this.sources.stream().forEach(ls -> ls.reset());
	}
	
	/**
	 * Utilityu method to convert checked to unchecked exception due to lambda not catering
	 * for Checked Exception
	 * @param ls
	 */
	private void gl(final LetterSource<? extends Record> ls) {
		try {
			ls.generateLetters();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
