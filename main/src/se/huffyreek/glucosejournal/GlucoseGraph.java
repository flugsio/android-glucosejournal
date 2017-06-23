package se.huffyreek.glucosejournal;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import android.text.format.Time;

public class GlucoseGraph extends View {
    private static final String TAG = "GlucoseJournal.GlucoseGraph";
    private static final int HOURS_IN_MILLIS = 60*60*1000;
    private static final int MINS_IN_MILLIS = 60*1000;
    Paint redPaint = new Paint();
    Paint nightBackground = new Paint();
    Paint sleepBackground = new Paint();
    Paint exerciseBackground = new Paint();
    Paint glucosePoint = new Paint();
    Paint glucosePointFill = new Paint();
    Paint glucosePointGood = new Paint();
    Paint glucosePointFillGood = new Paint();
    Paint glucoseLine;
    Paint glucoseLineDark;
    Paint glucoseLabelBackground;
    Paint glucoseLabelBorder;
    Paint glucoseLabelBorderGood;
    Paint gridPaint = new Paint();
    Paint dateFont = new Paint();
    Paint timeFont = new Paint();
    Paint glucoseFont = new Paint();
    Paint timeBackground = new Paint();
    public List<JournalEntry> journalEntries;
    public int millisPerPixel = 2*MINS_IN_MILLIS;
    public long topTime;
    public long bottomTime;
    private int w;
    private int h;
    private int capTop = 20;
    private int capBottom = 1;
    private int carbohydratesCap = 300;
    private float valueLow = 3.6f;
    private float valueGood = 10.0f;
    private float valueHigh = 15.0f;
    public int futureRange = 2 * HOURS_IN_MILLIS;
    public int[] hourLines = {4, 8, 12, 16, 20};


    public GlucoseGraph(Context context, AttributeSet attrs) {
        super(context, attrs);

        redPaint.setColor(Color.RED);
        nightBackground.setColor(Color.rgb(35, 35, 35));
        sleepBackground.setColor(Color.rgb(32, 32, 32));
        exerciseBackground.setColor(Color.rgb(40, 78, 40));
        glucosePoint.setColor(Color.rgb(160, 160, 255));
        glucosePoint.setStyle(Style.STROKE);
        glucosePointFill.setColor(Color.rgb(100, 100, 255));
        glucosePointFill.setStyle(Style.FILL);

        glucosePointGood.setColor(Color.rgb(160, 255, 160));
        glucosePointGood.setStyle(Style.STROKE);
        glucosePointFillGood.setColor(Color.rgb(100, 255, 100));
        glucosePointFillGood.setStyle(Style.FILL);


        glucoseLine = new Paint() {{
            setStyle(Paint.Style.STROKE);
            setAntiAlias(true);
            setStrokeWidth(1.5f);
            setColor(Color.rgb(100, 100, 225));
        }};

        glucoseLineDark = new Paint() {{
            setStyle(Paint.Style.STROKE);
            setAntiAlias(true);
            setStrokeWidth(3.0f);
            setStrokeCap(Cap.ROUND);
            setColor(Color.rgb(30, 30, 50));
        }};

        glucoseLabelBackground = new Paint() {{
            setStyle(Style.FILL);
            setColor(Color.rgb(40, 40, 40));
        }};

        glucoseLabelBorder = new Paint() {{
            setStyle(Paint.Style.STROKE);
            setAntiAlias(true);
            setStrokeWidth(1.5f);
            setStrokeCap(Cap.ROUND);
            setColor(Color.rgb(60, 60, 165));
        }};

        glucoseLabelBorderGood = new Paint() {{
            setStyle(Paint.Style.STROKE);
            setAntiAlias(true);
            setStrokeWidth(1.5f);
            setStrokeCap(Cap.ROUND);
            setColor(Color.rgb(60, 165, 60));
        }};


        gridPaint.setColor(Color.GRAY);

        dateFont.setColor(Color.GRAY);
        dateFont.setTextSize(16);

        timeFont.setColor(Color.rgb(160, 160, 255));
        timeFont.setTextSize(16);

        glucoseFont.setColor(Color.rgb(160, 160, 255));
        glucoseFont.setTextSize(13);

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

        Time firstDay = millisToStartOfDay(topTime);
        long millisStart = firstDay.toMillis(false);
        Time lastDay = millisToStartOfDay(bottomTime);
        long millisLast= lastDay.toMillis(false);

        // sleep background {{{1
        for (long i=millisStart; i > millisLast; i -= 24*HOURS_IN_MILLIS) {
            // sleep
            //canvas.drawRect(0, timeToY(i+2*HOURS_IN_MILLIS), xHigh, timeToY(i-6*HOURS_IN_MILLIS), sleepBackground);
            // night
            canvas.drawRect(xHigh/2, timeToY(i+6*HOURS_IN_MILLIS), xHigh, timeToY(i-2*HOURS_IN_MILLIS), nightBackground);
            // exercise
            // cycling/running
            //canvas.drawRect(0, timeToY(i+6*HOURS_IN_MILLIS), xHigh/2, timeToY(i+4*HOURS_IN_MILLIS), exerciseBackground);
            // walking
            //canvas.drawRect(0, timeToY(i+8*HOURS_IN_MILLIS), xHigh/4, timeToY(i+6*HOURS_IN_MILLIS), exerciseBackground);
        }

        // grid {{{1
        canvas.drawLine(0, futureRange/millisPerPixel, w, futureRange/millisPerPixel, gridPaint);

        canvas.drawLine(xLow, 0, xLow, h, gridPaint);
        canvas.drawLine(xGood, 0, xGood, h, gridPaint);
        canvas.drawLine(xHigh, 0, xHigh, h, gridPaint);

        drawDays(canvas);
        drawCarbohydratesJournalEntries(canvas);
        drawJournalEntries(canvas);

        // }}}
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

    private void drawCarbohydratesJournalEntries(Canvas canvas) {
        JournalEntry lastEntry = null;
        for (JournalEntry entry : journalEntries) {
            if (!entry.carbohydrates.isEmpty()) {
                //drawTime(canvas, entry.at);
                /*if (lastEntry != null) {
                    canvas.drawLine(calculateX(lastEntry), calculateY(lastEntry), calculateX(entry), calculateY(entry), glucosePointFill);
                }*/
                Paint pLine1 = new Paint() {{
                    setStyle(Paint.Style.STROKE);
                    setAntiAlias(true);
                    setStrokeWidth(1.5f);
                    setColor(Color.rgb(255, 255, 255)); // Line color
                }};

                Paint pLineBorder1 = new Paint() {{
                    setStyle(Paint.Style.STROKE);
                    setAntiAlias(true);
                    setStrokeWidth(3.0f);
                    setStrokeCap(Cap.ROUND);
                    setColor(Color.rgb(205, 205, 205)); // Darker version of the color
                }};

                Path p = new Path();
                Point mid = new Point();
                // ...
                Point start = new Point(w-2, calculateY(entry));
                Point end = new Point(w-2, calculateY(entry)-2*HOURS_IN_MILLIS/millisPerPixel);
                mid.set((int)((start.x + end.x) / 2 - Float.parseFloat(entry.carbohydrates)/carbohydratesCap*w), (start.y + end.y) / 2);

                // Draw line connecting the two points:
                p.reset();
                p.moveTo(start.x, start.y);
                p.quadTo((start.x + mid.x) / 2, start.y, mid.x, mid.y);
                p.quadTo((mid.x + end.x) / 2, end.y, end.x, end.y);

                canvas.drawPath(p, pLineBorder1);
                canvas.drawPath(p, pLine1);
                lastEntry = entry;
            }
        }
    }

    private void drawJournalEntries(Canvas canvas) {
        JournalEntry lastEntry = null;
        for (JournalEntry entry : journalEntries) {
            if (!entry.glucose.isEmpty()) {
                drawTime(canvas, entry.at);
                if (lastEntry != null) {
                    canvas.drawLine(calculateX(lastEntry), calculateY(lastEntry), calculateX(entry), calculateY(entry), glucoseLineDark);
                    canvas.drawLine(calculateX(lastEntry), calculateY(lastEntry), calculateX(entry), calculateY(entry), glucoseLine);
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
        if (MainActivity.isFloat(entry.glucose)) {
            int x = calculateX(entry);
            int y = calculateY(entry);
            float glucose = Float.parseFloat(entry.glucose);
            if (glucose < valueHigh && glucose > valueLow) {
                drawCenteredLabel(canvas, String.format("%.1f", glucose), x, y,
                        glucoseLabelBackground, glucoseLabelBorderGood, glucoseFont);
            } else {
                drawCenteredLabel(canvas, String.format("%.1f", glucose), x, y,
                        glucoseLabelBackground, glucoseLabelBorder, glucoseFont);
            }

        }
    }

    private static void drawCenteredLabel(Canvas canvas, String text, int x, int y,
            Paint bg, Paint border, Paint font) {
        Rect bounds = new Rect();
        font.getTextBounds(text, 0, text.length(), bounds);

        RectF rect = new RectF(bounds);
        rect.offsetTo(x-bounds.width()/2, y-bounds.height()/2);
        rect.inset(-4, -3); // padding
        canvas.drawRoundRect(rect, 3, 3, bg);
        canvas.drawRoundRect(rect, 3, 3, border);

        canvas.drawText(text, x-bounds.width()/2, y+bounds.height()/2, font);
    }

    private Time millisToStartOfDay(long millis) {
        Time time = new Time();
        time.set(millis);
        time.set(time.monthDay, time.month, time.year);
        return time;
    }
}
