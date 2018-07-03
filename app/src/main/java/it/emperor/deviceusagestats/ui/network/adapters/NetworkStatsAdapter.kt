package it.emperor.deviceusagestats.ui.network.adapters

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
import it.emperor.deviceusagestats.services.RxBus
import it.emperor.deviceusagestats.ui.network.model.NetworkStats
import javax.inject.Inject

class NetworkStatsAdapter(val context: Context, val items: List<Pair<Int, NetworkStats>>) : RecyclerView.Adapter<NetworkStatsAdapter.ViewHolder>() {

    @Inject
    private lateinit var rxBus: RxBus

    init {
        App.feather.injectFields(this)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_network_app, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val networkUsage = items.get(position)

        holder.name.text = networkUsage.second.name
        holder.packageName.text = networkUsage.second.packageName
        holder.download.text = networkUsage.second.formatValue(networkUsage.second.valueDownload)
        holder.upload.text = networkUsage.second.formatValue(networkUsage.second.valueUpload)

        networkUsage.second.icon?.let { Glide.with(context).load(networkUsage.second.icon).into(holder.icon) }
                ?: kotlin.run { Glide.with(context).clear(holder.icon) }

        holder.row.setOnClickListener {
            rxBus.send(AppDetailEvent(networkUsage.second.packageName.toString(), holder.icon, holder.name))
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val row = view.findViewById<View>(R.id.row)

        val icon = view.findViewById<ImageView>(R.id.icon)
        val name = view.findViewById<TextView>(R.id.name)
        val packageName = view.findViewById<TextView>(R.id.package_name)
        val download = view.findViewById<TextView>(R.id.download)
        val upload = view.findViewById<TextView>(R.id.upload)
    }
}