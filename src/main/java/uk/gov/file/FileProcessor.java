package uk.gov.file;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import uk.gov.records.Record1;
import uk.gov.records.Record1.Record1A;

/**
 * 
 * @author regen
 * 
 * 'Front-Office' class to poll the input directory for the required COMPANY files and the possible termination file.
 * Processed files will be relocated to either the archive or error directory depending on the success of the processing
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

	/**
	 * Start the ball rolling
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		FileProcessor fp = new FileProcessor();

		fp.process();
		fp.purge();
	}

	public FileProcessor() {
		try {
			this.inputPath = Paths.get(URI.create(INPUT_DIR));
			this.archivePath = Paths.get(URI.create(ARCHIVE_DIR));
			this.errorPath = Paths.get(URI.create(ERROR_DIR));
		} catch (Exception e) {
			throw new RuntimeException("Cannot start processor:" + e);
		}
	}

	/**
	 * Control the polling of the input directory until a quit event is discovered
	 * @throws Exception
	 */
	private void process() throws Exception {
		while (!this.quitFilePresent()) {
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

	private boolean quitFilePresent() throws Exception {
		return Files.list(this.inputPath).anyMatch(p -> p.endsWith(QUIT_FILE));
	}

	/**
	 * This method will iterate through any COMPANY files and open a dialogue with the SourceManager to 
	 * process each file in turn, validate and generate any letters if the file is clean
	 * @throws Exception
	 */
	private void processFiles() throws Exception {
		List<Path> files = Files.list(this.inputPath).collect(Collectors.toList());

		for (Path p : files) {
			List<String> file = Files.readAllLines(p);
			//Remove blank lines
			file = file.stream().filter(l -> !(l.trim().length()==0)).collect(Collectors.toList());

			try {
				this.processSingleFile(file);
				moveFile(p, this.archivePath);
				SourceManager.getInstance().dumpSources();
			} catch (Exception e) {
				System.out.println(
						"Error processing the file at:" + p + " moving to error and continuing with further files\n" + e);
				moveFile(p, this.errorPath);
			}
		}
	}

	/**
	 * Process a single file by validating its structure and building the Record objects for letter generation.
	 * @param lines List of lines contained in the file
	 * @throws Exception
	 */
	private void processSingleFile(List<String> lines) throws Exception {
		boolean oneActive = false, threeActive = false;

		String currentLine = null;
		Record1 rec1 = null;
		try {
			for (String line : lines) {
				currentLine = line;
				String rec = getRecType(line);

				if (rec.equals("1")) {
					if (oneActive) {
						SourceManager.getInstance().addRecord(rec1);
					}
					oneActive = true;
					rec1 = new Record1(line);
				} else if (rec.equals("1A")) {
					if (oneActive) {
						Record1A rec1A = new Record1.Record1A(line);
						rec1.addContact(rec1A);
					} else {
						throw new Exception("1A can only exist following parent Record 1");
					}
				} else {
					if (oneActive) {
						oneActive = false;
						SourceManager.getInstance().addRecord(rec1);
						rec1 = null;
					}
				}
			}
		} catch (Exception e) {
			SourceManager.getInstance().reset();
			throw new Exception("File corrupted at line: " + currentLine + "\nexception" + e);
		}

	}

	private static Path moveFile(Path file, Path newDir) throws Exception {
		return Files.move(file, newDir.resolve(file.getFileName()));
	}

	private static String getRecType(String line) {
		return line.substring(0, line.indexOf("|"));
	}

	/**
	 * Remove all instances of the quit file from the input
	 * @throws Exception
	 */
	
	private void purge() throws Exception {
		Files.list(this.inputPath).filter(p -> p.endsWith(QUIT_FILE)).forEach(FileProcessor::df);
	}

	/**
	 * Used to avoid Java not being able to handle exception in Stream
	 * @param p The file path to delete
	 */
	private static void df(Path p) {
		try {
			Files.delete(p);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
