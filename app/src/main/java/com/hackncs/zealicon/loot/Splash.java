package com.hackncs.zealicon.loot;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hackncs.zealicon.loot.databinding.FragmentSplashBinding;
import com.orhanobut.logger.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


public class Splash extends Fragment {

    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    FirebaseFirestore db;
    DatabaseReference users,missions;
    FirebaseUser fbuser;
    User user;
    ProgressBar loader;
    Animation fade , blinkinf, popup;
    ArrayList<Mission> missionsList = new ArrayList<>();
    ImageView title;
    boolean isConnected, logged_in, synced_user, synced_missions, isVerified;
    FragmentSplashBinding binding;

    public Splash() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        user = new User();
        binding = FragmentSplashBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        users = database.getReference("Users");
        missions = database.getReference("Current_missions");
        loader=(ProgressBar)getView().findViewById(R.id.loadersplash);
        title = getView().findViewById(R.id.loot_title);

         fade = AnimationUtils.loadAnimation(getContext(), R.anim.fadeanim);
         blinkinf = AnimationUtils.loadAnimation(getContext(), R.anim.blink_infinite);
         popup = AnimationUtils.loadAnimation(getContext(), R.anim.popup);
         int timeout = 2000;
        loader.setMax(timeout);
        title.setAnimation(popup);
        loader.setAnimation(blinkinf);

        db  = FirebaseFirestore.getInstance();

        if (!isEmulator()){

        //blink.start();
        new CountDownTimer(timeout+100, 1) {
            @Override
            public void onTick(long l) {
                //int progress=(int)((5000-l)/50);
                loader.setProgress(timeout-(int)l);
            }

            @Override
            public void onFinish() {
                loader.clearAnimation();
                loader.setVisibility(View.GONE);
            }
        }.start();

        new BackgroundTasks().execute();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isConnected) {
                    if (logged_in) {
                        syncSharedPrefs(user);
                    }
                    else {
                        changeView();
                    }
                }
                else {
                    Toast.makeText(getActivity(),"You're not connected!",Toast.LENGTH_SHORT).show();
                    Objects.requireNonNull(getActivity()).finish();
                }
            }
        }, timeout);
        }else {

            createDialog("Emulator detected", "Loot cannot run on emulator for security reasons", true).show();

        }

    }


    public AlertDialog createDialog(String title, String message, Boolean isCancellable){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title);
        builder.setMessage(message);


        builder.setPositiveButton("Exit", (dialog, which) -> {
            Objects.requireNonNull(getActivity()).finish();
        })
        ;

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener( new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);

            }
        });

        dialog.setCancelable(isCancellable);

        return dialog;
    }

    public static boolean isEmulator() {
        boolean isEmulator = false;
        String model = Build.MODEL;
        String product = Build.PRODUCT;
        String manufacturer = Build.MANUFACTURER;
        String brand = Build.BRAND;
        if (model != null && model.toLowerCase().contains("sdk")
                || product != null && product.toLowerCase().contains("sdk")
                || manufacturer != null && manufacturer.toLowerCase().contains("genymotion")
                || brand != null && brand.toLowerCase().contains("generic")) {
            isEmulator = true;
        }
        return isEmulator;
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    private void syncUser(String userID) {

        StringRequest syncRequest = new StringRequest(Request.Method.GET,
                Endpoints.syncRequest+userID,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject jsonObject;
                        try {
                            jsonObject = new JSONObject(response);
                            user.setUserID(jsonObject.getString("reference_token"));
                            user.setUsername(jsonObject.getString("username"));
                            user.setAdmissionNo(jsonObject.getString("admission_no"));
                            user.setName(jsonObject.getString("name"));
                            user.setEmail(jsonObject.getString("email"));
                            user.setAvatarID(Integer.valueOf(jsonObject.getString("avatar_id")));
                            user.setScore(Integer.valueOf(jsonObject.getString("score")));
                            user.setStage(Integer.valueOf(jsonObject.getString("stage")));
                            Log.i("Stage1",jsonObject.getString("stage"));
                            Log.i("Stage2",String.valueOf(user.getStage()));
                            user.setState(jsonObject.getString("mission_state").equals("false")?0:1);
                            user.setDropCount(Integer.valueOf(jsonObject.getString("drop_count")));
                            user.setDuelWon(Integer.valueOf(jsonObject.getString("duel_won")));
                            user.setDuelLost(Integer.valueOf(jsonObject.getString("duel_lost")));
                            user.setContactNumber(Long.valueOf(jsonObject.getString("contact_number")));
//                            user.setDropped();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("x-auth",Endpoints.apikey);
                    return params;
                }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        requestQueue.add(syncRequest);
    }

    public void syncSharedPrefs(User user) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("LootPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("com.hackncs.userID", user.getUserID());
        editor.putString("com.hackncs.username", user.getUsername());
        editor.putString("com.hackncs.admissionNo", user.getAdmissionNo());
        editor.putString("com.hackncs.name", user.getName());
        editor.putString("com.hackncs.email", user.getEmail());
        editor.putInt("com.hackncs.avatarID", user.getAvatarID());
        editor.putInt("com.hackncs.score", user.getScore());
        editor.putInt("com.hackncs.stage", user.getStage());
        editor.putInt("com.hackncs.state", user.getState());
        editor.putInt("com.hackncs.dropCount", user.getDropCount());
        editor.putInt("com.hackncs.duelWon", user.getDuelWon());
        editor.putInt("com.hackncs.duelLost", user.getDuelLost());
        editor.putLong("com.hackncs.contactNumber", user.getContactNumber());
//        editor.putStringSet("com.hackncs.dropped", new HashSet<>(user.getDropped()));
        editor.apply();
        Intent i = new Intent(getContext(), DashboardLoot.class);
        i.putExtra("UID", fbuser.getUid());
        startActivity(i);
    }

    public boolean isOnline() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    Toast.makeText(getActivity(), "Connected!", Toast.LENGTH_SHORT).show();
                }
            });
            return true;
        }
        else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), "Disconnected!", Toast.LENGTH_SHORT).show();
                }
            });
            return false;
        }
    }

    private  void backgroundTasks() {
        isConnected = isOnline();
        logged_in = mAuth.getCurrentUser() != null;
        fbuser = mAuth.getCurrentUser();

        if(logged_in) {
            isVerified=fbuser.isEmailVerified();
            if(isVerified)
                syncUser(fbuser.getUid());
        }
    }

    public class BackgroundTasks extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            backgroundTasks();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    private void changeView() {

        ProgressBar loader = (ProgressBar) getView().findViewById(R.id.loadersplash);
        RelativeLayout layout = getView().findViewById(R.id.rel_layout);
        loader.setVisibility(View.GONE);
        layout.setVisibility(View.GONE);
        binding.loginbtn.setVisibility(View.VISIBLE);
        binding.getStartedButton.setVisibility(View.VISIBLE);

       binding.loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                Login fragment = new Login();
                fragmentTransaction.replace(R.id.login_frame, fragment,"login");
                fragmentTransaction.commit();
            }
        });
        binding.getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                Register fragment = new Register();
                fragmentTransaction.replace(R.id.login_frame, fragment,"register");
                fragmentTransaction.commit();
            }
        });
    }


    public void saveDeviceID(){

        Map<String, Object> data = new HashMap<>();
        data.put("email", "sudoarmax@gmail.com");
        data.put("modelName", "Pixel a2");
        String docID = "e0dcb34326976f23";


        db.collection("deviceIDs")
                .document(docID)
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    Logger.d("Data added");
                    Toast.makeText(getContext(), "Data added", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Logger.d("Data addition ");
                    Toast.makeText(getContext(), "Error addition", Toast.LENGTH_SHORT).show();

                });


    }


}