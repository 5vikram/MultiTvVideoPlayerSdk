package com.multitv.ott.multitvvideoplayer.popup;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;

import com.multitv.ott.multitvvideoplayer.R;

public class MyDialogFragment extends DialogFragment implements
        OnItemClickListener {

    private String[] listitems;
    private ListView mylist;
    private int typeOfSelection;
    private static ResolutionAudioSrtSelection selection;

    // MyDialogFragment myDialogFragmnt;

    public static MyDialogFragment getInstance(String title, String[] content,
                                               ResolutionAudioSrtSelection rs,
                                               int resolutionSelectedItemPosition,
                                               int typeOfSelection) {

        MyDialogFragment dialouge = new MyDialogFragment();
        // String[] stringArray = Arrays.copyOf(content, content.length,
        // String[].class);
        Bundle bundle = new Bundle();
        bundle.putString("Title", title);
        bundle.putStringArray("Data", content);
        bundle.putInt("current_selected", resolutionSelectedItemPosition);
        bundle.putInt("type_of_selection", typeOfSelection);
        dialouge.setArguments(bundle);
        selection = rs;

        return dialouge;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.resolution_dialouge_fragment,
                null, false);
        mylist = (ListView) view.findViewById(R.id.list);

        listitems = getArguments().getStringArray("Data");

        int resolutionSelectedItemPosition = getArguments().getInt("current_selected");
        typeOfSelection = getArguments().getInt("type_of_selection");

        String title = getArguments().getString("Title");

        setCancelable(true);
        getDialog().setCancelable(true);
        getDialog().setTitle(title);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                R.layout.resolution_item_layout, listitems);

        mylist.setAdapter(adapter);
        mylist.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        //int index = Arrays.asList(listitems).indexOf("currentSelected");
        mylist.setItemChecked(resolutionSelectedItemPosition, true);

        mylist.setOnItemClickListener(this);
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        dismiss();
        if (selection != null) {
            selection.onResolutionAudioSrtSelection(listitems[position], position, typeOfSelection);
            mylist.setItemChecked(position, true);
        }
    }

    public interface ResolutionAudioSrtSelection {
        void onResolutionAudioSrtSelection(String index, int selectedItemPosition, int typeOfSelection);
    }

}
