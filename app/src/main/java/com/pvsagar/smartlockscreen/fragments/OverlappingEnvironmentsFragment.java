package com.pvsagar.smartlockscreen.fragments;

import android.app.Fragment;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.pvsagar.smartlockscreen.R;
import com.pvsagar.smartlockscreen.adapters.RadioButtonListAdapter;
import com.pvsagar.smartlockscreen.applogic_objects.Environment;
import com.pvsagar.smartlockscreen.applogic_objects.OverlappingEnvironmentIdsWithResolved;
import com.pvsagar.smartlockscreen.backend_helpers.SharedPreferencesHelper;
import com.pvsagar.smartlockscreen.services.BaseService;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class OverlappingEnvironmentsFragment extends Fragment {

    private LinearLayout mLinearLayout;
    private int mListPreferredItemHeight, mVerticalPadding, mHorizontalPadding;
    private boolean isEmpty = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setDimensions();
        ScrollView rootView = new ScrollView(getActivity());
        rootView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mLinearLayout = new LinearLayout(getActivity());
        mLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
        rootView.setBackgroundColor(getResources().getColor(R.color.card_background_grey));

        addRadioButtonLists(inflater);
        if(isEmpty){
            TextView textView = new TextView(getActivity());
            textView.setText(getString(R.string.no_overlap_detected));
            textView.setGravity(Gravity.CENTER);
            textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
            return textView;
        }

        rootView.addView(mLinearLayout);

        SystemBarTintManager tintManager = new SystemBarTintManager(getActivity());
        int mPaddingBottom, mPaddingTop;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            mPaddingBottom = tintManager.getConfig().getNavigationBarHeight();
            mPaddingTop = tintManager.getConfig().getPixelInsetTop(true);
            if(Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT){
                mPaddingTop += 16;
            }
            switch (getActivity().getResources().getConfiguration().orientation){
                case Configuration.ORIENTATION_UNDEFINED:
                case Configuration.ORIENTATION_PORTRAIT:
                    rootView.setPadding(rootView.getPaddingLeft(), rootView.getPaddingTop() + mPaddingTop,
                            rootView.getPaddingRight(), rootView.getPaddingBottom());
                    View bottomPaddingView = new View(getActivity());
                    bottomPaddingView.setBackgroundColor(Color.TRANSPARENT);
                    bottomPaddingView.setLayoutParams(new AbsListView.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, mPaddingBottom));
                    bottomPaddingView.setEnabled(false);
                    mLinearLayout.addView(bottomPaddingView);
                    break;
                case Configuration.ORIENTATION_LANDSCAPE:
                    rootView.setPadding(rootView.getPaddingLeft(), rootView.getPaddingTop() + mPaddingTop,
                            rootView.getPaddingRight() + mPaddingBottom, rootView.getPaddingBottom());
                    break;
            }
        }
        return rootView;
    }

    private void setDimensions(){
        mListPreferredItemHeight = (int) getListPreferredItemHeight();
        mHorizontalPadding = (int) getActivity().getResources().getDimension(R.dimen.activity_horizontal_margin);
        mVerticalPadding = (int) getActivity().getResources().getDimension(R.dimen.activity_vertical_margin);
    }

    private void addRadioButtonLists(LayoutInflater inflater){
        final List<OverlappingEnvironmentIdsWithResolved> environmentIdsWithResolved =
                SharedPreferencesHelper.getAllEnvironmentOverlaps(getActivity());
        if(environmentIdsWithResolved == null || environmentIdsWithResolved.size() == 0){
            isEmpty = true;
            return;
        }
        for (final OverlappingEnvironmentIdsWithResolved resolved : environmentIdsWithResolved) {
            ListView listView = new ListView(getActivity());
            AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    resolved.setResolvedEnvId(resolved.getOverlappingEnvIds().get(position));
                    resolved.getBareboneEnvironmentList(getActivity());
                    SharedPreferencesHelper.setEnvironmentOverlapChoice(
                            resolved.getBareboneEnvironmentList(getActivity()),
                            resolved.getResolvedEnvId(), getActivity());
                    getActivity().startService(BaseService.getServiceIntent(getActivity(), null,
                            BaseService.ACTION_DETECT_ENVIRONMENT));
                }
            };
            RadioButtonListAdapter<Environment> adapter =
                    new RadioButtonListAdapter<Environment>(getActivity(),
                            R.layout.list_item_environment_overlap, R.id.radio_button_environment_overlap,
                            resolved.getBareboneEnvironmentList(getActivity()), listener);
            listView.setAdapter(adapter);
            adapter.setSelectedPosition(resolved.getResolvedEnvIndex());
            LinearLayout.LayoutParams listParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    mListPreferredItemHeight * resolved.getOverlappingEnvIds().size());
            listParams.setMargins(mVerticalPadding, mHorizontalPadding, mVerticalPadding, 0);
            listView.setLayoutParams(listParams);
            listView.setBackgroundColor(Color.WHITE);
            listView.setDividerHeight(0);
            mLinearLayout.addView(listView);
            View separatorView = inflater.inflate(R.layout.shadow_separator, mLinearLayout, false);
            LinearLayout.LayoutParams separatorParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            separatorParams.setMargins(mVerticalPadding, 0, mVerticalPadding, 0);
            separatorView.setLayoutParams(separatorParams);
            mLinearLayout.addView(separatorView);
        }
    }

    private float getListPreferredItemHeight(){
        android.util.TypedValue value = new android.util.TypedValue();
        getActivity().getTheme().resolveAttribute(
                android.R.attr.listPreferredItemHeightSmall, value, true);
        TypedValue.coerceToString(value.type, value.data);
        android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return value.getDimension(metrics);
    }
}
