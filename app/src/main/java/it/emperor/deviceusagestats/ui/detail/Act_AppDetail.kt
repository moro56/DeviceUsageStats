package it.emperor.deviceusagestats.ui.detail

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import com.bumptech.glide.Glide
import it.emperor.deviceusagestats.R
import it.emperor.deviceusagestats.transitions.EnterSharedElementTextSizeHandler
import it.emperor.deviceusagestats.ui.base.BaseActivity
import kotlinx.android.synthetic.main.act_app_detail.*

fun Context.appDetail(packageName: String, textSize: Float): Intent {
    return Intent(this, Act_AppDetail::class.java)
            .putExtra(INTENT_APP_PACKAGE_NAME, packageName)
            .putExtra(INTENT_APP_TEXT_SIZE, textSize)
}

private const val INTENT_APP_PACKAGE_NAME = "packageName"
private const val INTENT_APP_TEXT_SIZE = "textSize"

class Act_AppDetail : BaseActivity() {

    private lateinit var appPackageName: String
    private var appNameTextSize: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val handler = EnterSharedElementTextSizeHandler(this)
        handler.addTextView(app_title, appNameTextSize.toInt(), app_title.textSize.toInt())
    }

    override fun getLayoutId(): Int {
        return R.layout.act_app_detail
    }

    override fun initVariables() {
    }

    override fun loadParameters(extras: Bundle) {
        appPackageName = extras.getString(INTENT_APP_PACKAGE_NAME)
        appNameTextSize = extras.getFloat(INTENT_APP_TEXT_SIZE)
    }

    override fun loadInfos(savedInstanceState: Bundle) {
    }

    override fun saveInfos(outState: Bundle) {
    }

    override fun initialize() {
        loadAppInfo()
    }

    // FUNCTIONS

    private fun loadAppInfo() {
        var name: String = appPackageName
        var icon: Drawable? = null

        try {
            val applicationInfo = packageManager.getApplicationInfo(appPackageName, PackageManager.GET_META_DATA)
            try {
                icon = packageManager.getApplicationIcon(applicationInfo)
            } catch (ex2: PackageManager.NameNotFoundException) {
            }

            name = packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (ex: PackageManager.NameNotFoundException) {
        }

        app_title.text = name

        icon?.let { Glide.with(this).load(icon).into(app_icon) }
                ?: kotlin.run { Glide.with(this).clear(app_icon) }
    }
}