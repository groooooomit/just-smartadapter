package just.smartadapter.util;


import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;


/**
 * 内部绑定 LoadMoreAdapter 的 RecyclerView
 */
public class LoadMoreRecyclerView extends RecyclerView {

    private LoadMoreHelper loadMoreHelper;
    private LoadMoreHelper.OnLoadMoreListener onLoadMoreListener;

    public LoadMoreRecyclerView(Context context) {
        super(context);
        init();
    }

    public LoadMoreRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoadMoreRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    protected void init() {
        loadMoreHelper = new LoadMoreHelper(this, () -> {
            if (null != onLoadMoreListener) {
                onLoadMoreListener.onLoadMore();
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        loadMoreHelper.onTouchEvent(e);
        return super.onInterceptTouchEvent(e);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        loadMoreHelper.onTouchEvent(e);
        return super.onTouchEvent(e);
    }

    public void setOnLoadMoreListener(LoadMoreHelper.OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    /**
     * 判断是否能够垂直方向滑动
     */
    public boolean isVerticalScrollable() {
        return loadMoreHelper.isVerticalScrollable();
    }
}
