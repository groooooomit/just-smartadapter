package just.recyclerview.adapter.lv;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.StringRes;
import androidx.collection.SparseArrayCompat;

public class ViewHolder {
    private final SparseArrayCompat<View> mViews = new SparseArrayCompat<>();
    private View mConvertView;

    private ViewHolder(Context context, ViewGroup parent, @LayoutRes int layoutResId) {
        mConvertView = LayoutInflater.from(context).inflate(layoutResId, parent, false);
        mConvertView.setTag(this);
    }

    public View getConvertView() {
        return mConvertView;
    }

    public <T extends View> T getView(int viewId) {
        View view = mViews.get(viewId);
        if (view == null) {
            view = mConvertView.findViewById(viewId);
            mViews.put(viewId, view);
        }
        //noinspection unchecked
        return (T) view;
    }


    public static ViewHolder create(Context context, View convertView, ViewGroup parent, @LayoutRes int layoutResId) {
        if (null == convertView) {
            return new ViewHolder(context, parent, layoutResId);
        } else {
            return (ViewHolder) convertView.getTag();
        }
    }

    public ViewHolder setText(@IdRes int viewId, String text) {
        TextView view = getView(viewId);
        view.setText(text);
        return this;
    }

    public ViewHolder setText(@IdRes int viewId, @StringRes int resId) {
        TextView view = getView(viewId);
        view.setText(resId);
        return this;
    }

}
