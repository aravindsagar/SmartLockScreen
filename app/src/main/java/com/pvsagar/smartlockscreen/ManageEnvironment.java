package com.pvsagar.smartlockscreen;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.pvsagar.smartlockscreen.applogic_objects.Environment;

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

        String[] environmentNames;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_manage_environment, container, false);

            //Init
            environmentNames = Environment.getAllEnvironments(getActivity()).toArray(new String[0]);    //Getting environment list
            ListView environmentsListView = (ListView)rootView.findViewById(R.id.list_view_environments);

            /* Creating the adapter */
            EnvironmentListAdapter listAdapter = new EnvironmentListAdapter(getActivity(),environmentNames);
            environmentsListView.setAdapter(listAdapter);
            /* End of adapter code */

            return rootView;
        }
    }
}
