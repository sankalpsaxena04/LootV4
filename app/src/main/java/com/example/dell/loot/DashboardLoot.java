package com.example.dell.loot;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.luseen.spacenavigation.SpaceItem;
import com.luseen.spacenavigation.SpaceNavigationView;
import com.luseen.spacenavigation.SpaceOnClickListener;

import java.util.HashMap;
import java.util.Map;

public class DashboardLoot extends AppCompatActivity {


    BottomNavigationView bottomNavigationView;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_loot);
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar2);

        db= FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        bottomNavigationView=findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id=item.getItemId();
                switch (id)
                {
                    case R.id.navigation_duel:

                        loadFragment(new Duel(),"duel");
//
                        break;
                    case R.id.navigation_current_mission:
                        Toast.makeText(DashboardLoot.this,"onCentreButtonClick", Toast.LENGTH_SHORT).show();

                        loadFragment(new Missions(),"missions");
                        break;
                    case R.id.navigation_leaderboard:

                        loadFragment(new LeaderBoard(),"leaderboard");
                        break;
                }
                return true;
            }
        });
//        bottomNavigationView.getMenu().getItem(1).setChecked(true);
//        Fragment fragment=new Missions();
//        loadFragment(fragment,"missions");
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateFirebase(mAuth.getCurrentUser(),true);
        bottomNavigationView.getMenu().getItem(1).setChecked(true);
        Fragment fragment=new Missions();
        loadFragment(fragment,"missions");
    }

    private void loadFragment(Fragment fragment, String tag) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment,tag);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBackPressed() {
//
        int size=getSupportFragmentManager().getFragments().size();

        String fragmentTag=getSupportFragmentManager().getFragments().get(size-1).getTag();
        Log.i("Fragment",fragmentTag);


        if(fragmentTag.equals("duel")||fragmentTag.equals("leaderboard")||fragmentTag.equals("current_mission")) {

            bottomNavigationView.getMenu().getItem(1).setChecked(true);
            android.support.v4.app.Fragment fragment=new Missions();
            loadFragment(fragment,"missions");

        }
        else if(fragmentTag.equals("online_users"))
        {
            bottomNavigationView.getMenu().getItem(0).setChecked(true);
            loadFragment(new Duel(),"duel");
        }
        else if (fragmentTag.equals("missions"))
        {
            finishAffinity();
        }
        else if(fragmentTag.equals("about")||fragmentTag.equals("how_to")||fragmentTag.equals("help")||fragmentTag.equals("contact_us"))
        {
            bottomNavigationView.getMenu().getItem(1).setChecked(true);
            android.support.v4.app.Fragment fragment=new Missions();
            loadFragment(fragment,"missions");
        }

        else {
            super.onBackPressed();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CurrentMission.LOCATION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "GPS enabled!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "GPS disabled!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onStop() {
        super.onStop();

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onPause() {

        if(mAuth.getCurrentUser()!=null)
            updateFirebase(mAuth.getCurrentUser(),false);
        else
            finishAffinity();
        super.onPause();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.popup_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.item_stats:
                loadFragment(new Stats(),"stats");
                break;

            case R.id.item_howTo:
                loadFragment(new HowTo(),"how_to");
                break;
            case R.id.item_help:
                loadFragment(new Help(),"help");
                break;
            case R.id.pop_logout:
                updateFirebase(mAuth.getCurrentUser(),false);
                mAuth.signOut();
                Intent intent=new Intent(getApplicationContext(), Main3Activity.class);
                startActivity(intent);
                break;
            default:
        }
        return super.onOptionsItemSelected(menuItem);
    }
    public void updateFirebase(FirebaseUser firebaseUser,boolean state)
    {
        Map<String, Object> user = new HashMap<>();
        user.put("userID", firebaseUser.getUid());
        user.put("online", state);

        db.collection("users").document(firebaseUser.getUid())
                .set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful())
                {
                    Log.i("Added Succesfully","");
                }
                else
                {
                    Log.i("Error",task.getException().getMessage());
                }
            }
        });

    }
}
