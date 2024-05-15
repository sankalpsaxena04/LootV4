package com.hackncs.zealicon.loot;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class Help extends Fragment {

    public Help() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_help, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Button anuragContact=getView().findViewById(R.id.anuragContact);
        Button alokContact=getView().findViewById(R.id.alokContact);
//        final TextView facebook=getView().findViewById(R.id.textView6);
        final TextView anurag=getView().findViewById(R.id.textView2);
        final TextView alok=getView().findViewById(R.id.textView4);
        anuragContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialPhoneNumber("9839545115");
            }
        });
        alokContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialPhoneNumber("8887358051");
            }
        });
        anurag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialPhoneNumber("9839545115");
            }
        });
        alok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialPhoneNumber("8887358051");
            }
        });
//        facebook.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try {
//                    Intent facebookIntent = new Intent(Intent.ACTION_VIEW);
//                    String facebookUrl = getFacebookPageURL(getActivity());
//                    facebookIntent.setData(Uri.parse(facebookUrl));
//                    startActivity(facebookIntent);
//                }
//                catch (Exception e){
//                    String FACEBOOK_URL = "https://www.facebook.com/LootZealicon";
//                    Intent i = new Intent(Intent.ACTION_VIEW);
//                    i.setData(Uri.parse(FACEBOOK_URL));
//                    startActivity(i);
//                }
//
//            }
//        });
    }

    public void dialPhoneNumber(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }
//    public String getFacebookPageURL(Context context) {
//         String FACEBOOK_URL = "https://www.facebook.com/LootZealicon";
//         String FACEBOOK_PAGE_ID = "LootZealicon";
//        PackageManager packageManager = context.getPackageManager();
//        try {
//            int versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode;
//            if (versionCode >= 3002850) { //newer versions of fb app
//                return "fb://facewebmodal/f?href=" + FACEBOOK_URL;
//            } else { //older versions of fb app
//                return "fb://page/" + FACEBOOK_PAGE_ID;
//            }
//        } catch (PackageManager.NameNotFoundException e) {
//            return FACEBOOK_URL; //normal web url
//        }
//      }

}
