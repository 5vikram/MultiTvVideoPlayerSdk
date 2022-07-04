/*
package com.multitv.ott.multitvvideoplayer.popup;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ExpandableListView;

import com.multitv.ott.multitvvideoplayer.MultiTvPlayerSdk;
import com.multitv.ott.multitvvideoplayer.R;
import com.multitv.ott.multitvvideoplayer.models.PlayerSelection;
import com.multitv.ott.multitvvideoplayer.playeradapters.ElvAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ResolutionDailog {
    AlertDialog dialog = null;

    public void showResolutionDailog(Context context, MultiTvPlayerSdk multiTvPlayer) {
        try {
            if (multiTvPlayer == null)
                return;


            if (dialog != null && dialog.isShowing())
                dialog.dismiss();

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("");

//            final Dialog dialog = new Dialog(context);
//            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//            dialog.setContentView(R.layout.menu_dialog);

            View dialogView = LayoutInflater.from(context).inflate(R.layout.menu_dialog,null);
            builder.setView(dialogView);

            HashMap<Integer, PlayerSelection> playerHashMap = new HashMap<>();

            if (multiTvPlayer.availableSrtTracksList != null && !multiTvPlayer.availableSrtTracksList.isEmpty()) {
                List<String> listChildData = new ArrayList<>();
                listChildData.addAll(multiTvPlayer.availableSrtTracksList);

                PlayerSelection playerSelection = new PlayerSelection();
                playerSelection.header = "Language";
                playerSelection.typeOfSelection = 2;
                playerSelection.selectedItemPosition = multiTvPlayer.srtSelectedItemPosition;
                playerSelection.listChildData = listChildData;

                playerHashMap.put(0, playerSelection);
            }

            if (multiTvPlayer.availableAudioTracksList != null && !multiTvPlayer.availableAudioTracksList.isEmpty()) {
                List<String> listChildData = new ArrayList<>();
                listChildData.addAll(multiTvPlayer.availableAudioTracksList);

                PlayerSelection playerSelection = new PlayerSelection();
                playerSelection.header = "Audio";
                playerSelection.typeOfSelection = 1;
                playerSelection.selectedItemPosition = multiTvPlayer.audioSelectedItemPosition;
                playerSelection.listChildData = listChildData;

                if (!playerHashMap.isEmpty()) {
                    playerHashMap.put(playerHashMap.size(), playerSelection);
                } else
                    playerHashMap.put(0, playerSelection);
            }

            if (multiTvPlayer.availableResolutionContainerList != null && !multiTvPlayer.availableResolutionContainerList.isEmpty()) {
                List<String> listChildData = new ArrayList<>();
                listChildData.addAll(multiTvPlayer.availableResolutionContainerList);

                PlayerSelection playerSelection = new PlayerSelection();
                playerSelection.header = "Quality";
                playerSelection.typeOfSelection = 0;
                playerSelection.selectedItemPosition = multiTvPlayer.resolutionSelectedItemPosition;
                playerSelection.listChildData = listChildData;

                if (!playerHashMap.isEmpty()) {
                    playerHashMap.put(playerHashMap.size(), playerSelection);
                } else
                    playerHashMap.put(0, playerSelection);
            }


            ExpandableListView expandableListView = dialogView.findViewById(R.id.elv);
            ElvAdapter elvAdapter = new ElvAdapter(context, expandableListView, dialog, multiTvPlayer,
                    playerHashMap);
            expandableListView.setAdapter(elvAdapter);

//            ListView expandableListView = dialog.findViewById(R.id.elv);
//            ElvAdapter elvAdapter = new ElvAdapter(context, expandableListView, dialog, multiTvPlayer,
//                    playerHashMap);
//
//            ResolutionAdapter adapter = new ResolutionAdapter(context,playerHashMap,expandableListView,dialog,multiTvPlayer);
//
//            expandableListView.setAdapter(adapter);


            if (playerHashMap.size() == 1)
                expandableListView.expandGroup(0);


            if (!playerHashMap.isEmpty()) {
                dialog = builder.create();
                dialog.show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
*/
