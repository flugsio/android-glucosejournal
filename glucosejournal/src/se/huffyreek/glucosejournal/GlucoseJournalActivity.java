package se.huffyreek.glucosejournal;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.FocusFinder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GlucoseJournalActivity extends Activity {
	
	private List<JournalEntry> journalEntries;
	private EditText editAt;
	private EditText editGlucose;
	private EditText editCarbohydrates;
	private EditText editDose;
	private Button   buttonSave;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        journalEntries = new ArrayList<JournalEntry>();
        refreshEntries();
        
        initializeControls();
    }
    
    private void initializeControls() {
    	editAt = (EditText) findViewById(R.id.edit_at);
    	editGlucose = (EditText) findViewById(R.id.edit_glucose);
    	editCarbohydrates = (EditText) findViewById(R.id.edit_carbohydrates);
    	editDose = (EditText) findViewById(R.id.edit_dose);
    	buttonSave = (Button) findViewById(R.id.button_save);
    	
    	editGlucose.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				if(s.length() >= 4) {
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
				if(s.length() >= 4 || (s.length() >= 2 && (s.charAt(s.length()-2) == '.'))) {
					buttonSave.callOnClick();
				}
			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
    	});
    }
    
    public void saveEntry(View view) {
    	JournalEntry entry = new JournalEntry(editAt.getText().toString(), editGlucose.getText().toString(), editCarbohydrates.getText().toString(), editDose.getText().toString());
    	journalEntries.add(entry);
    	
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
    	
    	for (JournalEntry entry : journalEntries) {
    		TextView tv = new TextView(this);
    		tv.setText(entry.toString());
    		list_entries.addView(tv);
    	}
    }
}
