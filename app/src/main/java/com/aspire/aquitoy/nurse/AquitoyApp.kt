package com.aspire.aquitoy.nurse

import android.app.Application
import com.google.firebase.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import timber.log.Timber.DebugTree

@HiltAndroidApp
class AquitoyApp : Application()