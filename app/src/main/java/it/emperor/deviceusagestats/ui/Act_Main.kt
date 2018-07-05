package it.emperor.deviceusagestats.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.AppOpsManager.MODE_ALLOWED
import android.app.AppOpsManager.OPSTR_GET_USAGE_STATS
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.support.v7.widget.LinearLayoutManager
import android.telephony.TelephonyManager
import com.sembozdemir.permissionskt.askPermissions
import it.emperor.deviceusagestats.R
import it.emperor.deviceusagestats.services.UsageService
import it.emperor.deviceusagestats.ui.base.BaseActivity
import it.emperor.deviceusagestats.ui.network.Act_Network
import it.emperor.deviceusagestats.ui.usage.Act_AppUsage2
import kotlinx.android.synthetic.main.act_main.*
import org.joda.time.DateTime
import javax.inject.Inject


class Act_Main : BaseActivity() {

    private var permissionGranted: Boolean = false
    private lateinit var usageStatsManager: UsageStatsManager
    private lateinit var networkStatsManager: NetworkStatsManager

    @Inject
    lateinit var usageService: UsageService


    override fun getLayoutId(): Int {
        return R.layout.act_main
    }

    override fun initVariables() {
        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        networkStatsManager = getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
    }

    override fun loadParameters(extras: Bundle) {
    }

    override fun loadInfos(savedInstanceState: Bundle) {
    }

    override fun saveInfos(outState: Bundle) {
    }

    override fun onResume() {
        super.onResume()
        checkPermission(this)
    }

    override fun initialize() {
        list.layoutManager = LinearLayoutManager(this)

        network_title.setOnClickListener({
            startActivity(Intent(this, Act_Network::class.java))
        })
        usage_title.setOnClickListener({
            startActivity(Intent(this, Act_AppUsage2::class.java))
        })

//        val series = ValueLineSeries()
//        series.color = ContextCompat.getColor(this, R.color.system_primary)
//
//        series.addPoint(ValueLinePoint("Jan", 2.4f))
//        series.addPoint(ValueLinePoint("Feb", 3.4f))
//        series.addPoint(ValueLinePoint("Mar", .4f))
//        series.addPoint(ValueLinePoint("Apr", 1.2f))
//        series.addPoint(ValueLinePoint("Mai", 2.6f))
//        series.addPoint(ValueLinePoint("Jun", 1.0f))
//        series.addPoint(ValueLinePoint("Jul", 3.5f))
//        series.addPoint(ValueLinePoint("Aug", 2.4f))
//        series.addPoint(ValueLinePoint("Sep", 2.4f))
//        series.addPoint(ValueLinePoint("Oct", 3.4f))
//        series.addPoint(ValueLinePoint("Nov", .4f))
//        series.addPoint(ValueLinePoint("Dec", 1.3f))
//
//        cubiclinechart.addSeries(series)
//        cubiclinechart.startAnimation()
    }

    // FUNCTIONS

    fun checkPermission(context: Context): Boolean {
        if (permissionGranted) {
            loadUsageStats(context)
            return true
        }

        val usagePermission = checkForUsagePermission(context)
        if (!usagePermission) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        } else {
            checkForPermission(context)
        }

        return false
    }

    fun checkForUsagePermission(context: Context): Boolean {
        val appOpsManager: AppOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode: Int = appOpsManager.checkOpNoThrow(OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        return mode == MODE_ALLOWED;
    }

    fun checkForPermission(context: Context) {
        askPermissions(Manifest.permission.READ_PHONE_STATE) {
            onGranted {
                permissionGranted = true;
//                loadUsageStats(context)
            }

            onDenied {

            }

            onShowRationale {

            }

            onNeverAskAgain {

            }
        }
    }

    fun loadUsageStats(context: Context) {
        val start: DateTime = DateTime.now().minusDays(3)
        val end: DateTime = DateTime.now()

//        loadNetworkSummaryStats(context, start, end)
//        usageService.loadNetworkSummaryStats(0, start, end)
        loadUsageSummaryStats(context)
    }

    @SuppressLint("MissingPermission", "HardwareIds")
    fun loadNetworkSummaryStats(context: Context, start: DateTime, end: DateTime) {
        val telephonyManager: TelephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val subscriberId = telephonyManager.subscriberId ?: ""
        val bucket: NetworkStats.Bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_WIFI, subscriberId, start.toDate().time, end.toDate().time)

        when (bucket.state) {
            NetworkStats.Bucket.STATE_ALL -> state.text = "ALL"
            NetworkStats.Bucket.STATE_DEFAULT -> state.text = "DEFAULT"
            NetworkStats.Bucket.STATE_FOREGROUND -> state.text = "FOREGROUND"
        }

        rx_bytes.text = bucket.rxBytes.toString()
        tx_bytes.text = bucket.txBytes.toString()
        rx_packets.text = bucket.rxPackets.toString()
        tx_packets.text = bucket.txPackets.toString()

        println("#######")
        print(bucket.rxPackets)
        print(" ")
        print(bucket.startTimeStamp)
        print(" ")
        print(bucket.endTimeStamp)
        print(" ")
        print(bucket.state)
        print(" ")
        print(bucket.rxBytes)
        print(" ")
        println("@@@@@@")
    }

    fun loadUsageSummaryStats(context: Context) {
        val usageStatsManager: UsageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val start: DateTime = DateTime.now().minusDays(3)
        val end: DateTime = DateTime.now()
        val stats: Map<String, UsageStats> = usageStatsManager.queryAndAggregateUsageStats(start.toDate().time, end.toDate().time);
        val array: ArrayList<String> = ArrayList()
        for ((key, value) in stats) {
            array.add(key)
            println(key)
            print(value.firstTimeStamp)
            print(" ")
            print(value.lastTimeStamp)
            print(" ")
            print(value.lastTimeUsed)
            print(" ")
            print(value.totalTimeInForeground)
        }
//        list.adapter = NetworkUsageAdapter(array, this)
    }

    @SuppressLint("WrongConstant", "MissingPermission", "HardwareIds")
    fun loadUsageStats2(context: Context) {
        val usageStatsManager: UsageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val start: DateTime = DateTime.now().minusDays(3)
        val end: DateTime = DateTime.now()
        val stats: Map<String, UsageStats> = usageStatsManager.queryAndAggregateUsageStats(start.toDate().time, end.toDate().time);
        for ((key, value) in stats) {
            println(key)
            print(value.firstTimeStamp)
            print(" ")
            print(value.lastTimeStamp)
            print(" ")
            print(value.lastTimeUsed)
            print(" ")
            print(value.totalTimeInForeground)
        }

        val networkStatsManager: NetworkStatsManager = context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
        val telephonyManager: TelephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val subscriberId = telephonyManager.subscriberId ?: ""
        val bucket: NetworkStats.Bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_WIFI, subscriberId, start.toDate().time, end.toDate().time)
        println("#######")
        print(bucket.rxPackets)
        print(" ")
        print(bucket.startTimeStamp)
        print(" ")
        print(bucket.endTimeStamp)
        print(" ")
        print(bucket.state)
        print(" ")
        print(bucket.rxBytes)
        print(" ")
        println("@@@@@@")

        val nStats: NetworkStats = networkStatsManager.queryDetails(ConnectivityManager.TYPE_WIFI, subscriberId, start.toDate().time, end.toDate().time)
        while (nStats.hasNextBucket()) {
            var nBucket: NetworkStats.Bucket = NetworkStats.Bucket();
            nStats.getNextBucket(nBucket);
            print(nBucket.rxPackets)
            print(" ")
            print(nBucket.startTimeStamp)
            print(" ")
            print(nBucket.endTimeStamp)
            print(" ")
            print(nBucket.state)
            print(" ")
            print(nBucket.rxBytes)
            print(" ")
            print(nBucket.uid)
            println("@@@@@@ BESTEMMIE")
        }
    }
}
