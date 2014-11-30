package com.pvsagar.smartlockscreen;

import android.animation.Animator;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.pvsagar.smartlockscreen.adapters.PictureSpinnerAdapter;
import com.pvsagar.smartlockscreen.applogic_objects.Environment;
import com.pvsagar.smartlockscreen.applogic_objects.User;
import com.pvsagar.smartlockscreen.backend_helpers.Picture;
import com.pvsagar.smartlockscreen.backend_helpers.Utility;
import com.pvsagar.smartlockscreen.cards.InnerViewElementsSetUpListener;
import com.pvsagar.smartlockscreen.cards.OptionCardHeader;
import com.pvsagar.smartlockscreen.frontend_helpers.CharacterDrawable;

import java.util.ArrayList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.ViewToClickToExpand;
import it.gmariotti.cardslib.library.view.CardView;

/**
 * Activity which provides the ui for user to pick a picture for user/environment
 */
public class ChoosePicture extends Activity {
    private static final String LOG_TAG = ChoosePicture.class.getSimpleName();
    private static final String PACKAGE_NAME = ChoosePicture.class.getPackage().getName();
    public static final String EXTRA_OBJECT_TYPE = PACKAGE_NAME + ".OBJECT_TYPE";
    public static final String EXTRA_OBJECT_ID = PACKAGE_NAME + ".OBJECT_ID";
    public static final String EXTRA_IMAGE_VIEW_START_LOCATION = PACKAGE_NAME + ".START_LOCATION";

    private int initX, initY;

    private ImageView imageView, backgroundImageView;
    private LinearLayout cardLinearLayout;

    private OptionCardHeader colorCardHeader, inBuiltPictureCardHeader, customPictureCardHeader;

    public enum ObjectType {USER, ENVIRONMENT}

    ObjectType pictureObjectType;
    private long objectId;

    private Environment environment;
    private User user;
    private Picture pictureUnderEdit;

    private boolean entryAnimationDone = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_picture);
        imageView = (ImageView) findViewById(R.id.image_view_edit_picture);
        cardLinearLayout = (LinearLayout) findViewById(R.id.card_linear_layout);
        backgroundImageView = (ImageView) findViewById(R.id.backgroud_image_view);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            int flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            getWindow().setFlags(flags, flags);
        }

        pictureObjectType = (ObjectType) getIntent().getSerializableExtra(EXTRA_OBJECT_TYPE);

        setUpImageView();
        setUpCards();
        initCards();
    }

    private void setUpCards() {
        Card colorCard, inBuiltPictureCard, customPictureCard;
        colorCard = new Card(this);
        ViewToClickToExpand viewToClickToExpand = ViewToClickToExpand.builder().enableForExpandAction();
        colorCard.setViewToClickToExpand(viewToClickToExpand);

        inBuiltPictureCard = new Card(this);
        ViewToClickToExpand viewToClickToExpand2 = ViewToClickToExpand.builder().enableForExpandAction();
        inBuiltPictureCard.setViewToClickToExpand(viewToClickToExpand2);

        customPictureCard = new Card(this);
        ViewToClickToExpand viewToClickToExpand3 = ViewToClickToExpand.builder().enableForExpandAction();
        customPictureCard.setViewToClickToExpand(viewToClickToExpand3);

        colorCardHeader = new OptionCardHeader(this, new InnerViewElementsSetUpListener<OptionCardHeader>() {
            @Override
            public void onInnerViewElementsSetUp(OptionCardHeader card) {
                card.setTitle("Use letter with background");
                card.getRadioButton().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        enableColorCard();
                        disableBuiltInPictureCard();
                        disableCustomPictureCard();
                    }
                });
                setupColorCardSpinner(card.getSpinner1());
                card.getSpinner2().setVisibility(View.GONE);
            }
        });
        colorCard.addCardHeader(colorCardHeader);

        inBuiltPictureCardHeader = new OptionCardHeader(this, new InnerViewElementsSetUpListener<OptionCardHeader>() {
            @Override
            public void onInnerViewElementsSetUp(OptionCardHeader card) {
                card.setTitle("Use in built picture");
                card.getRadioButton().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        disableColorCard();
                        enableBuiltInPictureCard();
                        disableCustomPictureCard();
                    }
                });
                setupBuiltInPictureCardSpinner(card.getSpinner1());
                setupColorCardSpinner(card.getSpinner2());
            }
        });
        inBuiltPictureCard.addCardHeader(inBuiltPictureCardHeader);

        customPictureCardHeader = new OptionCardHeader(this, new InnerViewElementsSetUpListener<OptionCardHeader>() {
            @Override
            public void onInnerViewElementsSetUp(OptionCardHeader card) {
                card.setTitle("Use custom picture");
                card.getRadioButton().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        disableColorCard();
                        disableBuiltInPictureCard();
                        enableCustomPictureCard();
                    }
                });

                card.getSpinner1().setVisibility(View.GONE);
                card.getSpinner2().setVisibility(View.GONE);
            }
        });
        customPictureCard.addCardHeader(customPictureCardHeader);

        CardView colorCardView = (CardView) cardLinearLayout.findViewById(R.id.card_pick_color);
        colorCardView.setCard(colorCard);

        CardView buildInCardView = (CardView) cardLinearLayout.findViewById(R.id.card_pick_built_in);
        buildInCardView.setCard(inBuiltPictureCard);
        buildInCardView.setVisibility(View.GONE);

        CardView customCardView = (CardView) cardLinearLayout.findViewById(R.id.card_pick_custom);
        customCardView.setCard(customPictureCard);
    }

    private void initCards() {

    }

    private void disableColorCard(){
        colorCardHeader.getRadioButton().setChecked(false);
        colorCardHeader.getSpinner1().setVisibility(View.GONE);
    }

    private void enableColorCard(){
        colorCardHeader.getRadioButton().setChecked(true);
        colorCardHeader.getSpinner1().setVisibility(View.VISIBLE);
    }

    private void disableBuiltInPictureCard(){
        inBuiltPictureCardHeader.getRadioButton().setChecked(false);
        inBuiltPictureCardHeader.getSpinner1().setVisibility(View.GONE);
        inBuiltPictureCardHeader.getSpinner2().setVisibility(View.GONE);
    }

    private void enableBuiltInPictureCard(){
        inBuiltPictureCardHeader.getRadioButton().setChecked(true);
        inBuiltPictureCardHeader.getSpinner1().setVisibility(View.VISIBLE);
        inBuiltPictureCardHeader.getSpinner2().setVisibility(View.VISIBLE);
    }

    private void disableCustomPictureCard(){
        customPictureCardHeader.getRadioButton().setChecked(false);
    }

    private void enableCustomPictureCard(){
        customPictureCardHeader.getRadioButton().setChecked(true);
    }

    private void setupColorCardSpinner(Spinner spinner){
        int borderType;
        if(user != null){
            borderType = CharacterDrawable.BORDER_DARKER;
        } else if(environment != null){
            borderType = CharacterDrawable.BORDER_LIGHTER;
        } else {
            return;
        }
        List<Drawable> drawables = new ArrayList<Drawable>();
        for(Integer color:Utility.getPictureColors()){
            drawables.add(new CharacterDrawable(' ', this.getResources().getColor(color), borderType));
        }

        spinner.setAdapter(new PictureSpinnerAdapter(this, R.layout.list_item_picture, drawables));
    }

    private void setupBuiltInPictureCardSpinner(Spinner spinner){
        spinner.setAdapter(new PictureSpinnerAdapter(this, R.layout.list_item_picture, Utility.getBuiltInPictureDrawables(this)));
    }

    private void setUpImageView(){

        int[] location = getIntent().getIntArrayExtra(EXTRA_IMAGE_VIEW_START_LOCATION);
//        clickedImageView.getLocationInWindow(location);
        if(location != null && location.length >= 2) {
            initX = location[0];
            initY = location[1];
        } else {
            initX = initY = 0;
        }

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
        params.setMargins(initX, initY, 0, 0);
        imageView.setLayoutParams(params);

        objectId = getIntent().getLongExtra(EXTRA_OBJECT_ID, -1);
        Drawable imageDrawable;
        switch (pictureObjectType){
            case ENVIRONMENT:
                environment = Environment.getBareboneEnvironment(this, objectId);
                pictureUnderEdit = environment.getEnvironmentPicture();
                imageDrawable = environment.getEnvironmentPictureDrawable(this);
                break;
            case USER:
                user = User.getUserWithId(this, objectId);
                pictureUnderEdit = user.getUserPicture();
                imageDrawable = user.getUserPictureDrawable(this);
                break;
            default:
                imageDrawable = null;
        }
        imageView.setImageDrawable(imageDrawable);
    }

    @Override
    public void onBackPressed() {
        imageView.animate().translationY(0).setInterpolator(new AccelerateDecelerateInterpolator()).
                setDuration(200).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        finish();
                        overridePendingTransition(0, android.R.anim.fade_out);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        finish();
                        overridePendingTransition(0, android.R.anim.fade_out);
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
//        backgroundImageView.animate().alpha(0.0f).start();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(entryAnimationDone){
            return;
        }
        imageView.animate().translationY(cardLinearLayout.getY() - imageView.getY()).
                setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(200).start();
        backgroundImageView.animate().alpha(0.9f).start();
        cardLinearLayout.animate().alpha(1).start();
        entryAnimationDone = true;
    }
}