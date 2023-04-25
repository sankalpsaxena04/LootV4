package com.hackncs.zealicon.loot;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hackncs.zealicon.loot.databinding.ActivityDashboardLootBinding;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class DashboardLoot extends AppCompatActivity implements ClickCallback {


    BottomNavigationView bottomNavigationView;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    MediaPlayer mediaPlayer;
    TextView action_bar_usercoins, title_bar;
    ImageButton vol;
    final static String t1 = "DUEL", t2 = "Missions", t3 = "Leaderboard";
    Fragment fDuel , fMission , fLeaderboard;
    boolean mute = false;
    ActivityDashboardLootBinding binding;
    int score, avatarID;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideNavigationMenu();
        binding = ActivityDashboardLootBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar2);

        vol = findViewById(R.id.volume);
        title_bar = findViewById(R.id.playername);
        db= FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        setSupportActionBar(toolbar);
        fDuel = new Duel();
        fMission = new Missions();
        fLeaderboard = new LeaderBoard();

        getSupportActionBar().setDisplayShowTitleEnabled(false);
      //  TextView action_bar_username=findViewById(R.id.user_name);
        action_bar_usercoins=findViewById(R.id.user_coins);
       // ImageView action_bar_useravatar=findViewById(R.id.avatar);

        SharedPreferences  sharedPreferences = getSharedPreferences("LootPrefs", Context.MODE_PRIVATE);
         score = sharedPreferences.getInt("com.hackncs.score", 0);
         username= sharedPreferences.getString("com.hackncs.username", null);
         avatarID = sharedPreferences.getInt("com.hackncs.avatarID", R.drawable.avatar_1);
//        action_bar_useravatar.setImageResource(avatarID);
//        action_bar_username.setText(username);
//        action_bar_usercoins.setText(score+"");

        mediaPlayer = MediaPlayer.create(this, R.raw.bgmusicsg);

        mediaPlayer.setLooping(true);
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }


        bottomNavigationView=findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id=item.getItemId();
                switch (id)
                {
                    case R.id.navigation_duel:

                        loadFragment(fDuel,"duel");
                        title_bar.setText(t1);

//
                        break;
                    case R.id.navigation_current_mission:
                        loadFragment(fMission,"missions");
                        title_bar.setText(t2);
                        break;
                    case R.id.navigation_leaderboard:
                        loadFragment(fLeaderboard,"leaderboard");
                        title_bar.setText(t3);
                        break;
                }
                return true;
            }
        });


        vol.setOnClickListener(view -> {
            if (!mute){
               // stopPlaying();
                mediaPlayer.setVolume(0,0);
                vol.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_volume_off_24));
                mute = true;
            }else {
              //  mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.bgmusicsg);
              //  mediaPlayer.start();
                mediaPlayer.setVolume(1,1);
                vol.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_volume_up_24));
                mute = false;
            }
        });


        BottomScreen bottomSheet = new BottomScreen();

        binding.optionButton.setOnClickListener(v->{
            bottomSheet.callback = this;
            bottomSheet.setDetails(avatarID,username,action_bar_usercoins.getText().toString());
            bottomSheet.show(getSupportFragmentManager(), "bottomsheet");

        });


          }


        private  void stopPlaying(){
        if (mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
           // mediaPlayer = null;
        }
        }


    void hideNavigationMenu(){
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }



    @Override
    protected void onResume() {
        super.onResume();
//        Log.i("Resume","Resume");
        if(mAuth.getCurrentUser().isEmailVerified()) {
//            Log.i("Resume","Resume"+mediaPlayer.isPlaying());
            mediaPlayer.start();
//            Log.i("Resume","Resume1"+mediaPlayer.isPlaying());
            SharedPreferences sharedPreferences = getSharedPreferences("LootPrefs", Context.MODE_PRIVATE);
            int score = sharedPreferences.getInt("com.hackncs.score", 0);
            action_bar_usercoins.setText(" "+score);
            userOnlineUpdate(mAuth.getCurrentUser().getUid(),false);
            updateFirebase(mAuth.getCurrentUser(), true);
            bottomNavigationView.getMenu().getItem(1).setChecked(true);
            Fragment fragment = new Missions();
            loadFragment(fragment, "missions");

        }
        else{
            Toast.makeText(this,"Please verify your email to continue",Toast.LENGTH_LONG).show();
            mAuth.signOut();
            Intent intent=new Intent(getApplicationContext(), Main3Activity.class);
            startActivity(intent);
        }
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
            Fragment fragment=new Missions();
            loadFragment(fragment,"missions");

        }
        else if(fragmentTag.equals("online_users"))
        {
            bottomNavigationView.getMenu().getItem(0).setChecked(true);
            loadFragment(new Duel(),"duel");
        }
        else if (fragmentTag.equals("missions"))
        {
            updateFirebase(mAuth.getCurrentUser(),false);
            finishAffinity();
        }
        else if(fragmentTag.equals("about")||fragmentTag.equals("how_to")||fragmentTag.equals("help")||fragmentTag.equals("contact_us"))
        {

            bottomNavigationView.getMenu().getItem(1).setChecked(true);
            Fragment fragment=new Missions();
            loadFragment(fragment,"missions");
        }

        else {
            super.onBackPressed();
        }

    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == Missions.LOCATION_REQUEST_CODE) {
//            if (resultCode == RESULT_OK) {
//                Toast.makeText(this, "GPS enabled!", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(this, "GPS disabled!", Toast.LENGTH_SHORT).show();
//                finish();
//            }
//        }
//    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onPause() {


        if(mAuth.getCurrentUser()!=null)
            updateFirebase(mAuth.getCurrentUser(),false);
        else{
            finishAffinity();
        }
        mediaPlayer.pause();
        super.onPause();

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.popup_menu, menu);
//        return super.onCreateOptionsMenu(menu);
//    }
//    @Override
//    public boolean onOptionsItemSelected(MenuItem menuItem) {
//        switch (menuItem.getItemId()) {
//            case R.id.item_howTo:
//                loadFragment(new HowTo(),"how_to");
////                Intent i1 = new Intent(this, MiniGame.class);
////                i1.putExtra("duel_id","nknfknskfdsf" );
////                i1.putExtra("player_type", "challenger");
////                startActivity(i1);
//                break;
//            case R.id.item_about:
//                loadFragment(new AboutFragment(),"about");
//                break;
//            case R.id.item_story:
//                Intent i = new Intent(this, WelcomeSlider.class);
//                startActivity(i);
//                break;
//            case R.id.item_help:
//                loadFragment(new Help(),"help");
//                break;
//            case R.id.pop_logout:
//                updateFirebase(mAuth.getCurrentUser(),false);
//                mAuth.signOut();
//                Intent intent=new Intent(getApplicationContext(), Main3Activity.class);
//                startActivity(intent);
//                break;
//            default:
//        }
//        return super.onOptionsItemSelected(menuItem);
//    }
//


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
//                    Log.i("Added Succesfully","");
                }
                else
                {
                    Log.i("Error",task.getException().getMessage());
                }
            }
        });




    }

    public void userOnlineUpdate(String userID, final boolean state){
        RequestQueue requestQueue=requestQueue = Volley.newRequestQueue(this);
        StringRequest updateUser = new StringRequest(Request.Method.POST,
                Endpoints.updateUser + userID + "/edit/",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("updateVolley", response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
//                        Log.d("updateVolley", error.getMessage());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map map = new HashMap();
                map.put("payment", state+"");
                return map;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("x-auth",Endpoints.apikey);
                return params;
            }
        };
        requestQueue.add(updateUser);
    }

    @Override
    public void onClick(int pos) {
         switch (pos){

             case 3:
                loadFragment(new HowTo(),"how_to");
                break;
            case 0:
                loadFragment(new AboutFragment(),"about");
                break;
            case 1:
                Intent i = new Intent(this, WelcomeSlider.class);
                startActivity(i);
                break;
            case 2:
                loadFragment(new Help(),"help");
                break;
            case 4:
                updateFirebase(Objects.requireNonNull(mAuth.getCurrentUser()),false);
                mAuth.signOut();
                Intent intent=new Intent(getApplicationContext(), Main3Activity.class);
                startActivity(intent);
                break;

             default :

         }
    }
}
