package com.freeletics.khonshu.navigation.internal

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts

/**
 * Wrapper for [ActivityResultContracts.RequestMultiplePermissions] so that a [List] is used
 * as input instead of [Array]. The reason for this is that [Array.equals] does not compare
 * the contents which makes testing [NavEvent.ActivityResultEvent] painful.
 */
@InternalNavigationApi
public class RequestPermissionsContract :
    ActivityResultContract<List<String>, Map<String, Boolean>>() {

    private val contract = ActivityResultContracts.RequestMultiplePermissions()

    override fun createIntent(context: Context, input: List<String>): Intent {
        return contract.createIntent(context, input.toTypedArray())
    }

    override fun getSynchronousResult(
        context: Context,
        input: List<String>,
    ): SynchronousResult<Map<String, Boolean>>? {
        return contract.getSynchronousResult(context, input.toTypedArray())
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Map<String, Boolean> {
        return contract.parseResult(resultCode, intent)
    }
}
