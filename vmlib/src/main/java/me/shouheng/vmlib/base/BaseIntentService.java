package me.shouheng.vmlib.base;

import android.app.IntentService;
import me.shouheng.vmlib.anno.ServiceConfiguration;
import me.shouheng.vmlib.bus.Bus;

/**
 * Base intent service implementation to handle {@link ServiceConfiguration} annotation.
 *
 * @author <a href="mailto:shouheng2020@gmail.com">WngShhng</a>
 * @version 2019-7-01
 */
public abstract class BaseIntentService extends IntentService {

    private boolean useEventBus = false;

    public BaseIntentService(String name) {
        super(name);
        ServiceConfiguration configuration = this.getClass().getAnnotation(ServiceConfiguration.class);
        if (configuration != null) {
            useEventBus = configuration.useEventBus();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (useEventBus) {
            Bus.get().register(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (useEventBus) {
            Bus.get().unregister(this);
        }
    }
}
