package uk.gov.letter;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import uk.gov.records.Record;
import uk.gov.records.RecordUtils.Field;

/**
 * Superclass for the different letter types providing core functionality and
 * expectations of the subclasses
 * 
 * @author regen
 *
 * @param <T>
 *            The specific record type that the source is associated with
 */
public abstract class LetterSource<T extends Record> {

	private List<T> letterSet = new ArrayList<>();

	private Template template;

	// The location of the specific template required
	protected abstract String getTemplateURI();

	// The location of the output directory where the generated letter will be
	// deposited
	protected abstract String getOutputDir();

	// The destination filename
	protected abstract String getFilename(T letterRecord);

	public abstract void consumeRecord(Record r);

	/**
	 * Load the template into memory
	 * 
	 * @throws Exception
	 */
	protected void loadTemplate() throws Exception {
		if (this.template == null) {
			URI uri = URI.create(getTemplateURI());
			this.template = new Template(uri);
		}

	}

	/**
	 * 
	 * @param record
	 *            A candidate record for letter production
	 */
	public void addSource(T record) {
		letterSet.add(record);
	}

	/**
	 * Using the provided records, produce the associated letters
	 * 
	 * @throws Exception
	 */
	public void generateLetters() throws Exception {
		this.loadTemplate();

		for (T r : letterSet) {
			List<String> out = injectValues(r);

			Path p = Paths.get(URI.create(getOutputDir() + "/" + getFilename(r)));
			p = Files.createFile(p);
			Files.write(p, out);
		}

		this.reset();
	}

	public LetterSource<T> reset() {
		this.letterSet.clear();
		return this;
	}

	/**
	 * Grunt work for performing the field matching and replacement
	 * 
	 * @param r
	 *            The record used as source for field replacement.
	 * @return The 'in-memory' lines of replaced lines
	 */
	private List<String> injectValues(Record r) {
		List<String> outLines = new ArrayList<String>();

		List<? extends Record> groupActive = null;
		int groupIndex = 0;

		int lineNumber = 0;
		for (Iterator<String> lineIt = this.template.templateLines.iterator(); lineIt.hasNext();) {
			String line = lineIt.next();
			
			List<FieldPos> repFields = this.template.lineTags.get(lineNumber);

			// Check for group
			CompositeTag ct = null;
			if (repFields.size() == 1) {
				String ns = repFields.get(0).field.replace("<<", "").replace(">>", "");
				ct = identifyPreTag(ns);
			}

			if (ct != null && ct.command.equals("group")) {
				if (groupActive == null) {
					groupActive = r.getChildren().get(ct.data);
				}
			} else if (repFields.size() > 0) {

				boolean done = false;

				String origLine = line;
				while (!done) {
					Map<String, Field> repVals = groupActive != null ? groupActive.get(groupIndex).getFields()
							: r.getFields();

					line = constructLine(line, repFields, repVals);
					outLines.add(line);
					done = true;

					if (groupActive != null) {
						groupIndex += 1;

						if (groupIndex == groupActive.size()) {
							groupIndex = 0;
							groupActive = null;
						} else {
							line = origLine;
							done = false;
						}
					}
				}

			} else {
				outLines.add(line);
			}
			lineNumber++;
		}
		return outLines;
	}

	/**
	 * Identify any system tags in the form <<tag.item>>
	 * 
	 * @param tag
	 *            The full stringy tag
	 * @return The CompositeTag object containing the detail.
	 */
	private static CompositeTag identifyPreTag(final String tag) {

		CompositeTag ct = null;
		if (tag.indexOf(".") >= 0 && tag.indexOf(".") < tag.length()) {
			ct = new CompositeTag();
			ct.command = tag.substring(0, tag.indexOf("."));
			ct.data = tag.substring(tag.indexOf(".") + 1);
		}

		return ct;
	}

	/**
	 * 
	 * @param line
	 *            The line from the templated
	 * @param repFields
	 *            The candidate fields for replacement
	 * @param repVals
	 *            The candidate values for replacement
	 * @return
	 */
	private static String constructLine(String line, final List<FieldPos> repFields, final Map<String, Field> repVals) {
		//Resolve fields which will not shift the length of the line
		List<FieldPos> fixedFields = repFields.stream().filter(fp -> fp.limit != null && fp.limit > 0).collect(Collectors.toList());
		
		for (FieldPos fp:fixedFields) {
			String val = getFieldValue(fp.field, repVals);
			val = String.format("%-" + fp.limit + "." + fp.limit + "s",val);
			line = line.substring(0, fp.start) + val + line.substring(fp.start + fp.limit);
		}

		// Resolve fields that should just be a replacement of the tag
		List<FieldPos> varFields = repFields.stream().filter(fp -> fp.limit == null).collect(Collectors.toList());

		for (FieldPos fp:varFields) {
			String val = getFieldValue(fp.field, repVals);
			line = line.replace(fp.field, val);
		}
		
		return line;
	}
	
	/**
	 * Parse the field between the << >>
	 * @param repField
	 * @param repVals
	 * @return The value of the field
	 */
	private static String getFieldValue(String repField, Map<String, Field> repVals) {
		String val = null;
		String ns = repField.replace("<<", "").replace(">>", "");

		if (ns.startsWith("system.")) {
			if (ns.substring(7).equals("today")) {
				val = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
			}
		} else {
			Field curField = repVals.get(ns);

			if (curField != null) {
				val = curField.getValue();
			}
		}		
		return val;
	}

	/**
	 * Class to hold the contents and tags of a template document to negate the need for repeated parsing.
	 * @author regen
	 *
	 */
	public static class Template {
		private List<String> templateLines;
		private List<List<FieldPos>> lineTags;

		public Template(URI uri) throws Exception {
			Path p = Paths.get(uri);
			this.templateLines = Files.lines(p).collect(Collectors.toList());
			this.lineTags = new ArrayList<>(this.templateLines.size());
			this.extractFields();
		}

		public void extractFields() {

			for (String line : this.templateLines) {
				boolean active = line.length() > 5;
				int pos = 0;
				List<FieldPos> fpl = new ArrayList<>();
				while (active) {
					pos = line.indexOf("<<", pos);
					int start = pos;
					if (pos < 0 || pos > line.length() - 5) {
						active = false;
					} else {
						pos += 2;
						pos = line.indexOf(">>", pos);

						if (pos < 0) {
							active = false;
						} else {
							pos += 2;
							FieldPos fp = new FieldPos(line.substring(start, pos), start);
							boolean done = false;
							for (; (pos < line.length()) && !done; pos++) {
								if (line.charAt(pos) != ' ')
									done = true;
							}
							
							pos--;
							
							if (line.indexOf("<<", pos) == pos) {
								fp.limit = pos - start - 2;
							}
							fpl.add(fp);
						}
					}
				}
				this.lineTags.add(fpl);
			}
		}

	}

	/**
	 * Holder class for fields containing a 'command'
	 * 
	 * @author regen
	 *
	 */
	private static class CompositeTag {
		private String command;
		private String data;

		public String toString() {
			return this.command + "." + this.data;
		}
	}

	/**
	 * Holder class for a field tag
	 */
	private static class FieldPos {
		private String field;
		private int start;
		private Integer limit = null;

		public FieldPos(final String field, final int start) {
			this.field = field;
			this.start = start;
		}
	}

}
