package com.multitv.ott.multitvvideoplayer.providers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.SessionProvider;
import com.multitv.ott.multitvvideoplayer.R;

import java.util.Collections;
import java.util.List;

public class CastOptionsProvider implements OptionsProvider {
    public static final String APP_ID_DEFAULT_RECEIVER_WITH_DRM = "A12D4273";

    @Override
    public CastOptions getCastOptions(Context context) {
        CastOptions castOptions = new CastOptions.Builder()
                .setReceiverApplicationId(APP_ID_DEFAULT_RECEIVER_WITH_DRM)
//                .setSupportedNamespaces(supportedNamespaces)
                .build();
        return castOptions;
    }

    @Override
    public List<SessionProvider> getAdditionalSessionProviders(Context context) {
        return Collections.emptyList();
    }
}