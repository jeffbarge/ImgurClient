package com.barger.sofichallenge

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.widget.SearchView
import android.view.Menu
import android.view.View
import android.widget.ImageView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var disposable: Disposable
    private lateinit var textWatchSubscription: Disposable
    private val searchResultsSubject = PublishSubject.create<String>()
    private val adapter = ImgurAdapter()
    private val imgurService = ImgurService.create()
    private var pageNumber = 0
    private var query = ""
    private var fetching = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        savedInstanceState?.apply {
            adapter.clearData()
            val data = getParcelableArrayList<ImgurViewModel>(KEY_IMAGES)
            if (!data.isEmpty()) {
                adapter.addData(data)
                recycler_view.visibility = View.VISIBLE
            }
            query = getString(KEY_QUERY)
            pageNumber = getInt(KEY_PAGE_NUMBER)
            if (getBoolean(KEY_REQUERY)) {
                fetchResults(pageNumber, query)
            }
        }

        adapter.onImageClickedListener = object:ImgurAdapter.OnImageClickedListener {
            override fun onClicked(url: String, caption: String, imageView: ImageView) {
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this@MainActivity,
                        imageView,
                        "image_view")
                startActivity(ImageActivity.createIntent(this@MainActivity, url, caption), options.toBundle())
            }
        }

        recycler_view.adapter = adapter
        val layoutManager = LinearLayoutManager(this)
        recycler_view.layoutManager = layoutManager
        recycler_view.addOnScrollListener(object:RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                //we're going to fetch the next page before you get to the end; that way,
                //you're less likely to ever see the 'loading' spinner
                if (layoutManager.findLastVisibleItemPosition() >= adapter.itemCount - 10 && !fetching) {
                    fetchResults(++pageNumber, query)
                }
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.apply {
            putParcelableArrayList(KEY_IMAGES, adapter.data)
            putString(KEY_QUERY, query)
            putInt(KEY_PAGE_NUMBER, pageNumber)
            putBoolean(KEY_REQUERY, fetching)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        val searchView = menu?.findItem(R.id.search)?.actionView as SearchView
        searchView.queryHint = getString(R.string.search_hint)
        searchView.isIconified = false
        if (!TextUtils.isEmpty(query)) {
            searchView.setQuery(query, false)
        }
        searchView.setOnQueryTextListener(object:SearchView.OnQueryTextListener {
            override fun onQueryTextChange(query: String?): Boolean {
                query?.let {
                    searchResultsSubject.onNext(it)
                }
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    searchResultsSubject.onNext(it)
                }
                return true
            }
        })

        return true
    }

    private fun createSubscriptions() {
        textWatchSubscription = searchResultsSubject
                .debounce(250, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .subscribe { newQuery(it) }
    }

    override fun onPause() {
        super.onPause()
        disposable.dispose()
        textWatchSubscription.dispose()
    }

    override fun onResume() {
        super.onResume()
        disposable = CompositeDisposable()
        createSubscriptions()
    }

    private fun newQuery(query: String) {
        runOnUiThread {
            adapter.clearData()
            recycler_view.visibility = View.VISIBLE
        }

        fetchResults(0, query)
    }

    private fun fetchResults(page: Int, q: String) {
        fetching = true
        query = q
        pageNumber = page
        disposable.dispose()

        disposable = (imgurService.searchImages(page, query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { handleResults(it) },
                        { handleError(it) }
                ))
    }

    private fun handleResults(results: SearchResult) {
        val images = flattenImages(results.data)
        adapter.addData(images.map { ImgurViewModel(it.link, it.id, it.title ?: "", it.description ?: "") })
        fetching = false
    }

    private fun flattenImages(images: List<Image>) : List<Image> {
        val flattened = arrayListOf<Image>()
        images.forEach { image ->
            when (image.is_album) {
                true -> if (image.images != null) flattened.addAll(image.images)
                else -> flattened.add(image)
            }
        }
        return flattened
    }

    private fun handleError(error: Throwable) {
        fetching = false
    }

    companion object {
        val KEY_IMAGES = "key_images"
        val KEY_QUERY = "key_query"
        val KEY_PAGE_NUMBER = "key_page_number"
        val KEY_REQUERY = "key_requery"
    }
}
