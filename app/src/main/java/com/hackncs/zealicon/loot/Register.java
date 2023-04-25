package com.hackncs.zealicon.loot;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.messaging.FirebaseMessaging;
import com.hackncs.zealicon.loot.databinding.FragmentRegisterBinding;
import com.orhanobut.logger.Logger;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import org.json.JSONException;
import org.json.JSONObject;


public class Register extends Fragment implements View.OnClickListener{

    private final String TAG = this.getClass().getSimpleName();
    private FirebaseAuth mAuth;
    View view;
    EditText name, email, contact, zeal, username, password;
    ImageView avatars[]=new ImageView[6];
    ImageView tick[]=new ImageView[6];
    int avatarIds[]=new int[6];
    ProgressDialog dialog;
    FirebaseFirestore db;
    int selectedAvatar;
    FirebaseUser firebaseUser;
    FirebaseFirestore firestore;
    User user;
    FragmentRegisterBinding binding;
    String deviceID;
    public Register() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_register, container, false);
        binding = FragmentRegisterBinding.bind(view);


        initializeViews();
        Logger.t("Testing logger").d(getDeviceId(Objects.requireNonNull(getActivity()).getApplicationContext()));
        deviceID = getDeviceId(getActivity().getApplicationContext());
      //  validateDeviceID();
        return view;
    }


    @SuppressLint("HardwareIds")
    public static String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        final Button register = getView().findViewById(R.id.submit);

        dialog.setTitle("Please Wait");
        dialog.setCancelable(false);
        dialog.setMessage("Registering you...");
        final Animation blinkAnim = AnimationUtils.loadAnimation(getContext(), R.anim.blink) ;

        binding.backbutton.setOnClickListener(view -> {

            Splash fragmet = new Splash();
            loadFragment(fragmet, "Splash");
        });



       //region Register button
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                register.setAnimation(blinkAnim);

                if (!validation()) {
                    Toast.makeText(getActivity(), "Please fill in all the field", Toast.LENGTH_SHORT).show();
                }else {
                        dialog.show();
                        mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            dialog.dismiss();
                                            firebaseUser = mAuth.getCurrentUser();
                                            assert firebaseUser != null;
                                            firebaseUser.sendEmailVerification().addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Toast.makeText(getActivity(),
                                                                "Verification Mail Sent. Please verify to continue",
                                                                Toast.LENGTH_SHORT).show();
                                                        createUser();
                                                    }
                                                    else{
                                                        firebaseUser.delete();
                                                        Log.e(TAG, "sendEmailVerification", task.getException());
                                                        Toast.makeText(getActivity(),
                                                                "Failed to send verification email.Invalid Email. Try Again",
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });

                                        } else {
                                            dialog.dismiss();
                                            Toast.makeText(getContext(), "Firebase : Registration failed." + task.getException().getMessage(),
                                                    Toast.LENGTH_SHORT).show();

                                        }
                                    }
                                });



                }


            }
        });
        //endregion

    }
    private void createUser(){
        updateFirebase(firebaseUser);
        StringRequest register = new StringRequest(Request.Method.POST,
                Endpoints.register,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("volley request", "response");
                        syncSharedPrefs();
                        Toast.makeText(getContext(), "Server : You're registered successfully!", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(getContext(), WelcomeSlider.class);
                        startActivity(i);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        firebaseUser.delete();

                        Logger.e("volley request Error message Cause : "+error.getCause());
                        Logger.e("volley request :"+error.getMessage());
                        Toast.makeText(getContext(), "Server: Error -> Code: "+error.networkResponse.statusCode, Toast.LENGTH_SHORT).show();


                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap();
                map.put("reference_token", firebaseUser.getUid());
                map.put("email", email.getText().toString().trim());
                map.put("name", name.getText().toString().trim());
                map.put("username", username.getText().toString().trim());
                map.put("admission_no", zeal.getText().toString().trim());
                map.put("contact_number", contact.getText().toString().trim());
                map.put("score", "0");
                map.put("stage", "1");
                map.put("mission_state", "false");
                map.put("drop_count", "0");
                map.put("duel_won", "0");
                map.put("duel_lost", "0");
                map.put("avatar_id", String.valueOf(avatarIds[selectedAvatar]));
                getFCMToken();
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("LootPrefs", Context.MODE_PRIVATE);
                final String fcmToken = sharedPreferences.getString("com.hackncs.FCMToken", "");
                if (!fcmToken.equals("")) {
                    map.put("fcm_token", fcmToken);
                }

                Logger.d("volley request map"+map);

                return map;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("x-auth",Endpoints.apikey);

                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        requestQueue.add(register);
    }

    private void initializeViews() {


        name = view.findViewById(R.id.name);
        email = view.findViewById(R.id.email);
        contact = view.findViewById(R.id.contactNumber);
        zeal = view.findViewById(R.id.zealId);
        username = view.findViewById(R.id.username);
        password = view.findViewById(R.id.password);
        avatars[0]=view.findViewById(R.id.avatar_1);
        avatars[1]=view.findViewById(R.id.avatar_2);
        avatars[2]=view.findViewById(R.id.avatar_3);
        avatars[3]=view.findViewById(R.id.avatar_4);
        avatars[4]=view.findViewById(R.id.avatar_5);
        avatars[5]=view.findViewById(R.id.avatar_6);


        for (int x=0;x<6;x++)
        {
            avatars[x].setOnClickListener(this);
        }

        tick[0]=view.findViewById(R.id.tick_1);
        tick[1]=view.findViewById(R.id.tick_2);
        tick[2]=view.findViewById(R.id.tick_3);
        tick[3]=view.findViewById(R.id.tick_4);
        tick[4]=view.findViewById(R.id.tick_5);
        tick[5]=view.findViewById(R.id.tick_6);


        avatarIds[0]=R.drawable.avatar_1;
        avatarIds[1]=R.drawable.avatar_2;
        avatarIds[2]=R.drawable.avatar_3;
        avatarIds[3]=R.drawable.avatar_4;
        avatarIds[4]=R.drawable.avatar_5;
        avatarIds[5]=R.drawable.avatar_6;

        dialog=new ProgressDialog(getContext());
    }

    @Override
    public void onStart() {
        super.onStart();
        
        
    }

    private User getUser() {
        user = new User();
        user.setUserID(firebaseUser.getUid());
        user.setUsername(username.getText().toString());
        user.setAdmissionNo(zeal.getText().toString());
        user.setName(name.getText().toString());
        user.setEmail(email.getText().toString());
        user.setAvatarID(avatarIds[selectedAvatar]);
        user.setScore(0);
        user.setStage(1);
        user.setState(0);
        user.setDropCount(0);
        user.setDuelWon(0);
        user.setDuelLost(0);
        user.setContactNumber(Long.valueOf(contact.getText().toString()));
        return  user;
    }

    public void syncSharedPrefs() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("LootPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("com.hackncs.userID", firebaseUser.getUid());
        editor.putString("com.hackncs.username", username.getText().toString());
        editor.putString("com.hackncs.admissionNo", zeal.getText().toString());
        editor.putString("com.hackncs.name", name.getText().toString());
        editor.putString("com.hackncs.email", email.getText().toString());
        editor.putInt("com.hackncs.avatarID", avatarIds[selectedAvatar]);
        editor.putInt("com.hackncs.score", 0);
        editor.putInt("com.hackncs.stage", 1);
        editor.putInt("com.hackncs.state", 0);
        editor.putInt("com.hackncs.dropCount", 0);
        editor.putInt("com.hackncs.duelWon", 0);
        editor.putInt("com.hackncs.duelLost", 0);
        editor.putLong("com.hackncs.contactNumber", Long.valueOf(contact.getText().toString()));
        editor.apply();
    }

    public void updateFirebase(FirebaseUser firebaseUser)
    {
        Map<String, Object> user = new HashMap<>();
        user.put("userID", firebaseUser.getUid());
        user.put("online", false);

        db.collection("users").document(firebaseUser.getUid())
                .set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful())
                {
                    Log.i("F Added Succesfully","");
                }
                else
                {
                    Log.i("Firebase : Error",task.getException().getMessage());
                }
            }
        });

    }

    private void loadFragment(Fragment fragment, String tag) {
        // load fragment
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.login_frame, fragment,tag);
        transaction.addToBackStack(null);
        transaction.commit();
    }


    @Override
    public void onClick(View view) {


        int id=view.getId();
        Log.i("Clicked",id+"");
        for(int i=0;i<6;i++)
        {
            if(avatars[i].getId()==id)
            {
                tick[selectedAvatar].setVisibility(View.GONE);
                tick[i].setVisibility(View.VISIBLE);
                //TODO:Update avatarId
                selectedAvatar=i;
                Log.i("Selected",selectedAvatar+"");
            }
        }


        binding.scrollView2.post(() -> {
           binding.scrollView2.smoothScrollBy(0,binding.scrollView2.getBottom());
        });

    }

    private boolean validation()
    {
        boolean validate=true;
        if(name.getText()==null||name.getText().toString().trim().length()==0)
        {
            validate=false;
        }
        else if(email.getText()==null||email.getText().toString().trim().length()==0)
        {
            validate=false;
        }
        else if(zeal.getText()==null||zeal.getText().toString().trim().length()==0)
        {
            validate=false;
        }
        else if(contact.getText()==null||contact.getText().toString().trim().length()==0)
        {
            validate=false;
        }
        else if(password.getText()==null||password.getText().toString().trim().length()==0)
        {
            validate=false;
        }
        else if(username.getText()==null||username.getText().toString().trim().length()==0)
        {
            validate=false;
        }
        return validate;

    }
    public void getFCMToken(){


        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if(!task.isComplete()){
                Log.w(TAG, "getInstanceId failed", task.getException());
                return;
            }

            String token = task.getResult();
            Log.i("FCMTOKEN",token);
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("LootPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("com.hackncs.FCMToken", token);
            editor.apply();

        });

    }


    public void validateDeviceID(){
        CollectionReference deviceIDCollectionRef = db.collection("deviceIDs");
        String docID = deviceID;


        Logger.d(docID);
        DocumentReference didRef = deviceIDCollectionRef.document(docID);


        didRef.get().addOnSuccessListener(documentSnapshot -> {

            if (documentSnapshot.exists()){

                String email = documentSnapshot.getString("email");
                String modelName = documentSnapshot.getString("modelName");
                String username = documentSnapshot.getString("username");

                createDialog("Account exists","Account exists with following creds :\n\nDevice model : "+modelName+" \nEmail ID : "+email+"\nUsername : "+username+"\n\nLogin using above ID to continue...").show();
            }else {
                Toast.makeText(requireContext(),"Validated", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e->{
            Toast.makeText(requireContext(),"Error occurred while retrieving", Toast.LENGTH_SHORT).show();

        });

    }


    public AlertDialog createDialog(String title, String message){
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

        dialog.setCancelable(false);

        return dialog;
    }
}
