package it.emperor.deviceusagestats.ui.network

import android.app.ActivityOptions
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.PopupMenu
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Pair
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import it.emperor.deviceusagestats.R
import it.emperor.deviceusagestats.events.AppDetailEvent
import it.emperor.deviceusagestats.extensions.formatBytes
import it.emperor.deviceusagestats.extensions.formatBytesWithDecimal
import it.emperor.deviceusagestats.extensions.px
import it.emperor.deviceusagestats.models.NetworkTimeType
import it.emperor.deviceusagestats.services.UsageService
import it.emperor.deviceusagestats.ui.base.BaseActivity
import it.emperor.deviceusagestats.ui.detail.appDetail
import it.emperor.deviceusagestats.ui.network.formatters.NetworkAxisFormatter
import it.emperor.deviceusagestats.ui.network.formatters.TimeAxisFormatter
import it.emperor.deviceusagestats.ui.network.model.NetworkStatsInternal
import it.emperor.deviceusagestats.ui.network.model.NetworkStatsMaps
import it.emperor.deviceusagestats.ui.views.RoundProgressBar
import it.emperor.deviceusagestats.ui.views.WheelProgressView
import it.emperor.deviceusagestats.utils.EasySpan
import kotlinx.android.synthetic.main.act_network.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.joda.time.DateTime
import javax.inject.Inject

class Act_Network : BaseActivity(), OnChartValueSelectedListener {

    @Inject
    private lateinit var usageService: UsageService

    private lateinit var networkUsageMaps: NetworkStatsMaps
    private lateinit var timeType: NetworkTimeType
    private lateinit var start: DateTime
    private lateinit var end: DateTime
    private var showRx: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayShowTitleEnabled(true)
        updateToolbarSubtitleWithNetworkFilter(timeType)
    }

    override fun getLayoutId(): Int {
        return R.layout.act_network
    }

    override fun initVariables() {
        networkUsageMaps = NetworkStatsMaps(packageManager)
        timeType = NetworkTimeType.TODAY
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
        inflater.inflate(R.menu.act_network, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.filter -> {
                val popupMenu = PopupMenu(this, placeholder, Gravity.BOTTOM)
                popupMenu.inflate(R.menu.act_network_filter)
                popupMenu.setOnMenuItemClickListener {
                    chart_bubble.visibility = View.GONE
                    when (it.itemId) {
                        R.id.today -> {
                            timeType = NetworkTimeType.TODAY
                            start = DateTime.now().withTimeAtStartOfDay()
                            end = DateTime.now()
                            update()
                            updateToolbarSubtitleWithNetworkFilter(timeType)
                        }
                        R.id.week -> {
                            timeType = NetworkTimeType.WEEK
                            start = DateTime.now().minusDays(7).withTimeAtStartOfDay()
                            end = DateTime.now()
                            update()
                            updateToolbarSubtitleWithNetworkFilter(timeType)
                        }
                        R.id.month -> {
                            timeType = NetworkTimeType.MONTH
                            start = DateTime.now().minusMonths(1).withTimeAtStartOfDay()
                            end = DateTime.now()
                            update()
                            updateToolbarSubtitleWithNetworkFilter(timeType)
                        }
                        R.id.last_month -> {
                            timeType = NetworkTimeType.LAST_MONTH
                            start = DateTime.now().minusMonths(2).withTimeAtStartOfDay()
                            end = DateTime.now().minusMonths(1)
                            update()
                            updateToolbarSubtitleWithNetworkFilter(timeType)
                        }
                        R.id.year -> {
                            timeType = NetworkTimeType.YEAR
                            start = DateTime.now().minusYears(1).withTimeAtStartOfDay()
                            end = DateTime.now()
                            update()
                            updateToolbarSubtitleWithNetworkFilter(timeType)
                        }
                        R.id.custom -> {
                            timeType = NetworkTimeType.CUSTOM

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
        network_chart.description.text = ""
        network_chart.isScaleYEnabled = false
        network_chart.legend.textColor = Color.WHITE
        network_chart.setViewPortOffsets(0f, 15f.px, 0f, 40f.px)
//        val marker = MarkerImage(this, R.drawable.ic_pin)
//        marker.setOffset((-4f).px, (-4f).px)
//        network_chart.marker = marker
        network_chart.setOnChartValueSelectedListener(this)

        val xAxis: XAxis = network_chart.xAxis
        xAxis.gridColor = getColor(R.color.system_background_light)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = Color.WHITE
        xAxis.setAvoidFirstLastClipping(true)
        xAxis.granularity = 1f

        val yAxisLeft: YAxis = network_chart.axisLeft
        yAxisLeft.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
        yAxisLeft.setDrawGridLines(false)
        yAxisLeft.valueFormatter = NetworkAxisFormatter()
        yAxisLeft.textColor = Color.WHITE
        yAxisLeft.axisMinimum = 0f
        val yAxisRight: YAxis = network_chart.axisRight
        yAxisRight.isEnabled = false

        update()

        rx_tx_switch.setOnCheckedChangeListener { _, checked ->
            run {
                showRx = checked
                chart_bubble.visibility = View.GONE
                network_chart.hideHighlight()
                if (checked) {
                    bytes_title.text = getString(R.string.network_rx_bytes_title)
                    rx_tx_switch.text = getString(R.string.network_switch_on)
                } else {
                    bytes_title.text = getString(R.string.network_tx_bytes_title)
                    rx_tx_switch.text = getString(R.string.network_switch_off)
                }
                updateRxTx()
            }
        }

        apps_show_all.setOnClickListener {
            startActivity(networkAppList(timeType, start, end))
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
        rx_tx_switch.visibility = View.GONE

        val xAxis: XAxis = network_chart.xAxis
        xAxis.valueFormatter = null
        network_chart.data = null
        network_chart.hideHighlight()

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
        val wifi = usageService.loadNetworkSummaryForUserStats(UsageService.WIFI, start, end)
        val mobile = usageService.loadNetworkSummaryForUserStats(UsageService.MOBILE, start, end)

        networkUsageMaps.update(wifi, mobile, timeType,
                usageService.loadNetworkDetailStats(UsageService.WIFI, start, end),
                usageService.loadNetworkDetailStats(UsageService.MOBILE, start, end),
                usageService.loadNetworkSummaryStats(UsageService.WIFI, start, end),
                usageService.loadNetworkSummaryStats(UsageService.MOBILE, start, end))
    }

    private fun updateViews() {
        val total = if (showRx) networkUsageMaps.rxWifiTotal + networkUsageMaps.rxMobileTotal else networkUsageMaps.txWifiTotal + networkUsageMaps.txMobileTotal

        loading.visibility = View.GONE
        rx_tx_switch.visibility = View.VISIBLE

        updateBytesValueAndUnit(total, bytes, bytes_unit)
        updatePacketsValueAndUnit(total, packets, packets_unit)

        updateProgressWheel(if (showRx) networkUsageMaps.rxWifiTotal else networkUsageMaps.txWifiTotal, total, progress_wifi, progress_wifi_text, R.string.type_wifi)
        updateProgressWheel(if (showRx) networkUsageMaps.rxMobileTotal else networkUsageMaps.txMobileTotal, total, progress_mobile, progress_mobile_text, R.string.type_mobile)
        updateProgressWheel(if (showRx) networkUsageMaps.rxBackgroundTotal else networkUsageMaps.txBackgroundTotal, total, progress_background, progress_background_text, R.string.type_background)
        updateProgressWheel(if (showRx) networkUsageMaps.rxForegroundTotal else networkUsageMaps.txForegroundTotal, total, progress_foreground, progress_foreground_text, R.string.type_foreground)

        updateCharMaxYValue(networkUsageMaps.maxValue)
        loadChart()
        showMostUsedApps()
    }

    private fun updateRxTx() {
        loading.visibility = View.VISIBLE

        val xAxis: XAxis = network_chart.xAxis
        xAxis.valueFormatter = null
        network_chart.data = null
        network_chart.invalidate()

        updateViews()
    }

    private fun loadChart() {
        val wifiEntry: MutableList<Entry> = mutableListOf()
        val wifiMap = if (showRx) networkUsageMaps.wifi.rxByTime else networkUsageMaps.wifi.txByTime
        if (wifiMap.isNotEmpty()) {
            for ((i, key) in networkUsageMaps.rxByTime.withIndex()) {
                wifiEntry.add(Entry(i.toFloat(), wifiMap[key]!!.toFloat()))
            }
        } else {
            wifiEntry.add(Entry(0f, 0f))
        }
        val wifiDataSet = LineDataSet(wifiEntry, "Wifi")
        wifiDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        wifiDataSet.setDrawCircles(false)
        wifiDataSet.setDrawFilled(true)
        wifiDataSet.setDrawValues(false)
        wifiDataSet.setDrawHorizontalHighlightIndicator(false)
        wifiDataSet.setDrawVerticalHighlightIndicator(false)
//        wifiDataSet.highlightLineWidth = 0.5f.px
//        wifiDataSet.highLightColor = ContextCompat.getColor(this, R.color.system_background_dark)
        wifiDataSet.color = getColor(R.color.system_secondary)
        wifiDataSet.fillColor = getColor(R.color.system_secondary)
        wifiDataSet.fillAlpha = 150
        wifiDataSet.axisDependency = YAxis.AxisDependency.LEFT

        val mobileEntry: MutableList<Entry> = mutableListOf()
        val mobileMap = if (showRx) networkUsageMaps.mobile.rxByTime else networkUsageMaps.mobile.txByTime
        if (mobileMap.isNotEmpty()) {
            for ((i, key) in networkUsageMaps.rxByTime.withIndex()) {
                mobileEntry.add(Entry(i.toFloat(), mobileMap[key]!!.toFloat()))
            }
        } else {
            mobileEntry.add(Entry(0f, 0f))
        }
        val mobileDataSet = LineDataSet(mobileEntry, "Mobile")
        mobileDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        mobileDataSet.setDrawCircles(false)
        mobileDataSet.setDrawFilled(true)
        mobileDataSet.setDrawValues(false)
        mobileDataSet.setDrawHorizontalHighlightIndicator(false)
        mobileDataSet.setDrawVerticalHighlightIndicator(false)
//        mobileDataSet.highlightLineWidth = 0.5f.px
//        mobileDataSet.highLightColor = ContextCompat.getColor(this, R.color.system_background_light)
        mobileDataSet.color = getColor(R.color.system_tertiary)
        mobileDataSet.fillColor = getColor(R.color.system_tertiary)
        mobileDataSet.fillAlpha = 150
        mobileDataSet.axisDependency = YAxis.AxisDependency.LEFT

        val dataSets: MutableList<ILineDataSet> = mutableListOf()
        dataSets.add(mobileDataSet)
        dataSets.add(wifiDataSet)

        val data = LineData(dataSets)

        val xAxis: XAxis = network_chart.xAxis
        xAxis.valueFormatter = TimeAxisFormatter(timeType, networkUsageMaps.rxByTime)

        network_chart.data = data
        network_chart.invalidate()
    }

    override fun onNothingSelected() {
        chart_bubble.visibility = View.GONE
        network_chart.hideHighlight()
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        val highlight = arrayOfNulls<Highlight>(network_chart.data.dataSets.size)
        for (j in 0 until network_chart.data.dataSets.size) {
            val iDataSet = network_chart.data.dataSets[j]

            var x = 0
            for (i in 0 until (iDataSet as LineDataSet).values.size) {
                if (iDataSet.values[i].x == e?.x) {
                    highlight[j] = Highlight(e.x, e.y, j)
                    x = e.x.toInt()
                }
            }

            try {
                val xText = network_chart.xAxis.valueFormatter.getFormattedValue(x.toFloat(), null)
                val xTextPrev = if (x >= 1) network_chart.xAxis.valueFormatter.getFormattedValue((x - 1).toFloat(), null) else ""
                val xValue = networkUsageMaps.rxByTime[x]
                val wifi = if (showRx) networkUsageMaps.wifi.rxByTime[xValue] else networkUsageMaps.wifi.txByTime[xValue]
                val mobile = if (showRx) networkUsageMaps.mobile.rxByTime[xValue] else networkUsageMaps.mobile.txByTime[xValue]

                chart_bubble.visibility = View.VISIBLE
                chart_bubble.text = getString(R.string.network_bubble_format).format(xTextPrev, xText, NetworkStatsInternal().formatValue(wifi?.toLong()), NetworkStatsInternal().formatValue(mobile?.toLong()))
            } catch (ex: Exception) {
            }
        }
        network_chart.highlightValues(highlight)

        val position = h?.x!!
        network_chart.highightSpace(position.toInt())
    }

    private fun updateBytesValueAndUnit(value: Long?, valueView: TextView, unitView: TextView) {
        val tera: Double = value?.div(Math.pow(2.0, 40.0)) ?: 0.0
        val giga: Double = value?.div(Math.pow(2.0, 30.0)) ?: 0.0
        val mega: Double = value?.div(Math.pow(2.0, 20.0)) ?: 0.0
        val kilo: Double = value?.div(Math.pow(2.0, 10.0)) ?: 0.0

        when {
            tera > 1.0 -> {
                valueView.text = getString(R.string.network_value_format).format(tera)
                unitView.text = getString(R.string.unit_tera)
            }
            giga > 1.0 -> {
                valueView.text = getString(R.string.network_value_format).format(giga)
                unitView.text = getString(R.string.unit_giga)
            }
            mega > 1.0 -> {
                valueView.text = getString(R.string.network_value_format).format(mega)
                unitView.text = getString(R.string.unit_mega)
            }
            else -> {
                valueView.text = getString(R.string.network_value_format).format(kilo)
                unitView.text = getString(R.string.unit_kilo)
            }
        }
    }

    private fun updatePacketsValueAndUnit(value: Long?, valueView: TextView, unitView: TextView) {
        val kilo: Double = value?.div(Math.pow(2.0, 10.0)) ?: 0.0
        if (kilo == 0.0) {
            valueView.text = getString(R.string.network_packet_format2).format(0)
        } else {
            valueView.text = getString(R.string.network_packet_format2).format(value)
        }
        unitView.text = getString(R.string.network_packet_unit)
    }

    private fun updateProgressWheel(value: Long?, total: Long, progressWheel: WheelProgressView, progressWheelText: TextView, typeString: Int) {
        val realValue = value ?: 0
        val perc = realValue.toFloat() / total.toFloat()

        progressWheel.setFilledPercent((perc * 100).toInt())
        progressWheel.setWheelIndicatorItem(2f, progressWheel.indicatorColor)
        progressWheel.startItemsAnimation()

        val easySpan: EasySpan.Builder = EasySpan.Builder()
        easySpan.appendSpans(getString(typeString), RelativeSizeSpan(0.65f))
                .appendText("\n")
                .appendSpans(realValue.toFloat().formatBytes(), StyleSpan(Typeface.BOLD))
//                .appendSpans(getString(R.string.network_value_format_no_point).format(perc * 100f), StyleSpan(Typeface.BOLD))
//                .appendText("%")
        progressWheelText.text = easySpan.build().spannedText
    }

    private fun updateCharMaxYValue(max: Long) {
        val yAxisLeft: YAxis = network_chart.axisLeft
        yAxisLeft.axisMaximum = max.toFloat()
    }

    private fun showMostUsedApps() {
        clearIcons()
        apps_layout.removeAllViews()

        var appList = if (showRx) networkUsageMaps.all.rxByApp.toList() else networkUsageMaps.all.txByApp.toList()
        appList = appList.sortedByDescending { it.second.valueDownload + it.second.valueUpload }
        val maxElements = Math.min(3, appList.size)
        val diff = appList.size - maxElements
        appList = appList.subList(0, maxElements)

        for (app in appList) {
            val view = layoutInflater.inflate(R.layout.item_network_app, apps_layout, false)

            val row: View = view.findViewById<View>(R.id.row)
            val icon: ImageView = view.findViewById(R.id.icon)
            val name: TextView = view.findViewById(R.id.name)
            val packageName: TextView = view.findViewById(R.id.package_name)
            val progress: RoundProgressBar = view.findViewById(R.id.progress)
            val progressPerc: TextView = view.findViewById(R.id.progress_perc)

            val value = if (showRx) app.second.valueDownload.toFloat() else app.second.valueUpload.toFloat()
            val perc: Float = if (showRx) value / networkUsageMaps.all.rxByAppTotal.toFloat() else value / networkUsageMaps.all.txByAppTotal.toFloat()

            name.text = app.second.name
            packageName.text = app.second.packageName
            progress.setProgress(perc)
            progressPerc.text = value.formatBytesWithDecimal()

            app.second.icon?.let { Glide.with(this).load(app.second.icon).into(icon) }
                    ?: kotlin.run { Glide.with(this).load(R.drawable.default_app).into(icon) }

            row.setOnClickListener {
                onEvent(AppDetailEvent(app.second.packageName.toString(), icon, name))
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