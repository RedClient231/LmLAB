package com.lmlab.voidguardian.stub

import android.app.Activity
import android.os.Bundle
import com.lmlab.voidguardian.core.VirtualCore

/**
 * Stub Activity used as placeholder for virtualized apps.
 * All real activities are redirected here then launched in virtual space.
 */
class StubActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // This is where we would normally launch the virtual activity
        val intent = intent
        VirtualCore.getInstance().installPackage(intent.component?.packageName ?: "")
        
        finish()
    }
}
