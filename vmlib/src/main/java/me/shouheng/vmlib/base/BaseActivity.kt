package me.shouheng.vmlib.base

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.umeng.analytics.MobclickAgent
import me.shouheng.utils.app.ActivityUtils
import me.shouheng.utils.constant.ActivityDirection
import me.shouheng.utils.permission.Permission
import me.shouheng.utils.permission.PermissionResultHandler
import me.shouheng.utils.permission.PermissionResultResolver
import me.shouheng.utils.permission.PermissionUtils
import me.shouheng.utils.permission.callback.OnGetPermissionCallback
import me.shouheng.utils.permission.callback.PermissionResultCallback
import me.shouheng.utils.permission.callback.PermissionResultCallbackImpl
import me.shouheng.utils.ui.ToastUtils
import me.shouheng.vmlib.Platform
import me.shouheng.vmlib.anno.ActivityConfiguration
import me.shouheng.vmlib.bean.Resources
import me.shouheng.vmlib.bus.Bus
import me.shouheng.vmlib.component.*
import java.lang.reflect.ParameterizedType

/**
 * Base activity
 *
 * @author [ShouhengWang](mailto:shouheng2020@gmail.com)
 * @version 2020-05-06 21:51
 */
abstract class BaseActivity<U : BaseViewModel> : AppCompatActivity(), PermissionResultResolver {
    protected val vm: U by lazy { createViewModel() }
    private var useEventBus = false
    private var needLogin = true

    /** Grouped values with [ActivityConfiguration].*/
    private var pageName: String = javaClass.simpleName
    private var hasFragment = false
    private var useUmengManual = false
    private var exitDirection = ActivityDirection.ANIMATE_NONE
    private var layoutResId = 0
    private var onGetPermissionCallback: OnGetPermissionCallback? = null

    /** Do create view business. */
    protected abstract fun doCreateView(savedInstanceState: Bundle?)

    /** Get the layout resource id from subclass. */
    @LayoutRes
    protected abstract fun getLayoutResId(): Int

    /** This method will be called before the [.setContentView] was called. */
    protected open fun setupContentView(savedInstanceState: Bundle?) {
        setContentView(layoutResId)
    }

    /**
     * Initialize view model from generic type of current activity.
     * This method will visit all generic types of current activity and choose the FIRST ONE
     * that assigned from [ViewModel].
     *
     * Override this method to add your own implementation.
     */
    protected fun createViewModel(): U {
        val vmClass = (this.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments
            .firstOrNull { ViewModel::class.java.isAssignableFrom(it as Class<*>) } as? Class<U>
            ?: throw IllegalStateException("You must specify a view model class.")
        return ViewModelProviders.of(this)[vmClass]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (useEventBus) Bus.get().register(this)
        super.onCreate(savedInstanceState)
        layoutResId = getLayoutResId()
        setupContentView(savedInstanceState)
        doCreateView(savedInstanceState)
    }

    /** Observe data */
    protected fun <T> observe(
        dataType: Class<T>,
        success: (res: Resources<T>) -> Unit = {},
        fail:    (res: Resources<T>) -> Unit = {},
        loading: (res: Resources<T>) -> Unit = {}
    ) {
        observe(dataType, null, false, success, fail, loading)
    }

    /** Observe data */
    protected fun <T> observe(
        dataType: Class<T>,
        single: Boolean = false,
        success: (res: Resources<T>) -> Unit = {},
        fail:    (res: Resources<T>) -> Unit = {},
        loading: (res: Resources<T>) -> Unit = {}
    ) {
        observe(dataType, null, single, success, fail, loading)
    }

    /** Observe data */
    protected fun <T> observe(
        dataType: Class<T>,
        sid: Int? = null,
        success: (res: Resources<T>) -> Unit = {},
        fail:    (res: Resources<T>) -> Unit = {},
        loading: (res: Resources<T>) -> Unit = {}
    ) {
        observe(dataType, sid, false, success, fail, loading)
    }

    /** Observe data */
    protected fun <T> observe(
        dataType: Class<T>,
        sid: Int? = null,
        single: Boolean = false,
        success: (res: Resources<T>) -> Unit = {},
        fail:    (res: Resources<T>) -> Unit = {},
        loading: (res: Resources<T>) -> Unit = {}
    ) {
        observe(vm.getObservable(dataType, sid, single), success, fail, loading)
    }

    /** Observe list data */
    protected fun <T> observeList(
        dataType: Class<T>,
        success: (res: Resources<List<T>>) -> Unit = {},
        fail:    (res: Resources<List<T>>) -> Unit = {},
        loading: (res: Resources<List<T>>) -> Unit = {}
    ) {
        observeList(dataType, null, false, success, fail, loading)
    }

    /** Observe list data */
    protected fun <T> observeList(
        dataType: Class<T>,
        single: Boolean = false,
        success: (res: Resources<List<T>>) -> Unit = {},
        fail:    (res: Resources<List<T>>) -> Unit = {},
        loading: (res: Resources<List<T>>) -> Unit = {}
    ) {
        observeList(dataType, null, single, success, fail, loading)
    }

    /** Observe list data */
    protected fun <T> observeList(
        dataType: Class<T>,
        sid: Int? = null,
        success: (res: Resources<List<T>>) -> Unit = {},
        fail:    (res: Resources<List<T>>) -> Unit = {},
        loading: (res: Resources<List<T>>) -> Unit = {}
    ) {
        observeList(dataType, sid, false, success, fail, loading)
    }

    /** Observe list data */
    protected fun <T> observeList(
        dataType: Class<T>,
        sid: Int? = null,
        single: Boolean = false,
        success: (res: Resources<List<T>>) -> Unit = {},
        fail:    (res: Resources<List<T>>) -> Unit = {},
        loading: (res: Resources<List<T>>) -> Unit = {}
    ) {
        observe(vm.getListObservable(dataType, sid, single), success, fail, loading)
    }

    /** Get fragment of given resources id. */
    protected fun getFragment(@IdRes resId: Int): androidx.fragment.app.Fragment? {
        return supportFragmentManager.findFragmentById(resId)
    }

    /**
     * Does the user need login to enter this activity. This value was set by the
     * [ActivityConfiguration.needLogin], you can judge this value and implement your logic.
     *
     * @return true if the user need login.
     */
    protected fun needLogin(): Boolean {
        return needLogin
    }

    /** Make a simple toast*/
    protected fun toast(text: CharSequence?) {
        ToastUtils.showShort(text)
    }

    /** Make a simple toast*/
    protected fun toast(@StringRes resId: Int) {
        ToastUtils.showShort(resId)
    }

    /** Post one event by EventBus */
    protected fun post(event: Any) {
        Bus.get().post(event)
    }

    /**Post one sticky event by EventBus */
    protected fun postSticky(event: Any) {
        Bus.get().postSticky(event)
    }

    /**
     * To find view by id
     *
     * @param id  id
     * @param <V> the view type
     * @return    the view
     */
    protected fun <V : View> f(@IdRes id: Int): V {
        return findViewById(id)
    }

    /**
     * Correspond to fragment's [Fragment.getContext]
     *
     * @return context
     */
    protected val context: Context
        get() = this

    /**
     * Correspond to fragment's [Fragment.getActivity]
     *
     * @return activity
     */
    protected val activity: Activity
        get() = this

    /**
     * Check single permission. For multiple permissions at the same time, call
     * [.checkPermissions].
     *
     * @param permission the permission to check
     * @param onGetPermission the callback when got the required permission
     */
    protected fun check(@Permission permission: Int, onGetPermission: () -> Unit) {
        PermissionUtils.checkPermissions(this, OnGetPermissionCallback {
            onGetPermission()
        }, permission)
    }

    /**
     * Check multiple permissions at the same time.
     *
     * @param onGetPermission the callback when got all permissions required.
     * @param permissions     the permissions to request.
     */
    protected fun check(onGetPermission: () -> Unit, @Permission vararg permissions: Int) {
        PermissionUtils.checkPermissions(this, OnGetPermissionCallback {
            onGetPermission()
        }, *permissions)
    }

    /**
     * Get the permission check result callback, the default implementation was [PermissionResultCallbackImpl].
     * Override this method to add your own implementation.
     *
     * @return the permission result callback
     */
    protected val permissionResultCallback: PermissionResultCallback
        get() = PermissionResultCallbackImpl(this, onGetPermissionCallback)

    override fun setOnGetPermissionCallback(onGetPermissionCallback: OnGetPermissionCallback) {
        this.onGetPermissionCallback = onGetPermissionCallback
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionResultHandler.handlePermissionsResult(this, requestCode,
            permissions, grantResults, permissionResultCallback)
    }

    override fun onResume() {
        super.onResume()
        if (Platform.DEPENDENCY_UMENG_ANALYTICS && !useUmengManual) {
            if (hasFragment) {
                MobclickAgent.onPageStart(pageName)
            }
            MobclickAgent.onResume(this)
        }
    }

    override fun onPause() {
        super.onPause()
        if (Platform.DEPENDENCY_UMENG_ANALYTICS && !useUmengManual) {
            if (hasFragment) {
                MobclickAgent.onPageEnd(pageName)
            }
            MobclickAgent.onPause(this)
        }
    }

    override fun onDestroy() {
        if (useEventBus) {
            Bus.get().unregister(this)
        }
        super.onDestroy()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (exitDirection != ActivityDirection.ANIMATE_NONE) {
            ActivityUtils.overridePendingTransition(this, exitDirection)
        }
    }

    /**
     * This method is used to call the super [.onBackPressed] instead of the
     * implementation of current activity. Since the current [.onBackPressed] may be override.
     */
    fun superOnBackPressed() {
        super.onBackPressed()
    }

    init {
        val configuration = this.javaClass.getAnnotation(ActivityConfiguration::class.java)
        if (configuration != null) {
            useEventBus = configuration.useEventBus
            needLogin = configuration.needLogin
            exitDirection = configuration.exitDirection
            val umengConfiguration = configuration.umeng
            pageName = if (TextUtils.isEmpty(umengConfiguration.pageName))
                javaClass.simpleName else umengConfiguration.pageName
            hasFragment = umengConfiguration.fragmentActivity
            useUmengManual = umengConfiguration.useUmengManual
        }
    }
}
