package com.georgcantor.wallpaperapp.ui.fragment

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.georgcantor.wallpaperapp.R
import com.georgcantor.wallpaperapp.ui.adapter.WallpAdapter
import com.georgcantor.wallpaperapp.ui.util.EndlessRecyclerViewScrollListener
import com.georgcantor.wallpaperapp.ui.util.HideNavScrollListener
import com.georgcantor.wallpaperapp.ui.util.UtilityMethods
import com.georgcantor.wallpaperapp.viewmodel.MercedesViewModel
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.fragment_mercedes.*
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf

class MercedesFragment : Fragment() {

    companion object {
        fun newInstance(arguments: String): MercedesFragment {
            val fragment = MercedesFragment()
            val args = Bundle()
            args.putString("request", arguments)
            fragment.arguments = args

            return fragment
        }
    }

    private lateinit var viewModel: MercedesViewModel
    private var adapter: WallpAdapter? = null
    private var columnNo: Int = 0
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!UtilityMethods.isNetworkAvailable) {
            Toast.makeText(context, getString(R.string.check_internet), Toast.LENGTH_SHORT).show()
        }
        viewModel = getViewModel { parametersOf() }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_mercedes, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!UtilityMethods.isNetworkAvailable) {
            noInternetImageView.visibility = View.VISIBLE
        }

        mercedesRefreshLayout.setOnRefreshListener {
            loadData(1)
            mercedesRefreshLayout.isRefreshing = false
        }
        checkScreenSize()

        val gridLayoutManager = StaggeredGridLayoutManager(columnNo, StaggeredGridLayoutManager.VERTICAL)
        mercedesRecyclerView.setHasFixedSize(true)
        mercedesRecyclerView.layoutManager = gridLayoutManager

        val scrollListener = object : EndlessRecyclerViewScrollListener(gridLayoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                loadData(page)
            }
        }
        scrollListener.resetState()
        mercedesRecyclerView.addOnScrollListener(scrollListener)
        adapter = WallpAdapter(requireContext())
        mercedesRecyclerView.adapter = adapter

        val hideScrollListener = object : HideNavScrollListener(requireActivity().navigation) {}
        mercedesRecyclerView.addOnScrollListener(hideScrollListener)

        loadData(1)
    }

    @SuppressLint("CheckResult")
    private fun loadData(index: Int) {
        animationView?.visibility = View.VISIBLE
        animationView?.playAnimation()
        animationView?.loop(true)

        viewModel.getPictures(arguments?.getString("request") ?: "", index).subscribe({
            adapter?.setPicList(it.hits)
            animationView?.loop(false)
            animationView?.visibility = View.GONE
        }, {
            animationView?.loop(false)
            animationView?.visibility = View.GONE
            Toast.makeText(context, getString(R.string.wrong_message), Toast.LENGTH_SHORT).show()
        })
    }

    private fun checkScreenSize() {
        val screenSize = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK

        columnNo = when (screenSize) {
            Configuration.SCREENLAYOUT_SIZE_XLARGE -> 4
            Configuration.SCREENLAYOUT_SIZE_UNDEFINED -> 3
            Configuration.SCREENLAYOUT_SIZE_LARGE -> 3
            Configuration.SCREENLAYOUT_SIZE_NORMAL -> 2
            Configuration.SCREENLAYOUT_SIZE_SMALL -> 2
            else -> 2
        }
    }
}
