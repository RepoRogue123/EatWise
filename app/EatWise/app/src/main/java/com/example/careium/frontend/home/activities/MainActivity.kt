package com.example.careium.frontend.home.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.careium.R
import com.example.careium.core.models.DishClassification
import com.example.careium.core.models.DishNutritionRegression
import com.example.careium.databinding.ActivityMainBinding
import com.example.careium.databinding.LayoutFloatingMenuItemBinding
import com.example.careium.frontend.factory.*
import com.example.careium.frontend.home.fragments.*
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu

lateinit var nutritionViewModel: NutritionViewModel
lateinit var classifierViewModel: ClassifierViewModel

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var actionMenu: FloatingActionMenu
    private lateinit var dishImage: Bitmap

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val CAMERA_PERMISSION_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModels
        nutritionViewModel = ViewModelProviders.of(this).get(NutritionViewModel::class.java)
        classifierViewModel = ViewModelProviders.of(this).get(ClassifierViewModel::class.java)

        // Hide system bars
        hideNavigationAndStatusBars()

        // Bottom navigation menu items
        initializeBottomNavigation()

        // Add floating action menu
        buildFloatingActionMenu()

        // Add navigation view click actions
        makeNavigationViewItemsHooks()

        // Drawer menu icon click action
        binding.layoutToolbar.imageMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // Notification Icon click action
        binding.layoutToolbar.imageNotifications.setOnClickListener {
            loadFragment(ReminderFragment.newInstance())
        }
        
        // Request permissions
        requestCameraPermission()
    }
    
    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Camera permission denied. App will not function correctly.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initializeBottomNavigation() {
        // Set up the BottomNavigationView
        val bottomNavigation: BottomNavigationView = binding.bottomNavigation
        
        // Set up navigation item selection listener
        bottomNavigation.setOnItemSelectedListener { item ->
            val fragment: Fragment? = when (item.itemId) {
                R.id.navigation_home -> HomeFragment.newInstance()
                R.id.navigation_dashboard -> RecipesFragment.newInstance()
                R.id.navigation_notifications -> ReportsFragment.newInstance()
                else -> null
            }

            // Load Fragment
            if (fragment != null) {
                loadFragment(fragment)
                return@setOnItemSelectedListener true
            }
            return@setOnItemSelectedListener false
        }

        // Set the default selected item
        bottomNavigation.selectedItemId = R.id.navigation_home
    }

    private fun makeNavigationViewItemsHooks() {
        binding.navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menu_item_home -> {
                    loadFragment(HomeFragment.newInstance())
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.menu_item_recipes -> {
                    loadFragment(RecipesFragment.newInstance())
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.menu_item_diary -> {
                    loadFragment(DiaryFragment.newInstance())
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.menu_item_diet_calender -> {
                    loadFragment(CalenderFragment.newInstance())
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.menu_item_photo_album -> {
                    loadFragment(AlbumFragment.newInstance())
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.menu_item_reminder -> {
                    loadFragment(ReminderFragment.newInstance())
                    binding.drawerLayout.closeDrawers()
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.layout_main_frame, fragment)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .commit()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // Hide both the navigation bar and the status bar.
        if (hasFocus)
            hideSystemBars()
    }

    private fun hideNavigationAndStatusBars() {
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            // The system bars will only be "visible" if none of the
            // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
            if (visibility == 0)
                hideSystemBars()
        }
    }

    private fun hideSystemBars() {
        window.decorView.apply {
            systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    // View.SYSTEM_UI_FLAG_FULLSCREEN or // for Status bar
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        // Collapse FAB Menu and clear the overlay
        if (actionMenu.isOpen) {
            binding.layoutMainFrameOverlay.visibility = View.INVISIBLE
            actionMenu.close(true)
        }

        return super.dispatchTouchEvent(event)
    }

    private fun capturePlate() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } else {
            requestCameraPermission()
            Toast.makeText(this, "Camera permission required to capture food image", Toast.LENGTH_LONG).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
            try {
                dishImage = data.extras?.get("data") as Bitmap
                
                val nutritionList = makeRegressionPredictions(dishImage)
                val className = makeClassificationPrediction(dishImage)

                // Update MutableLiveData to display the output
                nutritionViewModel.mutableNutrition.value = nutritionList
                classifierViewModel.mutableDishName.value = className
                classifierViewModel.mutableDishImage.value = BitmapDrawable(resources, dishImage)
                
                // Show the results on the home screen
                binding.bottomNavigation.selectedItemId = R.id.navigation_home
                
                // Display results
                Toast.makeText(this, "Food detected: $className", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.d("DLModels", "Exception Occurred in DL Models: ${e.message}")
                Toast.makeText(this, "Error processing image: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun makeRegressionPredictions(dishImage: Bitmap): ArrayList<Float> {
        val nutrition = DishNutritionRegression(this)
        val tensorBuffer = nutrition.loadImageBuffer(dishImage, 224, 224)
        return (nutrition.predictNutritionModel(tensorBuffer))
    }

    private fun makeClassificationPrediction(dishImage: Bitmap): String {
        val classifier = DishClassification(this)
        val tensorBuffer2 = classifier.loadImageBuffer(dishImage, 250, 250)
        return classifier.classifyDish(tensorBuffer2)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun buildFloatingActionMenu() {
        // Building the primary button
        val primaryBtnIcon = ImageView(this)
        primaryBtnIcon.setImageDrawable(getDrawable(R.drawable.ic_round_add_24))
        val actionButton = FloatingActionButton.Builder(this).setContentView(primaryBtnIcon).build()
        val typedValue = TypedValue()
        theme.resolveAttribute(com.google.android.material.R.attr.colorSecondary, typedValue, true)
        val colorSecondary: Int = typedValue.data
        actionButton.background.setTint(colorSecondary)

        val param = actionButton.layoutParams as ViewGroup.MarginLayoutParams
        param.setMargins(10, 10, 10, 240)
        actionButton.layoutParams = param

        // Building up the menu
        // Item 1
        val item1 = inflateFABMenuItem(FABItem.WATER, R.drawable.ic_menu_water_48, "Water")
        // Item 2
        val item2 = inflateFABMenuItem(FABItem.DINNER, R.drawable.ic_menu_dinner_64, "Dinner")
        // Item 3
        val item3 = inflateFABMenuItem(FABItem.SNACK, R.drawable.ic_menu_snack_64, "Snack")
        // Item 4
        val item4 = inflateFABMenuItem(FABItem.LUNCH, R.drawable.ic_menu_lunch_64, "Lunch")
        // Item 5
        val item5 =
            inflateFABMenuItem(FABItem.BREAKFAST, R.drawable.ic_menu_breakfast_64, "Breakfast")
        // Item 6
        val item6 = inflateFABMenuItem(FABItem.TRACKER, R.drawable.ic_menu_camera_48, "Tracker")

        // Getting runtime screen width
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width: Float = displayMetrics.widthPixels.toFloat()
        val radius: Int = (width * 0.60f).toInt() // radius = 60% of the screen width

        // Attaching the menu to the primary button
        actionMenu = FloatingActionMenu.Builder(this)
            .setRadius(radius)
            .addSubActionView(item1)
            .addSubActionView(item2)
            .addSubActionView(item3)
            .addSubActionView(item4)
            .addSubActionView(item5)
            .addSubActionView(item6)
            .attachTo(actionButton)
            .build()

        actionButton.setOnClickListener {
            actionMenu.toggle(true)
            if (actionMenu.isOpen) {
                // add an overlay on opening
                binding.layoutMainFrameOverlay.visibility = View.VISIBLE
                // Close NavigationDrawer if opened
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START))
                    binding.drawerLayout.closeDrawers()
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun inflateFABMenuItem(id: FABItem, drawableID: Int, label: String): View {
        val itemBinding = LayoutFloatingMenuItemBinding.inflate(layoutInflater)
        val params = FrameLayout.LayoutParams(-2, -2, 51)
        itemBinding.root.layoutParams = params
        itemBinding.fabItem.setImageDrawable(getDrawable(drawableID))
        itemBinding.textFabLabel.text = label

        // Click Actions
        itemBinding.fabItem.setOnClickListener {
            when (id) {
                FABItem.TRACKER ->
                    capturePlate()
                FABItem.BREAKFAST,
                FABItem.LUNCH,
                FABItem.SNACK,
                FABItem.DINNER,
                FABItem.WATER ->
                    Toast.makeText(this, "Feature not available in demo mode", Toast.LENGTH_SHORT).show()
            }
        }
        return itemBinding.root
    }
}
