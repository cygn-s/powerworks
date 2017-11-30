package inv

import level.node.StorageNode
import level.resource.ResourceType

class Inventory(val width: Int, val height: Int) : StorageNode<ItemType>(ResourceType.ITEM) {

    private val items = arrayOfNulls<Item>(width * height)

    // TODO maybe have a "slotsFull" variable that tells me whether to iterate from the beginning or end

    var itemCount = 0
        private set

    val full: Boolean
        get() {
            if (items[items.lastIndex] != null) {
                return items.any { it!!.quantity != it.type.maxStack }
            }
            return false
        }

    override fun add(resource: ItemType, quantity: Int, checkForSpace: Boolean): Boolean {
        if (checkForSpace)
            if (!spaceFor(resource, quantity))
                return false
        var amountLeftToAdd = quantity
        var indexOfLastStackOfType = -1
        itemCount += quantity
        // Find if there is a slot that has this type but is not full
        for (i in items.indices) {
            val item = items[i]
            if (item != null) {
                if (item.type == resource) {
                    if (item.quantity < resource.maxStack) {
                        // This slot is not full

                        // If we don't have enough left to fill it totally or we can make it exactly the max stack
                        if (item.quantity + amountLeftToAdd <= resource.maxStack) {
                            // Add and return successfully
                            item.quantity += amountLeftToAdd
                            return true

                            // We have more than enough to fill the item stack. We know there should only be one item stack
                            // of each type that isn't full, so we can break here and, if necessary, create space at the end
                            // of this "block" of items to put a new stack in
                        } else {
                            amountLeftToAdd -= (resource.maxStack - item.quantity)
                            item.quantity = resource.maxStack
                            indexOfLastStackOfType = i
                            break
                        }
                    }
                }
            }
        }
        val needsShift = items[indexOfLastStackOfType + 1] != null
        val stacksLeft = Math.ceil(amountLeftToAdd.toDouble() / resource.maxStack).toInt()
        if (needsShift) {
            // We can assume that there was space to perform this
            shiftRight(indexOfLastStackOfType + 1, stacksLeft)
        }
        // Use the fill the newly empty spaces
        for (i in (indexOfLastStackOfType + 1) until (indexOfLastStackOfType + 1 + stacksLeft)) {
            if (amountLeftToAdd > resource.maxStack) {
                items[i] = Item(resource, resource.maxStack)
                amountLeftToAdd -= resource.maxStack
            } else {
                items[i] = Item(resource, amountLeftToAdd)
            }
        }
        return true
    }

    fun add(i: Item): Boolean {
        return add(i.type, i.quantity)
    }

    override fun spaceFor(resource: ItemType, quantity: Int): Boolean {
        var capacity = 0
        for (i in items.indices) {
            val item = items[i]
            if (item != null) {
                if (item.type == resource) {
                    if (item.quantity < resource.maxStack) {
                        capacity += (resource.maxStack - item.quantity)
                    }
                }
            } else {
                capacity += resource.maxStack
            }
        }
        return capacity >= quantity
    }

    override fun remove(resource: ItemType, quantity: Int, checkIfContains: Boolean): Boolean {
        if (checkIfContains)
            if (!contains(resource, quantity))
                return false
        var amountLeftToRemove = quantity
        var indexOfLastStackRemoved = -1
        var stacksRemoved = 0
        itemCount -= quantity
        // Go down from the top, for each stack encountered, if its quantity is greater than amount left to remove
        for (i in items.lastIndex downTo 0) {
            val item = items[i]
            if (item != null) {
                if (item.type == resource) {
                    if (item.quantity > amountLeftToRemove) {
                        item.quantity -= amountLeftToRemove
                        return true
                    } else {
                        amountLeftToRemove -= item.quantity
                        items[i] = null
                        indexOfLastStackRemoved = i
                        stacksRemoved++
                    }
                }
            }
        }
        val needsShift = items[indexOfLastStackRemoved + stacksRemoved] != null
        if (needsShift) {
            // There must be space to perform this (guaranteed)
            shiftLeft(indexOfLastStackRemoved, stacksRemoved)
        }
        return true
    }

    fun remove(i: Item): Boolean {
        return remove(i.type, i.quantity)
    }

    override fun contains(resource: ItemType, quantity: Int): Boolean {
        var contains = 0
        for (i in items.indices) {
            if (items[i] != null) {
                val item = items[i]!!
                if (item.type == resource) {
                    contains += item.quantity
                }
            }
        }
        return contains >= quantity
    }

    /**
     * Inclusive, goes to the end of items from this index
     */
    fun shiftRight(index: Int, num: Int): Int {
        var count = 0
        // Don't worry about out of bounds, we assume that we've already checked for space
        while (count < num) {
            for (i in items.lastIndex downTo (index + 1)) {
                items[i] = items[i - 1]
            }
            count++
        }
        return count
    }

    /**
     * Inclusive, goes to the end of items from this index
     */
    fun shiftLeft(index: Int, num: Int): Int {
        var count = 0
        // Don't worry about out of bounds, we assume that we've already checked for space
        while (count < num) {
            for (i in index until (items.lastIndex - 1)) {
                items[i] = items[i + 1]
            }
            count++
        }
        return count
    }

    fun print() {
        for(y in 0 until height) {
            for(x in 0 until width)
                print("${items[x + y * width]?.type?.name}      ")
            println()
        }
    }

    operator fun iterator(): Iterator<Item?> {
        return items.iterator()
    }

    operator fun get(i: Int): Item? {
        return items[i]
    }

    operator fun set(i: Int, v: Item?) {
        items[i] = v
    }
}
