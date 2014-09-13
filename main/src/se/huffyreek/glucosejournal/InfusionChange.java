package se.huffyreek.glucosejournal;

import android.R.integer;
import android.graphics.Color;
import android.text.format.Time;

public class InfusionChange {

    public int id;
    public Time at;
    public String units;

    public InfusionChange() {

    }

    public InfusionChange(String at, String units) {
        this.at = TimeGuesser.newFromString(at);
        this.units = units;
    }

    public void setAt(String at) {
        this.at = TimeGuesser.updateFromString(this.at, at);
    }

    public String toString() {
        return at.format("%H:%M") + " Infusion Change " + units;
    }

    public int hoursAgo() {
        Time now = new Time();
        now.setToNow();
        return millisToHours(now.toMillis(false)-this.at.toMillis(false));
    }

    public int colorPresentation() {
        int hoursAgo = hoursAgo();

        if (hoursAgo > 72)
            return Color.RED;
        else if (hoursAgo > 54)
            return Color.YELLOW;
        else if (hoursAgo > 48)
            return Color.CYAN;
        else
            return Color.WHITE;
    }

    private int millisToHours(long millis) {
        return (int)(millis/1000/60/60);
    }

}
