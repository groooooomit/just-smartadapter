package just.recyclerview.adapter.rv;


import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.*;
import androidx.collection.SparseArrayCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


public class ViewHolder extends RecyclerView.ViewHolder {
    private SparseArrayCompat<View> mViews = new SparseArrayCompat<>();

    private ViewHolder(View itemView) {
        super(itemView);
    }

    public static ViewHolder create(Context context, @LayoutRes int layoutResId, ViewGroup parent) {
        View itemView = LayoutInflater.from(context).inflate(layoutResId, parent, false);
        return create(itemView);
    }

    public static ViewHolder create(View view) {
        return new ViewHolder(view);
    }

    public <T extends View> T getView(@IdRes int viewId) {
        View view = mViews.get(viewId);
        if (null == view) {
            view = this.itemView.findViewById(viewId);
            mViews.put(viewId, view);
        }
        //noinspection unchecked
        return (T) view;
    }

    // ---------------------------------------------------------
    // 通用扩展方法
    // ---------------------------------------------------------
    public ViewHolder setText(@IdRes int viewId, CharSequence text) {
        TextView textView = getView(viewId);
        textView.setText(text);
        return this;
    }

    public ViewHolder setText(@IdRes int viewId, @StringRes int resId) {
        TextView textView = getView(viewId);
        textView.setText(resId);
        return this;
    }

    public ViewHolder setTextColor(@IdRes int viewId, @ColorInt int color) {
        TextView textView = getView(viewId);
        textView.setTextColor(color);
        return this;
    }

    public ViewHolder setImageResource(@IdRes int viewId, @DrawableRes int resId) {
        ImageView imageView = getView(viewId);
        imageView.setImageResource(resId);
        return this;
    }

    public ViewHolder setImageBitmap(@IdRes int viewId, Bitmap bm) {
        ImageView imageView = getView(viewId);
        imageView.setImageBitmap(bm);
        return this;
    }

    public ViewHolder setOnClickListener(@IdRes int viewId, @Nullable View.OnClickListener listener) {
        getView(viewId).setOnClickListener(listener);
        return this;
    }

    public ViewHolder setOnLongClickListener(@IdRes int viewId, @Nullable View.OnLongClickListener listener) {
        getView(viewId).setOnLongClickListener(listener);
        return this;
    }

    public ViewHolder setVisibility(@IdRes int viewId, @Visibility int visibility) {
        getView(viewId).setVisibility(visibility);
        return this;
    }

    @IntDef({View.VISIBLE, View.INVISIBLE, View.GONE})
    @Retention(RetentionPolicy.SOURCE)
    private @interface Visibility {
    }


}
