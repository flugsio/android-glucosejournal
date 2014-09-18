package se.huffyreek.glucosejournal;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import android.text.format.Time;

public class GlucoseGraph extends View {
    private static final String TAG = "GlucoseJournal.GlucoseGraph";
    private static final int HOURS_IN_MILLIS = 60*60*1000;
    private static final int MINS_IN_MILLIS = 60*1000;
    Paint redPaint = new Paint();
    Paint glucosePoint = new Paint();
    Paint glucosePointFill = new Paint();
    Paint glucosePointGood = new Paint();
    Paint glucosePointFillGood = new Paint();
    Paint gridPaint = new Paint();
    Paint dateFont = new Paint();
    Paint timeFont = new Paint();
    Paint timeBackground = new Paint();
    public List<JournalEntry> journalEntries;
    public int millisPerPixel = 2*MINS_IN_MILLIS;
    public long topTime;
    public long bottomTime;
    private int w;
    private int h;
    private int capTop = 20;
    private int capBottom = 1;
    private float valueLow = 3.6f;
    private float valueGood = 10.0f;
    private float valueHigh = 15.0f;
    public int futureRange = 2 * HOURS_IN_MILLIS;
    public int[] hourLines = {4, 8, 12, 16, 20};


    public GlucoseGraph(Context context, AttributeSet attrs) {
        super(context, attrs);

        redPaint.setColor(Color.RED);
        glucosePoint.setColor(Color.rgb(160, 160, 255));
        glucosePoint.setStyle(Style.STROKE);
        glucosePointFill.setColor(Color.rgb(100, 100, 255));
        glucosePointFill.setStyle(Style.FILL);

        glucosePointGood.setColor(Color.rgb(160, 255, 160));
        glucosePointGood.setStyle(Style.STROKE);
        glucosePointFillGood.setColor(Color.rgb(100, 255, 100));
        glucosePointFillGood.setStyle(Style.FILL);

        gridPaint.setColor(Color.GRAY);

        dateFont.setColor(Color.GRAY);
        dateFont.setTextSize(16);

        timeFont.setColor(Color.rgb(160, 160, 255));
        timeFont.setTextSize(16);

        timeBackground.setColor(Color.rgb(21, 34, 44));

        journalEntries = new ArrayList<JournalEntry>();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.w = w;
        this.h = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int xLow = glucoseToX(valueGood);
        int xGood = glucoseToX(valueGood);
        int xHigh = glucoseToX(valueHigh);

        canvas.drawLine(0, futureRange/millisPerPixel, w, futureRange/millisPerPixel, gridPaint);

        canvas.drawLine(xLow, 0, xLow, h, gridPaint);
        canvas.drawLine(xGood, 0, xGood, h, gridPaint);
        canvas.drawLine(xHigh, 0, xHigh, h, gridPaint);

        drawDays(canvas);
        drawJournalEntries(canvas);

    }

    // Draws dates and hour markers for 6/12/18 with horizontal lines
    private void drawDays(Canvas canvas) {
        Time firstDay = millisToStartOfDay(topTime);
        Time lastDay = millisToStartOfDay(bottomTime);

        int xHigh = glucoseToX(valueHigh);

        while (firstDay.toMillis(false) >= lastDay.toMillis(false)) {
            long millisStart = firstDay.toMillis(false);
            int y = timeToY(millisStart);
            canvas.drawLine(8, y, w, y, gridPaint);
            canvas.drawText(firstDay.format("%Y  %m-%d"), 12, y-6, dateFont);

            for (int hour : hourLines) {
                if (hour == 12) {
                    // draw from 25 to center xHigh, and then same length after
                    drawHour(canvas, millisStart, hour, 25, (xHigh-25)*2+25+1);
                } else {
                    // draw from 25 and stop at crossing line at xHigh
                    drawHour(canvas, millisStart, hour, 25, xHigh);
                }
            }

            // firstDay.monthDay -= 1; // doesn't change month/year
            // TODO: manual set() doesn't handle daylight saving time
            // TODO: (maybe) render DST changes?
            firstDay.set(firstDay.toMillis(false)-24*60*60*1000);
        }
    }

    private void drawJournalEntries(Canvas canvas) {
        JournalEntry lastEntry = null;
        for (JournalEntry entry : journalEntries) {
            if (!entry.glucose.isEmpty()) {
                drawTime(canvas, entry.at);
                if (lastEntry != null) {
                    canvas.drawLine(calculateX(lastEntry), calculateY(lastEntry), calculateX(entry), calculateY(entry), glucosePointFill);
                }
                lastEntry = entry;
            }
        }

        for (JournalEntry entry : journalEntries) {
            drawGlucosePoint(canvas, entry);
        }
    }

    private void drawHour(Canvas canvas, long millisStart, int hour, float startX, float stopX) {
        int y = timeToY(millisStart+hour*60*60*1000);
        canvas.drawText(String.format("%02d", hour), 4, y+5, dateFont);
        canvas.drawLine(startX, y, stopX, y, gridPaint);
    }

    private void drawTime(Canvas canvas, Time time) {
        int y = timeToY(time.toMillis(false));
        canvas.drawRoundRect(new RectF(2, y-9, 46, y+8), 5, 5, timeBackground);
        canvas.drawText(String.format("%02d:%02d", time.hour, time.minute), 4, y+5, timeFont);
    }

    private float[] calculateAllLocations() {
        float[] values = new float[journalEntries.size()*2];
        int i = 0;
        for (JournalEntry entry : journalEntries) {
            values[i*2] = calculateX(entry);
            values[i*2+1] = calculateY(entry);
            i++;
        }
        return values;
    }

    private Integer glucoseToX(Float glucose) {
        return (int)(w-((glucose-capBottom)*(w/(capTop-capBottom))));
    }

    private Integer calculateX(JournalEntry entry) {
        return glucoseToX(MainActivity.isFloat(entry.glucose) ? Float.parseFloat(entry.glucose) : 0);
    }

    private Integer calculateY(JournalEntry entry) {
        return timeToY(entry.at.toMillis(false));
    }

    private Integer timeToY(long millis) {
        return (int)((topTime-millis)/((long)millisPerPixel));
    }

    private void drawGlucosePoint(Canvas canvas, JournalEntry entry) {
        int x = calculateX(entry);
        int y = calculateY(entry);
        if (MainActivity.isFloat(entry.glucose) && Float.parseFloat(entry.glucose) >= valueLow && Float.parseFloat(entry.glucose) <= valueGood) {
            canvas.drawCircle(x, y, 6, glucosePointGood);
            canvas.drawCircle(x, y, 5, glucosePointFillGood);
        } else {
            canvas.drawCircle(x, y, 6, glucosePoint);
            canvas.drawCircle(x, y, 5, glucosePointFill);
        }
    }

    private Time millisToStartOfDay(long millis) {
        Time time = new Time();
        time.set(millis);
        time.set(time.monthDay, time.month, time.year);
        return time;
    }
}
