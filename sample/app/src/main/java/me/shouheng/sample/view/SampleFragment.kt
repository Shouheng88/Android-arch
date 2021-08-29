package me.shouheng.sample.view

import android.os.Bundle
import me.shouheng.sample.databinding.FragmentSampleBinding
import me.shouheng.vmlib.anno.FragmentConfiguration
import me.shouheng.vmlib.base.ViewBindingFragment
import me.shouheng.vmlib.comn.EmptyViewModel

/**
 * Sample fragment for ContainerActivity
 *
 * @author <a href="mailto:shouheng2020@gmail.com">WngShhng</a>
 */
@FragmentConfiguration
class SampleFragment : ViewBindingFragment<EmptyViewModel, FragmentSampleBinding>() {

    /** Used to test doOnCreateView method callback count */
    private var count = 0

    companion object {
        const val ARGS_KEY_TEXT = "__args_key_text"
    }

    override fun doCreateView(savedInstanceState: Bundle?) {
        val text = arguments?.getString(ARGS_KEY_TEXT)
        binding.tv.text =  "$text ${++count}"
    }

}