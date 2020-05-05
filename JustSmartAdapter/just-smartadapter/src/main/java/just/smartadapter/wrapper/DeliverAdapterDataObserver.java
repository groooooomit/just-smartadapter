package just.smartadapter.wrapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public final class DeliverAdapterDataObserver extends RecyclerView.AdapterDataObserver {

    @NonNull
    private final DataObserver dataObserver;

    public DeliverAdapterDataObserver(@NonNull DataObserver dataObserver) {
        this.dataObserver = dataObserver;
    }

    @Override
    public final void onChanged() {
        dataObserver.onOriginChanged();
    }

    @Override
    public final void onItemRangeChanged(int positionStart, int itemCount) {
        dataObserver.onOriginItemRangeChanged(positionStart, itemCount);
    }

    @Override
    public final void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
        dataObserver.onOriginItemRangeChanged(positionStart, itemCount, payload);
    }

    @Override
    public final void onItemRangeInserted(int positionStart, int itemCount) {
        dataObserver.onOriginItemRangeInserted(positionStart, itemCount);
    }

    @Override
    public final void onItemRangeRemoved(int positionStart, int itemCount) {
        dataObserver.onOriginItemRangeRemoved(positionStart, itemCount);
    }

    @Override
    public final void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
        dataObserver.onOriginItemRangeMoved(fromPosition, toPosition, itemCount);
    }

    public interface DataObserver {

        void onOriginChanged();

        void onOriginItemRangeChanged(int positionStart, int itemCount);

        void onOriginItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload);

        void onOriginItemRangeInserted(int positionStart, int itemCount);

        void onOriginItemRangeRemoved(int positionStart, int itemCount);

        void onOriginItemRangeMoved(int fromPosition, int toPosition, int itemCount);
    }
}
