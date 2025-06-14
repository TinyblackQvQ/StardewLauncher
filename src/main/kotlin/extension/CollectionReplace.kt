package extension

fun <T> MutableList<T>.replace(item: T, newItem: T): MutableList<T> {
    val index = this.indexOf(item)
    if (index != -1) {
        this[index] = newItem
    }
    return this
}

fun <T> MutableSet<T>.replace(item: T, newItem: T): MutableSet<T> {
    remove(item)
    add(newItem)
    return this
}