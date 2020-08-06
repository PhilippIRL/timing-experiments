package de.ppluss.timingexperiments;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    private SeekBar fireBar, waitBar;
    private ToggleButton loopButton;
    private TextView fireLabel, waitLabel, modeLabel;
    private Spinner modeSelect;
    private Button singleRunButton;

    private final int maxValue = 1000;

    private int waitTime = 100, fireTime = 100, mode = 0;

    private WorkerThread workerThread = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.fireBar = findViewById(R.id.fireBar);
        this.waitBar = findViewById(R.id.waitBar);
        this.loopButton = findViewById(R.id.loopButton);
        this.fireLabel = findViewById(R.id.fireLabel);
        this.waitLabel = findViewById(R.id.waitLabel);
        this.modeLabel = findViewById(R.id.modeLabel);
        this.modeSelect = findViewById(R.id.modeSelect);
        this.singleRunButton = findViewById(R.id.singleRunButton);

        waitBar.setMax(maxValue);
        fireBar.setMax(maxValue);

        waitBar.setProgress(waitTime);
        fireBar.setProgress(fireTime);
        modeSelect.setSelection(mode);
        modeSelect.setEnabled(true);
        singleRunButton.setEnabled(true);

        updateFireLabel();
        updateWaitLabel();
        updateModeLabel();

        SeekBar.OnSeekBarChangeListener changeListener = new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(seekBar.equals(waitBar)) {
                    waitTime = progress;
                    if(workerThread != null) {
                        workerThread.waitTime = progress;
                    }
                    updateWaitLabel();
                } else if(seekBar.equals(fireBar)) {
                    fireTime = progress;
                    if(workerThread != null) {
                        workerThread.fireTime = progress;
                    }
                    updateFireLabel();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}

        };

        waitBar.setOnSeekBarChangeListener(changeListener);
        fireBar.setOnSeekBarChangeListener(changeListener);

        modeSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mode = position;
                workerThread.mode = position;
                updateModeLabel();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

        workerThread = new WorkerThread(this);
        workerThread.start();

        loopButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    modeSelect.setEnabled(false);
                    singleRunButton.setEnabled(false);

                    workerThread.mode = mode;
                    workerThread.fireTime = fireTime;
                    workerThread.waitTime = waitTime;

                    workerThread.shouldWork = true;

                } else {

                    workerThread.shouldWork = false;
                    workerThread.stopEverything();

                    modeSelect.setEnabled(true);
                    singleRunButton.setEnabled(true);
                }
            }

        });

        singleRunButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                workerThread.mode = mode;
                workerThread.fireTime = fireTime;
                workerThread.waitTime = waitTime;

                workerThread.singleRun = true;
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.warningTitle)
                .setMessage(R.string.warningText)
                .setPositiveButton(R.string.warningDismiss, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();

    }

    private void updateModeLabel() {
        String[] modesInText = getResources().getStringArray(R.array.modesInText);
        modeLabel.setText(getString(R.string.modeText, modesInText[this.mode]));
    }

    private void updateFireLabel() {
        fireLabel.setText(getString(R.string.fireText, fireTime));
    }

    private void updateWaitLabel() {
        waitLabel.setText(getString(R.string.waitText, waitTime));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        workerThread.shouldWork = false;
        workerThread.stopEverything();
        workerThread.interrupt();
    }
}
