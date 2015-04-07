package com.pvsagar.smartlockscreen.services.window_helpers;

import android.animation.Animator;
import android.content.Context;
import android.graphics.PixelFormat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.pvsagar.smartlockscreen.R;
import com.pvsagar.smartlockscreen.baseclasses.LockScreenOverlay;

/**
 * Created by aravind on 9/1/15.
 */
public class MinimalLockScreenOverlay extends LockScreenOverlay {
    private View minimalLockScreenOverlay;
    private ViewPager viewPager;
    private ImageView icon;

    public MinimalLockScreenOverlay(Context context, WindowManager windowManager) {
        super(context, windowManager);
    }

    @Override
    protected WindowManager.LayoutParams buildLayoutParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);

        params.x = 0;
        params.y = 0;
        params.gravity = Gravity.CENTER_VERTICAL | Gravity.START;
        params.systemUiVisibility = getFullScreenSystemUiVisibility();
        return params;
    }

    @Override
    protected View buildLayout() {
        MinimalLockScreenPagerAdapter pagerAdapter = new MinimalLockScreenPagerAdapter();
        if(minimalLockScreenOverlay == null){
            minimalLockScreenOverlay = getInflater().inflate(R.layout.minimal_lockscreen_overlay, null);
            icon = (ImageView) minimalLockScreenOverlay.findViewById(R.id.image_view_minimal_lockscreen_options);
            viewPager = (ViewPager) minimalLockScreenOverlay.findViewById(R.id.view_pager_lockscreen_options);
            viewPager.setAdapter(pagerAdapter);

            ImageView lockscreenIcon = (ImageView) minimalLockScreenOverlay.findViewById(R.id.image_view_minimal_lockscreen_options);
            lockscreenIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(viewPager.getVisibility() == View.GONE){
                       showOptions();
                    } else {
                        hideOptions();
                    }
                }
            });
            minimalLockScreenOverlay.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    lockScreenDismiss(0);
                    return false;
                }
            });
        }
        resetOverlayProperties();

        setUpUserOptions();
        setUpEnvironmentOptions();
        viewPager.invalidate();
        hideOptions();

        return minimalLockScreenOverlay;
    }

    private void hideOptions(){
        viewPager.animate().scaleX(0f).scaleY(0f).translationX(-viewPager.getWidth()/2).translationY(-viewPager.getHeight()/2).
                setInterpolator(new DecelerateInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                viewPager.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                viewPager.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).start();

    }

    private void showOptions(){
        /*RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(viewPager.getLayoutParams());
        params.width = getDisplayWidth() - icon.getWidth() - viewPager.getPaddingLeft() - viewPager.getPaddingRight();
        viewPager.setLayoutParams(params);
        minimalLockScreenOverlay.requestLayout();*/

        viewPager.setScaleX(0);
        viewPager.setScaleY(0);
        viewPager.setTranslationY(-viewPager.getHeight() / 2);
        viewPager.setTranslationX(-viewPager.getWidth() / 2);
        viewPager.setVisibility(View.VISIBLE);
        viewPager.animate().scaleX(1.0f).scaleY(1.0f).translationX(0).translationY(0).setListener(null).
                setInterpolator(new AccelerateDecelerateInterpolator()).start();
    }

    private class MinimalLockScreenPagerAdapter extends PagerAdapter {
        private final int NUMBER_OF_PAGES = 2;
        private final int ENVIRONMENT_PAGE = 0;
        private final int USER_PAGE = 1;

        @Override
        public int getCount() {
            return NUMBER_OF_PAGES;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            switch (position) {
                case ENVIRONMENT_PAGE:
                    buildEnvironmentOptions(container, false);
                    setUpEnvironmentOptions();
                    container.addView(getEnvironmentOptionsView());
                    return getEnvironmentOptionsView();
                case USER_PAGE:
                    buildUserOptions(container, false);
                    setUpUserOptions();
                    container.addView(getUserOptionsView());
                    return getUserOptionsView();
                default:
                    throw new IllegalArgumentException("Position " + position + " does not exist in ViewPager");
            }
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case ENVIRONMENT_PAGE:
                    return "Environment";
                case USER_PAGE:
                    return "User";
                default:
                    throw new IllegalArgumentException("Position " + position + " does not exist in ViewPager");
            }
        }
    }

    @Override
    protected void lockScreenDismiss(float endVelocity) {
        remove();
        /*minimalLockScreenOverlay.animate().scaleX(0f).scaleY(0f).translationY(-minimalLockScreenOverlay.getHeight()/2).
                translationX(-minimalLockScreenOverlay.getWidth()/2).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                remove();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                remove();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).start();*/
    }

    private void resetOverlayProperties(){
        minimalLockScreenOverlay.animate().setListener(null).scaleY(1f).scaleX(1f).translationY(0).translationX(0).start();
    }
}
