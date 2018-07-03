package it.emperor.deviceusagestats.ui.network

import android.app.ActivityOptions
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.transition.Fade
import android.transition.Slide
import android.transition.TransitionManager
import android.transition.TransitionSet
import android.util.Pair
import android.view.*
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerImage
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
import it.emperor.deviceusagestats.extensions.px
import it.emperor.deviceusagestats.services.RxBus
import it.emperor.deviceusagestats.services.UsageService
import it.emperor.deviceusagestats.ui.base.BaseActivity
import it.emperor.deviceusagestats.ui.detail.appDetail
import it.emperor.deviceusagestats.ui.network.adapters.NetworkStatsAdapter
import it.emperor.deviceusagestats.ui.network.formatters.TimeAxisFormatter
import it.emperor.deviceusagestats.ui.network.gestures.FlingGestureListener
import it.emperor.deviceusagestats.ui.network.model.NetworkStats
import it.emperor.deviceusagestats.ui.network.model.NetworkStatsMaps
import it.emperor.deviceusagestats.ui.network.model.NetworkStatsMapsTimeType
import kotlinx.android.synthetic.main.act_network.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.joda.time.DateTime
import javax.inject.Inject

class Act_Network : BaseActivity(), OnChartValueSelectedListener {

    @Inject
    private lateinit var usageService: UsageService
    @Inject
    private lateinit var rxBus: RxBus

    private lateinit var gestureDetector: GestureDetector
    private lateinit var networkUsageMaps: NetworkStatsMaps
    private lateinit var networkUsageMapsTimeType: NetworkStatsMapsTimeType
    private var showRx: Boolean = true

    override fun getLayoutId(): Int {
        return R.layout.act_network
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
                    chart_bubble.visibility = View.GONE
                    when (it.itemId) {
                        R.id.today -> {
                            networkUsageMapsTimeType = NetworkStatsMapsTimeType.HOUR
                            loadInfo(networkUsageMapsTimeType, DateTime.now().withTimeAtStartOfDay(), DateTime.now())
                        }
                        R.id.week -> {
                            networkUsageMapsTimeType = NetworkStatsMapsTimeType.DAY
                            loadInfo(networkUsageMapsTimeType, DateTime.now().minusDays(7).withTimeAtStartOfDay(), DateTime.now())
                        }
                        R.id.month -> {
                            networkUsageMapsTimeType = NetworkStatsMapsTimeType.DAY
                            loadInfo(networkUsageMapsTimeType, DateTime.now().minusMonths(1).withTimeAtStartOfDay(), DateTime.now())
                        }
                        R.id.last_month -> {
                            networkUsageMapsTimeType = NetworkStatsMapsTimeType.DAY
                            loadInfo(networkUsageMapsTimeType, DateTime.now().minusMonths(2).withTimeAtStartOfDay(), DateTime.now().minusMonths(1))
                        }
                        R.id.year -> {
                            networkUsageMapsTimeType = NetworkStatsMapsTimeType.DAY
                            loadInfo(networkUsageMapsTimeType, DateTime.now().minusYears(1).withTimeAtStartOfDay(), DateTime.now())
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

    override fun onBackPressed() {
        if (network_chart.visibility == View.GONE) {
            animateAppUsageOut()
            return
        }
        super.onBackPressed()
    }

    override fun initialize() {
        gestureDetector = GestureDetector(this, FlingGestureListener {
            onFling(it)
        })
        networkUsageMaps = NetworkStatsMaps()

        container.setOnTouchListener { _, motionEvent -> gestureDetector.onTouchEvent(motionEvent) }

        network_chart.description.text = ""
        network_chart.isScaleYEnabled = false
        network_chart.legend.textColor = Color.WHITE
        val marker = MarkerImage(this, R.drawable.ic_pin)
        marker.setOffset((-4f).px, (-4f).px)
        network_chart.marker = marker
        network_chart.setOnChartValueSelectedListener(this)

        val xAxis: XAxis = network_chart.xAxis
        xAxis.gridColor = ContextCompat.getColor(this, R.color.system_background_light)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = Color.WHITE
        xAxis.granularity = 1f

        val yAxisLeft: YAxis = network_chart.axisLeft
        yAxisLeft.setDrawLabels(false)
        yAxisLeft.setDrawAxisLine(false)
        yAxisLeft.setDrawGridLines(false)
        yAxisLeft.axisMinimum = 0f
        val yAxisRight: YAxis = network_chart.axisRight
        yAxisRight.setDrawLabels(false)
        yAxisRight.setDrawAxisLine(false)
        yAxisRight.setDrawGridLines(false)
        yAxisLeft.axisMinimum = 0f

        list.layoutManager = LinearLayoutManager(this)

        rx_tx_switch.setOnCheckedChangeListener { _, checked ->
            run {
                showRx = checked; loadChart()
                chart_bubble.visibility = View.GONE
                if (checked) {
                    rx_tx_switch.text = getString(R.string.network_switch_on)
                } else {
                    rx_tx_switch.text = getString(R.string.network_switch_off)
                }
            }
        }

        networkUsageMapsTimeType = NetworkStatsMapsTimeType.HOUR
        loadInfo(networkUsageMapsTimeType, DateTime.now().withTimeAtStartOfDay(), DateTime.now())

        showmore.setOnClickListener {
            animateAppUsageIn()
        }
        showless.setOnClickListener {
            animateAppUsageOut()
        }

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

    private fun loadInfo(timeType: NetworkStatsMapsTimeType, start: DateTime, end: DateTime) {
        list.adapter = NetworkStatsAdapter(this, mutableListOf())

        max_y.visibility = View.GONE
        middle_y.visibility = View.GONE
        val xAxis: XAxis = network_chart.xAxis
        xAxis.valueFormatter = null
        network_chart.data = null
        network_chart.invalidate()

        rx_tx_switch.visibility = View.GONE

        Handler().postDelayed({
            doAsync {
                val wifi = usageService.loadNetworkSummaryStats(UsageService.WIFI, start, end)
                val mobile = usageService.loadNetworkSummaryStats(UsageService.MOBILE, start, end)

                uiThread {
                    updateValueAndUnit((mobile?.rxBytes?.let { wifi?.rxBytes?.plus(it) }), rx_bytes_value, rx_bytes_unit)
                    updateValueAndUnit((mobile?.txBytes?.let { wifi?.txBytes?.plus(it) }), tx_bytes_value, tx_bytes_unit)

                    updateValueAndUnit(wifi?.rxBytes, rx_bytes_wifi_value, rx_bytes_wifi_unit)
                    updateValueAndUnit(mobile?.rxBytes, rx_bytes_mobile_value, rx_bytes_mobile_unit)
                    updateValueAndUnit(wifi?.txBytes, tx_bytes_wifi_value, tx_bytes_wifi_unit)
                    updateValueAndUnit(mobile?.txBytes, tx_bytes_mobile_value, tx_bytes_mobile_unit)

                    updateValueAndUnit(wifi?.rxPackets, rx_packets_wifi)
                    updateValueAndUnit(wifi?.txPackets, tx_packets_wifi)
                    updateValueAndUnit(mobile?.rxPackets, rx_packets_mobile)
                    updateValueAndUnit(mobile?.txPackets, tx_packets_mobile)
                }

                networkUsageMaps.init(packageManager,
                        timeType,
                        usageService.loadNetworkDetailStats(UsageService.WIFI, start, end),
                        usageService.loadNetworkDetailStats(UsageService.MOBILE, start, end))

                uiThread {
                    updateCharMaxYValue(networkUsageMaps.maxValue)
                    loadChart()
                    list.adapter = NetworkStatsAdapter(this@Act_Network,
                            networkUsageMaps.rxWifiByApp.toList().sortedByDescending { it.second.valueDownload + it.second.valueUpload })
                }
            }
        }, 100)
    }

    private fun loadChart() {
        val wifiEntry: MutableList<Entry> = mutableListOf()
        val wifiMap = if (showRx) networkUsageMaps.rxWifiByTime else networkUsageMaps.txWifiByTime
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
        wifiDataSet.highlightLineWidth = 0.5f.px
        wifiDataSet.highLightColor = ContextCompat.getColor(this, R.color.system_background_dark)
        wifiDataSet.color = ContextCompat.getColor(this, R.color.system_primary)
        wifiDataSet.fillColor = ContextCompat.getColor(this, R.color.system_primary)
        wifiDataSet.fillAlpha = 150
        wifiDataSet.axisDependency = YAxis.AxisDependency.LEFT

        val mobileEntry: MutableList<Entry> = mutableListOf()
        val mobileMap = if (showRx) networkUsageMaps.rxMobileByTime else networkUsageMaps.txMobileByTime
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
        mobileDataSet.highlightLineWidth = 0.5f.px
        mobileDataSet.highLightColor = ContextCompat.getColor(this, R.color.system_background_light)
        mobileDataSet.color = ContextCompat.getColor(this, R.color.system_tertiary)
        mobileDataSet.fillColor = ContextCompat.getColor(this, R.color.system_tertiary)
        mobileDataSet.fillAlpha = 150
        mobileDataSet.axisDependency = YAxis.AxisDependency.LEFT

        val dataSets: MutableList<ILineDataSet> = mutableListOf()
        dataSets.add(mobileDataSet)
        dataSets.add(wifiDataSet)

        val data = LineData(dataSets)

        val xAxis: XAxis = network_chart.xAxis
        xAxis.valueFormatter = TimeAxisFormatter(networkUsageMapsTimeType, networkUsageMaps.rxByTime)

        max_y.visibility = View.VISIBLE
        middle_y.visibility = View.VISIBLE
        network_chart.data = data
        network_chart.invalidate()

        if (network_chart.visibility == View.VISIBLE) {
            rx_tx_switch.visibility = View.VISIBLE
        }
    }

    override fun onNothingSelected() {
        chart_bubble.visibility = View.GONE
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
                val xValue = networkUsageMaps.rxByTime[x]
                val wifi = if (showRx) networkUsageMaps.rxWifiByTime[xValue] else networkUsageMaps.txWifiByTime[xValue]
                val mobile = if (showRx) networkUsageMaps.rxMobileByTime[xValue] else networkUsageMaps.txMobileByTime[xValue]

                chart_bubble.visibility = View.VISIBLE
                chart_bubble.text = getString(R.string.network_bubble_format).format(xText, NetworkStats().formatValue(wifi?.toLong()), NetworkStats().formatValue(mobile?.toLong()))
            } catch (ex: Exception) {
            }
        }
        network_chart.highlightValues(highlight)
    }

    private fun updateValueAndUnit(value: Long?, valueView: TextView, unitView: TextView) {
        val tera: Double = value?.div(Math.pow(2.0, 40.0)) ?: 0.0
        val giga: Double = value?.div(Math.pow(2.0, 30.0)) ?: 0.0
        val mega: Double = value?.div(Math.pow(2.0, 20.0)) ?: 0.0
        val kilo: Double = value?.div(Math.pow(2.0, 10.0)) ?: 0.0

        when {
            tera > 1.0 -> {
                valueView.text = getString(R.string.network_value_format).format(tera)
                unitView.text = getString(R.string.network_unit_tera)
            }
            giga > 1.0 -> {
                valueView.text = getString(R.string.network_value_format).format(giga)
                unitView.text = getString(R.string.network_unit_giga)
            }
            mega > 1.0 -> {
                valueView.text = getString(R.string.network_value_format).format(mega)
                unitView.text = getString(R.string.network_unit_mega)
            }
            else -> {
                valueView.text = getString(R.string.network_value_format).format(kilo)
                unitView.text = getString(R.string.network_unit_kilo)
            }
        }
    }

    private fun updateCharMaxYValue(max: Long) {
        max_y.text = NetworkStats().formatValueNoPoint(max)
        middle_y.text = NetworkStats().formatValueNoPoint(max / 2)

        val yAxisLeft: YAxis = network_chart.axisLeft
        yAxisLeft.axisMaximum = max.toFloat()
        val yAxisRight: YAxis = network_chart.axisRight
        yAxisRight.axisMaximum = max.toFloat()
    }

    private fun updateValueAndUnit(value: Long?, valueView: TextView) {
        val kilo: Double = value?.div(Math.pow(2.0, 10.0)) ?: 0.0
        if (kilo == 0.0) {
            valueView.text = "0"
        } else {
            valueView.text = getString(R.string.network_packet_format).format(kilo)
        }
    }

    private fun onFling(isDown: Boolean) {
        if (isDown) {
            if (network_chart.visibility == View.GONE) {
                onBackPressed()
            }
        } else {
            animateAppUsageIn()
        }
    }

    private fun animateAppUsageIn() {
        val transitionSet = TransitionSet()
        transitionSet.addTransition(Fade()
                .addTarget(network_chart)
                .addTarget(rx_tx_switch)
                .addTarget(max_y)
                .addTarget(middle_y)
                .addTarget(chart_bubble)
                .addTarget(more_info_layout)
                .addTarget(more_info_title)
                .addTarget(showmore))
        transitionSet.addTransition(Slide()
                .addTarget(list))
        TransitionManager.beginDelayedTransition(container, transitionSet)

        list.visibility = View.VISIBLE
        network_chart.visibility = View.GONE
        rx_tx_switch.visibility = View.GONE
        max_y.visibility = View.GONE
        middle_y.visibility = View.GONE
        chart_bubble.visibility = View.GONE
        more_info_layout.visibility = View.GONE
        more_info_title.visibility = View.GONE
        showmore.visibility = View.GONE
        list_title_container.visibility = View.VISIBLE
    }

    private fun animateAppUsageOut() {
        val transitionSet = TransitionSet()
        transitionSet.addTransition(Fade()
                .addTarget(network_chart)
                .addTarget(rx_tx_switch)
                .addTarget(max_y)
                .addTarget(middle_y)
                .addTarget(more_info_layout)
                .addTarget(more_info_title)
                .addTarget(showmore))
        transitionSet.addTransition(Slide()
                .addTarget(list))
        TransitionManager.beginDelayedTransition(container, transitionSet)

        network_chart.visibility = View.VISIBLE
        if (network_chart.data != null) {
            rx_tx_switch.visibility = View.VISIBLE
        }
        max_y.visibility = View.VISIBLE
        middle_y.visibility = View.VISIBLE
        more_info_layout.visibility = View.VISIBLE
        more_info_title.visibility = View.VISIBLE
        showmore.visibility = View.VISIBLE
        list_title_container.visibility = View.GONE
        list.visibility = View.GONE
    }
}