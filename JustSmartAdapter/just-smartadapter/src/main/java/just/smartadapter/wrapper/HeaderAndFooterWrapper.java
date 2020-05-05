package just.smartadapter.wrapper;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.SparseArrayCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;


public class HeaderAndFooterWrapper extends AdapterWrapper {
    private static final int BASE_ITEM_TYPE_HEADER = -10000;
    private static final int BASE_ITEM_TYPE_FOOTER = -20000;

    // header set
    private SparseArrayCompat<View> headers = new SparseArrayCompat<>();
    // footer set
    private SparseArrayCompat<View> footers = new SparseArrayCompat<>();

    public HeaderAndFooterWrapper(@NonNull RecyclerView.Adapter originAdapter) {
        super(originAdapter);
    }

    ///////////////////////////////////////////////////////////////////////////

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View header = headers.get(viewType);
        if (null != header) {
            return new SimpleViewHolder(header);
        }
        final View footer = footers.get(viewType);
        if (null != footer) {
            return new SimpleViewHolder(footer);
        }
        return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (!isHeaderViewPos(position) && !isFooterViewPos(position)) {
            super.onBindViewHolder(holder, calculateOriginPosition(position));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isHeaderViewPos(position)) {
            return headers.keyAt(position);
        }
        if (isFooterViewPos(position)) {
            return footers.keyAt(calculateOriginPosition(position) - getOriginItemCount());
        }
        return super.getItemViewType(calculateOriginPosition(position));
    }

    private int calculateOriginPosition(int position) {
        return position - getHeaderCount();
    }


    @Override
    public int getItemCount() {
        final int headerCount = getHeaderCount();
        final int footerCount = getFooterCount();
        final int originItemCount = getOriginItemCount();
        return headerCount + footerCount + originItemCount;
    }

    ///////////////////////////////////////////////////////////////////////////
    // 兼容 grid layout
    ///////////////////////////////////////////////////////////////////////////


    /**
     * 兼容 grid layout
     */
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        /* 让原始的 adapter 执行 onAttachedToRecyclerView.  */
        super.onAttachedToRecyclerView(recyclerView);

        /* 对 gridLayoutManager 进行适配. */
        final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            final GridLayoutManager.SpanSizeLookup originSpanSizeLookup = gridLayoutManager.getSpanSizeLookup();
            final int spanCount = gridLayoutManager.getSpanCount();
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    // header 或 footer 占满一整行
                    if (isHeaderViewPos(position) || isFooterViewPos(position)) {
                        return spanCount;
                    } else {
                        return null != originSpanSizeLookup ? originSpanSizeLookup.getSpanSize(calculateOriginPosition(position)) : 1;
                    }
                }
            });
        }
    }


    /**
     * 兼容 StaggeredGridLayoutManager
     */
    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        //noinspection unchecked
        super.onViewAttachedToWindow(holder);
        final int position = holder.getAdapterPosition();
        if (isHeaderViewPos(position) || isFooterViewPos(position)) {
            // 如果是 header 或 footer 的位置，设置占满整行
            final ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
            if (layoutParams instanceof StaggeredGridLayoutManager.LayoutParams) {
                StaggeredGridLayoutManager.LayoutParams sgLayoutParams = (StaggeredGridLayoutManager.LayoutParams) layoutParams;
                sgLayoutParams.setFullSpan(true);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // 修正 notify 的 position
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onOriginItemRangeChanged(int positionStart, int itemCount) {
        super.onOriginItemRangeChanged(positionStart + getHeaderCount(), itemCount);
    }

    @Override
    public void onOriginItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
        super.onOriginItemRangeChanged(positionStart + getHeaderCount(), itemCount, payload);
    }

    @Override
    public void onOriginItemRangeInserted(int positionStart, int itemCount) {
        super.onOriginItemRangeInserted(positionStart + getHeaderCount(), itemCount);
    }

    @Override
    public void onOriginItemRangeRemoved(int positionStart, int itemCount) {
        super.onOriginItemRangeRemoved(positionStart + getHeaderCount(), itemCount);
    }

    @Override
    public void onOriginItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
        super.onOriginItemRangeMoved(fromPosition + getHeaderCount(), toPosition + getHeaderCount(), itemCount);
    }

    ///////////////////////////////////////////////////////////////////////////

    /**
     * 判断对应的位置是不是 header
     */
    private boolean isHeaderViewPos(int position) {
        return position < getHeaderCount();
    }

    /**
     * 判断对应的位置是不是 footer
     */
    private boolean isFooterViewPos(int position) {
        return position >= getHeaderCount() + getOriginItemCount();
    }

    /**
     * 获取 header 的个数
     */
    private int getHeaderCount() {
        return headers.size();
    }

    /**
     * 获取 footer 的个数
     */
    private int getFooterCount() {
        return footers.size();
    }

    /**
     * 获取原始的 item 的 Count
     */
    private int getOriginItemCount() {
        return getOriginAdapter().getItemCount();
    }

    /**
     * 添加 header
     */
    public void addHeader(@NonNull View view) {
        final int viewType = BASE_ITEM_TYPE_HEADER - getHeaderCount();
        headers.put(viewType, view);
    }

    /**
     * 添加 footer
     */
    public void addFooter(@NonNull View view) {
        final int viewType = BASE_ITEM_TYPE_FOOTER - getFooterCount();
        footers.put(viewType, view);
    }

    ///////////////////////////////////////////////////////////////////////////

    private static final class SimpleViewHolder extends RecyclerView.ViewHolder {
        SimpleViewHolder(View itemView) {
            super(itemView);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
}
