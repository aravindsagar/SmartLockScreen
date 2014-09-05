package com.pvsagar.smartlockscreen;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.pvsagar.smartlockscreen.applogic_objects.Environment;

import java.util.ArrayList;
import java.util.List;

public class ManageEnvironment extends ActionBarActivity {

    private static final String LOG_TAG = ManageEnvironment.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_environment);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.manage_environment, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if(id == R.id.action_add_environment){
            Intent intent = new Intent(this,AddEnvironment.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        List<String> environmentNames = new ArrayList<String>();
        List<Environment> environments;
        List<Boolean> enabledValues = new ArrayList<Boolean>();
        List<String> environmentHints = new ArrayList<String>();
        ListView environmentsListView;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_manage_environment, container, false);
            environmentsListView = (ListView)rootView.findViewById(R.id.list_view_environments);
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
                    mode.setTitle(checkedCount + " Selected");
                    // Calls toggleSelection method from ListViewAdapter Class
                    listAdapter.toggleSelection(position);

                }
                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    mode.getMenuInflater().inflate(R.menu.multi_select_cab_menu,menu);
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
                                    //Todo: Delete the environment
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
                }
            });
            /* End of adapter code */
        }

        @Override
        public void onResume() {
            super.onResume();
            init();
        }
    }
}
