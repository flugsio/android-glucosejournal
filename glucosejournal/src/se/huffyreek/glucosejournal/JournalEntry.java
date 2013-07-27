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
		update_at_time(at);
		this.glucose = glucose;
		this.carbohydrates = carbohydrates;
		this.dose = dose;
	}
	
	public void update_at_time(String at) {
		int hour = -1;
		int minute = -1;
		int monthDay = -1;
		int month = -1;
		int year = -1;
		
		if (!at.isEmpty()) {
			hour = Integer.parseInt(at.substring(0, at.length()-2));
			minute = Integer.parseInt(at.substring(at.length()-2, at.length()));
		}
		
		if (this.at == null) {
			this.at = new Time();
			this.at.setToNow(); // sets date and time
			
			monthDay = this.at.monthDay;
			month = this.at.month;
			year = this.at.year;
			if (hour != -1 && minute != -1) {
				if (hour > this.at.hour || (hour == this.at.hour && minute > this.at.minute)) {
					// TODO: test if negative values works
					if (monthDay > 0)
						monthDay--;
					// clock may be out of sync, remove only if more than 1 hour in the future maybe
				}
			} else {
				hour = this.at.hour;
				minute = this.at.minute;
			}
		} else {
			monthDay = this.at.monthDay;
			month = this.at.month;
			year = this.at.year;
		}
		this.at.set(this.at.second, minute, hour, monthDay, month, year);
		
	}
	
	public String toString() {
		return at.format("%H:%M") + " " + glucose + " " + carbohydrates + " "  + dose;
	}

}
