package it.emperor.deviceusagestats.ui.network

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
import it.emperor.deviceusagestats.models.TimeType
import it.emperor.deviceusagestats.services.RxBus
import it.emperor.deviceusagestats.services.UsageService
import it.emperor.deviceusagestats.ui.base.BaseActivity
import it.emperor.deviceusagestats.ui.detail.appDetail
import it.emperor.deviceusagestats.ui.network.adapters.NetworkStatsAppsAdapter
import it.emperor.deviceusagestats.ui.network.model.NetworkStatsMaps
import kotlinx.android.synthetic.main.act_network_app_list.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.joda.time.DateTime
import javax.inject.Inject

fun Context.networkAppList(mapsType: TimeType, start: DateTime, end: DateTime): Intent {
    return Intent(this, Act_NetworkAppList::class.java)
            .putExtra(INTENT_NETWORK_MAPS_TYPE, mapsType)
            .putExtra(INTENT_NETWORK_START, start)
            .putExtra(INTENT_NETWORK_END, end)
}

private const val INTENT_NETWORK_MAPS_TYPE = "mapsType"
private const val INTENT_NETWORK_START = "start"
private const val INTENT_NETWORK_END = "end"

class Act_NetworkAppList : BaseActivity() {

    @Inject
    private lateinit var usageService: UsageService
    @Inject
    private lateinit var rxBus: RxBus

    private lateinit var networkUsageMaps: NetworkStatsMaps

    private lateinit var mapsType: TimeType
    private lateinit var start: DateTime
    private lateinit var end: DateTime
    private var showRx: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayShowTitleEnabled(true)
        updateToolbarSubtitleWithFilter(mapsType)
    }

    override fun getLayoutId(): Int {
        return R.layout.act_network_app_list
    }

    override fun initVariables() {
        networkUsageMaps = NetworkStatsMaps(packageManager)
    }

    override fun loadParameters(extras: Bundle) {
        mapsType = extras.getSerializable(INTENT_NETWORK_MAPS_TYPE) as TimeType
        start = extras.getSerializable(INTENT_NETWORK_START) as DateTime
        end = extras.getSerializable(INTENT_NETWORK_END) as DateTime
    }

    override fun loadInfos(savedInstanceState: Bundle) {
    }

    override fun saveInfos(outState: Bundle) {
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.act_network, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.filter -> {
                val popupMenu = PopupMenu(this, placeholder, Gravity.BOTTOM)
                popupMenu.inflate(R.menu.act_network_filter)
                popupMenu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.today -> {
                            mapsType = TimeType.TODAY
                            start = DateTime.now().withTimeAtStartOfDay()
                            end = DateTime.now()
                            update()
                            updateToolbarSubtitleWithFilter(mapsType)
                        }
                        R.id.week -> {
                            mapsType = TimeType.WEEK
                            start = DateTime.now().minusDays(7).withTimeAtStartOfDay()
                            end = DateTime.now()
                            update()
                            updateToolbarSubtitleWithFilter(mapsType)
                        }
                        R.id.month -> {
                            mapsType = TimeType.MONTH
                            start = DateTime.now().minusMonths(1).withTimeAtStartOfDay()
                            end = DateTime.now()
                            update()
                            updateToolbarSubtitleWithFilter(mapsType)
                        }
                        R.id.last_month -> {
                            mapsType = TimeType.LAST_MONTH
                            start = DateTime.now().minusMonths(2).withTimeAtStartOfDay()
                            end = DateTime.now().minusMonths(1)
                            update()
                            updateToolbarSubtitleWithFilter(mapsType)
                        }
                        R.id.year -> {
                            mapsType = TimeType.YEAR
                            start = DateTime.now().minusYears(1).withTimeAtStartOfDay()
                            end = DateTime.now()
                            update()
                            updateToolbarSubtitleWithFilter(mapsType)
                        }
                        R.id.custom -> {
                            mapsType = TimeType.CUSTOM

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
        list.adapter = NetworkStatsAppsAdapter(this, mutableListOf(), 0, showRx)

        update()

        rxBus.toObservable().subscribe {
            when (it) {
                is AppDetailEvent -> onEvent(it)
            }
        }
    }

    private fun onEvent(event: AppDetailEvent) {
        val pairIcon: Pair<View, String> = Pair.create(event.iconView, getString(R.string.transition_app_icon))
        val pairName: Pair<View, String> = Pair.create(event.nameView, getString(R.string.transition_app_name))
        startActivity(appDetail(event.packageName, event.nameView.textSize), ActivityOptions.makeSceneTransitionAnimation(this, pairIcon, pairName).toBundle())
    }

    // FUNCTIONS

    private fun update() {
        loading.visibility = View.VISIBLE
        (list.adapter as NetworkStatsAppsAdapter).setIsLoading(true)

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
        networkUsageMaps.update(usageService.loadNetworkDetailStats(UsageService.WIFI, start, end),
                usageService.loadNetworkDetailStats(UsageService.MOBILE, start, end))
    }

    private fun updateViews() {
        loading.visibility = View.GONE

        var appList = if (showRx) networkUsageMaps.all.rxByApp.toList() else networkUsageMaps.all.txByApp.toList()
        appList = appList.sortedByDescending { it.second.valueDownload + it.second.valueUpload }

        (list.adapter as NetworkStatsAppsAdapter).swapItems(appList, if (showRx) networkUsageMaps.all.rxByAppTotal else networkUsageMaps.all.txByAppTotal, showRx)
        (list.adapter as NetworkStatsAppsAdapter).setIsLoading(false)
    }
}

