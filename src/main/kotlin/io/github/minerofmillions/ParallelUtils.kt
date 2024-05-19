package io.github.minerofmillions

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import java.util.*
import java.util.concurrent.ConcurrentHashMap

fun <T> synchronizedListOf(vararg elements: T): MutableList<T> =
    Collections.synchronizedList(mutableListOf(elements = elements))

fun <T> synchronizedSetOf(vararg elements: T): MutableSet<T> =
    ConcurrentHashMap.newKeySet<T>().apply { addAll(elements) }

fun <K, V> synchronizedMapOf(vararg pairs: Pair<K, V>): MutableMap<K, V> =
    ConcurrentHashMap<K, V>().apply { putAll(pairs) }

suspend fun <T> Sequence<T>.forEachParallel(action: suspend (T) -> Unit) = coroutineScope {
    forEach { launch { action(it) } }
}

suspend fun <T> Iterable<T>.forEachParallel(action: suspend (T) -> Unit) = asSequence().forEachParallel(action)

suspend fun <T> Array<T>.forEachParallel(action: suspend (T) -> Unit) = asSequence().forEachParallel(action)

suspend fun <K, V> Map<K, V>.forEachParallel(action: suspend (Map.Entry<K, V>) -> Unit) = asSequence().forEachParallel(action)

suspend fun <T, R, C : MutableCollection<R>> Sequence<T>.mapParallelTo(
    output: C,
    transform: suspend (T) -> R
): C {
    coroutineScope {
        val queue = Channel<Deferred<R>>(Runtime.getRuntime().availableProcessors())
        fun launch(it: T) = async { transform(it) }
        val launcher = launch(Dispatchers.IO) {
            forEach { queue.send(launch(it)) }
            queue.close()
        }
        val taker = launch(Dispatchers.IO) {
            queue.consumeEach {
                output.add(it.await())
            }
        }
        launcher.join()
        taker.join()
    }
    return output
}

suspend fun <T, R, C : MutableCollection<R>> Iterable<T>.mapParallelTo(
    output: C,
    transform: suspend (T) -> R
): C = asSequence().mapParallelTo(output, transform)

suspend fun <T, R, C : MutableCollection<R>> Array<T>.mapParallelTo(
    output: C,
    transform: suspend (T) -> R
): C = asSequence().mapParallelTo(output, transform)

suspend fun <T, R> Iterable<T>.mapParallel(transform: suspend (T) -> R): List<R> =
    mapParallelTo(synchronizedListOf(), transform)

suspend fun <T, R> Array<T>.mapParallel(transform: suspend (T) -> R): List<R> =
    mapParallelTo(synchronizedListOf(), transform)

fun <T, R> Sequence<T>.mapParallel(transform: suspend (T) -> R): Sequence<R> = ParallelTransformingSequence(this, transform)

internal class ParallelTransformingSequence<T, R>(private val base: Sequence<T>, private val transform: suspend (T) -> R) : Sequence<R> {
    private val queue = Channel<Deferred<R>>(Runtime.getRuntime().availableProcessors())
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        scope.launch {
            base.forEach { queue.send(async { transform(it) }) }
            queue.close()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun iterator(): Iterator<R> = object : Iterator<R> {
        override fun hasNext(): Boolean = !queue.isClosedForReceive

        override fun next(): R = runBlocking {
            queue.receive().await()
        }
    }
}