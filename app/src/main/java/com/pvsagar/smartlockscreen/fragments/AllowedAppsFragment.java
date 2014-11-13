package com.pvsagar.smartlockscreen.fragments;

import android.app.Fragment;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.pvsagar.smartlockscreen.R;
import com.pvsagar.smartlockscreen.adapters.AllowedAppsListAdapter;
import com.pvsagar.smartlockscreen.applogic_objects.App;
import com.pvsagar.smartlockscreen.applogic_objects.User;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.List;

/**
 * Created by aravind on 11/11/14.
 */
public class AllowedAppsFragment extends Fragment {
    private static String ARG_CURRENT_USER = "current_user_id";

    private ListView allowedAppsListView;
    private ProgressBar progressBar;
    private int mPaddingBottom;
    private int mPaddingTop;
    private User mCurrentUser;

    public AllowedAppsFragment(){

    }

    public static AllowedAppsFragment getNewInstance(Long currentUserId){
        AllowedAppsFragment fragment = new AllowedAppsFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_CURRENT_USER, currentUserId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null){
            mCurrentUser = User.getUserWithId(getActivity(), getArguments().getLong(ARG_CURRENT_USER));
        } else {
            throw new IllegalArgumentException("Current user id must be passed into the fragment. Use "
                    + AllowedAppsFragment.class.getSimpleName() + ".getNewInstance(Long currentUserId)" +
                    " to get an instance of the fragment.");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_allowed_apps, container, false);
        allowedAppsListView = (ListView) rootView.findViewById(R.id.list_view_allowed_apps);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar_allowed_apps);

        SystemBarTintManager tintManager = new SystemBarTintManager(getActivity());
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            mPaddingBottom = tintManager.getConfig().getNavigationBarHeight();
            mPaddingTop = tintManager.getConfig().getPixelInsetTop(true);
            if(Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT){
                mPaddingTop += 16;
            }
        }

        getActivity().setProgressBarIndeterminateVisibility(true);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);

        switch (getActivity().getResources().getConfiguration().orientation){
            case Configuration.ORIENTATION_UNDEFINED:
            case Configuration.ORIENTATION_PORTRAIT:
                rootView.setPadding(rootView.getPaddingLeft(), rootView.getPaddingTop() + mPaddingTop,
                        rootView.getPaddingRight(), rootView.getPaddingBottom());
                View bottomPaddingView = new View(getActivity());
                bottomPaddingView.setBackgroundColor(Color.TRANSPARENT);
                bottomPaddingView.setLayoutParams(new AbsListView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, mPaddingBottom));
                allowedAppsListView.addFooterView(bottomPaddingView, null, false);
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                rootView.setPadding(rootView.getPaddingLeft(), rootView.getPaddingTop() + mPaddingTop,
                        rootView.getPaddingRight() + mPaddingBottom, rootView.getPaddingBottom());
                break;
        }
        new GetAppsAsyncTask().execute();

        return rootView;
    }

    private class GetAppsAsyncTask extends AsyncTask<Void, Void, List<App>>{
        @Override
        protected List<App> doInBackground(Void... params) {
            PackageManager packageManager = getActivity().getPackageManager();
            return App.getAllApps(packageManager);
        }

        @Override
        protected void onPostExecute(List<App> apps) {
            allowedAppsListView.setAdapter(new AllowedAppsListAdapter(getActivity(),
                    R.layout.list_item_allowed_apps, apps, mCurrentUser));
            getActivity().setProgressBarIndeterminateVisibility(false);
            progressBar.setVisibility(View.GONE);
            super.onPostExecute(apps);
        }
    }
}