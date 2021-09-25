package me.shouheng.vmlib.comn;

import android.app.Application;
import androidx.annotation.NonNull;
import me.shouheng.vmlib.base.BaseViewModel;

/**
 * Empty view model with no business method. Mainly used as the generic type
 * for {@link me.shouheng.vmlib.comn.ContainerActivity}.
 * If you have just want to use the data binding but the view model, use this
 * view model as a generic type for your activity.
 *
 * @author <a href="mailto:shouheng2020@gmail.com">ShouhengWang</a>
 */
public class EmptyViewModel extends BaseViewModel {

    public EmptyViewModel(@NonNull Application application) {
        super(application);
    }
}
