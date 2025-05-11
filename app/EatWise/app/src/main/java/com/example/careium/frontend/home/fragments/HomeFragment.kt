package com.example.careium.frontend.home.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.example.careium.R
import com.example.careium.databinding.FragmentHomeBinding
import com.example.careium.frontend.factory.ClassifierViewModel
import com.example.careium.frontend.factory.NutritionViewModel
import com.example.careium.frontend.home.activities.classifierViewModel
import com.example.careium.frontend.home.activities.nutritionViewModel
import java.text.SimpleDateFormat
import java.util.*


class HomeFragment : Fragment(R.layout.fragment_home) {

    lateinit var binding: FragmentHomeBinding

    // Date View Day
    private var daysFrom: Int = 0

    // Main Components
    private var progressVal: Int = 3000
    private var caloriesTarget: Int = 3000
    private var caloriesVal: Int = 0
    private var carbsTarget: Float = 100f
    private var carbsVal: Float = 0f
    private var fatsTarget: Float = 100f
    private var fatsVal: Float = 0f
    private var proteinsTrgt: Float = 100f
    private var proteinsVal: Float = 0f

    companion object {
        @JvmStatic
        fun newInstance() =
            HomeFragment().apply {}
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        // Observe The Nutrition Values and Class Name
        observeNutrition()
        observeClassification()

        // setting target values for each component
        hookTargets()

        // initializing date section
        hookDateSection()

        // initializing progress section
        hookProgressSection()

        // initializing components values
        hookComponentsSection()
        
        // Initially hide the last captured card until an image is processed
        binding.cardLastCaptured.visibility = View.GONE

        // Ensure the app title is visible only on the home page
        binding.textAppDetails.visibility = View.VISIBLE
    }

    private fun hookProgressSection() {
        binding.progressHomeCircular.max = caloriesTarget
        updateProgress()
    }

    @SuppressLint("SetTextI18n")
    private fun updateProgress() {
        binding.texTProgressHome.text = "$progressVal"
        binding.progressHomeCircular.progress = progressVal
    }

    private fun hookDateSection() {
        updateDate()
        binding.imageButtonPrevDate.setOnClickListener {
            goToPrevDay()
        }
        binding.imageButtonNextDate.setOnClickListener {
            goToNextDay()
        }
    }

    private fun goToPrevDay() {
        daysFrom++
        if (daysFrom > 0)
            binding.imageButtonNextDate.visibility = View.VISIBLE
        updateDate()
    }

    private fun goToNextDay() {
        daysFrom--
        if (daysFrom <= 0) {
            binding.imageButtonNextDate.visibility = View.INVISIBLE
        }
        updateDate()
    }

    @SuppressLint("SimpleDateFormat")
    private fun updateDate() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysFrom)
        val dateFull = calendar.time
        val formatter = SimpleDateFormat("EEEE MMM dd, yyyy")
        binding.textCalendarDate.text = formatter.format(dateFull)

        // Date comparison Color Change
        val sdf = SimpleDateFormat("dd MM yyyy")
        val strDate = sdf.format(Calendar.getInstance().time)
        val strDateFromView = sdf.format(dateFull)
        if (strDate != strDateFromView)
            binding.textCalendarDate.setTextColor(Color.GRAY)
        else
            binding.textCalendarDate.setTextColor(Color.CYAN)
    }

    private fun hookTargets() {
        // Default settings for progress components max values
        binding.layoutCalsItem.progressCmpnt.max = caloriesTarget
        binding.layoutCarbsItem.progressCmpnt.max = carbsTarget.toInt()
        binding.layoutFatsItem.progressCmpnt.max = fatsTarget.toInt()
        binding.layoutProteinsItem.progressCmpnt.max = proteinsTrgt.toInt()
    }

    private fun hookComponentsSection() {
        // Hooking CALORIES
        binding.layoutCalsItem.textCmpntLabel.text = getString(R.string.calories)
        // Hooking CARBS
        binding.layoutCarbsItem.textCmpntLabel.text = getString(R.string.carbs)
        // Hooking FATS
        binding.layoutFatsItem.textCmpntLabel.text = getString(R.string.fats)
        // Hooking PROTEINS
        binding.layoutProteinsItem.textCmpntLabel.text = getString(R.string.proteins)

        updateComponentsSection()
    }

    @SuppressLint("SetTextI18n")
    private fun updateComponentsSection() {
        // CALORIES
        binding.layoutCalsItem.textCmpntValue.text = "$caloriesVal"
        binding.layoutCalsItem.progressCmpnt.progress = caloriesVal
        binding.layoutCalsItem.textCmpntTarget.text = "/$caloriesTarget cal"
        // CARBS
        binding.layoutCarbsItem.textCmpntValue.text = "$carbsVal"
        binding.layoutCarbsItem.progressCmpnt.progress = carbsVal.toInt()
        binding.layoutCarbsItem.textCmpntTarget.text = "/$carbsTarget g"
        // FATS
        binding.layoutFatsItem.textCmpntValue.text = "$fatsVal"
        binding.layoutFatsItem.progressCmpnt.progress = fatsVal.toInt()
        binding.layoutFatsItem.textCmpntTarget.text = "/$fatsTarget g"
        // PROTEINS
        binding.layoutProteinsItem.textCmpntValue.text = "$proteinsVal"
        binding.layoutProteinsItem.progressCmpnt.progress = proteinsVal.toInt()
        binding.layoutProteinsItem.textCmpntTarget.text = "/$proteinsTrgt g"
    }

    private fun observeNutrition() {
        nutritionViewModel = ViewModelProviders.of(requireActivity()).get(NutritionViewModel::class.java)
        nutritionViewModel.mutableNutrition.observe(viewLifecycleOwner) { nutritionList ->
            caloriesVal = nutritionList[0].toInt()
            fatsVal = nutritionList[2]
            carbsVal = nutritionList[3]
            proteinsVal = nutritionList[4]

            if (progressVal - caloriesVal > 0) {
                progressVal -= caloriesVal
            } else progressVal = 0

            updateProgress()
            updateComponentsSection()
        }
    }

    private fun observeClassification() {
        classifierViewModel = ViewModelProviders.of(requireActivity()).get(ClassifierViewModel::class.java)

        classifierViewModel.mutableDishName.observe(viewLifecycleOwner) { predictedClass ->
            binding.textLastCaptured.text = predictedClass
        }
        
        classifierViewModel.mutableDishImage.observe(viewLifecycleOwner) { dishImage ->
            binding.imageLastCaptured.setImageDrawable(dishImage)
            binding.cardLastCaptured.visibility = View.VISIBLE
        }
    }
}