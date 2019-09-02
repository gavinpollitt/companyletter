package uk.gov.records;

import java.util.regex.Pattern;

public class RecordUtils {
	
	public static class Field {
		private FieldSpec fieldSpec;
		private String value;
		
		public Field(final FieldSpec fieldSpec, final String value) {
			this.value = value;
			this.fieldSpec = fieldSpec;
		}
		
		public FieldSpec getFieldSpec() {
			return fieldSpec;
		}

		public String getValue() {
			return value;
		}

	}
	
	public static class FieldSpec {
		private String name;
		private Pattern regex;
		
		public FieldSpec(final String name, final Pattern regex) {
			this.name = name;
			this.regex = regex;
		}
		
		public Pattern getRegex() {
			return regex;
		}

		public String getName() {
			return name;
		}	

	}
}
