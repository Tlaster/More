package moe.tlaster.more.widget

import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.tlaster.more.common.CollectionChangedEventArg
import moe.tlaster.more.common.ObservableCollection


class DelayAutoCompleteTextView: AppCompatAutoCompleteTextView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    var autoCompleteDelay = DEFAULT_AUTOCOMPLETE_DELAY

    init {
        AutoCompleteTextView::class.java.getDeclaredField("mPopup").run {
            isAccessible = true
            get(this@DelayAutoCompleteTextView) as ListPopupWindow
        }.promptPosition = ListPopupWindow.POSITION_PROMPT_ABOVE
    }

    private val handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super@DelayAutoCompleteTextView.performFiltering(msg.obj as CharSequence, msg.arg1)
        }
    }
    override fun performFiltering(text: CharSequence, keyCode: Int) {
        handler.removeMessages(MESSAGE_TEXT_CHANGED)
        handler.sendMessageDelayed(handler.obtainMessage(MESSAGE_TEXT_CHANGED, text), autoCompleteDelay)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeMessages(MESSAGE_TEXT_CHANGED)
    }

    companion object {

        private val MESSAGE_TEXT_CHANGED = 100
        private val DEFAULT_AUTOCOMPLETE_DELAY = 500L
    }
}

class AutoCompleteAdapter<T>(private val context: Context, @LayoutRes val layout: Int = android.R.layout.simple_list_item_1): BaseAdapter(), Filterable {
    data class ActionData<T>(
        @IdRes val id: Int,
        val action: (View, T, position: Int, AutoCompleteAdapter<T>) -> Unit
    )

    var filterChanged: ((CharSequence?) -> Unit)? = null
    private val actions: ArrayList<ActionData<T>> = ArrayList()

    private val onItemsChanged: (Any, CollectionChangedEventArg) -> Unit = { _, _ ->
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                notifyDataSetChanged()
            }
        }
    }
    val items = ObservableCollection<T>().apply {
        collectionChanged += onItemsChanged
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val view = p1 ?: LayoutInflater.from(context).inflate(layout, p2, false)
        val item = getItem(p0)
        if (item != null) {
            actions.forEach {
                it.action.invoke(view.findViewById(it.id), item, p0, this)
            }
        }
        return view
    }
    fun <K : View> bindCustom(@IdRes id: Int, action: (view: K, data :T, position: Int, AutoCompleteAdapter<T>) -> Unit) {
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

    override fun getItem(p0: Int): T {
        return items[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun getFilter(): Filter {
        return object: Filter() {
            override fun performFiltering(p0: CharSequence?): FilterResults {
                filterChanged?.invoke(p0)
                return FilterResults().apply {
                    values = items
                    count = items.size
                }
            }

            override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
//                if (p1 != null && p1.count > 0) {
//                    notifyDataSetChanged()
//                } else {
//                    notifyDataSetInvalidated()
//                }
            }

        }
    }

}