package it.emperor.deviceusagestats.ui.base

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import it.emperor.deviceusagestats.App
import it.emperor.deviceusagestats.R
import it.emperor.deviceusagestats.models.AppTimeType
import it.emperor.deviceusagestats.models.NetworkTimeType
import kotlinx.android.synthetic.main.toolbar.*
import net.danlew.android.joda.JodaTimeAndroid

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        JodaTimeAndroid.init(this)
        App.feather.injectFields(this)

        initVariables()

        intent?.extras?.let {
            loadParameters(it)
        }

        savedInstanceState?.let {
            loadInfos(savedInstanceState)
        }

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar?.setNavigationIcon(R.drawable.ic_chevron_left_white_24dp)
        toolbar?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        initialize()
    }

    protected abstract fun getLayoutId(): Int

    protected abstract fun initVariables()

    protected abstract fun loadParameters(extras: Bundle)

    protected abstract fun loadInfos(savedInstanceState: Bundle)

    protected abstract fun saveInfos(outState: Bundle)

    protected abstract fun initialize()

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.let {
            saveInfos(outState)
        }
    }

    protected fun updateToolbarSubtitleWithNetworkFilter(timeType: NetworkTimeType) {
        when (timeType) {
            NetworkTimeType.TODAY -> supportActionBar?.subtitle = getString(R.string.time_today)
            NetworkTimeType.WEEK -> supportActionBar?.subtitle = getString(R.string.time_week)
            NetworkTimeType.MONTH -> supportActionBar?.subtitle = getString(R.string.time_month)
            NetworkTimeType.LAST_MONTH -> supportActionBar?.subtitle = getString(R.string.time_last_month)
            NetworkTimeType.YEAR -> supportActionBar?.subtitle = getString(R.string.time_year)
            NetworkTimeType.CUSTOM -> supportActionBar?.subtitle = getString(R.string.time_today)
        }
    }

    protected fun updateToolbarSubtitleWithAppFilter(timeType: AppTimeType) {
        when (timeType) {
            AppTimeType.DAY -> supportActionBar?.subtitle = getString(R.string.time_day)
            AppTimeType.WEEK -> supportActionBar?.subtitle = getString(R.string.time_week)
            AppTimeType.MONTH -> supportActionBar?.subtitle = getString(R.string.time_month)
            AppTimeType.YEAR -> supportActionBar?.subtitle = getString(R.string.time_year)
        }
    }
}