package com.pvsagar.smartlockscreen.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.pvsagar.smartlockscreen.AddEnvironment;
import com.pvsagar.smartlockscreen.EditEnvironment;
import com.pvsagar.smartlockscreen.R;
import com.pvsagar.smartlockscreen.SetUnknownEnvironmentPassword;
import com.pvsagar.smartlockscreen.adapters.EnvironmentListAdapter;
import com.pvsagar.smartlockscreen.applogic_objects.Environment;
import com.pvsagar.smartlockscreen.backend_helpers.Utility;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aravind on 7/10/14.
 * Fragment which shows a list of added Environments.
 */
public class ManageEnvironmentFragment extends Fragment {
    private static final String LOG_TAG = ManageEnvironmentFragment.class.getSimpleName();

    List<String> environmentNames = new ArrayList<String>();
    List<Environment> environments;
    List<Boolean> enabledValues = new ArrayList<Boolean>();
    List<String> environmentHints = new ArrayList<String>();
    ListView environmentsListView;

    private int mPaddingTop, mPaddingBottom;

    int textViewTouchedColor, textViewNormalColor;

    private ActionModeListener actionModeListener;

    public ManageEnvironmentFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        textViewNormalColor = Color.argb(0,0,0,0);
        textViewTouchedColor = getResources().getColor(R.color.text_view_touched);

        View rootView = inflater.inflate(R.layout.fragment_manage_environment, container, false);
        environmentsListView = (ListView)rootView.findViewById(R.id.list_view_environments);
        SystemBarTintManager tintManager = new SystemBarTintManager(getActivity());
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            mPaddingBottom = tintManager.getConfig().getNavigationBarHeight();
            mPaddingTop = tintManager.getConfig().getPixelInsetTop(true);
        }
        environmentsListView.addFooterView(getUnknownEnvironmentLayout(inflater));
        switch (getActivity().getResources().getConfiguration().orientation){
            case Configuration.ORIENTATION_UNDEFINED:
            case Configuration.ORIENTATION_PORTRAIT:
                rootView.setPadding(rootView.getPaddingLeft(), rootView.getPaddingTop() + mPaddingTop,
                        rootView.getPaddingRight(), rootView.getPaddingBottom());
                View bottomPaddingView = new View(getActivity());
                bottomPaddingView.setBackgroundColor(Color.TRANSPARENT);
                bottomPaddingView.setLayoutParams(new AbsListView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, mPaddingBottom));
                environmentsListView.addFooterView(bottomPaddingView, null, false);
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                rootView.setPadding(rootView.getPaddingLeft(), rootView.getPaddingTop() + mPaddingTop,
                        rootView.getPaddingRight() + mPaddingBottom, rootView.getPaddingBottom());
                break;
        }

        //Init
        init();
        return rootView;
    }

    private void init(){

        environments = Environment.getAllEnvironmentBarebones(getActivity());
        enabledValues.clear();
        environmentHints.clear();
        environmentNames.clear();
        for(Environment e : environments){
            environmentNames.add(e.getName());
            environmentHints.add(e.getHint());
            enabledValues.add(e.isEnabled());
        }
            /* Creating the adapter */
        final EnvironmentListAdapter listAdapter = new EnvironmentListAdapter(getActivity(),
                environmentNames, enabledValues, environmentHints);
        environmentsListView.setAdapter(listAdapter);
        environmentsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(),EditEnvironment.class);
                intent.putExtra(EditEnvironment.INTENT_EXTRA_ENVIRONMENT,listAdapter.getItem(position));
                getActivity().startActivity(intent);
            }
        });
        environmentsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        environmentsListView.setMultiChoiceModeListener(new ListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                // Capture total checked items
                final int checkedCount = environmentsListView.getCheckedItemCount();
                // Set the CAB title according to total checked items
                mode.setTitle(checkedCount + " selected");
                // Calls toggleSelection method from ListViewAdapter Class
                listAdapter.toggleSelection(position);

            }
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.multi_select_cab_menu, menu);
                actionModeListener.onActionModeCreated();
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete:
                        // Calls getSelectedIds method from ListViewAdapter Class
                        SparseBooleanArray selected = listAdapter
                                .getSelectedIds();
                        // Captures all selected ids with a loop
                        for (int i = (selected.size() - 1); i >= 0; i--) {
                            if (selected.valueAt(i)) {
                                String selectedItem = listAdapter
                                        .getItem(selected.keyAt(i));
                                Environment.deleteEnvironmentFromDatabase(getActivity(),selectedItem);
                                // Remove selected items following the ids
                                listAdapter.remove(selectedItem);
                            }
                        }
                        // Close CAB
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                listAdapter.removeSelection();
                actionModeListener.onActionModeDestroyed();
            }
        });
            /* End of adapter code */
    }

    private LinearLayout getUnknownEnvironmentLayout(LayoutInflater inflater){
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.list_item_unknown_environment,
                environmentsListView, false);

        ImageView imageView = (ImageView) layout.findViewById(R.id.image_view_environment_picture);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.andquestionsag);
        imageView.setImageBitmap(Utility.getCroppedBitmap(bitmap, Color.DKGRAY));

        TextView textView = (TextView) layout.findViewById(R.id.text_view_environment_list);
        textView.setText("Unknown Environment");

        layout.setOnTouchListener(new TextViewTouchListener());

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SetUnknownEnvironmentPassword.class));
            }
        });
        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        init();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            actionModeListener = (ActionModeListener) activity;
        } catch (ClassCastException e){
            throw new InstantiationException("Activity using " + LOG_TAG + " should implement "
                    + ActionModeListener.class.getSimpleName(), e);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.manage_environment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id == R.id.action_add_environment){
            Intent intent = new Intent(getActivity(),AddEnvironment.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public interface ActionModeListener{
        public void onActionModeDestroyed();
        public void onActionModeCreated();
    }

    public class TextViewTouchListener implements View.OnTouchListener{
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    v.setBackgroundColor(textViewTouchedColor);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.setBackgroundColor(textViewNormalColor);
            }
            return false;
        }
    }
}
