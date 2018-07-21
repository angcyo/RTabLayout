package com.angcyo.uiview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/07/21 14:42
 * 修改人员：Robi
 * 修改时间：2017/07/21 14:42
 * 修改备注：
 * Version: 1.0.0
 */
abstract class RAnimListener : AnimatorListenerAdapter() {

    private var isCancel = false

    override fun onAnimationRepeat(animation: Animator?) {
        super.onAnimationRepeat(animation)
    }

    final override fun onAnimationEnd(animation: Animator?) {
        super.onAnimationEnd(animation)
        if (isCancel) {
            //当动画被取消的时候, 系统会回调onAnimationCancel, 然后 onAnimationEnd
            //所以, 这里过滤一下
        } else {
            onAnimationFinish(animation)
            onAnimationFinish(animation, false)
        }
    }

    final override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
        super.onAnimationEnd(animation, isReverse)
    }

    override fun onAnimationStart(animation: Animator?, isReverse: Boolean) {
        super.onAnimationStart(animation, isReverse)
    }

    final override fun onAnimationCancel(animation: Animator?) {
        super.onAnimationCancel(animation)
        isCancel = true
        onAnimationFinish(animation)
        onAnimationFinish(animation, isCancel)
    }

    override fun onAnimationPause(animation: Animator?) {
        super.onAnimationPause(animation)
    }

    override fun onAnimationStart(animation: Animator?) {
        super.onAnimationStart(animation)
        isCancel = false
    }

    override fun onAnimationResume(animation: Animator?) {
        super.onAnimationResume(animation)
        isCancel = false
    }

    /**自定义的进度回调*/
    open fun onAnimationProgress(animation: Animator?, progress: Float /*0-1f*/) {

    }

    /**动画完成, 或者取消都会执行*/
    @Deprecated("")
    open fun onAnimationFinish(animation: Animator?) {

    }

    open fun onAnimationFinish(animation: Animator?, cancel: Boolean) {

    }

    /**当设置了动画setStartDelay时, 此方法会先于onAnimationStart调用*/
    open fun onDelayBeforeStart(animation: Animator?) {

    }

    /**当动画结束时, 延迟一定时间回调此方法*/
    open fun onDelayAfterEnd(animation: Animator?) {

    }
}