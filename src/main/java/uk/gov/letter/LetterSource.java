package uk.gov.letter;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import uk.gov.records.Record;
import uk.gov.records.RecordUtils.Field;

public abstract class LetterSource<T extends Record> {
	private final static Pattern FIELD_REG_EX = Pattern.compile("(.*?)(<<.*?>>)");
	
	private List<T> letterSet = new ArrayList<>();
	private List<String> templateLines;

	protected abstract String getTemplateURI();

	protected abstract String getOutputDir();

	protected abstract String getFilename(T letterRecord);
	
	protected void loadTemplate() throws Exception {
		URI uri = URI.create(getTemplateURI());
		Path p = Paths.get(uri);
		templateLines = Files.lines(p).collect(Collectors.toList());
	}

	public void addSource(T record) {
		letterSet.add(record);
	}

	public void generateLetters() throws Exception {
		if (this.templateLines == null) {
			this.loadTemplate();
		}

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
	
	private List<String> injectValues(Record r) {
		List<String> outLines = new ArrayList<String>();

		List<? extends Record> groupActive = null;
		int groupIndex = 0;

		for (Iterator<String> lineIt = this.templateLines.iterator(); lineIt.hasNext();) {
			String line = lineIt.next();
			Matcher m = FIELD_REG_EX.matcher(line);

			List<String> repFields = new ArrayList<>();

			while (m.find()) {
				repFields.add(m.group(2));
			}

			// Check for group
			CompositeTag ct = null;
			if (repFields.size() == 1) {
				String ns = repFields.get(0).replace("<<", "").replace(">>", "");
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
		}
		return outLines;
	}

	private static CompositeTag identifyPreTag(final String tag) {

		CompositeTag ct = null;
		if (tag.indexOf(".") >= 0 && tag.indexOf(".") < tag.length()) {
			ct = new CompositeTag();
			ct.command = tag.substring(0, tag.indexOf("."));
			ct.data = tag.substring(tag.indexOf(".") + 1);
		}

		return ct;
	}

	private static String constructLine(String line, List<String> repFields, Map<String, Field> repVals) {
		for (String s : repFields) {
			Field curField = null;

			String ns = s.replace("<<", "").replace(">>", "");

			if (ns.startsWith("system.")) {
				if (ns.substring(7).equals("today")) {
					line = line.replace(s, new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
				}
			} else {
				curField = repVals.get(ns);

				if (curField != null) {
					String repVal = curField.getValue();
					line = line.replace(s, repVal);
				}
			}
		}

		return line;

	}

	private static class CompositeTag {
		private String command;
		private String data;

		public String toString() {
			return this.command + "." + this.data;
		}
	}

}
