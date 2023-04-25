package com.hackncs.zealicon.loot;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.orhanobut.logger.Logger;

public class Main3Activity extends AppCompatActivity {


    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        db = FirebaseFirestore.getInstance();
        checkForUpdates(BuildConfig.VERSION_NAME);
        hideNavigationMenu();


    }

    void hideNavigationMenu(){
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public void onBackPressed() {
//
        int size=getSupportFragmentManager().getFragments().size();

        String fragmentTag=getSupportFragmentManager().getFragments().get(size-1).getTag();
        Log.i("Fragment",fragmentTag);


        if(fragmentTag.equals("register")||fragmentTag.equals("login")) {

            Fragment fragment=new Splash();
            loadFragment(fragment,"splash");

        }
        else if (fragmentTag.equals("splash"))
        {
            finishAffinity();
        }
        else {
            super.onBackPressed();
        }

    }

    private void loadFragment(Fragment fragment, String tag) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.login_frame, fragment,tag);
        transaction.addToBackStack(null);
        transaction.commit();
    }



    void checkForUpdates(String currentVersion){


        CollectionReference deviceIDCollectionRef = db.collection("appConfig");
        String docID = "updateRelated";
        DocumentReference didRef = deviceIDCollectionRef.document(docID);


        didRef.get().addOnSuccessListener(documentSnapshot -> {

            if (documentSnapshot.exists()){

                String latestVersion = documentSnapshot.getString("latestVersion");
                String appLink = documentSnapshot.getString("link");
                Logger.d("Update related : Version "+latestVersion);

                if (versionCompare(currentVersion, latestVersion) < 0) {
                    Logger.d("Update related :  Current version is smaller, new update");
                    updateAlertDialog(appLink);
                }else {

                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    Splash fragment = new Splash();
                    loadFragment(fragment,"splash");
                }

            }
        }).addOnFailureListener(e->{
            // Toast.makeText(requireContext(),"Critical Error in checking Updates, r", Toast.LENGTH_SHORT).show();
           // createDialog("Critical error", "Error in checking new updates", false);
        });


    }

    private void updateAlertDialog(String link) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Version in the air!");
        builder.setMessage("Update to the latest version to continue loot.");
        builder.setIcon(R.drawable.baseline_hive_24);
        builder.setPositiveButton("UPDATE", (dialogInterface, i) -> {

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            startActivity(intent);
            dialogInterface.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setOnShowListener(arg0 -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED));


        dialog.show();
    }


    static int versionCompare(String v1, String v2)
    {
        int vnum1 = 0, vnum2 = 0;

        for (int i = 0, j = 0; (i < v1.length()
                || j < v2.length());) {

            while (i < v1.length()
                    && v1.charAt(i) != '.') {
                vnum1 = vnum1 * 10
                        + (v1.charAt(i) - '0');
                i++;
            }

            while (j < v2.length()
                    && v2.charAt(j) != '.') {
                vnum2 = vnum2 * 10
                        + (v2.charAt(j) - '0');
                j++;
            }

            if (vnum1 > vnum2)
                return 1;
            if (vnum2 > vnum1)
                return -1;

            vnum1 = vnum2 = 0;
            i++;
            j++;
        }
        return 0;
    }
}
