package com.angcyo.rtablayout

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import com.angcyo.uiview.RTabLayout

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_tab_layout_demo)


        val tabLayout: RTabLayout = findViewById(R.id.base_tab_layout)
        val tabLayout2: RTabLayout = findViewById(R.id.base_tab_layout2)
        val tabLayout2_1: RTabLayout = findViewById(R.id.base_tab_layout2_1)
        val tabLayout2_2: RTabLayout = findViewById(R.id.base_tab_layout2_2)
        val tabLayout3: RTabLayout = findViewById(R.id.base_tab_layout3)

        val viewPager: RViewPager = findViewById(R.id.view_pager)

        tabLayout.onTabLayoutListener = object : RTabLayout.OnTabLayoutListener() {
            override fun onPageScrolled(tabLayout: RTabLayout, currentView: View?, nextView: View?, positionOffset: Float) {
                super.onPageScrolled(tabLayout, currentView, nextView, positionOffset)
            }

            override fun onSelectorItemView(tabLayout: RTabLayout, itemView: View, index: Int) {
                super.onSelectorItemView(tabLayout, itemView, index)
                if (itemView is TextView) {
                    //L.e("call: onSelectorItemView -> $index")
                    itemView.setTextColor(Color.YELLOW)
                }
            }

            override fun onUnSelectorItemView(tabLayout: RTabLayout, itemView: View, index: Int) {
                super.onUnSelectorItemView(tabLayout, itemView, index)
                if (itemView is TextView) {
                    //L.e("call: onUnSelectorItemView -> $index")
                    itemView.setTextColor(getColor(R.color.base_link_color_dark))
                }
            }

            override fun onTabSelector(tabLayout: RTabLayout, fromIndex: Int, toIndex: Int) {
                super.onTabSelector(tabLayout, fromIndex, toIndex)
                //L.e("RTabLayout: onTabSelector -> fo:$fromIndex to:$toIndex")
                viewPager.currentItem = toIndex
            }

            override fun onTabReSelector(tabLayout: RTabLayout, itemView: View, index: Int) {
                super.onTabReSelector(tabLayout, itemView, index)
                //L.e("RTabLayout: onTabReSelector -> $index")
            }
        }
        tabLayout2_1.onTabLayoutListener = tabLayout.onTabLayoutListener
        tabLayout2_2.onTabLayoutListener = tabLayout.onTabLayoutListener
        tabLayout2.onTabLayoutListener = tabLayout.onTabLayoutListener
        tabLayout3.onTabLayoutListener = tabLayout.onTabLayoutListener

        viewPager.apply {
            adapter = object : RPagerAdapter() {
                override fun getCount(): Int {
                    return 40
                }

                override fun getItemType(position: Int): Int {
                    return position % 4
                }

                override fun getLayoutId(position: Int, itemType: Int): Int {
                    return when (itemType) {
                        0 -> R.layout.item_text_view
                        1 -> R.layout.item_text_view
                        2 -> R.layout.item_text_view
                        else -> R.layout.item_text_view
                    }
                }

                override fun initItemView(rootView: View, position: Int, itemType: Int) {
                    super.initItemView(rootView, position, itemType)
                    rootView.setBackgroundColor(Color.DKGRAY)
                    rootView.findViewById<TextView>(R.id.text_view).text = "测试数据 第$position 页"
                }
            }

            tabLayout2.setupViewPager(this)
            tabLayout3.setupViewPager(this)
            tabLayout2_1.setupViewPager(this)

            tabLayout.setupViewPager(this)
        }

//                tabLayout.setCurrentItem(1)
//                tabLayout3.setCurrentItem(2)
    }
}
