package just.smartadapter.core;

import androidx.annotation.NonNull;

/**
 * AdapterNotifier 提供者
 */
public interface AdapterNotifierOwner {

    @NonNull
    AdapterNotifier getNotifier();
}
