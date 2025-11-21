package com.beemdevelopment.aegis;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.beemdevelopment.aegis.receivers.VaultLockReceiver;
import com.beemdevelopment.aegis.ui.MainActivity;
import com.beemdevelopment.aegis.util.IOUtils;
import com.beemdevelopment.aegis.vault.VaultManager;
import com.topjohnwu.superuser.Shell;

import java.util.Collections;

import dagger.hilt.InstallIn;
import dagger.hilt.android.EarlyEntryPoint;
import dagger.hilt.android.EarlyEntryPoints;
import dagger.hilt.components.SingletonComponent;

public abstract class AegisApplicationBase extends Application {
    private static final String CODE_LOCK_STATUS_ID = "lock_status_channel";

    private VaultManager _vaultManager;

    static {
        // Enable verbose libsu logging in debug builds
        Shell.enableVerboseLogging = BuildConfig.DEBUG;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        _vaultManager = EarlyEntryPoints.get(this, EntryPoint.class).getVaultManager();

        VaultLockReceiver lockReceiver = new VaultLockReceiver();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        ContextCompat.registerReceiver(this, lockReceiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED);

        // lock the app if the user moves the application to the background
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new AppLifecycleObserver());

        // clear the cache directory on startup, to make sure no temporary vault export files remain
        IOUtils.clearDirectory(getCacheDir(), false);

        initAppShortcuts();

        // NOTE: Disabled for now. See issue: #1047
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            initNotificationChannels();
        }*/
    }

    private void initAppShortcuts() {
        ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
        if (shortcutManager == null) {
            return;
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("action", "scan");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setAction(Intent.ACTION_MAIN);

        ShortcutInfo shortcut = new ShortcutInfo.Builder(this, "shortcut_new")
                .setShortLabel("new_entry")
                .setLongLabel("add_new_entry")
                .setIcon(Icon.createWithResource(this, R.drawable.ic_qr_code))
                .setIntent(intent)
                .build();

        shortcutManager.setDynamicShortcuts(Collections.singletonList(shortcut));
    }

    private class AppLifecycleObserver implements LifecycleEventObserver {
        @Override
        public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
            if (event == Lifecycle.Event.ON_STOP
                    && _vaultManager.isAutoLockEnabled(Preferences.AUTO_LOCK_ON_MINIMIZE)
                    && !_vaultManager.isAutoLockBlocked()) {
                _vaultManager.lock(false);
            }
        }
    }

    @EarlyEntryPoint
    @InstallIn(SingletonComponent.class)
    interface EntryPoint {
        VaultManager getVaultManager();
    }
}
