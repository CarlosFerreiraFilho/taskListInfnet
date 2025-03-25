package br.edu.infnet.atv.one.tasklist.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import br.edu.infnet.atv.one.tasklist.R
import br.edu.infnet.atv.one.tasklist.databinding.FragmentSaveButtonBinding

class SaveButtonFragment : Fragment() {

    private var _binding: FragmentSaveButtonBinding? = null
    private val binding get() = _binding!!

    private var buttonText: String? = null
    private var listener: OnSaveButtonClickListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnSaveButtonClickListener) {
            listener = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buttonText = arguments?.getString(ARG_BUTTON_TEXT) ?: getString(R.string.save)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSaveButtonBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSave.text = buttonText
        binding.btnSave.setOnClickListener { listener?.onSaveClicked() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_BUTTON_TEXT = "button_text"

        fun newInstance(buttonText: String): SaveButtonFragment {
            val fragment = SaveButtonFragment()
            val args = Bundle()
            args.putString(ARG_BUTTON_TEXT, buttonText)
            fragment.arguments = args
            return fragment
        }
    }

    interface OnSaveButtonClickListener {
        fun onSaveClicked()
    }
}
