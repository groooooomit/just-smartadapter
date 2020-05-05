package just.smartadapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.SparseArrayCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import just.smartadapter.core.AdapterNotifier;
import just.smartadapter.core.AdapterNotifierOwner;
import just.smartadapter.core.CommonViewHolder;
import just.smartadapter.core.FixedAdapterNotifier;

/**
 * 多 Item 类型支持的 adapter
 */
public final class SmartAdapter<E> extends RecyclerView.Adapter<CommonViewHolder> implements AdapterNotifierOwner {

    @NonNull
    private final DataSource<E> dataSource;

    @NonNull
    private final SparseArrayCompat<TypeParam<E>> typeParams = new SparseArrayCompat<>();

    public SmartAdapter(@NonNull DataSource<E> dataSource, @NonNull List<TypeParam<E>> typeParams) {
        this.dataSource = dataSource;

        /* 将 TypeParam 列表转换成 type 作为 key 的 map. */
        for (TypeParam<E> typeParam : typeParams) {
            this.typeParams.put(typeParam.type, typeParam);
        }
    }

    @Override
    public int getItemCount() {
        return dataSource.size();
    }

    @Override
    public int getItemViewType(int position) {
        /* 遍历 typeParams 以获取对应 position 位置的 item 的 type. */
        final int size = typeParams.size();
        for (int i = 0; i < size; i++) {
            final TypeParam<E> typeParam = typeParams.valueAt(i);
            final ItemTypePredicate<E> filter = typeParam.itemTypePredicate;
            if (null != filter) {
                final E data = this.dataSource.get(position);
                if (filter.test(data, position)) {
                    return typeParams.keyAt(i);
                }
            }
        }
        throw new IllegalArgumentException("No item type matched. position = " + position);
    }

    @NonNull
    @Override
    public CommonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        /* 获取对应 viewType 的 typeParam. */
        final TypeParam<E> typeParam = Objects.requireNonNull(typeParams.get(viewType));

        /* 创建 CommonViewHolder. */
        final CommonViewHolder viewHolder = CommonViewHolder.fromInflateLayout(parent.getContext(), typeParam.layoutResId, parent);

        /* 修改 ItemView 的宽高尺寸. */
        overrideItemSIze(parent, viewHolder, typeParam.itemWidth, typeParam.itemHeight);

        return viewHolder;
    }

    private void overrideItemSIze(@NonNull ViewGroup parent, @NonNull CommonViewHolder viewHolder, @Nullable ItemSizeRefinery itemSizeRefineryForWidth, @Nullable ItemSizeRefinery itemSizeRefineryForHeight) {
        /* 获取 ItemView 所在 RecyclerView 容器的宽高. */
        final int parentWidth = parent.getMeasuredWidth();
        final int parentHeight = parent.getMeasuredHeight();

        /* 获取 ItemView 原本自己的宽高. */
        final ViewGroup.LayoutParams layoutParams = viewHolder.itemView.getLayoutParams();
        final int originWidth = layoutParams.width;
        final int originHeight = layoutParams.height;

        /* 根据 itemSizeRefineryForWidth 计算 ItemView 最终的宽度. */
        if (null != itemSizeRefineryForWidth) {
            layoutParams.width = itemSizeRefineryForWidth.getSize(originWidth, originHeight, parentWidth, parentHeight);
        }

        /* 根据 itemSizeRefineryForHeight 计算 itemSizeRefineryForHeight 最终的高度. */
        if (null != itemSizeRefineryForHeight) {
            layoutParams.height = itemSizeRefineryForHeight.getSize(originWidth, originHeight, parentWidth, parentHeight);
        }

        /* 跟新 layoutParams. */
        viewHolder.itemView.setLayoutParams(layoutParams);
    }

    @Override
    public void onBindViewHolder(@NonNull CommonViewHolder holder, int position) {
        /* 记录下绑定时的位置信息，因为一些扩展的 adapter 会导致 position 整体偏移，例如 添加了 Header 或 Footer 后，坐标就不准确了. */
        holder.calculatePositionOffset(position);

        final int itemViewType = getItemViewType(position);
        final TypeParam<E> typeParam = Objects.requireNonNull(typeParams.get(itemViewType));
        final ViewHolderBinder<E> binder = typeParam.viewHolderBinder;

        if (null != binder) {
            final E data = this.dataSource.get(position);
            binder.convert(holder, data, position, itemViewType, this);
        }

        /*
         * 设置点击事件
         */
        final OnItemClickListener<E> onItemClickListener = typeParam.onItemClickListener;
        if (null != onItemClickListener) {
            holder.itemView.setOnClickListener(v -> {
                /* 点击事件使用修正后的 layoutPosition  */
                /* 当场景为点击一个 Item 然后删除这个 Item 时，如果使用 adapterPosition，那么 adapterPosition 更新很快，这样 Item 移除动画尚未结束时如果再次触发了，那么会错误地触发该 Item 相邻的 Item 删除，所以点击事件需要使用 layoutPosition. */
                /* layoutPosition 在动画完成后更新，所以需要进行 size 大小判断，否则在删除尾部元素的场景中很容易就出现数组越界. */
                /* 还应该对点击过快进行限制. */
                final int fixedLayoutPosition = holder.getFixedLayoutPosition();
                if (fixedLayoutPosition >= 0 && fixedLayoutPosition < getItemCount()) {
                    onItemClickListener.onItemClick(dataSource.get(fixedLayoutPosition), fixedLayoutPosition, holder.getLayoutPosition(), itemViewType, v, this);
                }
            });
        }

        /* 设置长按事件. */
        final OnItemLongClickListener<E> onItemLongClickListener = typeParam.onItemLongClickListener;
        if (null != onItemLongClickListener) {
            holder.itemView.setOnLongClickListener(v -> {
                final int fixedLayoutPosition = holder.getFixedLayoutPosition();
                if (fixedLayoutPosition >= 0 && fixedLayoutPosition < getItemCount()) {
                    return onItemLongClickListener.onItemLongClick(dataSource.get(fixedLayoutPosition), fixedLayoutPosition, holder.getLayoutPosition(), itemViewType, v, this);
                } else {
                    return false;
                }
            });
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        this.dataSource.onAttach(this);
        checkGridSpanSize(recyclerView);
    }

    /**
     * 重设 GridLayoutManager spanSize.
     * <p>
     * 注意，RecyclerView 要先设置 LayoutManager，再设置 Adapter，此方法才能生效
     */
    private void checkGridSpanSize(@NonNull RecyclerView recyclerView) {
        final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if ((layoutManager instanceof GridLayoutManager)) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            final int spanCount = gridLayoutManager.getSpanCount();
            final GridLayoutManager.SpanSizeLookup spanSizeLookup = gridLayoutManager.getSpanSizeLookup();
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    /* 通过 position 获取 itemViewType. */
                    final int itemViewType = getItemViewType(position);

                    /* 通过 ItemViewType 获取 viewType 对应的参数配置. */
                    final int gridSpanSize = Objects.requireNonNull(typeParams.get(itemViewType)).gridSpanSize;

                    /* 计算 gridSpanSize。 */
                    if (gridSpanSize > 0) {
                        return Math.min(gridSpanSize, spanCount);
                    } else {
                        return spanSizeLookup.getSpanSize(position);
                    }
                }
            });
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        this.dataSource.onDetach(this);
    }

    /**
     * 获取数据源
     */
    @NonNull
    public DataSource<E> getDataSource() {
        return dataSource;
    }

    ///////////////////////////////////////////////////////////////////////////
    // 实现 AdapterNotifierOwner, 修复 notifyXXX 方法的位置错乱问题
    ///////////////////////////////////////////////////////////////////////////

    @NonNull
    private final AdapterNotifier adapterNotifier = new FixedAdapterNotifier(this);

    /**
     * 代替 adapter 原先的 notifyXXX 方法，因为原先的 notify 方法有位置错乱问题
     */
    @NonNull
    @Override
    public AdapterNotifier getNotifier() {
        return adapterNotifier;
    }

    ///////////////////////////////////////////////////////////////////////////
    // static component
    ///////////////////////////////////////////////////////////////////////////


    /**
     * Adapter 数据源
     *
     * @param <E>
     */
    public interface DataSource<E> {

        /**
         * 关联 Adapter
         */
        void onAttach(@NonNull AdapterNotifierOwner owner);

        /**
         * 与 Adapter 解除关联
         */
        void onDetach(@NonNull AdapterNotifierOwner owner);

        /**
         * Item 总数
         */
        int size();

        /**
         * 获取指定位置的 Item
         */
        @NonNull
        E get(int position);

        /**
         * 获取所有 Item 集合
         */
        @NonNull
        Collection<E> getAll();

        /**
         * 获取指定 Item 所在位置
         */
        int positionOf(@NonNull E element);

        /**
         * 刷新指定元素对应的 viewHolder，即再次执行数据和 View 的绑定
         */
        void refresh(@NonNull E element);

        /**
         * 刷新指定位置的 viewHolder，即再次执行数据和 View 的绑定
         */
        void refresh(int position);

        // modify method

        /**
         * 跟新指定位置的 item
         */
        void set(int position, @NonNull E newElement);

        /**
         * 添加一个元素
         */
        void add(@NonNull E element);

        /**
         * 指定位置添加一个元素
         */
        void add(int position, @NonNull E element);

        /**
         * 添加多个元素
         */
        void addAll(@NonNull Collection<? extends E> collection);

        /**
         * 指定位置添加多个元素
         */
        void addAll(int position, @NonNull Collection<? extends E> collection);

        /**
         * 替换当前数据源的所有 item
         */
        void replace(@NonNull Collection<? extends E> collection);

        /**
         * 移除指定 item
         */
        void remove(@NonNull E element);

        /**
         * 移除指定位置 item
         */
        void removeAt(int position);

        /**
         * 移除符合条件的 item
         */
        void removeIf(@NonNull ItemTypePredicate<? super E> filter);

        /**
         * 根据位置移动 item
         */
        void move(int fromPosition, int toPosition);

        /**
         * 将 item 移动到指定位置
         */
        void move(E element, int toPosition);

        /**
         * 清空
         */
        void clear();

    }

    /**
     * 数据过滤，哪些数据显示到该类型的 view 中.
     */
    public interface ItemTypePredicate<T> {
        boolean test(@NonNull T data, int position);
    }

    /**
     * item 单击事件监听器
     */
    public interface OnItemClickListener<E> {
        /**
         * 点击事件处理
         *
         * @param data           data
         * @param position   data 在数据源中的位置
         * @param layoutPosition data 对应 ItemView 在界面上的位置
         * @param type           data 对应的 ItemViewType
         * @param view           data 对应的 ItemView
         * @param adapter        adapter
         */
        void onItemClick(@NonNull E data, int position, int layoutPosition, int type, @NonNull View view, @NonNull SmartAdapter<E> adapter);
    }

    /**
     * item 长按事件监听器
     *
     * @param <E>
     */
    public interface OnItemLongClickListener<E> {
        /**
         * 点击事件处理
         *
         * @param data           data
         * @param position   data 在数据源中的位置
         * @param layoutPosition data 对应 ItemView 在界面上的位置
         * @param type           data 对应的 ItemViewType
         * @param view           data 对应的 ItemView
         * @param adapter        adapter
         */
        boolean onItemLongClick(@NonNull E data, int position, int layoutPosition, int type, @NonNull View view, @NonNull SmartAdapter<E> adapter);
    }


    /**
     * 依据 ItemView 原始大小和 RecyclerView 的宽高确定最终大小
     */
    public interface ItemSizeRefinery {

        /**
         * 根据原始的 size 和 RecyclerView 的 size，决定当前 Item 的 size
         *
         * @param originWidth  itemView 原始宽度
         * @param originHeight itemView 原始高度
         * @param parentWidth  recyclerView 宽度
         * @param parentHeight recyclerView 高度
         */
        int getSize(int originWidth, int originHeight, int parentWidth, int parentHeight);

    }

    /**
     * 数据绑定，将数据显示到该类型的 viewHolder
     */
    public interface ViewHolderBinder<E> {
        void convert(@NonNull CommonViewHolder viewHolder, @NonNull E data, int position, int type, @NonNull SmartAdapter<E> adapter);
    }

    /**
     * 聚合 Item 参数
     */
    public static final class TypeParam<E> {

        /**
         * ItemViewType 类型
         */
        private final int type;

        /**
         * 布局资源 ID
         */
        @LayoutRes
        private final int layoutResId;

        /**
         * Item 宽度调整
         */
        @Nullable
        private final ItemSizeRefinery itemWidth;

        /**
         * Item 高度调整
         */
        @Nullable
        private final ItemSizeRefinery itemHeight;

        /**
         * 将数据与 viewHolder 绑定
         */
        @Nullable
        private final ViewHolderBinder<E> viewHolderBinder;

        /**
         * 判断哪些数据应适用于当前 type
         */
        @Nullable
        private final ItemTypePredicate<E> itemTypePredicate;

        /**
         * item 单击事件回调
         */
        @Nullable
        private final SmartAdapter.OnItemClickListener<E> onItemClickListener;

        /**
         * item 长按事件回调
         */
        @Nullable
        private final SmartAdapter.OnItemLongClickListener<E> onItemLongClickListener;

        /**
         * grid 布局下的 spanSize
         */
        private final int gridSpanSize;

        public TypeParam(
                int type,
                @LayoutRes int layoutResId,
                @Nullable ItemSizeRefinery itemWidth,
                @Nullable ItemSizeRefinery itemHeight,
                @Nullable ViewHolderBinder<E> viewHolderBinder,
                @Nullable ItemTypePredicate<E> itemTypePredicate,
                @Nullable SmartAdapter.OnItemClickListener<E> onItemClickListener,
                @Nullable SmartAdapter.OnItemLongClickListener<E> onItemLongClickListener,
                int gridSpanSize) {
            this.type = type;
            this.layoutResId = layoutResId;
            this.itemWidth = itemWidth;
            this.itemHeight = itemHeight;
            this.viewHolderBinder = viewHolderBinder;
            this.itemTypePredicate = itemTypePredicate;
            this.onItemClickListener = onItemClickListener;
            this.onItemLongClickListener = onItemLongClickListener;
            this.gridSpanSize = gridSpanSize;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // builder
    ///////////////////////////////////////////////////////////////////////////

    public static <E> SmartAdapterBuilder<E> newBuilder(@NonNull SmartAdapter.DataSource<E> dataSource) {
        return new SmartAdapterBuilder<>(dataSource);
    }

    public static final class SmartAdapterBuilder<E> {

        @NonNull
        private final SmartAdapter.DataSource<E> dataSource;

        @NonNull
        private final List<TypeParamBuilder<E>> typeParamBuilders = new LinkedList<>();

        private SmartAdapterBuilder(@NonNull SmartAdapter.DataSource<E> dataSource) {
            this.dataSource = dataSource;
        }

        public TypeParamBuilder<E> type(int itemType) {
            final TypeParamBuilder<E> typeParamBuilder = new TypeParamBuilder<>(this, itemType);
            typeParamBuilders.add(typeParamBuilder);
            return typeParamBuilder;
        }

        public TypeParamBuilder<E> singleType() {
            return type().filter((data, position) -> true);
        }

        public TypeParamBuilder<E> type() {
            /* 使用 负数. */
            final int itemType = -typeParamBuilders.size();
            return type(itemType);
        }

        @NonNull
        public SmartAdapter<E> build() {
            final List<SmartAdapter.TypeParam<E>> typeParams = new ArrayList<>(this.typeParamBuilders.size());
            for (TypeParamBuilder<E> typeParamBuilder : this.typeParamBuilders) {
                final SmartAdapter.TypeParam<E> typeParam = typeParamBuilder.buildTypeParam();
                typeParams.add(typeParam);
            }
            return new SmartAdapter<>(dataSource, typeParams);
        }
    }

    /**
     * 特定类型的 Builder
     */
    public static final class TypeParamBuilder<E> {
        @NonNull
        private final SmartAdapterBuilder<E> master;

        private final int type;

        @LayoutRes
        private int layoutResId;

        @Nullable
        private SmartAdapter.ItemSizeRefinery itemWidth;

        @Nullable
        private SmartAdapter.ItemSizeRefinery itemHeight;

        @Nullable
        private SmartAdapter.ViewHolderBinder<E> viewHolderBinder;

        @Nullable
        private SmartAdapter.ItemTypePredicate<E> itemTypePredicate;

        @Nullable
        private SmartAdapter.OnItemClickListener<E> onItemClickListener;

        @Nullable
        private SmartAdapter.OnItemLongClickListener<E> onItemLongClickListener;

        private int gridSpanSize;

        private TypeParamBuilder(@NonNull SmartAdapterBuilder<E> master, int itemType) {
            this.master = master;
            this.type = itemType;
        }

        public TypeParamBuilder<E> layout(@LayoutRes int layoutResId) {
            this.layoutResId = layoutResId;
            return this;
        }

        public TypeParamBuilder<E> overrideWidth(@Nullable SmartAdapter.ItemSizeRefinery itemWidth) {
            this.itemWidth = itemWidth;
            return this;
        }

        public TypeParamBuilder<E> overrideHeight(@Nullable SmartAdapter.ItemSizeRefinery itemHeight) {
            this.itemHeight = itemHeight;
            return this;
        }

        public TypeParamBuilder<E> overrideWidth(int itemWidth) {
            return overrideWidth((originWidth, originHeight, parentWidth, parentHeight) -> itemWidth);
        }

        public TypeParamBuilder<E> overrideHeight(int itemHeight) {
            return overrideHeight((originWidth, originHeight, parentWidth, parentHeight) -> itemHeight);
        }

        public TypeParamBuilder<E> onBind(@Nullable SmartAdapter.ViewHolderBinder<E> viewHolderBinder) {
            this.viewHolderBinder = viewHolderBinder;
            return this;
        }

        public TypeParamBuilder<E> filter(@Nullable SmartAdapter.ItemTypePredicate<E> itemTypePredicate) {
            this.itemTypePredicate = itemTypePredicate;
            return this;
        }

        public TypeParamBuilder<E> onItemClick(@Nullable SmartAdapter.OnItemClickListener<E> onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
            return this;
        }

        public TypeParamBuilder<E> onItemLongClick(@Nullable SmartAdapter.OnItemLongClickListener<E> onItemLongClickListener) {
            this.onItemLongClickListener = onItemLongClickListener;
            return this;
        }

        public TypeParamBuilder<E> gridSpanSize(int gridSpanSize) {
            this.gridSpanSize = gridSpanSize;
            return this;
        }

        public TypeParamBuilder<E> type(int itemType) {
            return master.type(itemType);
        }

        public TypeParamBuilder<E> type() {
            return master.type();
        }

        @NonNull
        public SmartAdapter<E> build() {
            return master.build();
        }

        @NonNull
        private SmartAdapter.TypeParam<E> buildTypeParam() {
            return new SmartAdapter.TypeParam<>(
                    this.type,
                    this.layoutResId,
                    this.itemWidth,
                    this.itemHeight,
                    this.viewHolderBinder,
                    this.itemTypePredicate,
                    this.onItemClickListener,
                    this.onItemLongClickListener,
                    this.gridSpanSize);
        }
    }

}
