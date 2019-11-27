package data

class ConcurrentlyModifiableWeakMutableList<T> {

    val size
        get() = elements.size + toAdd.size - toRemove.size

    var beingTraversed = false

    val elements = WeakMutableList<T>()

    val toAdd = mutableListOf<T>()

    val toRemove = mutableListOf<T>()

    fun add(l: T) {
        if (beingTraversed)
            toAdd.add(l)
        else
            elements.add(l)
    }

    fun remove(l: T) {
        if (beingTraversed) {
            toRemove.add(l)
        } else
            elements.remove(l)
    }

    fun contains(l: T) = !toRemove.contains(l) && (elements.contains(l) || toAdd.contains(l))

    fun <R : Comparable<R>>sortedBy(f: (T) -> R?) = elements.sortedBy(f)

    fun forEach(f: (T) -> Unit) {
        beingTraversed = true
        elements.forEach(f)
        beingTraversed = false
        elements.addAll(toAdd)
        toAdd.clear()
        elements.removeAll(toRemove)
        toRemove.clear()
    }
}