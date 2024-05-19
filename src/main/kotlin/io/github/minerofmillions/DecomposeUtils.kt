package io.github.minerofmillions

import javax.swing.SwingUtilities

internal fun <T> runOnUiThread(block: () -> T): T {
    if (SwingUtilities.isEventDispatchThread()) {
        return block()
    }

    var error: Throwable? = null
    var result: T? = null

    SwingUtilities.invokeAndWait {
        try {
            result = block()
        } catch (t: Throwable) {
            error = t
        }
    }

    error?.also { throw it }

    return result!!
}