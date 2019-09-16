package uk.gov.file;

import java.util.ArrayList;
import java.util.List;

import uk.gov.records.Record;

public class RecordConsumerImpl implements RecordConsumer {

	private static RecordConsumer _recordListener = new RecordConsumerImpl();
	
	private final List<Record> records = new ArrayList<>();
	private final List<String> problems = new ArrayList<>();
	
	
	public static RecordConsumer getInstance() {
		return _recordListener;
	}
	
	private RecordConsumerImpl() {}
	
	@Override
	public void acceptResult(Record r) {
		records.add(r);
	}

	@Override
	public void acceptResult(String problem) {
		this.problems.add(problem);
	}

	@Override
	public boolean processResults() {
		boolean success = true;
		if (this.problems.size() > 0) {
			this.problems.stream().forEach(System.out::println);
			success = false;
			this.problems.clear();
			this.records.clear();
		}
		else {
			this.records.stream().forEach(SourceManager.getInstance()::addRecord);
			
			try {
				SourceManager.getInstance().dumpSources();
			}
			catch (Exception e) {
				System.out.println(e);
				success = false;
			}
			finally {
				SourceManager.getInstance().reset();
				this.problems.clear();
				this.records.clear();
			}
		}
		
		return success;
	}


}
