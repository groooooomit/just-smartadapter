package just.recyclerview;


import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import just.recyclerview.adapter.rv.HeaderAndFooterAdapter;


/**
 * 内部绑定 LoadMoreAdapter 的 RecyclerView
 */
public class LoadMoreRecyclerView extends RecyclerView {
    private static final String TAG = "LoadMoreRecyclerView";

    private boolean mIsLoading;
    private OnLoadMoreListener onLoadMoreListener;
    private GestureDetectorCompat mGestureDetectorCompat;
    // 记录当前的状态值
    private int mCurrentScrollState = SCROLL_STATE_IDLE;

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
        // 手势检测
        mGestureDetectorCompat = new GestureDetectorCompat(getContext(), new SimpleOnGestureListener() {

            private boolean canLoadMoreWhenStart;

            // 测试结果表明，先触发 onDown ，然后 scroll state 才会变更
            @Override
            public boolean onDown(MotionEvent e) {
                // 是判断当前滚动停止？还是判断到底了没有？
                Log.i(TAG, "onDown: ");
                // 是否在最下面且是否停止滚动
                canLoadMoreWhenStart = canLoadMore();
                return false;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                processWhenFling(e1, e2);
                return false;
            }

            private void processWhenFling(MotionEvent e1, MotionEvent e2) {
                // 如果在 onDown 的时候不能触发上拉加载操作，那么直接返回。因为无法 在 onDown 中阻止 onFling 的产生，所以使用临时标记 canLoadMoreWhenStart
                if (!canLoadMoreWhenStart) {
                    return;
                }
                // 参数检查
                if (null == e1 || null == e2) {
                    return;
                }

                // X 坐标的变化量
                float deltaX = e2.getX() - e1.getX();

                // X 坐标的绝对值
                float distanceX = Math.abs(deltaX);

                // Y 坐标的变化量
                float deltaY = e2.getY() - e1.getY();

                // Y 坐标的绝对值
                float distanceY = Math.abs(deltaY);// y 移动距离的绝对值

                // 判断水平方向还是垂直方向
                if (distanceX > distanceY) {
                    // 水平方向
                    if (deltaX > 0) {
                        // 向右
                        Log.i(TAG, "onFling: right");
                    } else {
                        // 向左
                        Log.i(TAG, "onFling: left");
                    }
                } else {
                    if (deltaY > 0) {
                        // 向下
                        Log.i(TAG, "onFling: down");
                    } else {
                        // 向上
                        Log.i(TAG, "onFling: up");
                        performLoadMore();
                    }
                }
            }
        });
    }

    private void performLoadMore() {
        if (null != onLoadMoreListener) {
            mIsLoading = true;
            onLoadMoreListener.onLoadMore();
        }
    }

    // 要在set adapter 之前为 loadmre 赋值

    public void setAdapter(Adapter adapter, View loadMoreView, View... headerViews) {
        HeaderAndFooterAdapter headerAndFooterWrapper = new HeaderAndFooterAdapter(adapter);
        if (null != loadMoreView) {
            headerAndFooterWrapper.addFootView(loadMoreView);
        }
        if (null != headerViews && headerViews.length > 0) {
            for (View headerView : headerViews) {
                headerAndFooterWrapper.addHeaderView(headerView);
            }
        }
        setAdapter(headerAndFooterWrapper);
    }

    // SCROLL_STATE_IDLE        滑动停止    0
    // SCROLL_STATE_DRAGGING    拖拽滑动    1
    // SCROLL_STATE_SETTLING    惯性滑动    2
    // 自动加载比较方便，判断当前滚动是否停止，如果停止，是否在底部，如果在底部，是否在loading，如果没有loading，那么自动加载更多
    @Override
    public void onScrollStateChanged(int state) {
        mCurrentScrollState = state;
    }

    // 判断是否是线性可滑动的
    public boolean isLinearScrollable() {
        LayoutManager layoutManager = getLayoutManager();
        if (null == layoutManager) {
            return false;
        }
        if (!(layoutManager instanceof LinearLayoutManager)) {
            return false;
        }
        Adapter adapter = getAdapter();
        if (null == adapter) {
            return false;
        }
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
        int lastCompletelyVisibleItemPosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
        int itemCount = adapter.getItemCount();
        return lastCompletelyVisibleItemPosition < itemCount;// 本来要减去 1，但是现在多了一个 footer，又增加 1
    }

    public boolean isVerticalScrollable() {
        return canScrollVertically(1) || canScrollVertically(-1);
    }

    // 是否在底部（不能向下滑动）
    public boolean isAtBottom() {

        // 1 = down; -1 = up; 0 = up or down
        return !canScrollVertically(1);
    }

    private boolean canLoadMore() {
        // 满足 4 个条件
        // 没有在 loading
        // 处于滚动停止状态
        // 有区域可滚动
        // 已经在最底部了
        return !mIsLoading && mCurrentScrollState == SCROLL_STATE_IDLE && isVerticalScrollable() && isAtBottom();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        mGestureDetectorCompat.onTouchEvent(e);
        return super.onInterceptTouchEvent(e);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        mGestureDetectorCompat.onTouchEvent(e);
        return super.onTouchEvent(e);
    }

    public void setLoading(boolean isLoading) {
        this.mIsLoading = isLoading;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public interface OnLoadMoreListener {
        // 正在加载更多
        void onLoadMore();
    }


}
