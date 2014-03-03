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
	
	public JournalEntry(String at, String glucose, String carbohydrates,
			    String dose) {
		this.at = TimeGuesser.newFromString(at);
		this.glucose = glucose;
		this.carbohydrates = carbohydrates;
		this.dose = dose;
	}

	public void setAt(String at) {
		this.at = TimeGuesser.updateFromString(this.at, at);
	}
	
	public String toString() {
		return at.format("%H:%M") + " " + glucose + " " + carbohydrates + " "  + dose;
	}

}
