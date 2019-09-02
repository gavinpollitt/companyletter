package uk.gov.file;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import uk.gov.records.Record1;
import uk.gov.records.Record1.Record1A;

public class FileProcessor {
	private final static String INPUT_DIR = "file:///home/regen/temp/input";
	private final static String ARCHIVE_DIR = "file:///home/regen/temp/archive";
	private final static String ERROR_DIR = "file:///home/regen/temp/error";
	private final static String QUIT_FILE = "q.txt";

	private final Path inputPath;
	private final Path archivePath;
	private final Path errorPath;

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

	private void processFiles() throws Exception {
		List<Path> files = Files.list(this.inputPath).collect(Collectors.toList());

		for (Path p : files) {
			List<String> file = Files.readAllLines(p);
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

	private void purge() throws Exception {
		Files.list(this.inputPath).filter(p -> p.endsWith(QUIT_FILE)).forEach(FileProcessor::df);
	}

	// Used to avoid Java not being able to handle exception in Stream
	private static void df(Path p) {
		try {
			Files.delete(p);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
