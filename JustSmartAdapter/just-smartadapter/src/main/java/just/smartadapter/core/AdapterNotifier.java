package just.smartadapter.core;

import androidx.annotation.Nullable;

/**
 * Adapter 的 notifyXXX 方法聚拢，原始的 Adapter notifyXXX 方法都是不可被 Override 的
 */
public interface AdapterNotifier {

    void notifyDataSetChanged();

    void notifyItemChanged(int position);

    void notifyItemChanged(int position, @Nullable Object payload);

    void notifyItemRangeChanged(int positionStart, int itemCount);

    void notifyItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload);

    void notifyItemMoved(int fromPosition, int toPosition);

    void notifyItemInserted(int position);

    void notifyItemRangeInserted(int positionStart, int itemCount);

    void notifyItemRemoved(int position);

    void notifyItemRangeRemoved(int positionStart, int itemCount);
}
