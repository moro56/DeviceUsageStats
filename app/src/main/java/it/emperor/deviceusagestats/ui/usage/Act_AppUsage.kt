package it.emperor.deviceusagestats.ui.usage

import android.app.ActivityOptions
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.PopupMenu
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.Pair
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import it.emperor.deviceusagestats.R
import it.emperor.deviceusagestats.events.AppDetailEvent
import it.emperor.deviceusagestats.extensions.px
import it.emperor.deviceusagestats.extensions.toTimeString
import it.emperor.deviceusagestats.extensions.toTimeStringArray
import it.emperor.deviceusagestats.models.AppTimeType
import it.emperor.deviceusagestats.services.UsageService
import it.emperor.deviceusagestats.ui.base.BaseActivity
import it.emperor.deviceusagestats.ui.detail.appDetail
import it.emperor.deviceusagestats.ui.usage.formatters.PercFormatter
import it.emperor.deviceusagestats.ui.usage.model.AppUsageStatsMaps
import it.emperor.deviceusagestats.ui.views.RoundProgressBar
import it.emperor.deviceusagestats.utils.EasySpan
import kotlinx.android.synthetic.main.act_appusage.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.joda.time.DateTime
import javax.inject.Inject


class Act_AppUsage : BaseActivity() {

    @Inject
    private lateinit var usageService: UsageService

    private lateinit var appUsageStatsMaps: AppUsageStatsMaps
    private lateinit var timeType: AppTimeType
    private lateinit var start: DateTime
    private lateinit var end: DateTime

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayShowTitleEnabled(true)
        updateToolbarSubtitleWithAppFilter(timeType)
    }

    override fun getLayoutId(): Int {
        return R.layout.act_appusage
    }

    override fun initVariables() {
        appUsageStatsMaps = AppUsageStatsMaps(packageManager)
        timeType = AppTimeType.DAY
        start = DateTime.now().withTimeAtStartOfDay()
        end = DateTime.now()
    }

    override fun loadParameters(extras: Bundle) {
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
                            timeType = AppTimeType.DAY
                            start = DateTime.now().withTimeAtStartOfDay()
                            end = DateTime.now()
                            update()
                            updateToolbarSubtitleWithAppFilter(timeType)
                        }
                        R.id.week -> {
                            timeType = AppTimeType.WEEK
                            start = DateTime.now().minusDays(7).withTimeAtStartOfDay()
                            end = DateTime.now()
                            update()
                            updateToolbarSubtitleWithAppFilter(timeType)
                        }
                        R.id.month -> {
                            timeType = AppTimeType.MONTH
                            start = DateTime.now().minusMonths(1).withTimeAtStartOfDay()
                            end = DateTime.now()
                            update()
                            updateToolbarSubtitleWithAppFilter(timeType)
                        }
                        R.id.year -> {
                            timeType = AppTimeType.YEAR
                            start = DateTime.now().minusYears(1).withTimeAtStartOfDay()
                            end = DateTime.now()
                            update()
                            updateToolbarSubtitleWithAppFilter(timeType)
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
        appusage_chart.setUsePercentValues(true)
        appusage_chart.description.isEnabled = false
        appusage_chart.isDrawHoleEnabled = true
        appusage_chart.setHoleColor(Color.TRANSPARENT)
        appusage_chart.setEntryLabelColor(Color.WHITE)
        appusage_chart.holeRadius = 48f
        appusage_chart.legend.textColor = Color.WHITE
        appusage_chart.legend.xOffset = 0f.px

        update()

        apps_show_all.setOnClickListener {
            startActivity(appusageAppList(timeType, start, end))
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

        val totalUseArray = appUsageStatsMaps.totalForeground.toTimeStringArray()

        val easySpan = EasySpan.Builder()
        for (i in 0 until totalUseArray.size) {
            val element = totalUseArray[i]
            if (i % 2 == 0) {
                easySpan.appendText(element)
            } else {
                easySpan.appendSpans(element, RelativeSizeSpan(0.7f), ForegroundColorSpan(getColor(R.color.system_primary)))
            }
        }

        total.text = easySpan.build().spannedText

        var appList = appUsageStatsMaps.totalUsage.toList().sortedByDescending { it.second.timeInForeground }
        val maxElements = Math.min(3, appList.size)
        appList = appList.subList(0, maxElements)

        val entries = mutableListOf<PieEntry>()
        var entrySum = 0f
        for (app in appList) {
            entrySum += app.second.timeInForeground.toFloat()
            entries.add(PieEntry(app.second.timeInForeground.toFloat(), app.second.name))
        }
        val perc = entrySum / appUsageStatsMaps.totalForeground
        val onePercValue = entrySum / (perc * 100f)
        val other = onePercValue * ((1f - perc) * 100)
        entries.add(PieEntry(other, "Others"))

        val dataSet = PieDataSet(entries, "")
        dataSet.setDrawIcons(false)
        dataSet.sliceSpace = 3f
        dataSet.valueTextSize = 15f
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueFormatter = PercFormatter()

        val colors = mutableListOf<Int>()
        for (c in ColorTemplate.COLORFUL_COLORS) {
            colors.add(c)
        }
        dataSet.colors = colors

        val data = PieData(dataSet)
        appusage_chart.data = data
        appusage_chart.animateY(400, Easing.EasingOption.EaseInOutQuad)

        showMostUsedApps()
    }

    private fun showMostUsedApps() {
        clearIcons()
        apps_layout.removeAllViews()

        var appList = appUsageStatsMaps.totalUsage.toList()
        appList = appList.sortedByDescending { it.second.timeInForeground }
        val maxElements = Math.min(3, appList.size)
        val diff = appList.size - maxElements
        appList = appList.subList(0, maxElements)

        for (app in appList) {
            val view = layoutInflater.inflate(R.layout.item_appusage_app, apps_layout, false)

            val row: View = view.findViewById<View>(R.id.row)
            val icon: ImageView = view.findViewById(R.id.icon)
            val name: TextView = view.findViewById(R.id.name)
            val packageName: TextView = view.findViewById(R.id.package_name)
            val progress: RoundProgressBar = view.findViewById(R.id.progress)
            val progressPerc: TextView = view.findViewById(R.id.progress_perc)

            val value = app.second.timeInForeground.toFloat()
            val perc: Float = value / appUsageStatsMaps.totalForeground.toFloat()

            name.text = app.second.name
            packageName.text = app.second.packageName
            progress.setProgress(perc)
            progressPerc.text = value.toLong().toTimeString(true)

            app.second.icon?.let { Glide.with(this).load(app.second.icon).into(icon) }
                    ?: kotlin.run { Glide.with(this).load(R.drawable.default_app).into(icon) }

            row.setOnClickListener {
                onEvent(AppDetailEvent(app.second.packageName, icon, name))
            }

            apps_layout.addView(view)
        }

        if (diff != 0) {
            apps_show_all.visibility = View.VISIBLE
        } else {
            apps_show_all.visibility = View.GONE
        }
    }

    private fun clearIcons() {
        for (i in 0 until apps_layout.childCount) {
            val view = apps_layout.getChildAt(i)
            val icon = view.findViewById<ImageView>(R.id.icon)
            Glide.with(this).clear(icon)
        }
    }
}