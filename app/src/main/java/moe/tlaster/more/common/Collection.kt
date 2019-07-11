package moe.tlaster.more.common

import android.os.Build
import androidx.annotation.RequiresApi
import java.util.function.Predicate

enum class CollectionChangedType {
    Add,
    Remove,
    Update,
}
class CollectionChangedEventArg(
    val type: CollectionChangedType,
    val startIndex: Int,
    val count: Int
)
class Event<T> {
    private val listeners = ArrayList<(sender: Any, arg: T) -> Unit>()
    fun invoke(sender: Any, arg: T) = listeners.forEach { it.invoke(sender, arg) }

    operator fun plusAssign(propertyChanged: (Any, T) -> Unit) {
        listeners.add(propertyChanged)
    }

    operator fun minusAssign(propertyChanged: (Any, T) -> Unit) {
        listeners.remove(propertyChanged)
    }

    fun clear() = listeners.clear()
    fun any() = listeners.any()
}

interface INotifyCollectionChanged {
    val collectionChanged: Event<CollectionChangedEventArg>
}

class ObservableCollection<T>: ArrayList<T>(), INotifyCollectionChanged {

    override val collectionChanged: Event<CollectionChangedEventArg> = Event()

    override fun add(element: T): Boolean {
        val result = super.add(element)
        if (result) {
            collectionChanged.invoke(this, CollectionChangedEventArg(CollectionChangedType.Add, size - 1, 1))
        }
        return result
    }

    override fun add(index: Int, element: T) {
        super.add(index, element)
        collectionChanged.invoke(this, CollectionChangedEventArg(CollectionChangedType.Add, index, 1))
    }

    override fun addAll(elements: Collection<T>): Boolean {
        val result = super.addAll(elements)
        if (result) {
            collectionChanged.invoke(this, CollectionChangedEventArg(CollectionChangedType.Add, size - elements.size - 1, elements.size))
        }
        return result
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        val result = super.addAll(index, elements)
        if (result) {
            collectionChanged.invoke(this, CollectionChangedEventArg(CollectionChangedType.Add, index, elements.size))
        }
        return result
    }

    override fun clear() {
        val count = size
        super.clear()
        collectionChanged.invoke(this, CollectionChangedEventArg(CollectionChangedType.Remove, 0, count))
    }

    override fun remove(element: T): Boolean {
        val index = indexOf(element)
        val result = super.remove(element)
        if (result) {
            collectionChanged.invoke(this, CollectionChangedEventArg(CollectionChangedType.Remove, index, 1))
        }
        return result
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        val index = indexOf(elements.first())
        val result = super.removeAll(elements)
        if (result) {
            collectionChanged.invoke(this, CollectionChangedEventArg(CollectionChangedType.Remove, index, elements.size))
        }
        return result
    }

    override fun removeAt(index: Int): T {
        val result = super.removeAt(index)
        if (result != null) {
            collectionChanged.invoke(this, CollectionChangedEventArg(CollectionChangedType.Remove, index, 1))
        }
        return result
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun removeIf(filter: Predicate<in T>): Boolean {
        val index = indexOfFirst {
            filter.test(it)
        }
        val result = super.removeIf(filter)
        if (result) {
            collectionChanged.invoke(this, CollectionChangedEventArg(CollectionChangedType.Remove, index, 1))
        }
        return result
    }

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        super.removeRange(fromIndex, toIndex)
        collectionChanged.invoke(this, CollectionChangedEventArg(CollectionChangedType.Remove, fromIndex, toIndex - fromIndex))
    }
}