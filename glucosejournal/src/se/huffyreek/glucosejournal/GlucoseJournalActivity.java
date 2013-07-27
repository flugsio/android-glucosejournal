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
import android.util.TypedValue;
import android.view.FocusFinder;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GlucoseJournalActivity extends Activity {
	private static final String TAG = "GlucoseJournalActivity";
	
	private List<JournalEntry> journalEntries;
	private EditText editAt;
	private EditText editGlucose;
	private EditText editCarbohydrates;
	private EditText editDose;
	private Button   buttonSave;
	private GlucoseJournalDatabaseHelper dbHandler;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        dbHandler = new GlucoseJournalDatabaseHelper(this);

        
        initializeControls();

        refreshEntries();
    }
    
    private void initializeControls() {
    	editAt = (EditText) findViewById(R.id.edit_at);
    	editGlucose = (EditText) findViewById(R.id.edit_glucose);
    	editCarbohydrates = (EditText) findViewById(R.id.edit_carbohydrates);
    	editDose = (EditText) findViewById(R.id.edit_dose);
    	buttonSave = (Button) findViewById(R.id.button_save);
    	
    	editGlucose.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				if(s.length() >= 4 || charAtIs(s, -2, '.')) {
					editCarbohydrates.requestFocus();
				}
			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
    	});
    	
    	editCarbohydrates.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				if(s.length() >= 3) {
					editDose.requestFocus();
				}
			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
    	});
    	
    	editDose.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				if(s.length() >= 4 || charAtIs(s, -2, '.')) {
					buttonSave.callOnClick();
				}
			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
    	});
    }
    
    public void saveEntry(View view) {
    	JournalEntry entry = new JournalEntry(editAt.getText().toString(), editGlucose.getText().toString(), editCarbohydrates.getText().toString(), editDose.getText().toString());

    	dbHandler.addJournalEntry(entry);
    	
    	// Return focus to glucose field unless time was filled; only for new records.
    	if (editAt.getText().toString().isEmpty())
    		editGlucose.requestFocus();
    	else
    		editAt.requestFocus();
    	
    	editAt.setText("");
    	editGlucose.setText("");
    	editCarbohydrates.setText("");
    	editDose.setText("");
    	
    	refreshEntries();
    	
    	editGlucose.requestFocus();
    }
    
    private void refreshEntries() {
    	LinearLayout list_entries = (LinearLayout) findViewById(R.id.list_entries);
    	list_entries.removeAllViews();
    	
    	journalEntries = dbHandler.getAllJournalEntries();
    	
    	for (JournalEntry entry : journalEntries) {
    		addRow(list_entries, entry.at.format("%H:%M"), entry.glucose, entry.carbohydrates, entry.dose);
    	}
    }
    
    private void addRow(ViewGroup target, String str1, String str2, String str3, String str4) {
    	DisplayMetrics metrics = new DisplayMetrics();
    	getWindowManager().getDefaultDisplay().getMetrics(metrics);
    	
		LinearLayout ll = new LinearLayout(this);
		ll.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		ll.setOrientation(LinearLayout.HORIZONTAL);
		
		TextView tv1 = new TextView(this);
		tv1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		tv1.setWidth((int)(metrics.widthPixels * 0.25));
		tv1.setText(str1);
		
		TextView tv2 = new TextView(this);
		tv2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		tv2.setWidth((int)(metrics.widthPixels * 0.30));
		tv2.setText(str2);
		
		TextView tv3 = new TextView(this);
		tv3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		tv3.setWidth((int)(metrics.widthPixels * 0.25));
		tv3.setText(str3);
		
		TextView tv4 = new TextView(this);
		tv4.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		tv4.setWidth((int)(metrics.widthPixels * 0.20));
		tv4.setText(str4);
		
		ll.addView(tv1);
		ll.addView(tv2);
		ll.addView(tv3);
		ll.addView(tv4);
		
		target.addView(ll);
    }


    // Missing framework function, to easily check if char is in string at a position.
    // Position can of course be negative, -1 equals last char and -2 is next to last.
    // No exceptions, just true or false. Index out of boundaries will return false.
    private boolean charAtIs(String s, int position, char c) {
    	if(position < 0) {
    		position = s.length() - (position * -1);
    	}
    	if(position < 0 || position >= s.length()) {
    		return false;
    	} else {
    		return s.charAt(position) == c;
    	}
    }
    private boolean charAtIs(Editable s, int position, char c) {
    	return charAtIs(s.toString(), position, c);
    }
}
