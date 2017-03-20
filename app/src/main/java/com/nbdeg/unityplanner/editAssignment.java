package com.nbdeg.unityplanner;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.nbdeg.unityplanner.data.Assignments;
import com.nbdeg.unityplanner.data.Classes;
import com.nbdeg.unityplanner.utils.Database;
import com.nbdeg.unityplanner.utils.EditTextDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class editAssignment extends AppCompatActivity  {

    private EditText mAssignmentName;
    private EditText mDueDate;
    private EditText mExtraInfo;
    private Spinner mDueClass;
    private ArrayList<String> classListNames = new ArrayList<>();
    private EditTextDatePicker datePicker;

    private int percentComplete = 0;
    private String assignmentName;
    private String assignmentClass;
    private String assignmentExtra;
    private Date assignmentDueDate;
    final String oldAssignmentID = getIntent().getStringExtra("OldAssignmentID");

    Database db = new Database();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_assignment);

        db.assignmentDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    if (Objects.equals(userSnapshot.getKey(), oldAssignmentID)) {
                        assignmentName = userSnapshot.getValue(Assignments.class).getAssignmentName();
                        assignmentClass = userSnapshot.getValue(Assignments.class).getAssignmentClassName();
                        assignmentExtra = userSnapshot.getValue(Assignments.class).getAssignmentExtra();
                        assignmentDueDate = new Date(userSnapshot.getValue(Assignments.class).getAssignmentDueDate());
                        percentComplete = userSnapshot.getValue(Assignments.class).getAssignmentPercent();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Database", databaseError.getMessage());
            }
        });

        // Find view by ID calls
        mAssignmentName = (EditText) findViewById(R.id.assignment_edit_name);
        mDueDate = (EditText) findViewById(R.id.due_date_edit_edittext);
        mExtraInfo = (EditText) findViewById(R.id.extra_homework_info_edit);
        mDueClass = (Spinner) findViewById(R.id.class_edit_spinner);
        SeekBar mPercentComplete = (SeekBar) findViewById(R.id.percent_complete_edit);

        // Sets DueDate EditText to open a datepicker when clicked
        datePicker = new EditTextDatePicker(this, R.id.due_date_edittext);

        mPercentComplete.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                percentComplete = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        DatabaseReference classDb = db.classDb;
        classDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                classListNames.clear();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    Classes mClass = userSnapshot.getValue(Classes.class);
                    classListNames.add(mClass.getClassName());
                    Log.i("Database", "Class loaded: " + mClass.getClassName());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(editAssignment.this, R.layout.spinner_layout, classListNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mDueClass.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Database", "Error loading classes: " + databaseError.getMessage());
            }
        });

        // Set Existing Data
        mAssignmentName.setText(assignmentName);
        SimpleDateFormat formatter = new SimpleDateFormat("d MMMM, yyyy");
        mDueDate.setText(formatter.format(assignmentDueDate));
        mExtraInfo.setText(assignmentExtra);
        mDueClass.setSelection(classListNames.indexOf(assignmentClass));
        mPercentComplete.setProgress(percentComplete);
    }

    // Adds a SAVE button to the Action Bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save, menu);
        return true;
    }

    // Gets and saves information when SAVE is clicked

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Getting information from views
        Long dueDate = datePicker.date.getTime();
        String assignmentName = mAssignmentName.getText().toString();
        String extraInfo = mExtraInfo.getText().toString();
        String dueClass = mDueClass.getItemAtPosition(mDueClass.getSelectedItemPosition()).toString();

        db.editAssignment(oldAssignmentID, new Assignments(assignmentName, dueClass, dueDate, extraInfo, percentComplete));

        // Bring user back to MainActivity
        startActivity(new Intent(editAssignment.this, MainActivity.class));

        return super.onOptionsItemSelected(item);
    }
}