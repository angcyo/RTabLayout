此项目已废弃, 请使用更强大的[DslTabLayout](https://github.com/angcyo/DslTabLayout)


# RTabLayout
超级高效,轻量,极强自定义指示器,任意自定义Tab类型的TabLayout, 直接继承自ViewGroup实现.

![](https://img-blog.csdnimg.cn/20190217162015592.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2FuZ2N5bw==,size_16,color_FFFFFF,t_70)


#### 前言
你能学到啥?

- 自定义View的基础知识
- ViewGroup中Child View的测量布局控制
- Touch事件的传递,拦截和处理
- draw和OnDraw方法的区别
- OverScroller的使用
- GestureDetector的使用
- ViewGroup中setWillNotDraw方法的作用
- Canvas的使用方法`(自绘的核心类)`

---

#### 需求分析

- TabLayout的宽高不限制, 可随意设置
- Tab可以支持文本,图片和ViewGroup等任意控件
- Tab的宽高可以不要求一致,每个Tab可以是任意宽高, (为了体验, 高度保持一致好一些)
- 指示器支持横线,圆角矩形,图片等任意Drawable
- 当Tab宽度总和大于TabLayout时, 需要支持滚动 `(难点哦)`

---

**再次介绍一下自定义View xml属性的定义和读取**

1. 先在values文件夹下, 创建任意文件名的属性xml文件, 比如`attr_r_tab_layout.xml`
2. 在文件中声明属性
```
//declare-styleable 是固定写法, name是自定义View的类名, 固定写法
<declare-styleable name="RTabLayout">
     <!--首次设置tabLayoutListener时, 是否通知回调-->
     <attr name="r_first_notify_listener" format="boolean"/>
     <attr name="r_item_equ_width"/>
     <attr name="r_current_item" format="integer"/>
 </declare-styleable>
 //attr 就是对应每个属性的名字(name), 和属性的类型格式(format), 不同的格式读取时调用的api不一样.其他都是一样的
```
*为什么有些attr 有format, 有些没有呢?*
>没有声明format的attr, 说明这个attr, 在其他地方已经声明了, 所以在这里直接用就行. 否则就会报多个attr重复的错误
比如:
```
//属性可以提前声明, 并且多个自定义View可以共用相同属性
<attr name="r_border_color" format="color"/>
<declare-styleable name="RTabLayout">
       <attr name="r_border_color"/>   //已经声明过的属性, 可以直接使用, 而不需要format
</declare-styleable>

```

3.属性的读取
```kotlin
init{
	val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.RTabLayout) //固定写法
	
	//不同的Format, 对应的get方法不一样, 其他都是一样的.
	val itemEquWidth = typedArray.getBoolean(R.styleable.RTabLayout_r_item_equ_width, itemEquWidth)
	val firstNotifyListener = typedArray.getBoolean(R.styleable.RTabLayout_r_first_notify_listener, firstNotifyListener)
	val currentItem = typedArray.getInt(R.styleable.RTabLayout_r_current_item, currentItem)
	
	typedArray.recycle() //固定写法
}
```

---

**任何自定义View, 都是从onMeasure, onLayout, onDraw, 开始的.**

### 1.onMeasure测量child view和设置自身的大小
在这个方法中, 你可以决定child view 的任意宽高. 甚至超过自身的大小都是允许的.

并且此方法有一个关键方法需要调用`setMeasuredDimension`, 这个方法的作用就是告诉系统自身测量后的宽高.

如果没有调用, 会崩溃.

请注意: 每个`view`都有`margin`和`padding`属性.

但是:`margin`属性是否有效或者生效, 取决于`ViewGroup`
而    `padding`属性是否有效或者生效, 取决于`View`自己本身

```
override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
     //super.onMeasure(widthMeasureSpec, heightMeasureSpec) //不需要系统的测量方法
     var widthSize = MeasureSpec.getSize(widthMeasureSpec) //获取参考的测量宽度
     val widthMode = MeasureSpec.getMode(widthMeasureSpec) //获取参考的测量模式
     var heightSize = MeasureSpec.getSize(heightMeasureSpec) //获取参考的测量高度
     val heightMode = MeasureSpec.getMode(heightMeasureSpec) //获取参考的测量模式

	//1.为什么要说 参考 呢? 
	//因为 这个值有没有卵用, 取决于你 用不用它, 如果你不用它, 那么它就没卵用.

	//2.测量模式是啥?
	//测量模式就是xml布局中的  warp_content 和 match_parent
	//测量模式有3种: 
	// MeasureSpec.EXACTLY -> 准确测量.对应 match_parent 或者 具体的30dp. 意思就是很明确的指定了自身大小
	// MeasureSpec.AT_MOST -> 参考测量.对应 warp_content. 意思就是根据自己的需求决定自己的大小.比如根据文本的宽度决定自己的宽度, 根据child view的宽度总和 决定自身的宽度等. 但是大小的约束就是不能超过参考的测量宽度和高度
	// MeasureSpec.UNSPECIFIED -> 模糊测量. 这个测量模式用的比较少, 在ListView, RecycleView, ScrollView等具有滚动属性或者允许无限宽高的布局中, 就会用到. 意思就是自身的大小不受限制, 你想要多大就多大, 没有约束.

     var heightSpec: Int
     if (heightMode != MeasureSpec.EXACTLY) {
         //没有明确指定高度的情况下, 默认的高度
         heightSize = (40 * density).toInt() + paddingTop + paddingBottom
         heightSpec = exactlyMeasure(heightSize)
     } else {
         heightSpec = exactlyMeasure(heightSize - paddingTop - paddingBottom)
     }

     //child总共的宽度
     childMaxWidth = 0  //这个值用来决定是否要开始滚动的唯一条件
     for (i in 0 until childCount) {
         val childView = getChildAt(i)
         val lp = childView.layoutParams as LayoutParams
         //不支持竖向margin支持
         lp.topMargin = 0
         lp.bottomMargin = 0

         val widthHeight = calcLayoutWidthHeight(lp.layoutWidth, lp.layoutHeight,
                 widthSize, heightSize, 0, 0)
         val childHeightSpec = if (widthHeight[1] > 0) {
             exactlyMeasure(widthHeight[1])
         } else {
             heightSpec
         }

		//调用childView.measure方法, 去测量child view, 最终的目的是决定Child View的宽高
         if (itemEquWidth) {
             childView.measure(exactlyMeasure((widthSize - paddingLeft - paddingRight) / childCount), childHeightSpec)
         } else {
             if (widthHeight[0] > 0) {
                 childView.measure(exactlyMeasure(widthHeight[0]), childHeightSpec)
             } else {
                 childView.measure(atmostMeasure(widthSize - paddingLeft - paddingRight), childHeightSpec)
             }
         }

		//margin属性的支持.
         childMaxWidth += childView.measuredWidth + lp.leftMargin + lp.rightMargin
     }

     if (widthMode != MeasureSpec.EXACTLY) {
         widthSize = (childMaxWidth + paddingLeft + paddingRight).maxValue(widthSize)
     }

	 //注意 注意 注意...此方法必须调用.
     setMeasuredDimension(widthSize, heightSize)
 }
```

经过以上方法后,必须明确的几点:
- 每个child的宽度和高度, 确定
- 自身的宽度和高度, 确定
- child宽度总和, 确定
- 是否需要滚动, 确定 `(child宽度总和 > 自身宽度)`

如果疑问, 请从头开始阅读.

### 2.onLayout放置child view在自身的坐标系中
经过之前的`onMeasure`方法, 只是决定了宽高大小.
`onLayout`方法, 决定将`child`显示在什么位置上.

*再次提醒:*
请注意: 每个`view`都有`margin`和`padding`属性.

但是:`margin`属性是否有效或者生效, 取决于`ViewGroup`
而    `padding`属性是否有效或者生效, 取决于`View`自己本身

```
override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
     var left = paddingLeft
     for (i in 0 until childCount) {
         val childView = getChildAt(i)
         val lp = childView.layoutParams as LayoutParams

		//left margin属性的支持
         left += lp.leftMargin

         val top = if (lp.gravity.have(Gravity.CENTER_VERTICAL)) {
             measuredHeight / 2 - childView.measuredHeight / 2
         } else {
             paddingTop + (measuredHeight - paddingTop - paddingBottom) / 2 - childView.measuredHeight / 2
         }

         /*默认垂直居中显示*/
         //核心方法: 通过 左 上 右 下 4个点的坐标, 布局childView
         childView.layout(left, top,
                 left + childView.measuredWidth,
                 top + childView.measuredHeight)

		//right margin属性的支持
         left += childView.measuredWidth + lp.rightMargin
     }
 }
```

### 3:Touch事件, GestureDetector的使用
`ViewGroup`中处理Touch事件的方法有:
1. `dispatchTouchEvent`                  
3. `onInterceptTouchEvent`
5. `onTouchEvent`

`View`中处理Touch事件的方法有:
2. `dispatchTouchEvent`              
4. `onTouchEvent`

正常情况下: Touch事件的传递顺序: `1.2.3.4.5`
如果`ViewGroup`需要拦截`View`的事件,只需要`3`返回`true`: 执行顺序`1.2.3.5`
如果`View`需要阻止`ViewGroup`拦截Touch事件,只需要在`4`中调用`parent.requestDisallowInterceptTouchEvent(true)`,记得调用`parent.requestDisallowInterceptTouchEvent(false)`释放.执行顺序`1.2.3.4` 之后 `1.2.4`//

任何拦截不拦截的情况下`1.2`都一定会执行.

**Touch事件, 我们使用 GestureDetector来接收**

```
private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
        val absX = Math.abs(velocityX)
        val absY = Math.abs(velocityY)

        if (absX > TouchLayout.flingVelocitySlop || absY > TouchLayout.flingVelocitySlop) {
            if (absY > absX) {
                //竖直方向的Fling操作
                onFlingChange(if (velocityY > 0) TouchLayout.ORIENTATION.BOTTOM else TouchLayout.ORIENTATION.TOP, velocityY)
            } else if (absX > absY) {
                //水平方向的Fling操作
                onFlingChange(if (velocityX > 0) TouchLayout.ORIENTATION.RIGHT else TouchLayout.ORIENTATION.LEFT, velocityX)
            }
        }

        return true
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        //L.e("call: onScroll -> \n$e1 \n$e2 \n$distanceX $distanceY")

        val absX = Math.abs(distanceX)
        val absY = Math.abs(distanceY)

        if (absX > TouchLayout.scrollDistanceSlop || absY > TouchLayout.scrollDistanceSlop) {
            if (absY > absX) {
                //竖直方向的Scroll操作
                onScrollChange(if (distanceY > 0) TouchLayout.ORIENTATION.TOP else TouchLayout.ORIENTATION.BOTTOM, distanceY)
            } else if (absX > absY) {
                //水平方向的Scroll操作
                onScrollChange(if (distanceX > 0) TouchLayout.ORIENTATION.LEFT else TouchLayout.ORIENTATION.RIGHT, distanceX)
            }
        }

        return true
    }
})
```
主要是想通过`GestureDetector`将`Touch`操作, 转换成`onScroll`和`onFling`操作.

```
override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
	//kotlin的扩展方法
    if (ev.isDown()) {
        interceptTouchEvent = canScroll()
    }
    val result = gestureDetector.onTouchEvent(ev)
    return result && interceptTouchEvent
}

override fun onTouchEvent(event: MotionEvent): Boolean {
    gestureDetector.onTouchEvent(event)
    if (isTouchFinish(event)) {
	    //如果TabLayout在ViewPager中,或者RecycleView中,调用这个方法可以让ViewPager/RecyclerView不会处理Touch事件
        parent.requestDisallowInterceptTouchEvent(false)
    } else if (event.isDown()) {
        overScroller.abortAnimation()
    }
    return true
}
```
通过以上方法, 已经成功的将Touch事件转换成了`onScrollChange`和`onFlingChange`的方法处理
```
/**Scroll操作的处理方法*/
fun onScrollChange(orientation: TouchLayout.ORIENTATION, distance: Float) {
   if (canScroll()) {
       if (orientation == TouchLayout.ORIENTATION.LEFT || orientation == TouchLayout.ORIENTATION.RIGHT) {
           scrollBy(distance.toInt(), 0)

           parent.requestDisallowInterceptTouchEvent(true)
       }
   }
}

/**Fling操作的处理方法*/
open fun onFlingChange(orientation: TouchLayout.ORIENTATION, velocity: Float /*瞬时值*/) {
   if (canScroll()) {
       if (orientation == TouchLayout.ORIENTATION.LEFT) {
           startFlingX(-velocity.toInt(), childMaxWidth)
       } else if (orientation == TouchLayout.ORIENTATION.RIGHT) {
           startFlingX(-velocity.toInt(), scrollX)
       }
   }
}
```

***滚动事件中, fling操作是比较难的.就是手指离屏后的惯性滚动***

### 4.OverScroller让ViewGroup滚动起来
在`ViewGroup`中, 让`child view`改变显示位置, 有2种方法:
1. 调用scrollTo方法
2. 直接调用`child view`的`layout`方法

为了方便使用, 系统提供了`OverScroller`类, 用来调用计算滚动坐标并配合`scrollTo`方法, 实现滚动效果.

*其实`OverScroller`本身和`View`没有半毛钱关系, `OverScroller`只是一套坐标计算,动画集成的工具类.最终滚动的实现是开发者调用`View.scrollTo`方法*

***注意:***既然用到了`OverScroller`,就必须要实现`View.computeScroll`方法.配套使用的方法.
```
//OverScroller滚动, 是一个持续的过程. 内部是一个动画在执行.
@Override
override fun computeScroll() {
    if (overScroller.computeScrollOffset() /*判断OverScroller是否还需要滚动*/) {
	    //如果还需要滚动
        scrollTo(overScroller.currX, overScroller.currY) //这才是滚动的核心操作.
        postInvalidate() //调用此方法, 最终又会回调到 computeScroll 方法中.这个View的机制.和OverScroller没关系, 如此往复调用 computeScroll->computeScrollOffset->scrollTo->postInvalidate->computeScroll->...scrollTo->...  , ViewGroup 就滚动起来啦,是不是很easy?
        if (overScroller.currX < 0 || overScroller.currX > childMaxWidth - measuredWidth) {
	        //细节处理, 达到滚动边界, 停止OverScroller的动画执行.
            overScroller.abortAnimation()
        }
    }
}
```

之后的操作就是`OverScroller`

```
open fun startFlingX(velocityX: Int, maxDx: Int) {
    startFling(velocityX, 0, maxDx, 0)
}

fun startFling(velocityX: Int, velocityY: Int, maxDx: Int, maxDy: Int) {
    overScroller.abortAnimation()
	//fling
    overScroller.fling(scrollX, scrollY, velocityX, velocityY, 0, maxDx, 0, maxDy, measuredWidth, measuredHeight)
    postInvalidate()  //这个方法是用来触发computeScroll的,必须调用,否则界面无效果.
}

fun startScroll(dx: Int, dy: Int = 0) {
	//scroll
    overScroller.startScroll(scrollX, scrollY, dx, dy, 300)
    postInvalidate() //这个方法是用来触发computeScroll的,必须调用,否则界面无效果.
}
```

经过以上操作, `ViewGroup`就可以支持`scroll`和`fling`操作了.

**小结:**
阅读到此, 你应该掌握的知识:
1. 自定义View的属性定义和读取
2. `onMeasure`和`onLayout`的作用
3. `Touch`事件的处理流程
4. `GestureDetector`的使用
5. `OverScroller`的使用

---
### 5:指示器的绘制, Canvas登场
与`Canvas`相关的2的常用方法`draw`和`onDraw`
其实`onDraw`方法是在`draw`方法中调用的.

使用`Canvas`最重要的就是绘制顺序, 先绘制的内容先展示, 后绘制的内容会覆盖在之前的内容上面.

```
override fun draw(canvas: Canvas) {
	//在super.draw(canvas)方法之前, 绘制的东西会被child view覆盖
   super.draw(canvas)
   //在super.draw(canvas)方法之后, 绘制的东西会覆盖child view
}

override fun onDraw(canvas: Canvas) {
	//在super.onDraw(canvas)方法之前, 绘制的东西会被child view的内容覆盖 (比如TextView原来的文本内容)
   super.onDraw(canvas)
   	//在super.onDraw(canvas)方法之后, 绘制的东西会覆盖child view的内容 (比如TextView原来的文本内容)
}
```

了解了`Canvas`之后, 就开始指示器的绘制吧.

***注意*** `ViewGroup`在默认情况下`draw`方法是不会执行的.所以你必须调用`setWillNotDraw(false)`方法,激活绘制流程.

`Canvas`绘制的时候, 坐标计算尤为频繁, 数学功底好不好, 在这里能够体现的淋淋尽致.
```
override fun onDraw(canvas: Canvas) {
     super.onDraw(canvas)

     if (curIndex in 0..(childCount - 1)) {
         //安全的index

         val childView = getChildAt(curIndex) //拿到当前指示的child view,用来确定指示器绘制的坐标

         //指示器的宽度
         val indicatorDrawWidth = if (isAnimStart()) {
             (animStartWidth + (animEndWidth - animStartWidth) * animatorValueInterpolator + indicatorWidthOffset).toInt()
         } else {
             getIndicatorWidth(curIndex) + indicatorWidthOffset
         }

         //child横向中心x坐标
         val childCenter: Int = if (isAnimStart()) {
             (animStartCenterX + (animEndCenterX - animStartCenterX) * animatorValueInterpolator).toInt()
         } else {
             getChildCenter(curIndex)
         }

         //L.e("RTabIndicator: draw ->$viewWidth $childCenter $indicatorDrawWidth $curIndex $animatorValueInterpolator")

         val left = (childCenter - indicatorDrawWidth / 2).toFloat()
         val right = (childCenter + indicatorDrawWidth / 2).toFloat()
         val top = when (indicatorType) {
             INDICATOR_TYPE_BOTTOM_LINE -> (viewHeight - indicatorOffsetY - indicatorHeight).toFloat()
             INDICATOR_TYPE_ROUND_RECT_BLOCK -> (childView.top - indicatorHeightOffset / 2).toFloat()
             else -> 0f
         }
         val bottom = when (indicatorType) {
             INDICATOR_TYPE_BOTTOM_LINE -> (viewHeight - indicatorOffsetY).toFloat()
             INDICATOR_TYPE_ROUND_RECT_BLOCK -> (childView.bottom + indicatorHeightOffset / 2).toFloat()
             else -> 0f
         }
         indicatorDrawRect.set(left, top, right, bottom)

         if (indicatorDrawable == null) {
             when (indicatorType) {
                 INDICATOR_TYPE_NONE -> {
                 }
                 INDICATOR_TYPE_BOTTOM_LINE -> {
                     mBasePaint.color = indicatorColor
                     //绘制圆角矩形的指示器
                     canvas.drawRoundRect(indicatorDrawRect, indicatorRoundSize.toFloat(), indicatorRoundSize.toFloat(), mBasePaint)
                 }
                 INDICATOR_TYPE_ROUND_RECT_BLOCK -> {
                     mBasePaint.color = indicatorColor
                     canvas.drawRoundRect(indicatorDrawRect, indicatorRoundSize.toFloat(), indicatorRoundSize.toFloat(), mBasePaint)
                 }
             }
         } else {
             indicatorDrawable?.let {
                 it.setBounds(indicatorDrawRect.left.toInt(),
                         indicatorDrawRect.top.toInt(),
                         indicatorDrawRect.right.toInt(),
                         indicatorDrawRect.bottom.toInt())
                 it.draw(canvas)
             }
         }
     }
 }
```

真正绘制的代码只有一行`canvas.drawRoundRect(indicatorDrawRect, indicatorRoundSize.toFloat(), indicatorRoundSize.toFloat(), mBasePaint)`,其他都是计算坐标,安全校验.

---
到这里核心部分都写的差不多了, 剩下的都是逻辑处理和一些细节.各位可以自由发挥,代码就不贴了.

源码地址: https://github.com/angcyo/RTabLayout

#### `也许你还想学习更多, 来我的群吧, 我写代码的能力, 远大于写文章的能力:`

## 联系作者
[点此快速加群](https://shang.qq.com/wpa/qunwpa?idkey=cbcf9a42faf2fe730b51004d33ac70863617e6999fce7daf43231f3cf2997460)

> 请使用QQ扫码加群, 小伙伴们都在等着你哦!

![](https://raw.githubusercontent.com/angcyo/res/master/image/qq/qq_group_code.png)

> 关注我的公众号, 每天都能一起玩耍哦!

![](https://raw.githubusercontent.com/angcyo/res/master/image/weixin/%E8%AE%A2%E9%98%85%E5%8F%B7_%E4%BA%8C%E7%BB%B4%E7%A0%81/qrcode_for_gh_59fa6d9a51d8_258_8cm.jpg)



