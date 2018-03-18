package resource

class ResourceList(private val resources: MutableMap<ResourceType, Int> = mutableMapOf()) {

    constructor(vararg pairs: Pair<ResourceType, Int>) : this(pairs.toMap().toMutableMap())

    fun enoughIn(other: ResourceList): Boolean {
        for ((k, v) in resources) {
            if (!other.resources.containsKey(k) || other.resources.get(k)!! < v)
                return false
        }
        return true
    }

    /**
     * @return a resource list that is equivalent to its quantities minus those in this list
     */
    fun consumeFrom(other: ResourceList, checkIfEnough: Boolean = true): ResourceList? {
        if(!enoughIn(other))
            return null
        val newMap = other.resources.toMutableMap()
        for((res, q) in resources) {
            newMap.replace(res, newMap.get(res)!! - q)
        }
        return ResourceList(newMap)
    }

    fun forEach(f: (ResourceType, Int) -> Unit) = resources.forEach(f)

    fun any(f: (Map.Entry<ResourceType, Int>) -> Boolean) = resources.any(f)

    fun addAll(other: ResourceList) {
        for((r, q) in other.resources) {
            if(r in resources) {
                resources.replace(r, resources.get(r)!! + q)
            } else {
                resources.put(r, q)
            }
        }
    }

    operator fun iterator() = resources.iterator()

    fun clear() = resources.clear()

    operator fun contains(resource: ResourceType) = resources.containsKey(resource)
}