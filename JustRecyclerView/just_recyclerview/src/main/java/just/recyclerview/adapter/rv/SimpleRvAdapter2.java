package just.recyclerview.adapter.rv;

import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.collection.SparseArrayCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import java.util.List;

/**
 * todo 修改名字
 * todo 去除闪烁动画配置
 * todo scroll bar 支持
 * todo 使用 RecyclerView 代替 ViewPager
 * todo {@link SnapHelper} 的使用
 *
 * @param <E>
 */
public class SimpleRvAdapter2<E> extends RecyclerView.Adapter<ViewHolder> implements View.OnClickListener, View.OnLongClickListener {
    private List<E> mDatas;
    private SparseArrayCompat<ItemBuilder<E>> mItemBuilders;

    private SimpleRvAdapter2(List<E> datas, SparseArrayCompat<ItemBuilder<E>> itemBuilders) {
        this.mDatas = datas;
        this.mItemBuilders = itemBuilders;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 获取 itemBuilder
        ItemBuilder<E> itemBuilder = mItemBuilders.get(viewType);

        // 创建 ViewHolder
        int layoutResId = itemBuilder.layoutResId;
        ViewHolder viewHolder = ViewHolder.create(parent.getContext(), layoutResId, parent);

        // 修改 item 的 size
        int itemWidth = itemBuilder.itemWidth;
        int itemHeight = itemBuilder.itemHeight;
        if (itemWidth > 0 || itemHeight > 0) {
            ViewGroup.LayoutParams layoutParams = viewHolder.itemView.getLayoutParams();
            if (itemWidth > 0) {
                layoutParams.width = itemWidth;
            }
            if (itemHeight > 0) {
                layoutParams.height = itemHeight;
            }
            viewHolder.itemView.setLayoutParams(layoutParams);
        }
        viewHolder.itemView.setOnClickListener(this);
        viewHolder.itemView.setOnLongClickListener(this);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int itemViewType = getItemViewType(position);
        ItemBuilder<E> itemBuilder = mItemBuilders.get(itemViewType);
        Converter<E> converter = itemBuilder.converter;
        if (null != converter) {
            converter.convert(this, holder, mDatas.get(position), position);
        }
        holder.itemView.setTag(position);// 更新 tag 中的位置
    }

    @Override
    public int getItemViewType(int position) {
        if (mItemBuilders.size() == 0) {
            return super.getItemViewType(position);
        }
        for (int i = 0; i < mItemBuilders.size(); i++) {
            ItemBuilder<E> itemBuilder = mItemBuilders.valueAt(i);
            if (itemBuilder.applyer.apply(mDatas.get(position), position)) {
                return mItemBuilders.keyAt(i);
            }
        }
        throw new IllegalArgumentException("No item type added that matches position=" + position + " in data source");
    }

    @Override
    public int getItemCount() {
        return null == mDatas ? 0 : mDatas.size();
    }

    @Override
    public void onClick(View v) {
        int position = (int) v.getTag();
        int size = getItemCount();
        if (position < size) {// position 如果等于mDatas.size，表示已经过期了，不用重复处理
            OnItemClickListener onItemClickListener = mItemBuilders.get(getItemViewType(position)).onItemClickListener;
            if (null != onItemClickListener) {
                onItemClickListener.onItemClick(this, v, position);
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        int position = (int) v.getTag();
        int size = getItemCount();
        if (position < size) {// position 如果等于mDatas.size，表示已经过期了，不用重复处理
            OnItemLongClickListener onItemLongClickListener = mItemBuilders.get(getItemViewType(position)).onItemLongClickListener;
            if (null != onItemLongClickListener) {
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
        private SparseArrayCompat<ItemBuilder<E>> itemBuilders = new SparseArrayCompat<>();

        private Builder() {
        }

        public Builder<E> dataSource(List<E> datas) {
            this.datas = datas;
            return this;
        }

        public ItemBuilder<E> and(int itemType) {
            ItemBuilder<E> itemBuilder = new ItemBuilder<>(this);
            itemBuilders.put(itemType, itemBuilder);
            return itemBuilder;
        }

        public ItemBuilder<E> and() {
            ItemBuilder<E> itemBuilder = new ItemBuilder<>(this);
            itemBuilders.put(itemBuilders.size(), itemBuilder);
            return itemBuilder;
        }

        public SimpleRvAdapter2<E> build() {
            return new SimpleRvAdapter2<>(this.datas, this.itemBuilders);
        }
    }

    public static final class ItemBuilder<E> {
        private Builder<E> builder;
        //
        @LayoutRes
        private int layoutResId;
        private int itemWidth;
        private int itemHeight;
        private Converter<E> converter;
        private Applyer<E> applyer;
        private OnItemClickListener onItemClickListener;
        private OnItemLongClickListener onItemLongClickListener;

        private ItemBuilder(Builder<E> builder) {
            this.builder = builder;
        }

        public ItemBuilder<E> itemLayout(@LayoutRes int layoutResId) {
            this.layoutResId = layoutResId;
            return this;
        }

        public ItemBuilder<E> itemOverrideWidth(int itemWidth) {
            this.itemWidth = itemWidth;
            return this;
        }

        public ItemBuilder<E> itemOverrideHeight(int itemHeight) {
            this.itemHeight = itemHeight;
            return this;
        }

        public ItemBuilder<E> itemConvert(Converter<E> converter) {
            this.converter = converter;
            return this;
        }

        public ItemBuilder<E> itemApply(Applyer<E> applyer) {
            this.applyer = applyer;
            return this;
        }

        public ItemBuilder<E> itemOnClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
            return this;
        }

        public ItemBuilder<E> itemOnLongClickListener(OnItemLongClickListener onItemLongClickListener) {
            this.onItemLongClickListener = onItemLongClickListener;
            return this;
        }

        public ItemBuilder<E> and(int itemType) {
            return builder.and(itemType);
        }

        public ItemBuilder<E> and() {
            return builder.and();
        }

        public SimpleRvAdapter2<E> build() {
            return builder.build();
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

    public interface Applyer<T> {
        boolean apply(@NonNull T data, int position);
    }


}
