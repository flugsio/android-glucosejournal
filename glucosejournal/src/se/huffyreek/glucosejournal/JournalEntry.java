package se.huffyreek.glucosejournal;

import android.R.integer;
import android.text.format.Time;

public class JournalEntry {
	
	public int id;
	public Time at;
	public String glucose;
	public String carbohydrates;
	public String dose;

	public JournalEntry() {
		
	}
	
	public JournalEntry(String at, String glucose, String carbohydrates, String dose) {
		this.at = new Time();
		if (at.isEmpty()) {
			this.at.setToNow();
		} else {
			this.at.parse(at);
		}
		this.glucose = glucose;
		this.carbohydrates = carbohydrates;
		this.dose = dose;
	}
	
	public String toString() {
		return at.format("%H:%M") + " " + glucose + " " + carbohydrates + " "  + dose;
	}

}
