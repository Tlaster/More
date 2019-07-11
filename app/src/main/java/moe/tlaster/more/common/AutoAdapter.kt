package moe.tlaster.more.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes


class AutoAdapter<T>(@LayoutRes val layout: Int = android.R.layout.simple_list_item_1)
    : androidx.recyclerview.widget.RecyclerView.Adapter<AutoAdapter.AutoViewHolder>() {
    class AutoViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)
    private enum class ViewType {
        Item,
        EmptyView,
        Header,
        Footer;

        companion object {
            private val values = values()
            fun getByValue(value: Int) = values.firstOrNull { it.ordinal == value }
        }
    }

    data class ItemClickEventArg<T>(
        val item: T
    )

    data class ActionData<T>(
        @IdRes val id: Int,
        val action: (View, T, position: Int, AutoAdapter<T>) -> Unit
    )

    private val onItemsChanged: (Any, CollectionChangedEventArg) -> Unit = { _, arg ->
        if (arg.count == 1) {
            when (arg.type) {
                CollectionChangedType.Add -> notifyItemInserted(arg.startIndex)
                CollectionChangedType.Remove -> notifyItemRemoved(arg.startIndex)
                CollectionChangedType.Update -> notifyItemChanged(arg.startIndex)
            }
        } else {
            when (arg.type) {
                CollectionChangedType.Add -> notifyItemRangeInserted(arg.startIndex, arg.count)
                CollectionChangedType.Remove -> notifyItemRangeRemoved(arg.startIndex, arg.count)
                CollectionChangedType.Update -> notifyItemRangeChanged(arg.startIndex, arg.count)
            }
        }
    }

    val items = ObservableCollection<T>().apply {
        collectionChanged += onItemsChanged
    }

    override fun getItemViewType(position: Int): Int {
        if (items.count() == 0 && emptyView != 0) {
            return ViewType.EmptyView.ordinal
        }

        if (hasHeader && position == 0) {
            return ViewType.Header.ordinal
        }

        if (hasFooter) {
            var requirePosition = items.count()
            if (hasHeader) {
                requirePosition += 1
            }
            if (position == requirePosition) {
                return ViewType.Footer.ordinal
            }
        }

        return ViewType.Item.ordinal
    }

    private val actions: ArrayList<ActionData<T>> = ArrayList()

    var itemClicked: Event<ItemClickEventArg<T>> = Event()
    var itemLongPressed: Event<ItemClickEventArg<T>> = Event()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AutoViewHolder {
        when (ViewType.getByValue(viewType)) {
            ViewType.Item -> return AutoViewHolder(LayoutInflater.from(parent.context).inflate(layout, parent, false))
            ViewType.EmptyView -> {
                if (emptyView != 0) {
                    return AutoViewHolder(LayoutInflater.from(parent.context).inflate(emptyView, parent, false))
                }
            }
            ViewType.Header -> {
                if (headerViewRes != 0) {
                    return AutoViewHolder(LayoutInflater.from(parent.context).inflate(headerViewRes, parent, false))
                }
                val view = headerView
                if (view != null) {
                    return AutoViewHolder(view)
                }
            }
            ViewType.Footer -> {
                if (footerViewRes != 0) {
                    return AutoViewHolder(LayoutInflater.from(parent.context).inflate(footerViewRes, parent, false))
                }
                val view = footerView
                if (view != null) {
                    return AutoViewHolder(view)
                }
            }
        }
        return AutoViewHolder(View(parent.context))
    }

    override fun getItemCount(): Int {
        var count = items.count()
        if (count == 0) {
            count += if (emptyView == 0) {
                0
            } else {
                1
            }
            if (emptyWithFooter) {
                count++
            }
            if (emptyWithHeader) {
                count++
            }
        } else {
            count += if (hasHeader) {
                1
            } else {
                0
            }
            count += if (hasFooter) {
                1
            } else {
                0
            }
        }
        return count
    }

    override fun onBindViewHolder(viewHolder: AutoViewHolder, position: Int) {
        var actualPosition = position
        if (hasHeader && emptyWithHeader) {
            actualPosition -= 1
        }
        if (hasHeader && actualPosition == -1 && emptyWithHeader) {
            onBindHeader?.invoke(viewHolder.itemView)
        }

        if (hasFooter && actualPosition == items.count() && emptyWithFooter) {
            onBindFooter?.invoke(viewHolder.itemView)
        }

        val item = items.getOrNull(actualPosition)
        if (item != null) {
            viewHolder.itemView.setOnClickListener {
                itemClicked.invoke(viewHolder.itemView, ItemClickEventArg(item))
            }
            viewHolder.itemView.setOnLongClickListener {
                itemLongPressed.invoke(viewHolder.itemView, ItemClickEventArg(item))
                itemLongPressed.any()
            }
            actions.forEach {
                it.action.invoke(viewHolder.itemView.findViewById(it.id), item, actualPosition, this)
            }
        }
    }

    var footerEnabled = true
    var headerEnabled = true
    var emptyWithFooter = false
    var emptyWithHeader = false

    private val hasHeader
        get() = (headerView != null || headerViewRes != 0) && headerEnabled
    private val hasFooter
        get() = (footerView != null || footerViewRes != 0) && footerEnabled

    private var emptyView: Int = 0
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun whenEmpty(@LayoutRes id: Int) {
        emptyView = id
    }

    private var headerViewRes: Int = 0

    fun withHeader(@LayoutRes id: Int) {
        headerViewRes = id
    }

    private var headerView: View? = null

    fun withHeader(view: View) {
        headerView = view
    }

    private var footerViewRes: Int = 0

    fun withFooter(@LayoutRes id: Int) {
        footerViewRes = id
    }

    private var footerView: View? = null

    fun withFooter(view: View) {
        footerView = view
    }

    private var onBindHeader: ((View) -> Unit)? = null

    fun bindHeader(block: (view: View) -> Unit) {
        onBindHeader = block
    }

    private var onBindFooter: ((View) -> Unit)? = null

    fun bindFooter(block: (view: View) -> Unit) {
        onBindFooter = block
    }

    fun <K : View> bindCustom(@IdRes id: Int, action: (view: K, data :T, position: Int, AutoAdapter<T>) -> Unit) {
        actions.add(ActionData(id) { view, t, position, autoAdapter ->
            if (view as? K != null) {
                action.invoke(view, t, position, autoAdapter)
            }
        })
    }

    fun bindText(@IdRes id: Int, value: (T) -> String) {
        actions.add(ActionData(id) { view, item, _, _ ->
            if (view is TextView) {
                view.text = value.invoke(item)
            }
        })
    }

}
