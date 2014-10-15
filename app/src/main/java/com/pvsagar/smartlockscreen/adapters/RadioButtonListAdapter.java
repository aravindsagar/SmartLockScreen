package com.pvsagar.smartlockscreen.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;

import com.pvsagar.smartlockscreen.R;

import java.util.List;

/**
 * Created by aravind on 9/10/14.
 * Adapter which works like an ArrayAdapter but instead of TextView, it populates the text fields
 * of radio buttons
 */
public class RadioButtonListAdapter<T> extends ArrayAdapter<T>{
    int selectedPosition = 0;
    int mLayoutId, mRadioButtonId;
    Context mContext;
    List<T> mValues;
    int textViewTouchedColor, textViewNormalColor;
    AdapterView.OnItemClickListener mListener;

    public RadioButtonListAdapter(Context context, int layoutId, int radioButtonId, List<T> values,
                                  AdapterView.OnItemClickListener listener){
        super(context, layoutId, radioButtonId, values);
        mContext = context;
        mLayoutId = layoutId;
        mRadioButtonId = radioButtonId;
        mValues = values;
        mListener = listener;

        textViewNormalColor = Color.argb(0, 0, 0, 0);
        textViewTouchedColor = context.getResources().getColor(R.color.text_view_touched);
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(mLayoutId, null);
        }

        RadioButton r = (RadioButton)v.findViewById(mRadioButtonId);
        r.setChecked(position == selectedPosition);
        r.setTag(position);
        r.setText(mValues.get(position).toString());
        r.setOnTouchListener(new TextViewTouchListener());
        r.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedPosition = (Integer)view.getTag();
                notifyDataSetInvalidated();
                mListener.onItemClick(null, convertView, position, getItemId(position));
            }
        });
        return v;
    }

    public void setSelectedPosition(int selectedPosition){
        this.selectedPosition = selectedPosition;
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
