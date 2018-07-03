package it.emperor.deviceusagestats.ui.usage.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import it.emperor.deviceusagestats.App
import it.emperor.deviceusagestats.R
import it.emperor.deviceusagestats.events.AppDetailEvent
import it.emperor.deviceusagestats.extensions.toTimeString
import it.emperor.deviceusagestats.services.RxBus
import it.emperor.deviceusagestats.ui.usage.model.AppUsageStats
import it.emperor.deviceusagestats.ui.usage.model.AppUsageStatsMaps
import it.emperor.deviceusagestats.ui.views.RoundProgressBar
import javax.inject.Inject

class AppUsageStatsAdapter(val context: Context, val appUsageStats: AppUsageStatsMaps) : RecyclerView.Adapter<AppUsageStatsAdapter.ViewHolder>() {

    @Inject
    private lateinit var rxBus: RxBus

    init {
        App.feather.injectFields(this)
    }

    private var showInForeground = true
    private var showLoading = false

    override fun getItemCount(): Int {
        return if (showLoading) 0 else getCorrectSize()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_usage_app, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appUsage = getCorrectItem(position)
        val perc: Float = if (showInForeground) appUsage.timeInForeground.toFloat() / getCorrectTotal().toFloat() else appUsage.usedTime.toFloat() / getCorrectTotal().toFloat()

        holder.name.text = appUsage.name
        holder.time.text = if (showInForeground) appUsage.timeInForeground.toTimeString() else appUsage.usedTime.toTimeString()

        appUsage.icon?.let { Glide.with(context).load(appUsage.icon).into(holder.icon) }
                ?: kotlin.run { Glide.with(context).clear(holder.icon) }

        holder.progress.setProgress(perc)
        holder.progressPerc.text = context.getString(R.string.app_usage_perc_format).format(perc * 100f)

        holder.row.setOnClickListener {
            rxBus.send(AppDetailEvent(appUsage.packageName, holder.icon, holder.name))
        }
    }

    fun setShowInForeground(showInForeground: Boolean) {
        this.showInForeground = showInForeground
        notifyDataSetChanged()
    }

    fun setShowLoading(showLoading: Boolean) {
        if (showLoading) {
            notifyItemRangeRemoved(0, itemCount)
            this.showLoading = showLoading
        } else {
            this.showLoading = showLoading
            notifyItemRangeInserted(0, itemCount)
        }
    }

    private fun getCorrectSize(): Int {
        return if (showInForeground) appUsageStats.foregroundUsage.size else appUsageStats.totalUsageList.size
    }

    private fun getCorrectItem(position: Int): AppUsageStats {
        return if (showInForeground) appUsageStats.foregroundUsage.get(position) else appUsageStats.totalUsageList.get(position)
    }

    private fun getCorrectTotal(): Long {
        return if (showInForeground) appUsageStats.foregroundUsageTotal else appUsageStats.totalUsageTotal
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val row = view.findViewById<View>(R.id.row)

        val icon = view.findViewById<ImageView>(R.id.icon)
        val name = view.findViewById<TextView>(R.id.name)
        val time = view.findViewById<TextView>(R.id.time)
        val progress = view.findViewById<RoundProgressBar>(R.id.progress)
        val progressPerc = view.findViewById<TextView>(R.id.progress_perc)
    }
}