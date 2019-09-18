package uk.gov.file;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import uk.gov.letter.ConfirmationSource;
import uk.gov.letter.DiscountSource;
import uk.gov.letter.InvoiceSource;
import uk.gov.letter.LetterSource;
import uk.gov.records.Record;

/**
 * 
 * @author regen
 * 
 *         'Front-Office' class to poll the input directory for the required
 *         COMPANY files and the possible termination file. Processed files will
 *         be relocated to either the archive or error directory depending on
 *         the success of the processing
 *
 */
public class FileProcessor {
	private final static String INPUT_DIR = "file:///home/regen/temp/input";
	private final static String ARCHIVE_DIR = "file:///home/regen/temp/archive";
	private final static String ERROR_DIR = "file:///home/regen/temp/error";
	private final static String QUIT_FILE = "q.txt";

	private final Path inputPath;
	private final Path archivePath;
	private final Path errorPath;

	private final List<LineListener> lineListeners;

	/**
	 * Start the ball rolling by injecting the various event listeners/providers
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		List<LetterSource<? extends Record>> sources = new ArrayList<>();
		sources.add(ConfirmationSource.getInstance());
		sources.add(DiscountSource.getInstance());
		sources.add(InvoiceSource.getInstance());
		SourceManager.getInstance().addLetterSources(sources);
		
		FileProcessor fp = new FileProcessor(
				Arrays.asList(new LineListener[] { 
						new ConfirmationListener(RecordConsumerImpl.getInstance()),
						new DiscountListener(RecordConsumerImpl.getInstance()),
						new InvoiceListener(RecordConsumerImpl.getInstance())
		}));

		
		fp.process();
		fp.purge();
	}

	/**
	 * 
	 * @param listeners The set of Line Listeners that will be awaiting a successfully read line
	 */
	public FileProcessor(final List<LineListener> listeners) {
		try {
			this.inputPath = Paths.get(URI.create(INPUT_DIR));
			this.archivePath = Paths.get(URI.create(ARCHIVE_DIR));
			this.errorPath = Paths.get(URI.create(ERROR_DIR));
			this.lineListeners = listeners;
		} catch (Exception e) {
			throw new RuntimeException("Cannot start processor:" + e);
		}
	}

	/**
	 * Control the polling of the input directory until a quit event is discovered
	 * 
	 * @throws Exception
	 */
	private void process() throws Exception {
		while (!this.quitEvent()) {
			if (!companyFilePresent()) {
				Thread.sleep(10000);
				System.out.println("Snoozing for 10 seconds");
			} else {
				this.processFiles();
			}
		}

	}

	private boolean companyFilePresent() throws Exception {
		return Files.list(this.inputPath).anyMatch(p -> p.getFileName().toString().startsWith("COMPANY"));
	}

	private boolean quitEvent() throws Exception {
		return Files.list(this.inputPath).anyMatch(p -> p.endsWith(QUIT_FILE)) && !companyFilePresent();
	}

	/**
	 * This method will iterate through any COMPANY files and open a dialogue with
	 * the SourceManager to process each file in turn, validate and generate any
	 * letters if the file is clean
	 * 
	 * @throws Exception
	 */
	private void processFiles() throws Exception {
		List<Path> files = Files.list(this.inputPath).collect(Collectors.toList());

		for (Path p : files) {
			List<String> file = Files.readAllLines(p);
			// Remove blank lines
			file = file.stream().filter(l -> !(l.trim().length() == 0)).collect(Collectors.toList());

			if (this.processSingleFile(file)) {
				moveFile(p, this.archivePath);
			}
			else {
				System.out.println("Error processing the file at:" + p
						+ " moving to error and continuing with further files\n");
				moveFile(p, this.errorPath);
			}
		}
	}

	/**
	 * Process a single file by validating its structure and building the Record
	 * objects for letter generation.
	 * 
	 * @param lines
	 *            List of lines contained in the file
	 */
	private boolean processSingleFile(List<String> lines) {

		int lineNumber = 1;
		for (String line : lines) {
			final int lno = lineNumber;
			this.lineListeners.stream().forEach(ll -> ll.acceptCandidate(lno, line));
			lineNumber++;
		}
		//Send an EOF event in case any post-processing is required
		this.lineListeners.stream().forEach(ll -> ll.acceptCandidate(-1, "EOF|"));
		
		return RecordConsumerImpl.getInstance().processResults();
	}

	private static Path moveFile(Path file, Path newDir) throws Exception {
		return Files.move(file, newDir.resolve(file.getFileName()));
	}

	/**
	 * Remove all instances of the quit file from the input
	 * 
	 * @throws Exception
	 */

	private void purge() throws Exception {
		Files.list(this.inputPath).filter(p -> p.endsWith(QUIT_FILE)).forEach(FileProcessor::df);
	}

	/**
	 * Used to avoid Java not being able to handle exception in Stream
	 * 
	 * @param p
	 *            The file path to delete
	 */
	private static void df(Path p) {
		try {
			Files.delete(p);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
