package se.huffyreek.glucosejournal;

import android.text.format.Time;

public class TimeGuesser {

    public static Time newFromString(String time) {
        return update_at_time(null, time);
    }

    public static Time updateFromString(Time previous, String time) {
        return update_at_time(previous, time);
    }

    private static Time update_at_time(Time guess, String at) {
        int hour = -1;
        int minute = -1;
        int monthDay = -1;
        int month = -1;
        int year = -1;

        if (!at.isEmpty()) {
            hour = Integer.parseInt(at.substring(0, at.length()-2));
            minute = Integer.parseInt(at.substring(at.length()-2, at.length()));
        }

        if (guess == null) {
            guess = new Time();
            guess.setToNow(); // sets date and time

            monthDay = guess.monthDay;
            month = guess.month;
            year = guess.year;
            if (hour != -1 && minute != -1) {
                if (hour > guess.hour || (hour == guess.hour && minute > guess.minute)) {
                    // TODO: test if negative values works
                    if (monthDay > 0)
                        monthDay--;
                    // clock may be out of sync, remove only if more than 1 hour in the future maybe
                }
            } else {
                hour = guess.hour;
                minute = guess.minute;
            }
        } else {
            monthDay = guess.monthDay;
            month = guess.month;
            year = guess.year;
        }
        guess.set(guess.second, minute, hour, monthDay, month, year);
        return guess;
    }
}
