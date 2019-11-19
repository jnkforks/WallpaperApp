package com.georgcantor.wallpaperapp.view.activity

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.georgcantor.wallpaperapp.R
import com.georgcantor.wallpaperapp.util.DisposableManager
import com.georgcantor.wallpaperapp.util.PreferenceManager
import com.georgcantor.wallpaperapp.util.openActivity
import com.georgcantor.wallpaperapp.util.openFragment
import com.georgcantor.wallpaperapp.util.showDialog
import com.georgcantor.wallpaperapp.view.fragment.*
import com.georgcantor.wallpaperapp.view.fragment.BmwFragment.Companion.REQUEST
import com.georgcantor.wallpaperapp.view.fragment.CarBrandFragment.Companion.FETCH_TYPE
import com.georgcantor.wallpaperapp.view.fragment.CategoryFragment.Companion.CATEGORIES
import com.georgcantor.wallpaperapp.viewmodel.MainViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    InstallStateUpdatedListener {

    companion object {
        private const val DEV_URL = "https://play.google.com/store/apps/dev?id=5242637664196553916"
        const val APP_URL = "https://play.google.com/store/apps/details?id=com.georgcantor.wallpaperapp"
        const val IS_RATING_EXIST = "is_rating_exist"
        const val LAUNCHES = "launches"
        const val RATING = "rating1"
        const val TAG_EXTRA_OPEN = "open_from_tag"
        const val TAG_EXTRA = "tag_extra"
    }

    override fun onStateUpdate(installState: InstallState) {
    }

    private lateinit var prefManager: PreferenceManager
    private lateinit var updateManager: AppUpdateManager
    private lateinit var mercedesFragment: Fragment
    private lateinit var bmwFragment: Fragment
    private lateinit var audiFragment: Fragment
    private lateinit var categoryFragment: Fragment
    private lateinit var brandFragment: Fragment

    private lateinit var viewModel: MainViewModel
    private lateinit var bundle: Bundle

    private val updateAvailable = MutableLiveData<Boolean>().apply { value = false }
    private var updateInfo: AppUpdateInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        viewModel = getViewModel { parametersOf() }
        prefManager = PreferenceManager(this)
        updateManager = AppUpdateManagerFactory.create(this)
        updateManager.registerListener(this)

        if (prefManager.getCategories(CATEGORIES)?.size ?: 0 < 16) loadCategories()

        checkForUpdate()

        updateAvailable.observe(this, Observer {
            if (it) showDialog(getString(R.string.update_dialog_message), ::goToGooglePlay)
        })

        supportFragmentManager.beginTransaction()
                .replace(R.id.frame_container, BmwFragment.newInstance(getString(R.string.bmw)))
                .commit()

        mercedesFragment = MercedesFragment.newInstance(getString(R.string.mercedes_request))
        bmwFragment = BmwFragment.newInstance(getString(R.string.bmw_request))
        audiFragment = AudiFragment.newInstance(getString(R.string.audi_request))
        categoryFragment = CategoryFragment()
        brandFragment = CarBrandFragment()

        bundle = Bundle()

        val myTitle = toolbar.getChildAt(0) as TextView
        myTitle.typeface = Typeface.create("cursive", Typeface.NORMAL)

        val toggle = ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val itemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_bmw -> {
                    toolbar.title = getString(R.string.bmw)
                    openFragment(bmwFragment, getString(R.string.bmw))
                    return@OnNavigationItemSelectedListener true
                }
                R.id.nav_audi -> {
                    toolbar.title = getString(R.string.audi)
                    openFragment(audiFragment, getString(R.string.audi))
                    return@OnNavigationItemSelectedListener true
                }
                R.id.nav_mercedes -> {
                    toolbar.title = getString(R.string.mercedes)
                    openFragment(mercedesFragment, getString(R.string.mercedes))
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }
        navigation.setOnNavigationItemSelectedListener(itemSelectedListener)
        navView.setNavigationItemSelectedListener(this)
        navView.itemIconTintList = null

        viewModel.checkNumberOfLaunches(this, prefManager)

        when (intent.getStringExtra(TAG_EXTRA_OPEN)) {
            TAG_EXTRA_OPEN -> {
                val bundle = Bundle()
                bundle.putString(FETCH_TYPE, intent.getStringExtra(TAG_EXTRA))
                brandFragment.arguments = bundle
                supportFragmentManager.beginTransaction().replace(R.id.frame_container, brandFragment).commit()
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val menuItem = menu.findItem(R.id.action_gallery)
        viewModel.isGalleryVisible.observe(this, Observer {
            menuItem.isVisible = it
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> openSearchActivity()
            R.id.action_gallery -> {
                toolbar.title = getString(R.string.gallery_toolbar)
                openFragment(categoryFragment, getString(R.string.gallery_toolbar))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_aston -> {
                openActivity(CarBrandActivity::class.java) {
                    putString(REQUEST, getString(R.string.aston))
                }
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left)
            }
            R.id.nav_bentley -> {
                openActivity(CarBrandActivity::class.java) {
                    putString(REQUEST, getString(R.string.bentley))
                }
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left)
            }
            R.id.nav_bugatti -> {
                openActivity(CarBrandActivity::class.java) {
                    putString(REQUEST, getString(R.string.bugatti))
                }
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left)
            }
            R.id.nav_ferrari -> {
                openActivity(CarBrandActivity::class.java) {
                    putString(REQUEST, getString(R.string.ferrari))
                }
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left)
            }
            R.id.nav_lambo -> {
                openActivity(CarBrandActivity::class.java) {
                    putString(REQUEST, getString(R.string.lamborghini))
                }
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left)
            }
            R.id.nav_mclaren -> {
                openActivity(CarBrandActivity::class.java) {
                    putString(REQUEST, getString(R.string.mclaren))
                }
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left)
            }
            R.id.nav_porsche -> {
                openActivity(CarBrandActivity::class.java) {
                    putString(REQUEST, getString(R.string.porsche))
                }
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left)
            }
            R.id.nav_rolls -> {
                openActivity(CarBrandActivity::class.java) {
                    putString(REQUEST, getString(R.string.rolls))
                }
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left)
            }
            R.id.nav_favorites -> {
                startActivity(Intent(this, FavoriteActivity::class.java))
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left)
            }
            R.id.nav_about -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(DEV_URL)))
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left)
            }
            R.id.nav_rate_us -> {
                viewModel.showRatingDialog(this, prefManager)
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)

        return true
    }

    override fun onBackPressed() {
        toolbar.title = getString(R.string.app_name)
        when {
            drawerLayout.isDrawerOpen(GravityCompat.START) -> {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            else -> {
                try {
                    super.onBackPressed()
                    overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right)
                } catch (e: IllegalStateException) {
                }
            }
        }
    }

    override fun onDestroy() {
        DisposableManager.dispose()
        super.onDestroy()
        updateManager.unregisterListener(this)
    }

    private fun checkForUpdate() {
        updateManager.appUpdateInfo.addOnSuccessListener {
            if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                it.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                updateInfo = it
                updateAvailable.value = true
            } else {
                updateAvailable.value = false
            }
        }
    }

    private fun loadCategories() {
        viewModel.loadCategories(prefManager).subscribe()
    }

    private fun goToGooglePlay() {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(APP_URL)))
        finish()
    }

    private fun openSearchActivity() {
        startActivity(Intent(this, SearchActivity::class.java))
        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left)
    }

}