package com.pvsagar.smartlockscreen.frontend_helpers;

import android.content.Context;
import android.content.Intent;

/**
 * Created by aravind on 27/11/14.
 */
public final class MediaStoreUtils {
    private MediaStoreUtils() {
    }

    public static Intent getPickImageIntent(final Context context) {
        final Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        return Intent.createChooser(intent, "Select picture");
    }
}
