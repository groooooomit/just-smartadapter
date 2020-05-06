package just.smartadapter.core;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import just.smartadapter.SmartAdapter;

/**
 * 数据源执行操作后，同时触发 adapter 更新，采用装饰器模式
 */
public class AutoNotifyDataSource<E> implements SmartAdapter.DataSource<E> {

    @NonNull
    private final SmartAdapter.DataSource<E> originDataSource;

    @NonNull
    private final List<AdapterNotifierOwner> adapterNotifierOwners = new ArrayList<>();

    public AutoNotifyDataSource(@NonNull SmartAdapter.DataSource<E> originDataSource) {
        this.originDataSource = originDataSource;
    }

    @Override
    public void onAttach(@NonNull AdapterNotifierOwner owner) {
        adapterNotifierOwners.add(owner);
    }

    @Override
    public void onDetach(@NonNull AdapterNotifierOwner owner) {
        adapterNotifierOwners.remove(owner);
    }

    @Override
    public int size() {
        return originDataSource.size();
    }

    @NonNull
    @Override
    public E get(int position) {
        return originDataSource.get(position);
    }

    @NonNull
    @Override
    public Collection<E> getAll() {
        return originDataSource.getAll();
    }

    @Override
    public int positionOf(@NonNull E element) {
        return originDataSource.positionOf(element);
    }

    @Override
    public void refresh(@NonNull E element) {
        originDataSource.refresh(element);
        final int position = originDataSource.positionOf(element);
        traversalAdapterNotifierOwners(it -> it.getNotifier().notifyItemChanged(position));
    }

    @Override
    public void refresh(int position) {
        originDataSource.refresh(position);
        traversalAdapterNotifierOwners(it -> it.getNotifier().notifyItemChanged(position));
    }

    @Override
    public void set(int position, @NonNull E newElement) {
        originDataSource.set(position, newElement);
        traversalAdapterNotifierOwners(it -> it.getNotifier().notifyItemChanged(position));
    }

    @Override
    public void add(@NonNull E element) {
        final int size = originDataSource.size();
        originDataSource.add(element);
        traversalAdapterNotifierOwners(it -> it.getNotifier().notifyItemInserted(size));
    }

    @Override
    public void add(int position, @NonNull E element) {
        originDataSource.add(position, element);
        traversalAdapterNotifierOwners(it -> it.getNotifier().notifyItemInserted(position));
    }

    @Override
    public void addAll(@NonNull Collection<? extends E> collection) {
        final int size = originDataSource.size();
        originDataSource.addAll(collection);
        traversalAdapterNotifierOwners(it -> it.getNotifier().notifyItemRangeInserted(size, collection.size()));
    }

    @Override
    public void addAll(int position, @NonNull Collection<? extends E> collection) {
        originDataSource.addAll(position, collection);
        traversalAdapterNotifierOwners(it -> it.getNotifier().notifyItemRangeInserted(position, collection.size()));/* 追加数据 RecyclerView 会自己滚动到底部. */
    }

    @Override
    public void replace(@NonNull Collection<? extends E> collection) {
        originDataSource.replace(collection);
        traversalAdapterNotifierOwners(it -> it.getNotifier().notifyDataSetChanged());/* 替换数据 RecyclerView 不会自己滚动到底部. */
    }

    @Override
    public void remove(@NonNull E element) {
        final int position = originDataSource.positionOf(element);
        originDataSource.remove(element);
        traversalAdapterNotifierOwners(it -> it.getNotifier().notifyItemRemoved(position));
    }

    @Override
    public void removeAt(int position) {
        originDataSource.removeAt(position);
        traversalAdapterNotifierOwners(it -> it.getNotifier().notifyItemRemoved(position));
    }

    @Override
    public void removeIf(@NonNull SmartAdapter.ItemTypePredicate<? super E> filter) {
        originDataSource.removeIf(filter);
        traversalAdapterNotifierOwners(it -> it.getNotifier().notifyDataSetChanged());
    }

    @Override
    public void move(int fromPosition, int toPosition) {
        originDataSource.move(fromPosition, toPosition);
        traversalAdapterNotifierOwners(it -> it.getNotifier().notifyItemMoved(fromPosition, toPosition));
    }

    @Override
    public void move(E element, int toPosition) {
        int fromPosition = positionOf(element);
        originDataSource.move(element, toPosition);
        traversalAdapterNotifierOwners(it -> it.getNotifier().notifyItemMoved(fromPosition, toPosition));
    }

    @Override
    public void clear() {
        originDataSource.clear();
        traversalAdapterNotifierOwners(it -> it.getNotifier().notifyDataSetChanged());
    }

    @FunctionalInterface
    private interface Consumer {
        void accept(@NonNull AdapterNotifierOwner owner);
    }

    /**
     * 遍历 AdapterNotifierOwner 集合
     */
    private void traversalAdapterNotifierOwners(@NonNull Consumer consumer) {
        for (AdapterNotifierOwner owner : adapterNotifierOwners) {
            consumer.accept(owner);
        }
    }
}
