package uk.gov.records;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import uk.gov.records.RecordUtils.Field;
import uk.gov.records.RecordUtils.FieldSpec;

public abstract class Record {

	protected Map<String,Field> fields;
	
	protected Function<String,String> fieldValue = k-> this.fields.get(k).getValue();
	
	protected BiPredicate<Record, String> fieldEquivCheck = 
			(r,s) -> r.getFields().get(s).getValue().equals(this.fieldValue.apply(s));

	public abstract List<FieldSpec> getRecordFields();
	
	public abstract void postValidate() throws Exception;
	
	protected Record(String line) throws Exception {
		fields = this.isolateFields(line);		
	}

	public Map<String, List<? extends Record>> getChildren() {
		Map<String, List<? extends Record>> children = new HashMap<>(1);
		return children;
	}


	protected int getFieldCount() {
		return getRecordFields().size();
	}
	
	public Map<String, Field> getFields() {
		return fields;
	}

	protected Map<String,Field> isolateFields(String record) throws Exception {
		final Map<String,Field> fieldMap = new HashMap<>();
		String[] fieldList = processRecordLine(record, this.getFieldCount());
		
		Field[] fields = new Field[getFieldCount()];
		for (int i = 0; i < this.getFieldCount(); i++) {
			 fields[i] = new Field(getRecordFields().get(i),fieldList[i+1]);
			 fieldMap.put(fields[i].getFieldSpec().getName(), fields[i]);
		}
		validFields(fields);
		
		return fieldMap;
	}
	
	protected static String[] processRecordLine(final String record, final int fieldCount) throws Exception {
		String[] fieldList = Record.getFields(record);

		if (fieldList.length - 1 != fieldCount) {
			throw new Exception("Invalid record contents for type: " + fieldList[0] + ", expected: " + fieldCount
					+ ", read: " + (fieldList.length - 1));
		}

		return fieldList;
	}

	protected static boolean validFields(Field... fields) throws InvalidRecordException {
		List<Field> errors = Arrays.asList(fields).stream().filter(f -> !(f.getFieldSpec().getRegex().matcher(f.getValue()).matches()))
				.collect(Collectors.toList());

		if (errors.size() > 0) {
			final InvalidRecordException ire = new InvalidRecordException();
			errors.stream().forEach(f -> ire.addInvalidField(f.getValue()));
			throw ire;
		}

		return true;
	}

	protected static String[] getFields(String line) {
		return line.split("\\|");
	}
}
