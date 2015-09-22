package xpshome.net.components;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by Christian Poschinger on 15.09.2015.
 */
public class CoordinatorActionBehavior<T extends View> extends CoordinatorLayout.Behavior<T>{

    /**
     * Base action class
     *
     * @param <T> is the
     */
    public static abstract class ActionBase<T extends View> {
        public abstract boolean execute(CoordinatorLayout parent, T child, View dependency);
    }

    public static final class ActionTranslateUp<T extends View> extends ActionBase<T> {
        @Override
        public boolean execute(CoordinatorLayout parent, T child, View dependency) {
            float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
            child.setTranslationY(translationY);
            return true;
        }
    }

    public static final class LinearLayoutTranslateUpBehavior extends CoordinatorActionBehavior<LinearLayout> {
        public LinearLayoutTranslateUpBehavior() {
            super(new CoordinatorActionBehavior.ActionTranslateUp<LinearLayout>());
        }

        public LinearLayoutTranslateUpBehavior(Context context, AttributeSet attrs) {
            super(new CoordinatorActionBehavior.ActionTranslateUp<LinearLayout>());
        }
    }



    private final CoordinatorActionBehavior.ActionBase<T> action;

    public CoordinatorActionBehavior(CoordinatorActionBehavior.ActionBase<T> action) throws NullPointerException{
        if (action == null) {
            throw new NullPointerException();
        }
        this.action = action;
    }

    public final CoordinatorActionBehavior.ActionBase<T> getAction() {
        return this.action;
    }

    public boolean layoutDependsOn(CoordinatorLayout parent, T child, View dependency) {
        return dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, T child, View dependency) {
        return action.execute(parent, child,dependency);
    }
}
