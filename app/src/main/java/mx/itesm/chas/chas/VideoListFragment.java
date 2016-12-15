package mx.itesm.chas.chas;

import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by Chris on 12/14/2016.
 */

public class VideoListFragment extends ListFragment {

    DatabaseReference mDatabase;

    ArrayList<String> videoIds,
            videoNames;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("videos");

        ValueEventListener videoListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                videoNames = new ArrayList<>();
                videoIds = new ArrayList<>();
                String targetMatchId = getArguments().getString("matchId");
                for(DataSnapshot videoSnapshot : dataSnapshot.getChildren()) {
                    Video n = videoSnapshot.getValue(Video.class);
                    if(n.matchId.equals(targetMatchId)) {
                        videoNames.add(n.title + " | " + n.date);
                        videoIds.add(videoSnapshot.getKey());
                    }
                }
                setListAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, videoNames));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("VideoListener", "loadPost:onCancelled", databaseError.toException());
            }
        };

        mDatabase.addValueEventListener(videoListener);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Log.d("ListItemClick", l.toString());
        Log.d("ListItemClick", v.toString());
        Log.d("ListItemClick", position + "");
        Log.d("ListItemClick", id + "");
    }

    public static VideoListFragment newInstance(String matchId) {
        VideoListFragment f = new VideoListFragment();

        Bundle args = new Bundle();
        args.putString("matchId", matchId);
        f.setArguments(args);

        return f;
    }
}
