package com.hackncs.zealicon.loot;/*
File : null.java -> com.hackncs.zealicon.loot
Description : This is bottom screen file 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : Loot-2022-Final Android)

Creation : 7:31 pm on 25/04/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 


*//*
File : null.java -> com.hackncs.zealicon.loot
Description : This is bottom screen file 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : Loot-2022-Final Android)

Creation : 7:31 pm on 25/04/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 


*/


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.hackncs.zealicon.loot.databinding.BottomScreenFileBinding;

import org.checkerframework.common.subtyping.qual.Bottom;

public class BottomScreen extends BottomSheetDialogFragment {


    BottomScreenFileBinding binding;
    public ClickCallback callback;
    int avatarID;
    String name,score;

    public void setDetails(int avatarID, String name, String score ){

        this.avatarID = avatarID;
        this.name = name;
        this.score = score;

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = BottomScreenFileBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.avatarImg.setImageResource(avatarID);
        binding.playername.setText(name);
        binding.score.setText(String.format("Score: %s",score));


        binding.aboutTV.setOnClickListener(v->{
            callback.onClick(0);
            this.dismiss();


        });

        binding.storyTV.setOnClickListener(v->{
            callback.onClick(1);
            this.dismiss();

        });

        binding.helpTV.setOnClickListener(v->{
            callback.onClick(2);
            this.dismiss();

        });

        binding.howtoPlayTV.setOnClickListener(v->{
            callback.onClick(3);
            this.dismiss();

        });

        binding.logOutTV.setOnClickListener(v->{
            callback.onClick(4);
            this.dismiss();

        });




    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}


interface ClickCallback{
    void onClick(int pos);
}