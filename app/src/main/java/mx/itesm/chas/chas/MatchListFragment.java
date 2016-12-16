package mx.itesm.chas.chas;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Chris on 12/14/2016.
 */

public class MatchListFragment extends ListFragment {
    OnMatchSelectedListener matchSelectedListener;

    DatabaseReference mDatabase;

    ArrayList<String> matchIds,
        matchNames;

    public static MatchListFragment newInstance(String teamId) {
        MatchListFragment f = new MatchListFragment();

        Bundle args = new Bundle();
        args.putString("teamId", teamId);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("matches");

        ValueEventListener matchListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                matchNames = new ArrayList<>();
                matchIds = new ArrayList<>();
                String targetTeamId = getArguments().getString("teamId");
                for(DataSnapshot matchSnapshot : dataSnapshot.getChildren()) {
                    Match n = matchSnapshot.getValue(Match.class);
                    if(n.teams.containsKey(targetTeamId)) {
                        matchNames.add(n.name + " | " + n.date);
                        matchIds.add(matchSnapshot.getKey());
                    }
                }
                setListAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, matchNames));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("MatchListener", "loadPost:onCancelled", databaseError.toException());
            }
        };

        mDatabase.addValueEventListener(matchListener);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            matchSelectedListener = (OnMatchSelectedListener)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnMatchSelectedListener");
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle("Partidos");
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Log.d("ListItemClick", matchNames.get(position));
        Log.d("ListItemClick", matchIds.get(position));
        matchSelectedListener.onMatchSelected(matchIds.get(position), matchNames.get(position));
    }

    public interface OnMatchSelectedListener {
        public void onMatchSelected(String matchId, String matchName);
    }

}
