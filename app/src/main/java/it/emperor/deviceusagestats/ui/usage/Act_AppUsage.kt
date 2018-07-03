package it.emperor.deviceusagestats.ui.usage

import android.app.ActivityOptions
import android.app.usage.UsageStats
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
import it.emperor.deviceusagestats.extensions.px
import it.emperor.deviceusagestats.services.RxBus
import it.emperor.deviceusagestats.services.UsageService
import it.emperor.deviceusagestats.ui.base.BaseActivity
import it.emperor.deviceusagestats.ui.detail.appDetail
import it.emperor.deviceusagestats.ui.usage.adapters.AppUsageStatsAdapter
import it.emperor.deviceusagestats.ui.usage.model.AppUsageStatsMaps
import kotlinx.android.synthetic.main.act_app_usage.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.joda.time.DateTime
import javax.inject.Inject

class Act_AppUsage : BaseActivity() {

    @Inject
    private lateinit var usageService: UsageService
    @Inject
    private lateinit var rxBus: RxBus

    private lateinit var appUsageStatsMaps: AppUsageStatsMaps
    private var showInForeground = true

    override fun getLayoutId(): Int {
        return R.layout.act_app_usage
    }

    override fun initVariables() {
    }

    override fun loadParameters(extras: Bundle) {
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
                            loadInfo(DateTime.now().withTimeAtStartOfDay(), DateTime.now())
                        }
                        R.id.week -> {
                            loadInfo(DateTime.now().minusDays(7).withTimeAtStartOfDay(), DateTime.now())
                        }
                        R.id.month -> {
                            loadInfo(DateTime.now().minusMonths(1).withTimeAtStartOfDay(), DateTime.now())
                        }
                        R.id.last_month -> {
                            loadInfo(DateTime.now().minusMonths(2).withTimeAtStartOfDay(), DateTime.now().minusMonths(1))
                        }
                        R.id.year -> {
                            loadInfo(DateTime.now().minusYears(1).withTimeAtStartOfDay(), DateTime.now())
                        }
                        R.id.custom -> {

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
        appUsageStatsMaps = AppUsageStatsMaps()

        list.layoutManager = LinearLayoutManager(this)
        list.adapter = AppUsageStatsAdapter(this@Act_AppUsage, appUsageStatsMaps)
        updateLoadingForAdapter(true)

        initTabs()
        loadInfo(DateTime.now().withTimeAtStartOfDay(), DateTime.now())

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

    private fun initTabs() {
        val offset = 8f.px
        tab_foreground.post {
            tab_bg.layoutParams.width = (tab_foreground.width - offset * 2).toInt()
            tab_bg.x = tab_foreground.x + tab_layout.x + offset
        }

        tab_foreground.setOnClickListener {
            if (!showInForeground) {
                showInForeground = true
                tab_bg.animate().x(tab_foreground.x + tab_layout.x + offset).setDuration(150).withStartAction {
                    showLoading(true)
                }.withEndAction {
                    Handler().post {
                        showLoading(false)
                        (list.adapter as AppUsageStatsAdapter).setShowInForeground(showInForeground)
                    }
                }.start()
            }
        }

        tab_totale.setOnClickListener {
            if (showInForeground) {
                showInForeground = false
                tab_bg.animate().x(tab_totale.x + tab_layout.x + offset).setDuration(150).withStartAction {
                    showLoading(true)
                }.withEndAction {
                    Handler().post {
                        showLoading(false)
                        (list.adapter as AppUsageStatsAdapter).setShowInForeground(showInForeground)
                    }
                }.start()
            }
        }
    }

    private fun loadInfo(start: DateTime, end: DateTime) {
        showLoading(true)

        Handler().postDelayed({
            doAsync {
                val usages: MutableMap<String, UsageStats>? = usageService.loadUsageSummaryStats(start, end)
                val usageDetails: MutableList<UsageStats>? = usageService.loadUsageStats(start, end)

                appUsageStatsMaps.init(packageManager, usages, usageDetails)

                uiThread {
                    showLoading(false)
                }
            }
        }, 100)
    }

    private fun showLoading(value: Boolean) {
        (list.adapter as AppUsageStatsAdapter).setShowLoading(value)
        if (value) {
            loading.visibility = View.VISIBLE
        } else {
            loading.visibility = View.GONE
        }
    }

    private fun updateLoadingForAdapter(value: Boolean) {
        (list.adapter as AppUsageStatsAdapter).setShowLoading(value)
    }
}