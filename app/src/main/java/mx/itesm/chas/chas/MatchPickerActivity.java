package mx.itesm.chas.chas;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.SubMenu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class MatchPickerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        MatchListFragment.OnMatchSelectedListener {

    private static final String TAG = "MatchPicker - UAS: ";

    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    DatabaseReference mDatabase;

    HashMap<MenuItem, String> menuKeys;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_picker);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    startLoginActivity();
                }
            }
        };

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.create_game_fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                Snackbar.make(view, "Not Implemented yet!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                */
                //TODO: SET APPROPIATE ACTION DEPENDING ON LOADED FRAGMENT (NEW MATCH OR NEW VIDEO)
                //TODO: USE MANUAL UPLOAD FRAGMENT INSTEAD OF ACTIVITY
                //TODO: SENT ACTIVE MATCH TO VIDEO UPLOAD
                startVideoUploadActivity();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        populateMenuWithTeams();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.match_picker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings: return true;
            default: return super.onOptionsItemSelected(item);
        }


    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_logout:
                mAuth.signOut();
                break;
            default:
                // A team was picked
                Log.d("Team Selected:", item.toString() + " " + menuKeys.get(item));
                loadTeamMatches(menuKeys.get(item));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    void startVideoUploadActivity() {
        Intent intent = new Intent(this, ManualVideoUploadActivity.class);
        startActivity(intent);
    }

    void populateMenuWithTeams() {
        DatabaseReference teamReference = mDatabase.child("teams");
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu mainMenu = navigationView.getMenu();
        MenuItem teamList = mainMenu.getItem(0);
        final Menu teamsMenu = teamList.getSubMenu();

        ValueEventListener teamListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                menuKeys = new HashMap<>();
                for(DataSnapshot teamSnapshot : dataSnapshot.getChildren()) {
                    Team t = teamSnapshot.getValue(Team.class);
                    MenuItem team = teamsMenu.add(t.name);
                    team.setIcon(R.drawable.ic_games_black_48dp);
                    menuKeys.put(team, teamSnapshot.getKey());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };

        teamReference.addValueEventListener(teamListener);
    }

    public void onMatchSelected(String matchId, String matchName) {
        loadMatchVideos(matchId, matchName);
    }

    void loadMatchVideos(String matchId, String matchName) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        VideoListFragment vlf = VideoListFragment.newInstance(matchId);

        fragmentTransaction.replace(R.id.match_picker_fragment_container, vlf);
        fragmentTransaction.addToBackStack(null);

        setTitle(matchName);

        fragmentTransaction.commit();
    }

    void loadTeamMatches(String teamId) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        MatchListFragment mlf = MatchListFragment.newInstance(teamId);

        fragmentTransaction.replace(R.id.match_picker_fragment_container, mlf);

        fragmentTransaction.commit();
    }
}
