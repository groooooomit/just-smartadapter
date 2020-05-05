package just.smartadapter.core;


import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.IntDef;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.collection.SparseArrayCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 通用的 ViewHolder
 */
public class CommonViewHolder extends RecyclerView.ViewHolder {

    /**
     * 通过 viewId 索引 View
     */
    @NonNull
    private final SparseArrayCompat<View> mViews = new SparseArrayCompat<>();

    public CommonViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    /**
     * 通过 viewId 获取对应的 View，通过 map 进行缓存，不用每次都 findViewById
     */
    @NonNull
    public <T extends View> T getView(@IdRes int viewId) {
        View view = mViews.get(viewId);
        if (null == view) {
            view = this.itemView.findViewById(viewId);
            if (null == view) {
                throw new RuntimeException("No view exists, viewId: " + viewId);
            } else {
                mViews.put(viewId, view);
            }
        }
        //noinspection unchecked
        return (T) view;
    }

    ///////////////////////////////////////////////////////////////////////////

    /**
     * 用于修正 layout 的位置
     */
    private int positionOffset;

    /**
     * 计算偏移量
     */
    public final void calculatePositionOffset(int bindPosition) {
        this.positionOffset = getLayoutPosition() - bindPosition;
    }

    /**
     * 获取修正后的 layoutPosition
     */
    public final int getFixedLayoutPosition() {
        return getLayoutPosition() - positionOffset;
    }

    ///////////////////////////////////////////////////////////////////////////
    // static
    ///////////////////////////////////////////////////////////////////////////

    @NonNull
    public static CommonViewHolder fromInflateLayout(@NonNull Context context, @LayoutRes int layoutResId, @NonNull ViewGroup parent) {
        if (layoutResId == 0) {
            throw new IllegalArgumentException("Invalid layoutResId");
        }
        final View itemView = LayoutInflater.from(context).inflate(layoutResId, parent, false);
        return new CommonViewHolder(itemView);
    }

    ///////////////////////////////////////////////////////////////////////////
    // 通用扩展方法
    ///////////////////////////////////////////////////////////////////////////

    public CommonViewHolder setText(@IdRes int viewId, @Nullable CharSequence text) {
        this.<TextView>getView(viewId).setText(text);
        return this;
    }

    public CommonViewHolder setText(@IdRes int viewId, @StringRes int resId) {
        this.<TextView>getView(viewId).setText(resId);
        return this;
    }

    public CommonViewHolder setTextColor(@IdRes int viewId, @ColorInt int color) {
        this.<TextView>getView(viewId).setTextColor(color);
        return this;
    }

    public CommonViewHolder setImageResource(@IdRes int viewId, @DrawableRes int resId) {
        this.<ImageView>getView(viewId).setImageResource(resId);
        return this;
    }

    public CommonViewHolder setImageBitmap(@IdRes int viewId, Bitmap bitmap) {
        this.<ImageView>getView(viewId).setImageBitmap(bitmap);
        return this;
    }

    public CommonViewHolder setOnClickListener(@IdRes int viewId, @Nullable View.OnClickListener listener) {
        this.getView(viewId).setOnClickListener(listener);
        return this;
    }

    public CommonViewHolder setOnLongClickListener(@IdRes int viewId, @Nullable View.OnLongClickListener listener) {
        this.getView(viewId).setOnLongClickListener(listener);
        return this;
    }

    public CommonViewHolder setVisibility(@IdRes int viewId, @Visibility int visibility) {
        this.getView(viewId).setVisibility(visibility);
        return this;
    }

    @IntDef({View.VISIBLE, View.INVISIBLE, View.GONE})
    @Retention(RetentionPolicy.SOURCE)
    private @interface Visibility {
    }
}
