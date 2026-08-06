package com.germandebustamante.fuelio.core.interop

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Cancellable handle for a Kotlin [Flow] collection started from Swift.
 *
 * The Objective-C exporter turns `Flow<T>` into an unparameterized `id<Kotlinx_coroutines_coreFlow>`
 * with no `AsyncSequence` conformance, and a coroutine started from Swift outlives the Swift object
 * that started it. Every subscription therefore hands back one of these, and the Swift owner must
 * call [cancel] — from `deinit` or when the observing task is cancelled.
 *
 * Named `FlowSubscription` rather than `Cancellable` on purpose: Swift would otherwise see
 * `Cancellable` and collide with Combine's protocol of the same name.
 */
class FlowSubscription internal constructor(private val job: Job) {

    /** `true` once [cancel] has run. Collection of a `StateFlow` never completes on its own. */
    val isCancelled: Boolean
        get() = job.isCancelled

    fun cancel() {
        job.cancel()
    }
}

/**
 * Collects this flow on [context] and forwards every emission to [onEach].
 *
 * [context] defaults to `Dispatchers.Main.immediate` so values land on the main thread before Swift
 * sees them — SwiftUI state must only ever be mutated there, and Kotlin `StateFlow`s in this project
 * are updated from `viewModelScope` and from `Dispatchers.Default` alike.
 *
 * `onEach` is a Swift closure held by Kotlin for as long as the collection runs. Kotlin's GC and
 * Swift's ARC do not see each other, so a closure that strongly captures the Swift owner forms a
 * cycle neither collector can break: capture `[weak self]` on the Swift side.
 */
internal fun <T : Any> Flow<T>.subscribe(
    context: CoroutineContext = Dispatchers.Main.immediate,
    onEach: (T) -> Unit,
): FlowSubscription {
    val scope = CoroutineScope(context + SupervisorJob())
    val job = scope.launch { collect { value -> onEach(value) } }
    return FlowSubscription(job)
}
