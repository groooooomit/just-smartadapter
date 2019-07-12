package just.recyclerview.common;


import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

public class AnimationUtil {

    public static void clearAnimation(RecyclerView recyclerView){
        ((DefaultItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);// 去掉闪烁
    }
}
