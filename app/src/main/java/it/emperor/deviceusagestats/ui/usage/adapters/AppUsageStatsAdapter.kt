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
import it.emperor.deviceusagestats.ui.views.RoundProgressBar
import javax.inject.Inject

class AppUsageStatsAdapter(val context: Context, var items: List<Pair<String, AppUsageStats>>, var total: Long) : RecyclerView.Adapter<AppUsageStatsAdapter.ViewHolder>() {

    @Inject
    private lateinit var rxBus: RxBus

    private var loading = false

    init {
        App.feather.injectFields(this)
    }

    override fun getItemCount(): Int {
        return if (loading) 0 else items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_appusage_app, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val networkUsage = items.get(position)
        val value = networkUsage.second.timeInForeground.toFloat()
        val perc: Float = value / total.toFloat()

        holder.name.text = networkUsage.second.name
        holder.packageName.text = networkUsage.second.packageName
        holder.progress.setProgress(perc)
        holder.progressPerc.text = value.toLong().toTimeString(true)

        networkUsage.second.icon?.let { Glide.with(context).load(networkUsage.second.icon).into(holder.icon) }
                ?: kotlin.run { Glide.with(context).load(R.drawable.default_app).into(holder.icon) }

        holder.row.setOnClickListener {
            rxBus.send(AppDetailEvent(networkUsage.second.packageName.toString(), holder.icon, holder.name))
        }
    }

    fun setIsLoading(loading: Boolean) {
        if (loading) {
            notifyItemRangeRemoved(0, itemCount)
            this.loading = loading
        } else {
            this.loading = loading
            notifyItemRangeInserted(0, itemCount)
        }
    }

    fun swapItems(items: List<Pair<String, AppUsageStats>>, total: Long) {
        this.items = items
        this.total = total
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val row: View = view.findViewById<View>(R.id.row)

        val icon: ImageView = view.findViewById(R.id.icon)
        val name: TextView = view.findViewById(R.id.name)
        val packageName: TextView = view.findViewById(R.id.package_name)
        val progress: RoundProgressBar = view.findViewById(R.id.progress)
        val progressPerc: TextView = view.findViewById(R.id.progress_perc)
    }
}