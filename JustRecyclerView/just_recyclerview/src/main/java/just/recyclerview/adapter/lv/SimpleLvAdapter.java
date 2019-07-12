package just.recyclerview.adapter.lv;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import androidx.annotation.LayoutRes;

import java.util.List;

public class SimpleLvAdapter<E> extends BaseAdapter {
    private Context mContext;
    private List<E> mDatas;
    @LayoutRes
    private int mLayoutResId;
    private Converter<E> mConverter;

    protected SimpleLvAdapter(Context context, List<E> datas, @LayoutRes int layoutResId, Converter<E> converter) {
        this.mContext = context;
        this.mDatas = datas;
        this.mLayoutResId = layoutResId;
        this.mConverter = converter;
    }

    @Override
    public int getCount() {
        return null == mDatas ? 0 : mDatas.size();
    }

    @Override
    public E getItem(int position) {
        return null == mDatas ? null : mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder = ViewHolder.create(mContext, convertView, parent, mLayoutResId);
        if (null != mConverter) {
            mConverter.convert(viewHolder, getItem(position), position);
        }
        return viewHolder.getConvertView();
    }

    // 局部更新

    /**
     * 局部刷新，对具有 Header 或 Footer 的ListView 无效
     */
    public void notifyDataSetChanged(ListView listView, int position) {
        // 第一个可见Item的位置
        int firstVisiblePosition = listView.getFirstVisiblePosition();
        // 最后一个可见Item的位置
        int lastVisiblePosition = listView.getLastVisiblePosition();
        // 在看见范围内才更新，不可见的滑动后自动会调用getView方法更新
        if (position >= firstVisiblePosition && position <= lastVisiblePosition) {
            View view = listView.getChildAt(position - firstVisiblePosition);
            // 执行一次 getView ，Google 推荐的做法
            getView(position, view, listView);
        }
        // 如果有 Footer 会数组越界
    }

    public static final class Builder<E> {
        private Context context;
        private List<E> datas;
        @LayoutRes
        private int layoutResId;
        private SimpleLvAdapter.Converter<E> converter;

        public Builder(Context context) {
            this.context = context;
        }

        public SimpleLvAdapter.Builder<E> dataSource(List<E> datas) {
            this.datas = datas;
            return this;
        }

        public SimpleLvAdapter.Builder<E> itemLayout(@LayoutRes int layoutResId) {
            this.layoutResId = layoutResId;
            return this;
        }

        public SimpleLvAdapter.Builder<E> whenConvert(Converter<E> converter) {
            this.converter = converter;
            return this;
        }

        public SimpleLvAdapter<E> build() {
            return new SimpleLvAdapter<>(this.context, this.datas, this.layoutResId, this.converter);
        }
    }

    public interface Converter<E> {
        void convert(ViewHolder holder, E data, int position);
    }
}
