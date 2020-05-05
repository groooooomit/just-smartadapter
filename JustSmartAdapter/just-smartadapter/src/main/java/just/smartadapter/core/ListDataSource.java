package just.smartadapter.core;

import androidx.annotation.NonNull;

import java.util.Collection;
import java.util.List;

import just.smartadapter.SmartAdapter;

/**
 * 使用 List 集合实现 adapter 数据源
 *
 * @param <E>
 */
public class ListDataSource<E> implements SmartAdapter.DataSource<E> {

    @NonNull
    private final List<E> list;

    public ListDataSource(@NonNull List<E> list) {
        this.list = list;
    }

    @Override
    public void onAttach(@NonNull AdapterNotifierOwner owner) {
    }

    @Override
    public void onDetach(@NonNull AdapterNotifierOwner owner) {
    }

    @Override
    public int size() {
        return list.size();
    }

    @NonNull
    @Override
    public E get(int position) {
        return list.get(position);
    }

    @NonNull
    @Override
    public Collection<E> getAll() {
        return list;
    }

    @Override
    public int positionOf(@NonNull E element) {
        return list.indexOf(element);
    }

    @Override
    public void refresh(@NonNull E element) {
        // nothing can do.
    }

    @Override
    public void refresh(int position) {
        // nothing can do.
    }

    @Override
    public void set(int position, @NonNull E newElement) {
        list.set(position, newElement);
    }

    @Override
    public void add(@NonNull E element) {
        list.add(element);
    }

    @Override
    public void add(int position, @NonNull E element) {
        list.add(position, element);
    }

    @Override
    public void addAll(@NonNull Collection<? extends E> collection) {
        list.addAll(collection);
    }

    @Override
    public void addAll(int position, @NonNull Collection<? extends E> collection) {
        list.addAll(position, collection);
    }

    @Override
    public void replace(@NonNull Collection<? extends E> collection) {
        list.clear();
        list.addAll(collection);
    }

    @Override
    public void remove(@NonNull E element) {
        list.remove(element);
    }

    @Override
    public void removeAt(int position) {
        list.remove(position);
    }

    @Override
    public void removeIf(@NonNull SmartAdapter.ItemTypePredicate<? super E> filter) {
        final int size = list.size();
        for (int i = size - 1; i >= 0; i--) {
            final E element = list.get(i);
            if (filter.test(element, i)) {
                list.remove(i);
            }
        }
    }


    @Override
    public void move(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }
        final E element = list.get(fromPosition);
        list.remove(fromPosition);
        list.add(toPosition, element);
    }

    @Override
    public void move(E element, int toPosition) {
        final int fromPosition = list.indexOf(element);
        if (fromPosition == toPosition) {
            return;
        }
        list.remove(fromPosition);
        list.add(toPosition, element);
    }

    @Override
    public void clear() {
        list.clear();
    }


}
