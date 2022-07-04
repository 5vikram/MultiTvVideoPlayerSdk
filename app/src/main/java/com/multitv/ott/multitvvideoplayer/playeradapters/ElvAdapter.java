package com.multitv.ott.multitvvideoplayer.playeradapters;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;


import com.multitv.ott.multitvvideoplayer.R;
import com.multitv.ott.multitvvideoplayer.models.PlayerSelection;
import com.multitv.ott.multitvvideoplayer.popup.MyDialogFragment;

import java.util.HashMap;


/**
 * Created by naseeb on 8/29/2017.
 */

public class ElvAdapter extends BaseExpandableListAdapter {

    private Context context;
    private String header;
    private int selectedItemPosition;
    private ListView expandableListView;
    private HashMap<Integer, PlayerSelection> playerHashMap;
    private MyDialogFragment.ResolutionAudioSrtSelection resolutionAudioSrtSelection;
    private RadioButton previouslySelectedRadioBtn;
    private TextView previouslySelectedTitleTV;
    private Dialog dialog;

    public ElvAdapter(Context context, ListView expandableListView, Dialog dialog,
                      MyDialogFragment.ResolutionAudioSrtSelection resolutionAudioSrtSelection,
                      HashMap<Integer, PlayerSelection> playerHashMap) {
        this.context = context;
        this.expandableListView = expandableListView;
        this.dialog = dialog;
        this.resolutionAudioSrtSelection = resolutionAudioSrtSelection;
        this.playerHashMap = playerHashMap;
    }

    @Override
    public int getGroupCount() {
        return playerHashMap.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return playerHashMap != null && !playerHashMap.isEmpty() && playerHashMap.get(groupPosition).listChildData != null
                && !playerHashMap.get(groupPosition).listChildData.isEmpty() ? playerHashMap.get(groupPosition).listChildData.size() : 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.playerHashMap.get(groupPosition).header;
    }

    @Override
    public String getChild(int groupPosition, int childPosition) {
        return this.playerHashMap.get(groupPosition).listChildData.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(final int groupPosition, final boolean isExpanded, View convertView, ViewGroup parent) {
        try {
            GroupViewHolder groupViewHolder;
            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.expand_row, null);

                groupViewHolder = new GroupViewHolder();
                groupViewHolder.selectionTypeTV = (TextView) convertView.findViewById(R.id.selectionTypeTV);
                groupViewHolder.groupTitleTV = (TextView) convertView.findViewById(R.id.titleTV);
                groupViewHolder.expandIV = (ImageView) convertView.findViewById(R.id.expand_iv);

                groupViewHolder.expandIV.setVisibility(View.GONE);

                convertView.setTag(groupViewHolder);
            } else
                groupViewHolder = (GroupViewHolder) convertView.getTag();

            groupViewHolder.selectionTypeTV.setText(this.playerHashMap.get(groupPosition).header);

            final int typeOfSelection = this.playerHashMap.get(groupPosition).typeOfSelection;

            /*String title = "";
            if (typeOfSelection == 0) {
                switch (this.playerHashMap.get(groupPosition).selectedItemPosition) {
                    case 0:
                        title = "Auto";
                        break;
                    case 1:
                        title = "Best";
                        break;
                    case 2:
                        title = "Better";
                        break;
                    case 3:
                        title = "Good";
                        break;
                    case 4:
                        title = "Data Saver";
                        break;
                }
            } else*/
            String title = this.playerHashMap.get(groupPosition).listChildData.get(
                    this.playerHashMap.get(groupPosition).selectedItemPosition);

            groupViewHolder.groupTitleTV.setText(title);

//            if (isExpanded)
//                groupViewHolder.expandIV.setImageDrawable(context.getResources().getDrawable(R.drawable.up_arrow));
//            else
//                groupViewHolder.expandIV.setImageDrawable(context.getResources().getDrawable(R.drawable.down_arrow));

//            groupViewHolder.groupTitleTV.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (isExpanded) {
//                        if (expandableListView != null)
//                            expandableListView.collapseGroup(groupPosition);
//
//                       /* if (resolutionAudioSrtSelection != null) {
//                            resolutionAudioSrtSelection.onResolutionAudioSrtSelection("", selectedItemPosition,
//                                    typeOfSelection);
//                        }*/
//                    } else {
//                        if (expandableListView != null)
//                            expandableListView.expandGroup(groupPosition);
//                    }
//                }
//            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        /*View childView = null;*/
        final int typeOfSelection = this.playerHashMap.get(groupPosition).typeOfSelection;
/*        if (typeOfSelection != 0) {
        try {
                childView = layoutInflater.inflate(R.layout.expand_row, null);
            TextView titleTV = (TextView) childView.findViewById(R.id.titleTV);
            titleTV.setText(getChild(groupPosition, childPosition));
                if (childPosition == this.playerHashMap.get(groupPosition).selectedItemPosition)
                titleTV.setTextColor(context.getResources().getColor(R.color.elv_selected_text));
            else
                titleTV.setTextColor(context.getResources().getColor(R.color.elv_unselected_text));
            titleTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        header = getChild(groupPosition, childPosition);

                        selectedItemPosition = childPosition;

                        if (expandableListView != null)
                            expandableListView.collapseGroup(groupPosition);

                        if (resolutionAudioSrtSelection != null) {
                            resolutionAudioSrtSelection.onResolutionAudioSrtSelection("", selectedItemPosition,
                                    typeOfSelection);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (
                Exception e)

        {
            e.printStackTrace();
        }
        } else {*/
        try {
            ChildViewHolder childViewHolder;
            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.expand_row_resolution, null);

                childViewHolder = new ChildViewHolder();
                childViewHolder.titleTV = (TextView) convertView.findViewById(R.id.titleTV);
                childViewHolder.subtitleTV = (TextView) convertView.findViewById(R.id.subtitleTV);
                childViewHolder.radioButton = (RadioButton) convertView.findViewById(R.id.radioBtn);
                childViewHolder.rootLayout = (LinearLayout) convertView.findViewById(R.id.rootLayout);

                convertView.setTag(childViewHolder);
            } else
                childViewHolder = (ChildViewHolder) convertView.getTag();

           /* String title = "", dataConsumed = "";
            if (typeOfSelection == 0) {
                switch (childPosition) {
                    case 0:
                        title = "Auto";
                        break;
                    case 1:
                        title = "Best";
                        dataConsumed = "0.50";
                        break;
                    case 2:
                        title = "Better";
                        dataConsumed = "0.35";
                        break;
                    case 3:
                        title = "Good";
                        dataConsumed = "0.20";
                        break;
                    case 4:
                        title = "Data Saver";
                        dataConsumed = "0.10";
                        break;
                }
            } else*/
            String title = this.playerHashMap.get(groupPosition).listChildData.get(childPosition);

            childViewHolder.titleTV.setText(title);
            childViewHolder.titleTV.setVisibility(View.GONE);
            childViewHolder.radioButton.setText(title);
            /*if (typeOfSelection == 0) {
                String subtitle;
                if (childPosition != 0)
                    subtitle = "Usage about " + dataConsumed + " GB/hour";
                else
                    subtitle = "Usage as per bandwidth";

                childViewHolder.subtitleTV.setText(subtitle);
                childViewHolder.subtitleTV.setVisibility(View.VISIBLE);
            } else*/
            childViewHolder.subtitleTV.setVisibility(View.GONE);

            if (childPosition == this.playerHashMap.get(groupPosition).selectedItemPosition) {
                childViewHolder.radioButton.setTextColor(context.getResources().getColor(R.color.white));
                childViewHolder.radioButton.setChecked(true);
                previouslySelectedRadioBtn = childViewHolder.radioButton;
                previouslySelectedTitleTV = childViewHolder.titleTV;
            } else {
                childViewHolder.radioButton.setTextColor(context.getResources().getColor(R.color.elv_unselected_text));
                childViewHolder.radioButton.setChecked(false);
            }

            childViewHolder.rootLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        selectedItemPosition = childPosition;

                        if (resolutionAudioSrtSelection != null) {
                            resolutionAudioSrtSelection.onResolutionAudioSrtSelection("", selectedItemPosition,
                                    typeOfSelection);
                        }

                        if (dialog != null)
                            dialog.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            childViewHolder.radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        selectedItemPosition = childPosition;

                        if (resolutionAudioSrtSelection != null) {
                            resolutionAudioSrtSelection.onResolutionAudioSrtSelection("", selectedItemPosition,
                                    typeOfSelection);
                        }

                        if (dialog != null)
                            dialog.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*}*/
        ;
        /*return childView;*/
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private static class GroupViewHolder {
        TextView selectionTypeTV;
        TextView groupTitleTV;
        ImageView expandIV;
    }

    private static class ChildViewHolder {
        TextView titleTV;
        TextView subtitleTV;
        RadioButton radioButton;
        LinearLayout rootLayout;
    }
}
