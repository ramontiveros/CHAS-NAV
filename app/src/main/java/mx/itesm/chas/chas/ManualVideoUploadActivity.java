package mx.itesm.chas.chas;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;

public class ManualVideoUploadActivity extends AuthenticatedActivity implements DialogInterface.OnDismissListener {
    public static String pickedDate = null;
    private static final int SELECT_VIDEO = 1;
    private Uri selectedVideo;
    private String selectedVideoPath;
    UploadTask videoUploadTask;
    Toast incompleteFormToast;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_manual_video_upload);

        super.onCreate(savedInstanceState);

        incompleteFormToast = Toast.makeText(getApplicationContext(), "Por favor llena los datos antes de subir el video.", Toast.LENGTH_LONG);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void pickVideo(View view) {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, SELECT_VIDEO);
    }

    @ Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_VIDEO) {
                selectedVideo = data.getData();
                selectedVideoPath = selectedVideo.getLastPathSegment() + ".mp4";
                TextView fileName = (TextView) findViewById(R.id.video_name);
                fileName.setText(selectedVideoPath);
            }
        }
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new ManualVideoUploadActivity.DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        if(pickedDate != null) {
            EditText dateText = (EditText)findViewById(R.id.dates);
            dateText.setText(pickedDate);
        }
    }

    public void uploadVideo(View view) {
        if(selectedVideo != null) {
            final Button uploadButton = (Button)findViewById(R.id.video_upload_button);
            final ProgressBar progressBar = (ProgressBar)findViewById(R.id.video_upload_progressbar);
            final EditText videoTitle = (EditText)findViewById(R.id.title),
                    videoDate = (EditText)findViewById(R.id.dates);
            final TextView fileName = (TextView) findViewById(R.id.video_name);
            String title = videoTitle.getText().toString(),
                    date = videoDate.getText().toString();

            progressBar.setVisibility(View.VISIBLE);
            uploadButton.setEnabled(false);

            videoUploadTask = VideoUploader.uploadVideo(selectedVideo, title, date, getApplicationContext());

            videoUploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    uploadButton.setEnabled(true);
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    uploadButton.setEnabled(true);
                    progressBar.setVisibility(View.INVISIBLE);
                    videoTitle.setText("");
                    videoDate.setText("");
                    fileName.setText("");
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    progressBar.setProgress((int) progress);
                }
            });
        } else {
            incompleteFormToast.show();
        }
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            pickedDate = year + "-" + month + "-" + day;
        }

        @Override
        public void onDismiss(final DialogInterface dialog) {
            super.onDismiss(dialog);
            final Activity activity = getActivity();
            if (activity instanceof DialogInterface.OnDismissListener) {
                ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
            }
        }
    }
}
