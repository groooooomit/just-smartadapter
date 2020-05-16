# just-smartadapter
 一个支持多 ItemType 的 RecyclerView adapter.
 
## 添加到你的项目
```gradle
implementation 'com.bfu:just-smartadapter:1.0.4'
```
## 开始使用
* api 说明
```kotlin
SmartAdapter
    
    /* 指定数据源. adapter 的数据源被抽象为了 DataSource<E>，ListDataSource<E> 是它的 List<E> 实现，AutoNotifyDataSource<E> 能够在数据源 item 变更时主动触发 adapter 的相应 notifyXXX 方法更新 recyclerView. */
    .newBuilder(AutoNotifyDataSource(ListDataSource(ArrayList<Item>())))

    /* 开始一种 type 的 item 的配置，itemType 设定为 1，下面的代码都是在配置本 itemType 的参数，直到碰到下一个 type(?) 配置为止. */
    .type(1) 
    
    /* item 布局. */
    .layout(R.layout.item) 
    
    /* 根据数据或数据位置判断哪些数据 item 的 itemType 是 1，以此对数据进行分类. */
    .filter { data, position -> 
        position % 2 == 0 /* 例如声明 position 为偶数的 item 的 itemType 是 1. */
    }
    
    /* 根据 item 的原始宽高和 recycleView 的宽高重新给出 item 的宽度. */
    .overrideWidth { originWidth, originHeight, parentWidth, parentHeight -> 
        ...
    }
    
    /* 根据 item 的原始宽高和 recycleView 的宽高重新给出 item 的高度. */
    .overrideHeight { originWidth, originHeight, parentWidth, parentHeight -> 
        ...
    }
    
    /* viewHolder 绑定数据到控件. */
    .onBind { viewHolder, data, position, type, adapter ->
        viewHolder.setText(R.id.txt_name, data.name)
        ...
    }
    
    /* item 点击事件. */
    .onItemClick { data, position, layoutPosition, type, view, adapter ->
        ...
    }
    
     /* item 长按事件. */
    .onItemLongClick { data, position, layoutPosition, type, view, adapter -> 
        ...
        true
    }
    
    /* 在 GridLayoutManager 环境下重写当前 itemType 的 item 所站的格子数. */
    .gridSpanSize(4) /* 如果 GridLayoutManager 的总格数为 4，那么 gridSpanSize(4) 表示当前 itemType 的 item 撑满 4 格，即独占一整行. */
    
    /* 更多类型的 item 配置. */
    .type(2)
    ...
    .type(3)
    ...
    .build()
```
