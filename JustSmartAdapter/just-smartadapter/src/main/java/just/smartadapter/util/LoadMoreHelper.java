package just.smartadapter.util;

import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 上拉加载更多手势判断
 */
public final class LoadMoreHelper {

    @NonNull
    private final RecyclerView recyclerView;

    @NonNull
    private final GestureDetectorCompat gestureDetector;

    @NonNull
    private final OnLoadMoreListener onLoadMoreListener;

    public LoadMoreHelper(@NonNull RecyclerView recyclerView, @NonNull OnLoadMoreListener onLoadMoreListener) {
        this.recyclerView = recyclerView;
        this.gestureDetector = new GestureDetectorCompat(recyclerView.getContext(), new GestureDetector.SimpleOnGestureListener() {

            private boolean canLoadMoreWhenDown;

            @Override
            public boolean onDown(MotionEvent e) {
                canLoadMoreWhenDown = canLoadMore();
                return super.onDown(e);
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (canLoadMoreWhenDown) {
                    processWhenFling(e1, e2, velocityX, velocityY);
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });
        this.onLoadMoreListener = onLoadMoreListener;
    }

    private boolean canLoadMore() {
        return isScrollStopped() && isVerticalScrollable() && isAtBottom();
    }

    public final void onTouchEvent(@NonNull MotionEvent event) {
        this.gestureDetector.onTouchEvent(event);
    }

    /**
     * @param e1        滑动起点
     * @param e2        滑动终点
     * @param velocityX 水平速度
     * @param velocityY 垂直速度
     */
    private void processWhenFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

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
            } else {
                // 向左
            }
        } else {
            if (deltaY > 0) {
                // 向下
            } else {
                // 向上
                onLoadMoreListener.onLoadMore();
            }
        }
    }


    /**
     * 判断滚动是否已经停止
     * <p>
     * SCROLL_STATE_IDLE        滑动停止    0
     * <p>
     * SCROLL_STATE_DRAGGING    拖拽滑动    1
     * <p>
     * SCROLL_STATE_SETTLING    惯性滑动    2
     */
    private boolean isScrollStopped() {
        return recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE;
    }

    public boolean isVerticalScrollable() {
        return recyclerView.canScrollVertically(1) || recyclerView.canScrollVertically(-1);
    }

    // 是否在底部（不能向下滑动）
    private boolean isAtBottom() {

        // 1 = down; -1 = up; 0 = up or down
        return !recyclerView.canScrollVertically(1);
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }
}
