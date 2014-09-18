package se.huffyreek.glucosejournal;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import se.huffyreek.glucosejournal.database.GlucoseJournalDatabaseHelper;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.FocusFinder;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.view.Menu;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    private List<JournalEntry> journalEntries;
    private EditText editId;
    private AutoNextEditText editAt;
    private AutoNextEditText editGlucose;
    private AutoNextEditText editCarbohydrates;
    private AutoNextEditText editDose;
    private Button   buttonSave;
    private GlucoseJournalDatabaseHelper dbHandler;
    private GlucoseGraph glucoseGraph;
    private TextView textViewInfusionChange;

    private Handler handler = new Handler();
    private int millisPerPixel = 2*60*1000;
    private int daysDisplayed = 7;
    private Runnable updateGlucoseGraph = new Runnable() {
        public void run() {
            refreshAll();
            handler.postDelayed(this, millisPerPixel);
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        createHeader();

        dbHandler = new GlucoseJournalDatabaseHelper(this);

        initializeControls();

        refreshAll();
    }

    @Override
    public void onResume() {
    super.onResume();

        handler.postDelayed(updateGlucoseGraph, millisPerPixel);
    }

    @Override
    public void onPause() {
    super.onPause();

        handler.removeCallbacks(updateGlucoseGraph);
    }

    private void initializeControls() {
        editId = (EditText) findViewById(R.id.edit_id);
        editAt = (AutoNextEditText) findViewById(R.id.edit_at);
        editGlucose = (AutoNextEditText) findViewById(R.id.edit_glucose);
        editCarbohydrates = (AutoNextEditText) findViewById(R.id.edit_carbohydrates);
        editDose = (AutoNextEditText) findViewById(R.id.edit_dose);
        buttonSave = (Button) findViewById(R.id.button_save);
        glucoseGraph = (GlucoseGraph) findViewById(R.id.graph);

        editAt.nextIfLength = 4;
        editAt.nextIfGreaterThan = 236;

        editGlucose.nextIfLength = 4;
        editGlucose.nextIfDecimals = 1;
        editCarbohydrates.nextIfEqual = "-";
        //editCarbohydrates.nextIfEqual = "0";

        editCarbohydrates.nextIfLength = 3;
        editCarbohydrates.nextIfEqual = "-";
        //editCarbohydrates.nextIfEqual = "0";
        editCarbohydrates.nextIfGreaterThan = 23;

        editDose.nextIfLength = 4;
        editDose.nextIfDecimals = 1;
        editDose.nextIfEqual = "-";
        //editDose.nextIfEqual = "0";

        glucoseGraph.millisPerPixel = millisPerPixel;
    }

    public void saveEntry(View view) {
        if (editId.getText().toString().isEmpty()) {
            JournalEntry entry = new JournalEntry(editAt.getText().toString(), editGlucose.getText().toString(), editCarbohydrates.getText().toString(), editDose.getText().toString());

            dbHandler.addJournalEntry(entry);

            // Return focus to glucose field unless time was filled; only for new records.
            if (editAt.getText().toString().isEmpty())
                editGlucose.requestFocus();
            else
                editAt.requestFocus();
        } else {

            JournalEntry entry = dbHandler.findJournalEntry(Integer.parseInt(editId.getText().toString()));
            entry.setAt(editAt.getText().toString());
            entry.glucose = editGlucose.getText().toString();
            entry.carbohydrates = editCarbohydrates.getText().toString();
            entry.dose = editDose.getText().toString();
            dbHandler.updateJournalEntry(entry);

            editGlucose.requestFocus();
        }

        editId.setText("");
        editAt.setText("");
        editGlucose.setText("");
        editCarbohydrates.setText("");
        editDose.setText("");

        refreshEntries();
    }

    private void editEntry(int id) {
        JournalEntry entry = dbHandler.findJournalEntry(id);
        editId.setText(Integer.toString(entry.id));
        editAt.setText(entry.at.format("%H%M"));
        editGlucose.setText(entry.glucose);
        editCarbohydrates.setText(entry.carbohydrates);
        editAt.requestFocus(); // Start editing "at"; need to come before updating "dose" to prevent auto-save and after updating "at" to keep the focus
        editAt.selectAll();
        editDose.setText(entry.dose);
    }

    private void refreshAll() {
        refreshEntries();
        refreshInfusionChange();
    }

    private void refreshEntries() {
        LinearLayout list_entries = (LinearLayout) findViewById(R.id.list_entries);
        list_entries.removeAllViews();

        journalEntries = dbHandler.getAllJournalEntries(daysAgoInMillis(daysDisplayed));

        JournalEntryLinearLayout lastLayout = null;
        Time now = new Time();
        now.setToNow();
        now.set(now.toMillis(false)+glucoseGraph.futureRange);

        int accumulatedSpace = 0;

        for (JournalEntry entry : journalEntries) {
            lastLayout = addRow(list_entries, entry, accumulatedSpace, now, entry.at.format("%H:%M"), entry.glucose, entry.carbohydrates, entry.dose, entry.id);
            accumulatedSpace += lastLayout.getPaddingTop() + 37; //lastLayout.getHeight();
        }


        glucoseGraph.journalEntries = journalEntries;
        glucoseGraph.topTime = now.toMillis(false);
        // set bottomTime to lastEntry or 24 hours ago
        if (journalEntries != null && !journalEntries.isEmpty()) {
            glucoseGraph.bottomTime =
                journalEntries.get(journalEntries.size()-1).at.toMillis(false);
        } else {
            glucoseGraph.bottomTime = glucoseGraph.topTime + 24*60*60*1000;
        }
        glucoseGraph.invalidate();
    }

    // temporary while evaluating usefulness, if so put in graph
    private void refreshInfusionChange() {
        InfusionChange infusionChange = dbHandler.findLatestInfusionChange();
        if (infusionChange != null) {
            textViewInfusionChange.setText(Integer.toString(infusionChange.hoursAgo()));
            textViewInfusionChange.setTextColor(infusionChange.colorPresentation());
        }
    }


    private void createHeader() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        LinearLayout ll = new LinearLayout(this);
        ll.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        ll.setOrientation(LinearLayout.HORIZONTAL);

        TextView tv1 = new TextView(this);
        tv1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        tv1.setWidth((int)(metrics.widthPixels * 0.15));
        tv1.setText("Time");

        TextView tv2 = new TextView(this);
        tv2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        tv2.setWidth((int)(metrics.widthPixels * 0.15));
        tv2.setText("Gluco");
        tv2.setGravity(Gravity.RIGHT);

        TextView tv3 = new TextView(this);
        tv3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        tv3.setWidth((int)(metrics.widthPixels * 0.15));
        tv3.setText("Carbs");
        tv3.setGravity(Gravity.RIGHT);

        TextView tv4 = new TextView(this);
        tv4.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        tv4.setWidth((int)(metrics.widthPixels * 0.15));
        tv4.setText("Dose");
        tv4.setGravity(Gravity.RIGHT);

        textViewInfusionChange = new TextView(this);
        textViewInfusionChange.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        textViewInfusionChange.setWidth((int)(metrics.widthPixels * 0.30));
        textViewInfusionChange.setText("_");
        textViewInfusionChange.setGravity(Gravity.RIGHT);

        ll.addView(tv1);
        ll.addView(tv2);
        ll.addView(tv3);
        ll.addView(tv4);
        ll.addView(textViewInfusionChange);

        ((LinearLayout) findViewById(R.id.list_entries_header)).addView(ll);
    }

    private JournalEntryLinearLayout addRow(ViewGroup target, JournalEntry entry, int accumulatedSpace, Time startTime, String str1, String str2, String str3, String str4, final int id) {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        JournalEntryLinearLayout ll = new JournalEntryLinearLayout(this);
        ll.journalEntry = entry;
        ll.setId(id);
        ll.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        ll.setOrientation(LinearLayout.HORIZONTAL);
        if (target.getChildCount() == 0)
        {
            ll.setClickable(true);
            ll.setOnClickListener(new OnClickListener() {

                public void onClick(View v) {
                    editEntry(id);
                }
            });

        }

        if (entry != null) {
            int top = Math.max(0, (int)((startTime.toMillis(false)-entry.at.toMillis(false)) / (millisPerPixel)) - 20 - accumulatedSpace)/* - ll.getHeight() == 37*/;
            ll.setPadding(0, top, 0, 0);
        }

        TextView tv1 = new TextView(this);
        tv1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        tv1.setWidth((int)(metrics.widthPixels * 0.15));
        tv1.setText(str1);

        TextView tv2 = new TextView(this);
        tv2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        tv2.setWidth((int)(metrics.widthPixels * 0.15));
        tv2.setText(str2);
        tv2.setGravity(Gravity.RIGHT);

        TextView tv3 = new TextView(this);
        tv3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        tv3.setWidth((int)(metrics.widthPixels * 0.15));
        tv3.setText(str3);
        tv3.setGravity(Gravity.RIGHT);

        TextView tv4 = new TextView(this);
        tv4.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        tv4.setWidth((int)(metrics.widthPixels * 0.15));
        tv4.setText(str4);
        tv4.setGravity(Gravity.RIGHT);

        ll.addView(tv1);
        ll.addView(tv2);
        ll.addView(tv3);
        ll.addView(tv4);

        target.addView(ll);
        registerForContextMenu(ll);

        return ll;
    }

    private void createInfusionChange() {
        String time = "";
        // if not editing and time is entered
        if (editId.getText().toString().isEmpty() &&
                !editAt.getText().toString().isEmpty()) {
            time = editAt.getText().toString();
            editAt.setText("");
            editGlucose.requestFocus();
        }
        InfusionChange infusionChange = new InfusionChange(time, "");

        dbHandler.addInfusionChange(infusionChange);
        refreshInfusionChange();
    }

    private void toggle_days_displayed() {
        if (daysDisplayed == 7) {
            daysDisplayed = 90;
        } else {
            daysDisplayed = 7;
        }
        refreshEntries();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        //MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.entry_context_menu, menu);
        JournalEntry entry = ((JournalEntryLinearLayout)v).journalEntry;
        menu.setHeaderTitle(entry.at.format("%Y-%m-%d %H:%M"));
        menu.add(0, v.getId(), 0, "Edit");
        //menu.add(0, v.getId(), 1, "Change Date");
        menu.add(0, v.getId(), 2, "Delete");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        //AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        //switch (item.getItemId()) {
        //case R.id.edit:
        if (item.getTitle() == "Edit") {
            editEntry(item.getItemId());
            return true;
        }
        else if (item.getTitle() == "Change Date") {
            //changeDateNote(info.id);
            return true;
        }
        else if (item.getTitle() == "Delete") {
            dbHandler.deleteJournalEntry(item.getItemId());
            refreshEntries();
            return true;
        }
        else {
            return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_toggle_days_displayed:
                toggle_days_displayed();
                return true;
            case R.id.action_infusion_changed:
                createInfusionChange();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Missing framework function, to easily check if char is in string at a position.
    // Position can of course be negative, -1 equals last char and -2 is next to last.
    // No exceptions, just true or false. Index out of boundaries will return false.
    protected static boolean charAtIs(String s, int position, char c) {
        if(position < 0) {
            position = s.length() - (position * -1);
        }
        if(position < 0 || position >= s.length()) {
            return false;
        } else {
            return s.charAt(position) == c;
        }
    }
    protected static boolean charAtIs(Editable s, int position, char c) {
        return charAtIs(s.toString(), position, c);
    }

    public static boolean isInteger(String number) {
        try {
            Integer.parseInt(number);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isFloat(String number) {
        try {
            Float.parseFloat(number);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static long daysAgoInMillis(int days) {
        long DAY_IN_MS = 1000 * 60 * 60 * 24;
        Time now = new Time();
        now.setToNow();
        return now.toMillis(false) - (days * DAY_IN_MS);
    }
}
