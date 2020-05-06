package just.smartadapter.util;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

/**
 * RecyclerView 工具类
 */
public final class RecyclerViews {

    private RecyclerViews() {
        throw new RuntimeException("RecyclerViewUtils cannot be instantiated");
    }

    /**
     * 当 adapter notifyItemChange 时，去掉 Item 上白光一闪的效果
     */
    public static void disableWhiteLight(@NonNull RecyclerView recyclerView) {
        final RecyclerView.ItemAnimator itemAnimator = recyclerView.getItemAnimator();
        if (itemAnimator instanceof DefaultItemAnimator) {
            final DefaultItemAnimator defaultItemAnimator = (DefaultItemAnimator) itemAnimator;
            defaultItemAnimator.setSupportsChangeAnimations(false);
        }
    }


}
