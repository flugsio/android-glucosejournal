package se.huffyreek.glucosejournal;

import junit.framework.TestCase;
import junit.framework.Assert;

import android.text.format.Time;

public class TimeGuesserTest extends TestCase {

    Time timeNow;

    public void setUp() {
        timeNow = new Time();
        timeNow.setToNow();
    }

    public void tearDown() {
        timeNow = null;
    }

    /* TODO public void testTime() {
        Time timeGuess = TimeGuesser.newFromString("03:00");

        timeNow.hour = 3;
        timeNow.minute = 0;
        timeNow.second = timeGuess.second;

        Assert.assertEquals(timeNow.toMillis(false),
                            timeGuess.toMillis(false));
    }*/

    public void testFourChars() {
        Time timeGuess = TimeGuesser.newFromString("0300");

        timeNow.hour = 3;
        timeNow.minute = 0;
        timeNow.second = timeGuess.second;

        Assert.assertEquals(timeNow.toMillis(false),
                            timeGuess.toMillis(false));
    }

    public void testThreeChars() {
        Time timeGuess = TimeGuesser.newFromString("0300");

        timeNow.hour = 3;
        timeNow.minute = 0;
        timeNow.second = timeGuess.second;

        Assert.assertEquals(timeNow.toMillis(false),
                            timeGuess.toMillis(false));
    }
}
