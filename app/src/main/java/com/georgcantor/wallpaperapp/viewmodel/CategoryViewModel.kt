package com.georgcantor.wallpaperapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.georgcantor.wallpaperapp.R
import com.georgcantor.wallpaperapp.model.Category
import com.georgcantor.wallpaperapp.model.Hit
import com.georgcantor.wallpaperapp.repository.ApiRepository
import com.georgcantor.wallpaperapp.ui.util.PicturesMapper
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function8
import io.reactivex.schedulers.Schedulers

class CategoryViewModel(private val context: Context,
                        private val apiRepository: ApiRepository) : ViewModel() {

    fun getCategories(): Observable<List<Category>> {
        return Observable.combineLatest<List<Hit>,
                List<Hit>,
                List<Hit>,
                List<Hit>,
                List<Hit>,
                List<Hit>,
                List<Hit>,
                List<Hit>,
                List<Category>>(
                apiRepository.getPixabayPictures(context.getString(R.string.animals), 1).map { it.hits },
                apiRepository.getPixabayPictures(context.getString(R.string.textures), 1).map { it.hits },
                apiRepository.getPixabayPictures(context.getString(R.string.buildings), 1).map { it.hits },
                apiRepository.getPixabayPictures(context.getString(R.string.nature), 1).map { it.hits },
                apiRepository.getPixabayPictures(context.getString(R.string.music), 1).map { it.hits },
                apiRepository.getPixabayPictures(context.getString(R.string.travel), 1).map { it.hits },
                apiRepository.getPixabayPictures(context.getString(R.string.business), 1).map { it.hits },
                apiRepository.getPixabayPictures(context.getString(R.string.fashion), 1).map { it.hits },
                Function8(PicturesMapper.Companion::mergeCategories)
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

}