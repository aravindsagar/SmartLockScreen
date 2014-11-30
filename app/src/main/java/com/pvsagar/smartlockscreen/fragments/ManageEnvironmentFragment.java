package com.pvsagar.smartlockscreen.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.pvsagar.smartlockscreen.AddEnvironment;
import com.pvsagar.smartlockscreen.EditEnvironment;
import com.pvsagar.smartlockscreen.R;
import com.pvsagar.smartlockscreen.SetUnknownEnvironmentPassword;
import com.pvsagar.smartlockscreen.adapters.EnvironmentListAdapter;
import com.pvsagar.smartlockscreen.applogic_objects.Environment;
import com.pvsagar.smartlockscreen.cards.CardTouchListener;
import com.pvsagar.smartlockscreen.frontend_helpers.CharacterDrawable;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.List;

/**
 * Created by aravind on 7/10/14.
 * Fragment which shows a list of added Environments.
 */
public class ManageEnvironmentFragment extends Fragment {
    private static final String LOG_TAG = ManageEnvironmentFragment.class.getSimpleName();

    List<Environment> environments;
    ListView environmentsListView;

    private int mPaddingTop, mPaddingBottom;

    int textViewTouchedColor, textViewNormalColor;

    private ActionModeListener actionModeListener;

    private View unknownEnvironmentView;
    private TextView noEnvironmentsTextView;

    public ManageEnvironmentFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        textViewNormalColor = Color.argb(0,0,0,0);
        textViewTouchedColor = getResources().getColor(R.color.text_view_touched_darker);

        View rootView = inflater.inflate(R.layout.fragment_manage_environment, container, false);
        CardView environmentsCardView = (CardView) rootView.findViewById(R.id.card_view_environments);
        environmentsCardView.setPreventCornerOverlap(true);
        environmentsListView = (ListView)environmentsCardView.findViewById(R.id.list_view_environments);
        SystemBarTintManager tintManager = new SystemBarTintManager(getActivity());
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            mPaddingBottom = tintManager.getConfig().getNavigationBarHeight();
            mPaddingTop = tintManager.getConfig().getPixelInsetTop(true);
            if(Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT){
                mPaddingTop += 24;
            }
        }
        setUnknownEnvironmentLayout(rootView);

        switch (getActivity().getResources().getConfiguration().orientation){
            case Configuration.ORIENTATION_UNDEFINED:
            case Configuration.ORIENTATION_PORTRAIT:
                rootView.setPadding(rootView.getPaddingLeft(), rootView.getPaddingTop() + mPaddingTop,
                        rootView.getPaddingRight(), rootView.getPaddingBottom() + mPaddingBottom);
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                rootView.setPadding(rootView.getPaddingLeft(), rootView.getPaddingTop() + mPaddingTop,
                        rootView.getPaddingRight() + mPaddingBottom, rootView.getPaddingBottom());
                break;
        }

        noEnvironmentsTextView = (TextView) rootView.findViewById(R.id.text_view_no_environment);
        noEnvironmentsTextView.setVisibility(View.GONE);

        //Init
        init();
        return rootView;
    }

    private void init(){
        environments = Environment.getAllEnvironmentBarebones(getActivity());

            /* Creating the adapter */
        final EnvironmentListAdapter listAdapter = new EnvironmentListAdapter(getActivity(), environments);
        environmentsListView.setAdapter(listAdapter);
        environmentsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(),EditEnvironment.class);
                intent.putExtra(EditEnvironment.INTENT_EXTRA_ENVIRONMENT,listAdapter.getItem(position).getName());
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
                unknownEnvironmentView.setAlpha(1);
                unknownEnvironmentView.animate().alpha(0).setDuration(200).setInterpolator(new AccelerateInterpolator()).start();
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
                                Environment selectedItem = listAdapter.getItem(selected.keyAt(i));
                                Environment.deleteEnvironmentFromDatabase(getActivity(),selectedItem.getName());
                                // Remove selected items following the ids
                                listAdapter.remove(selectedItem);
                            }
                        }
                        // Close CAB
                        mode.finish();
                        if(listAdapter.getCount() == 0){
                            noEnvironmentsTextView.setVisibility(View.VISIBLE);
                        }
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                listAdapter.removeSelection();
                actionModeListener.onActionModeDestroyed();
                unknownEnvironmentView.setAlpha(0);
                unknownEnvironmentView.animate().alpha(1).setDuration(200).setInterpolator(new AccelerateInterpolator()).start();
            }
        });

        if(environments.isEmpty()){
            noEnvironmentsTextView.setVisibility(View.VISIBLE);
        } else {
            noEnvironmentsTextView.setVisibility(View.GONE);
        }

            /* End of adapter code */
    }

    private void setUnknownEnvironmentLayout(View rootView){
        CardView unknownLayout = (CardView) rootView.findViewById(R.id.card_view_unknown_environment);

//        LinearLayout listItemLayout = (LinearLayout) unknownLayout.findViewById(R.id.linear_layout_list_items);
//        listItemLayout.setOnTouchListener(new TextViewTouchListener());

        ImageView imageView = (ImageView) unknownLayout.findViewById(R.id.image_view_environment_picture);
        imageView.setImageDrawable(new CharacterDrawable('?', Color.rgb(150, 150, 150)));

        TextView textView = (TextView) unknownLayout.findViewById(R.id.text_view_environment_list);
        textView.setText("Unknown Environment");

        unknownLayout.setOnTouchListener(new CardTouchListener(getResources().getColor(R.color.text_view_touched)));
        unknownLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SetUnknownEnvironmentPassword.class));
            }
        });

        unknownEnvironmentView = unknownLayout;
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
}
