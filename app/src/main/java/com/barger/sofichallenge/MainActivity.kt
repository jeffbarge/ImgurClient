package com.barger.sofichallenge

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private var disposable = CompositeDisposable()
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

        search_text.addTextChangedListener(object:TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                when (TextUtils.isEmpty(s)) {
                    true -> recycler_view.visibility = View.GONE //good time to show instructions or something
                    false -> searchResultsSubject.onNext(s.toString())
                }
            }

            override fun afterTextChanged(p0: Editable?) { }
        })

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

        disposable.add(imgurService.searchImages(page, query)
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
}
