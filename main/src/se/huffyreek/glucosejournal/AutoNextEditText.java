package se.huffyreek.glucosejournal;

import android.content.Context;
import android.util.AttributeSet;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;

class AutoNextEditText extends EditText {

    // NOTE: Integer can be null, in contrast to int
    // Activate next view if length euquals or greater than, and so on
    public Integer nextIfLength;
    public Integer nextIfDecimals;
    public Integer nextIfGreaterThan;
    public String nextIfEqual;

    public AutoNextEditText (Context context) {
        super(context);
        init();
    }

    public AutoNextEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AutoNextEditText(Context context, AttributeSet attrs,
                            int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setupWatchers();
    }

    private void setupWatchers() {
        addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if((nextIfLength != null && s.length() >= nextIfLength) ||
                   (nextIfGreaterThan != null && (GlucoseJournalActivity.isInteger(s.toString()) && Integer.parseInt(s.toString()) > nextIfGreaterThan)) ||
                   (nextIfDecimals != null && GlucoseJournalActivity.charAtIs(s, nextIfDecimals*-1-1, '.')) ||
                   (nextIfEqual != null && nextIfEqual == s.toString())) {
                    activateNextView();
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {
            }
            public void onTextChanged(CharSequence s, int start, int before,
                int count) {
            }
        });
    }

    private void activateNextView() {
        View nextView = this.focusSearch(View.FOCUS_FORWARD);
        if(nextView != null) {
            activateView(nextView);
        }
    }

    private void activatePreviousView() {
        View previousView = this.focusSearch(View.FOCUS_BACKWARD);
        if(previousView != null) {
            activateView(previousView);
        }
    }

    protected void activateView(View view) {
        // TODO: don't use instanceof?
        if(view instanceof EditText || view instanceof AutoNextEditText) {
            ((EditText)view).requestFocus();
            ((EditText)view).selectAll();
        }
        else if(view instanceof Button) {
            ((Button)view).callOnClick();
        }
        else {
            // TODO: log/raise
        }
    }
}
