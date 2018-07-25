package it.emperor.deviceusagestats.ui.usage

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.util.Pair
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import it.emperor.deviceusagestats.R
import it.emperor.deviceusagestats.events.AppDetailEvent
import it.emperor.deviceusagestats.models.AppTimeType
import it.emperor.deviceusagestats.services.RxBus
import it.emperor.deviceusagestats.services.UsageService
import it.emperor.deviceusagestats.ui.base.BaseActivity
import it.emperor.deviceusagestats.ui.detail.appDetail
import it.emperor.deviceusagestats.ui.usage.adapters.AppUsageStatsAdapter
import it.emperor.deviceusagestats.ui.usage.model.AppUsageStatsMaps
import it.emperor.deviceusagestats.ui.views.SearchView
import kotlinx.android.synthetic.main.act_appusage_app_list.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.joda.time.DateTime
import javax.inject.Inject

fun Context.appusageAppList(mapsType: AppTimeType, start: DateTime, end: DateTime): Intent {
    return Intent(this, Act_AppUsageAppList::class.java)
            .putExtra(INTENT_USAGE_MAPS_TYPE, mapsType)
            .putExtra(INTENT_USAGE_START, start)
            .putExtra(INTENT_USAGE_END, end)
}

private const val INTENT_USAGE_MAPS_TYPE = "mapsType"
private const val INTENT_USAGE_START = "start"
private const val INTENT_USAGE_END = "end"

class Act_AppUsageAppList : BaseActivity(), SearchView.OnTextChangeListener {

    @Inject
    private lateinit var usageService: UsageService
    @Inject
    private lateinit var rxBus: RxBus

    private lateinit var appUsageStatsMaps: AppUsageStatsMaps

    private lateinit var mapsType: AppTimeType
    private lateinit var start: DateTime
    private lateinit var end: DateTime

    private var searchText: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayShowTitleEnabled(true)
        updateToolbarSubtitleWithAppFilter(mapsType)
    }

    override fun getLayoutId(): Int {
        return R.layout.act_appusage_app_list
    }

    override fun initVariables() {
        appUsageStatsMaps = AppUsageStatsMaps(packageManager)
    }

    override fun loadParameters(extras: Bundle) {
        mapsType = extras.getSerializable(INTENT_USAGE_MAPS_TYPE) as AppTimeType
        start = extras.getSerializable(INTENT_USAGE_START) as DateTime
        end = extras.getSerializable(INTENT_USAGE_END) as DateTime
    }

    override fun loadInfos(savedInstanceState: Bundle) {
    }

    override fun saveInfos(outState: Bundle) {
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.act_appusage, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.filter -> {
                val popupMenu = PopupMenu(this, placeholder, Gravity.BOTTOM)
                popupMenu.inflate(R.menu.act_appusage_filter)
                popupMenu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.day -> {
                            mapsType = AppTimeType.DAY
                            start = DateTime.now().withTimeAtStartOfDay()
                            end = DateTime.now()
                            update()
                            updateToolbarSubtitleWithAppFilter(mapsType)
                        }
                        R.id.week -> {
                            mapsType = AppTimeType.WEEK
                            start = DateTime.now().minusDays(7).withTimeAtStartOfDay()
                            end = DateTime.now()
                            update()
                            updateToolbarSubtitleWithAppFilter(mapsType)
                        }
                        R.id.month -> {
                            mapsType = AppTimeType.MONTH
                            start = DateTime.now().minusMonths(1).withTimeAtStartOfDay()
                            end = DateTime.now()
                            update()
                            updateToolbarSubtitleWithAppFilter(mapsType)
                        }
                        R.id.year -> {
                            mapsType = AppTimeType.YEAR
                            start = DateTime.now().minusYears(1).withTimeAtStartOfDay()
                            end = DateTime.now()
                            update()
                            updateToolbarSubtitleWithAppFilter(mapsType)
                        }
                    }
                    popupMenu.dismiss()
                    true
                }
                popupMenu.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun initialize() {
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = AppUsageStatsAdapter(this, mutableListOf(), 0)

        update()

        rxBus.toObservable().subscribe({
            when (it) {
                is AppDetailEvent -> onEvent(it)
            }
        }, {})

        search_view.listener = this
    }

    override fun onTextChange(text: String) {
        if (text.isEmpty()) {
            searchText = null
        } else {
            searchText = text
        }
        updateViews()
    }

    private fun onEvent(event: AppDetailEvent) {
        val pairIcon: Pair<View, String> = Pair.create(event.iconView, getString(R.string.transition_app_icon))
        val pairName: Pair<View, String> = Pair.create(event.nameView, getString(R.string.transition_app_name))
        startActivity(appDetail(event.packageName, event.nameView.textSize), ActivityOptions.makeSceneTransitionAnimation(this, pairIcon, pairName).toBundle())
    }

    // FUNCTIONS

    private fun update() {
        loading.visibility = View.VISIBLE
        (list.adapter as AppUsageStatsAdapter).setIsLoading(true)

        Handler().postDelayed({
            doAsync {
                loadInfo()

                uiThread {
                    updateViews()
                }
            }
        }, 100)
    }

    private fun loadInfo() {
        val usageStats = usageService.loadUsageStats(start, end)
        val usageStatsSummary = usageService.loadUsageSummaryStats(start, end)

        appUsageStatsMaps.update(usageStats, usageStatsSummary)
    }

    private fun updateViews() {
        loading.visibility = View.GONE

        var appList = appUsageStatsMaps.totalUsage.toList()
        if (searchText != null) {
            appList = appList.filter { it.second.name.contains(searchText!!) || it.second.packageName.contains(searchText!!) }
        }
        appList = appList.sortedByDescending { it.second.timeInForeground }

        (list.adapter as AppUsageStatsAdapter).swapItems(appList, appUsageStatsMaps.totalForeground)
        (list.adapter as AppUsageStatsAdapter).setIsLoading(false)
    }
}

