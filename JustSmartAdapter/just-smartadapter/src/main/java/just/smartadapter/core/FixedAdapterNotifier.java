package just.smartadapter.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import just.smartadapter.core.AdapterNotifier;

/**
 * 修正 adapter notifyXXX 的问题
 */
public final class FixedAdapterNotifier implements AdapterNotifier {

    @NonNull
    private final RecyclerView.Adapter adapter;

    public FixedAdapterNotifier(@NonNull RecyclerView.Adapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void notifyDataSetChanged() {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void notifyItemChanged(int position) {
        adapter.notifyItemChanged(position);
    }

    @Override
    public void notifyItemChanged(int position, @Nullable Object payload) {
        adapter.notifyItemChanged(position, payload);
    }

    @Override
    public void notifyItemRangeChanged(int positionStart, int itemCount) {
        adapter.notifyItemRangeChanged(positionStart, itemCount);
    }

    @Override
    public void notifyItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
        adapter.notifyItemRangeChanged(positionStart, itemCount, payload);
    }

    @Override
    public void notifyItemMoved(int fromPosition, int toPosition) {
        if (fromPosition != toPosition) {
            adapter.notifyItemMoved(fromPosition, toPosition);
            final int min = Math.min(fromPosition, toPosition);
            final int max = Math.max(fromPosition, toPosition);
            adapter.notifyItemRangeChanged(min, max - min + 1);
        }
    }

    @Override
    public void notifyItemInserted(int position) {
        adapter.notifyItemInserted(position);
        if (isPartAffected(position)) {
            adapter.notifyItemRangeChanged(position, size() - position);
        }
    }

    @Override
    public void notifyItemRangeInserted(int positionStart, int itemCount) {
        adapter.notifyItemRangeInserted(positionStart, itemCount);
        if (isPartAffected(positionStart)) {
            adapter.notifyItemRangeChanged(positionStart, size() - positionStart);
        }
    }

    @Override
    public void notifyItemRemoved(int position) {
        adapter.notifyItemRemoved(position);
        if (isPartAffected(position)) {
            adapter.notifyItemRangeChanged(position, size() - position);
        }
    }

    @Override
    public void notifyItemRangeRemoved(int positionStart, int itemCount) {
        adapter.notifyItemRangeRemoved(positionStart, itemCount);
        if (isPartAffected(positionStart)) {
            adapter.notifyItemRangeChanged(positionStart, size() - positionStart);
        }
    }

    /**
     * 判断是否是局部受影响
     */
    private boolean isPartAffected(int position) {
        return position != size() - 1;
    }

    /**
     * 元素总个数
     */
    private int size() {
        return adapter.getItemCount();
    }

}
