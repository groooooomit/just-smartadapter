package just.smartadapter.wrapper;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;


/**
 * adapter 包装类
 */
public abstract class AdapterWrapper extends RecyclerView.Adapter implements DeliverAdapterDataObserver.DataObserver {

    @NonNull
    private final RecyclerView.Adapter originAdapter;

    /**
     * 将 origin adapter 的 notifyXXX 传递给 wrapper
     */
    private final DeliverAdapterDataObserver deliverAdapterDataObserver = new DeliverAdapterDataObserver(this);

    public AdapterWrapper(@NonNull RecyclerView.Adapter originAdapter) {
        this.originAdapter = originAdapter;
    }

    @NonNull
    protected final RecyclerView.Adapter getOriginAdapter() {
        return originAdapter;
    }

    ///////////////////////////////////////////////////////////////////////////
    // override
    ///////////////////////////////////////////////////////////////////////////

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return originAdapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        //noinspection unchecked
        originAdapter.onBindViewHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        return originAdapter.getItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        return originAdapter.getItemViewType(position);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        originAdapter.onAttachedToRecyclerView(recyclerView);
        originAdapter.registerAdapterDataObserver(deliverAdapterDataObserver);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        originAdapter.onDetachedFromRecyclerView(recyclerView);
        originAdapter.unregisterAdapterDataObserver(deliverAdapterDataObserver);
    }

    ///////////////////////////////////////////////////////////////////////////
    // implements DeliverAdapterDataObserver.DataObserver
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onOriginChanged() {
        this.notifyDataSetChanged();
    }

    @Override
    public void onOriginItemRangeChanged(int positionStart, int itemCount) {
        this.notifyItemRangeChanged(positionStart, itemCount);
    }

    @Override
    public void onOriginItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
        this.notifyItemRangeChanged(positionStart, itemCount, payload);
    }

    @Override
    public void onOriginItemRangeInserted(int positionStart, int itemCount) {
        this.notifyItemRangeInserted(positionStart, itemCount);
    }

    @Override
    public void onOriginItemRangeRemoved(int positionStart, int itemCount) {
        this.notifyItemRangeRemoved(positionStart, itemCount);
    }

    @Override
    public void onOriginItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
        this.notifyItemMoved(fromPosition, toPosition);
    }
}
