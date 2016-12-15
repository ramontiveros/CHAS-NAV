package mx.itesm.chas.chas;

import android.app.Activity;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
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

/**
 * Created by Chris on 12/12/2016.
 */

public class VideoUploader {
    static Toast videoUploadStartToast, videoUploadedToast, videoFailToast;

    static FirebaseStorage storage = FirebaseStorage.getInstance();
    static FirebaseDatabase database = FirebaseDatabase.getInstance();

    public static UploadTask uploadVideo(Uri videoUri, Context ctx) {
        Calendar cal = Calendar.getInstance();
        String date = cal.get(Calendar.YEAR) + "-" + cal.get(Calendar.MONTH) + "-" + cal.get(Calendar.DAY_OF_MONTH),
                title = "Video grabado el " + date;
        return VideoUploader.uploadVideo(videoUri, title, date, ctx);
    }

    public static UploadTask uploadVideo(Uri videoUri, String videoTitle, String videoDate, Context ctx) {
        String duration, key, rawDuration;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        UploadTask videoUploadTask;

        long millisecondDuration,
                secondDuration,
                minuteDuration;

        videoUploadStartToast = Toast.makeText(ctx, "Subiendo Video.", Toast.LENGTH_LONG);
        videoUploadedToast = Toast.makeText(ctx, "Video subido satisfactoriamente.", Toast.LENGTH_SHORT);
        videoFailToast = Toast.makeText(ctx, "Error al subir video.", Toast.LENGTH_SHORT);

        retriever.setDataSource(ctx, videoUri);
        rawDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

        if(rawDuration != null) {
            millisecondDuration = Long.parseLong(rawDuration);
            secondDuration = millisecondDuration / 1000;
            minuteDuration = millisecondDuration / 1000 / 60;
            duration = String.format("%1$01d:%2$01d:%3$01d", millisecondDuration / 1000 / 60 / 60, minuteDuration < 10? "0" + minuteDuration:minuteDuration, secondDuration < 10? "0" + secondDuration:secondDuration);
        } else {
            duration = "0:00:00";
            Log.d("VideoUploader", "Failed to retrieve video duration");
        }

        //TODO: REPLACE MATCH0 WITH ACTUAL MATCH ID
        Video videoData = new Video(videoTitle, duration, videoDate, "match0");

        StorageReference storageRef = storage.getReferenceFromUrl("gs://chas-itesm.appspot.com/");
        DatabaseReference databaseRef = database.getReference();
        key = databaseRef.child("videos").push().getKey();
        database.getReference().child("videos/" + key).setValue(videoData);
        videoUploadTask = storageRef.child("videos/" + key + ".mp4").putFile(videoUri);

        videoUploadStartToast.show();

        videoUploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                videoFailToast.show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                videoUploadedToast.show();
            }
        });

        return videoUploadTask;

    }
}
