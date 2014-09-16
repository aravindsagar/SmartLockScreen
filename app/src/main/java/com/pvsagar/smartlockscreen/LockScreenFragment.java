package com.pvsagar.smartlockscreen;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.view.CardListView;

/**
 * Created by aravind on 6/8/14.
 */
public class LockScreenFragment extends Fragment {

    public LockScreenFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_lock_screen, container, false);
        buildCardList(rootView);
        Button unlockButton = (Button) rootView.findViewById(R.id.unlock_button);
        unlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        return rootView;
    }

    private void buildCardList(View rootView){
        ArrayList<Card> cards = new ArrayList<Card>();
        //Create a Card
        Card card = new Card(getActivity());

        //Create a CardHeader
        CardHeader header = new CardHeader(getActivity());

        header.setTitle("Testing1");
        //Add Header to card
        card.addCardHeader(header);
        card.setTitle("Testing2");

        cards.add(card);

        CardArrayAdapter mCardArrayAdapter = new CardArrayAdapter(getActivity(),cards);
        CardListView listView = (CardListView) rootView.findViewById(R.id.notification_list);

        listView.setAdapter(mCardArrayAdapter);
    }
}