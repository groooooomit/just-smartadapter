package com.bfu.just.smartadapter

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import just.smartadapter.SmartAdapter
import just.smartadapter.core.ListDataSource
import just.smartadapter.core.AutoNotifyDataSource
import just.smartadapter.wrapper.HeaderFooterAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private companion object {
        private const val SPAN_COUNT = 4
    }

    private val dataSource = AutoNotifyDataSource(ListDataSource(ArrayList<Item>()))
    private val dataAdapter = SmartAdapter.newBuilder(dataSource)
        /* 标题类型. */
        .type()
        .layout(R.layout.item_person_grid)
        .gridSpanSize(SPAN_COUNT)
        .overrideHeight { _, _, parentWidth, _ -> parentWidth / SPAN_COUNT / 2 }
        .filter { data, _ -> data is Title }
        .onBind { viewHolder, data, _, _, _ ->
            val title = data as Title
            viewHolder.setText(R.id.txt_name, title.title)
        }
        .onItemClick { data, position, _, _, _, _ ->
            val title = data as Title
            title.title = "被点击了"
            dataSource.refresh(position)
        }

        /* 人员类型. */
        .type()
        .layout(R.layout.item_person_grid)
        .gridSpanSize(1)
        .overrideHeight { _, _, parentWidth, _ -> parentWidth / SPAN_COUNT }
        .filter { data, _ -> data is Person }
        .onBind { viewHolder, data, _, _, _ ->
            val person = data as Person
            viewHolder.setText(R.id.txt_name, person.name)
        }
        .onItemClick { data, _, _, _, _, _ ->
            val person = data as Person
            Toast.makeText(this@MainActivity, person.name, Toast.LENGTH_SHORT).show()
        }
        .build()
        .let { HeaderFooterAdapter(it) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /* 初始化 recyclerView. */
        recyclerView.setOnLoadMoreListener { Toast.makeText(this@MainActivity,"load more",Toast.LENGTH_SHORT).show() }

        /* 如果 ViewGroup 是 RecyclerView，需要在 RecyclerView 设置完 LayoutManager 后再调用 inflate */
        recyclerView.layoutManager = GridLayoutManager(this, SPAN_COUNT)
        val header1 = LayoutInflater.from(this).inflate(R.layout.layout_header, recyclerView, false)
        val header2 = LayoutInflater.from(this).inflate(R.layout.layout_header, recyclerView, false)
        val header3 = LayoutInflater.from(this).inflate(R.layout.layout_header, recyclerView, false)
        val footer1 = LayoutInflater.from(this).inflate(R.layout.layout_footer, recyclerView, false)
        val footer2 = LayoutInflater.from(this).inflate(R.layout.layout_footer, recyclerView, false)
        dataAdapter.addHeader(header1)
        dataAdapter.addHeader(header2)
        dataAdapter.addHeader(header3)
        dataAdapter.addFooter(footer1)
        dataAdapter.addFooter(footer2)
        recyclerView.adapter = dataAdapter

        /* 模拟加载数据. */
        recyclerView.postDelayed({
            val list = ArrayList<Item>()
            var titleCount = 0
            var personCount = 0
            for (i in 0..40) {
                val index = i % (SPAN_COUNT + 1)
                if (index == 0) {
                    list.add(Title("小组 $titleCount"))
                    titleCount++
                } else {
                    list.add(Person("组员 $personCount", 20))
                    personCount++
                }
            }
            dataSource.replace(list)
        }, 1000)
    }
}
