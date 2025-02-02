package sk.umb.fpv.promob.pokornymath

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import sk.umb.fpv.promob.pokornymath.databinding.FragmentMenuBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonQuizes.setOnClickListener {
            findNavController().navigate(R.id.action_MenuFragment_to_ExamListFragment)
        }

        // Nie je odporúčané!
        /*binding.buttonExit.setOnClickListener {

        }*/
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}