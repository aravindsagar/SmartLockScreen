package com.pvsagar.smartlockscreen;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sagar.lockpattern_gridview.PatternGridView;
import com.sagar.lockpattern_gridview.PatternInterface;

import java.util.ArrayList;
import java.util.List;


public class StorePattern extends Activity {
    private static final String PACKAGE_NAME = StorePattern.class.getPackage().getName();
    public static final String EXTRA_PATTERN = PACKAGE_NAME + ".EXTRA_PATTERN";
    private static final int COLOR_VALID_PATTERN = Color.rgb(70, 170, 60);
    private static final int COLOR_INVALID_PATTERN = Color.rgb(200, 70, 40);

    PatternGridView mPatternGridView;
    TextView mStatusView;
    Button mContinueButton, mResetButton;
    List<Integer> mPattern;

    private enum PatternEntryState {FIRST_TIME, CONFIRMATION}
    PatternEntryState mState = PatternEntryState.FIRST_TIME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_pattern);
        mStatusView = (TextView) findViewById(R.id.text_view_status);
        mStatusView.setText("Draw a pattern.");

        mContinueButton = (Button) findViewById(R.id.button_confirm);
        mContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mState == PatternEntryState.FIRST_TIME){
                    mState = PatternEntryState.CONFIRMATION;
                    mPatternGridView.clearPattern();
                    mContinueButton.setText(getString(R.string.pattern_confirm));
                    mStatusView.setText("Draw pattern again to confirm");
                } else {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(EXTRA_PATTERN, (ArrayList) mPattern);
                    setResult(RESULT_OK, returnIntent);
                    finish();
                }
            }
        });
        mResetButton = (Button) findViewById(R.id.button_reset);
        mResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPatternGridView.clearPattern();
            }
        });

        mPatternGridView = (PatternGridView) findViewById(R.id.pattern_view_store);
        mPatternGridView.setPatternListener(new PatternInterface.PatternListener() {
            @Override
            public void onPatternEntered(List<Integer> pattern) {
                if (pattern.size() < 4) {
                    mStatusView.setText("Connect at least 4 dots");
                    mPatternGridView.setRingColor(COLOR_INVALID_PATTERN);
                    mContinueButton.setEnabled(false);
                } else if(mState == PatternEntryState.FIRST_TIME){
                    mStatusView.setText("Pattern Recorded");
                    mContinueButton.setEnabled(true);
                    mPatternGridView.setRingColor(COLOR_VALID_PATTERN);
                    mPattern = pattern;
                } else {
                    if(mPattern.equals(pattern)){
                        mPatternGridView.setInputEnabled(false);
                        mContinueButton.setEnabled(true);
                        mResetButton.setEnabled(false);
                        mPatternGridView.setRingColor(COLOR_VALID_PATTERN);
                        mStatusView.setText("Your new pattern");
                    } else {
                        mContinueButton.setEnabled(false);
                        mStatusView.setText("Try again");
                        mPatternGridView.setRingColor(COLOR_INVALID_PATTERN);
                    }
                }
            }

            @Override
            public void onPatternStarted() {
                mStatusView.setText("Release finger when done");
                mContinueButton.setEnabled(false);
            }

            @Override
            public void onPatternCleared() {
                mContinueButton.setEnabled(false);
            }
        });
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }
}
