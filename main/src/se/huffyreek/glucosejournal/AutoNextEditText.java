package se.huffyreek.glucosejournal;

import android.content.Context;
import android.util.AttributeSet;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;

/* EditText that automatically moves to next field
 * at certain conditions. */
class AutoNextEditText extends EditText {

    // NOTE: Integer can be null, in contrast to int
    // Ignores condition if null
    // Activate next view if length euquals or greater than, and so on
    public Integer nextIfLength;
    public Integer nextIfDecimals;
    public Integer nextIfGreaterThan;
    public String nextIfEqual;

    // Defaults via autosearch
    public View previousView;
    public View nextView;

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
        previousView = this.focusSearch(View.FOCUS_BACKWARD);
        nextView     = this.focusSearch(View.FOCUS_FORWARD);
    }

    private void setupWatchers() {
        addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                //if (inputOracle.check(s)) { activateView(nextView); }
                if((nextIfLength != null && s.length() >= nextIfLength) ||
                   (nextIfGreaterThan != null && (MainActivity.isInteger(s.toString()) && Integer.parseInt(s.toString()) > nextIfGreaterThan)) ||
                   (nextIfDecimals != null && MainActivity.charAtIs(s, nextIfDecimals*-1-1, '.')) ||
                   (nextIfEqual != null && nextIfEqual == s.toString())) {
                    activateView(nextView);
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

    // should ignore null view
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
