package com.angcyo.rtablayout;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;

/**
 * Created by angcyo on 2016-11-26.
 */

public abstract class RPagerAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener {

    SparseArray<WeakReference<View>> mViewCache = new SparseArray<>();

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        Context context = container.getContext();

        View view = getCacheView(position);
        int itemType = getItemType(position);

        if (view == null) {
            int layoutId = getLayoutId(position, itemType);
            if (layoutId != -1) {
                view = LayoutInflater.from(context).inflate(layoutId, container, false);
            } else {
                view = createView(context, position, itemType);
            }
        }

        if (view.getParent() == null) {
            if (view.getLayoutParams() == null) {
                container.addView(view, -1, -1);
            } else {
                container.addView(view);
            }
        }

        initItemView(view, position, itemType);

        return view;
    }

    public int getItemType(int position) {
        return 1;
    }

    public View getCacheView(int position) {
        WeakReference<View> viewWeakReference = mViewCache.get(position);
        View view = null;
        if (viewWeakReference != null) {
            view = viewWeakReference.get();
        }
        return view;
    }

    protected int getLayoutId(int position, int itemType) {
        return -1;
    }

    @Deprecated
    protected View createView(Context context, int position) {
        return null;
    }

    protected View createView(Context context, int position, int itemType) {
        return createView(context, position);
    }

    protected void initItemView(@NonNull View rootView, int position, int itemType) {

    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        int itemType = getItemType(position);
        onItemDestroy((View) object, itemType);

        container.removeView((View) object);
        mViewCache.put(position, new WeakReference<>((View) object));
    }

    protected void onItemDestroy(@NonNull View rootView, int itemType) {

    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        //L.e("call: isViewFromObject([view, object])-> \n" + ((ViewGroup) view).getChildAt(0) + "\n" + ((ViewGroup) object).getChildAt(0));
        return view == object;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int position) {

    }
}
