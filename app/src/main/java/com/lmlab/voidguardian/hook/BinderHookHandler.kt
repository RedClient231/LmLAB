package com.lmlab.voidguardian.hook

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

/**
 * Base class for all Binder hook handlers.
 * Provides common functionality for intercepting and routing Binder transactions.
 */
abstract class BinderHookHandler : InvocationHandler {

    override fun invoke(proxy: Any?, method: Method?, args: Array<Any?>?): Any? {
        return method?.invoke(proxy, *args.orEmpty())
    }
}
