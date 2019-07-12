package just.recyclerview.adapter.rv;

import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SimpleRvAdapter<E> extends RecyclerView.Adapter<ViewHolder> implements View.OnClickListener, View.OnLongClickListener {
    private List<E> mDatas;
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    private Converter<E> mConverter;
    @LayoutRes
    private int mLayoutResId;
    private int mItemWidth;
    private int mItemHeight;

    private SimpleRvAdapter(List<E> datas, @LayoutRes int layoutResId, Converter<E> converter, OnItemClickListener onItemClickListener, OnItemLongClickListener onItemLongClickListener, int itemWidth, int itemHeight) {
        this.mDatas = datas;
        this.mLayoutResId = layoutResId;
        this.mConverter = converter;
        this.onItemClickListener = onItemClickListener;
        this.onItemLongClickListener = onItemLongClickListener;
        this.mItemWidth = itemWidth;
        this.mItemHeight = itemHeight;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder viewHolder = ViewHolder.create(parent.getContext(), mLayoutResId, parent);
        // 修改 item 的size TODO 考虑多 Item Type 下如何处理
        if (mItemWidth > 0 || mItemHeight > 0) {
            ViewGroup.LayoutParams layoutParams = viewHolder.itemView.getLayoutParams();
            if (mItemWidth > 0) {
                layoutParams.width = mItemWidth;
            }
            if (mItemHeight > 0) {
                layoutParams.height = mItemHeight;
            }
            viewHolder.itemView.setLayoutParams(layoutParams);
        }
        // 设置点击事件
        viewHolder.itemView.setOnClickListener(this);
        viewHolder.itemView.setOnLongClickListener(this);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // TODO 注意 mDatas 为 null 的情况
        if (null != mConverter) {
            mConverter.convert(this, holder, mDatas.get(position), position);
        }
        holder.itemView.setTag(position);// 更新 tag 中的位置
        // 不要使用 getlayoutPosition，当加入了 Header 或 Footer时就不准确了
    }

    @Override
    public int getItemCount() {
        return null == mDatas ? 0 : mDatas.size();
    }

    @Override
    public void onClick(View v) {
        if (null != onItemClickListener) {
            int position = (int) v.getTag();

            // 位置限制
            // RecycleView 的删除或者插入数据源和界面不是同步的，因为界面需要一段时间执行动画，在动画执行完成之前不会更新position
            // 所以当 mDatas.remove(position)的时候，这个position在界面上并没有及时remove，而是要等动画执行结束。
            // 假如动画执行尚未结束，那么当快速点击的时候，position通过onItemClick传递给mDatas执行remove的时候，mDatas事实上已经remove了，如果这个position是最后一个，那么就会crash
            int size = getItemCount();
            if (position < size) {// position 如果等于mDatas.size，表示已经过期了，不用重复处理
                onItemClickListener.onItemClick(this, v, position);// 或者直接使用 getAdapterPosition 进行处理
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (null != onItemLongClickListener) {
            int position = (int) v.getTag();
            int size = getItemCount();
            if (position < size) {// position 如果等于mDatas.size，表示已经过期了，不用重复处理
                return onItemLongClickListener.onItemLongClick(this, v, position);
            }
        }
        return false;
    }


    // ----------------------------------------------------------------------------
    // custom method
    // ----------------------------------------------------------------------------

    public void notifyItemMovedExactly(int fromPosition, int toPosition) {
        if (fromPosition != toPosition) {
            notifyItemMoved(fromPosition, toPosition);
            int min = Math.min(fromPosition, toPosition);
            int max = Math.max(fromPosition, toPosition);
            notifyItemRangeChanged(min, max - min + 1);// Google 的 bug
        }
    }

    public void notifyItemInsertedExactly(int position) {
        notifyItemInserted(position);
        if (isPartChange(position)) {
            notifyItemRangeChanged(position, getItemCount() - position);
        }
    }

    public void notifyItemRangeInsertedExactly(int position, int itemCount) {
        // 数据源的非空判断
        notifyItemRangeInserted(position, itemCount);
        if (isPartChange(position)) {
            notifyItemRangeChanged(position, getItemCount() - position);
        }
    }

    public void notifyItemRemovedExactly(int position) {
        notifyItemRemoved(position);
        if (isPartChange(position)) {
            notifyItemRangeChanged(position, getItemCount() - position);
        }
    }

    public void notifyItemRangeRemovedExactly(int position, int itemCount) {
        notifyItemRangeRemoved(position, itemCount);
        if (isPartChange(position)) {
            notifyItemRangeChanged(position, getItemCount() - position);
        }
    }

    private boolean isPartChange(int position) {
        return position != getItemCount() - 1;
    }


    // --------------------------------------------------------------------------------------------------------------------------------------------------
    // static components

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static final class Builder<E> {
        private List<E> datas;
        @LayoutRes
        private int layoutResId;
        private int itemWidth;
        private int itemHeight;
        private Converter<E> converter;
        private OnItemClickListener onItemClickListener;
        private OnItemLongClickListener onItemLongClickListener;

        private Builder() {
        }

        public Builder<E> dataSource(List<E> datas) {
            this.datas = datas;
            return this;
        }

        public Builder<E> itemLayout(@LayoutRes int layoutResId) {
            this.layoutResId = layoutResId;
            return this;
        }

        public Builder<E> overrideWidth(int itemWidth) {
            this.itemWidth = itemWidth;
            return this;
        }

        public Builder<E> overrideHeight(int itemHeight) {
            this.itemHeight = itemHeight;
            return this;
        }

        public Builder<E> whenConvert(Converter<E> converter) {
            this.converter = converter;
            return this;
        }

        public Builder<E> setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
            return this;
        }

        public Builder<E> setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
            this.onItemLongClickListener = onItemLongClickListener;
            return this;
        }

        public SimpleRvAdapter<E> build() {
            return new SimpleRvAdapter<>(this.datas, this.layoutResId, this.converter, this.onItemClickListener, this.onItemLongClickListener, this.itemWidth, this.itemHeight);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(RecyclerView.Adapter<ViewHolder> adapter, View v, int position);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(RecyclerView.Adapter<ViewHolder> adapter, View v, int position);
    }

    public interface Converter<E> {
        void convert(RecyclerView.Adapter<ViewHolder> adapter, ViewHolder viewHolder, E data, int position);
    }


}
