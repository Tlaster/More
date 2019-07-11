package moe.tlaster.more.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.android.synthetic.main.widget_navigation.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import moe.tlaster.more.R
import moe.tlaster.more.common.AutoAdapter
import moe.tlaster.more.common.SpaceItemDecoration
import moe.tlaster.more.common.dp

data class TabData(
    var title: String
)

class NavigationBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val onFilterChanged: ((CharSequence?) -> Unit) = {
        performFilter(it)
    }

    private fun performFilter(value: CharSequence?) {
        GlobalScope.launch {
            val result = arrayListOf<String>()
            if (value != null) {
                // TODO: search
                kotlinx.coroutines.delay(200)
                result.addAll((0 until 8).map { value.toString() + it.toString() })
            }
            adapter.items.clear()
            if (result.any()) {
                adapter.items.addAll(result)
            }
        }
    }

    private val adapter by lazy {
        AutoCompleteAdapter<String>(context, android.R.layout.simple_list_item_1).apply {
            bindText(android.R.id.text1) {
                it
            }
            filterChanged = onFilterChanged
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.widget_navigation, this)
        refresh_button?.setOnClickListener {

        }
        menu_button?.setOnClickListener {

        }
        back_button?.setOnClickListener {

        }
        forward_button?.setOnClickListener {

        }
        tabs_button?.setOnClickListener {

        }
        url_text.setAdapter(adapter)
        tabs_button?.setOnLongClickListener {
            true
        }
        tab_list?.let {
            it.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            it.adapter = AutoAdapter<TabData>(R.layout.widget_tab).apply {
                withFooter(R.layout.tab_footer)
                bindText(android.R.id.text1) { data ->
                    data.title
                }
                bindCustom<ImageButton>(R.id.close_button) { view, data, position, _ ->
                    view.setOnClickListener {
                        items.remove(data)
                    }
                }
                bindFooter {
                    it.setOnClickListener {
                        items.add(TabData("about:blank" + items.size.toString()))
                    }
                }
                emptyWithFooter = true
            }
            it.addItemDecoration(SpaceItemDecoration(context, LinearLayoutManager.HORIZONTAL).apply {
                spacing = 8.dp
            })
            it.itemAnimator = SlideInUpAnimator()
        }
    }
}
