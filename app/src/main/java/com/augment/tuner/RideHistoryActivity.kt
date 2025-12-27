package com.augment.tuner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.augment.tuner.data.AppDatabase
import com.augment.tuner.data.RideEntity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class RideHistoryActivity : AppCompatActivity() {

    private lateinit var tvTotalStats: TextView
    private lateinit var tvTotalRides: TextView
    private lateinit var tvTotalDistance: TextView
    private lateinit var tvTopSpeed: TextView
    private lateinit var chipGroupFilter: ChipGroup
    private lateinit var rvRideHistory: RecyclerView
    private lateinit var layoutEmptyState: LinearLayout

    private lateinit var database: AppDatabase
    private lateinit var rideAdapter: RideAdapter

    private var allRides = listOf<RideEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ride_history)

        database = AppDatabase.getDatabase(this)

        initViews()
        setupRecyclerView()
        setupFilters()
        loadRides()
        loadStatistics()
    }

    private fun initViews() {
        tvTotalStats = findViewById(R.id.tvTotalStats)
        tvTotalRides = findViewById(R.id.tvTotalRides)
        tvTotalDistance = findViewById(R.id.tvTotalDistance)
        tvTopSpeed = findViewById(R.id.tvTopSpeed)
        chipGroupFilter = findViewById(R.id.chipGroupFilter)
        rvRideHistory = findViewById(R.id.rvRideHistory)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)
    }

    private fun setupRecyclerView() {
        rideAdapter = RideAdapter()
        rvRideHistory.apply {
            layoutManager = LinearLayoutManager(this@RideHistoryActivity)
            adapter = rideAdapter
        }
    }

    private fun setupFilters() {
        chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener

            val filteredRides = when (checkedIds[0]) {
                R.id.chipToday -> filterRidesToday()
                R.id.chipWeek -> filterRidesThisWeek()
                R.id.chipMonth -> filterRidesThisMonth()
                else -> allRides
            }

            updateRideList(filteredRides)
        }
    }

    private fun loadRides() {
        lifecycleScope.launch {
            database.rideDao().getAllRides().collectLatest { rides ->
                allRides = rides
                updateRideList(rides)
            }
        }
    }

    private fun loadStatistics() {
        lifecycleScope.launch {
            val totalRides = database.rideDao().getTotalRidesCount()
            val totalDistance = database.rideDao().getTotalDistance() ?: 0f
            val topSpeed = database.rideDao().getTopSpeedEver() ?: 0f

            tvTotalRides.text = totalRides.toString()
            tvTotalDistance.text = String.format("%.1f", totalDistance)
            tvTopSpeed.text = String.format("%.1f", topSpeed)

            tvTotalStats.text = "$totalRides total rides • ${String.format("%.1f", totalDistance)} km traveled"
        }
    }

    private fun updateRideList(rides: List<RideEntity>) {
        if (rides.isEmpty()) {
            rvRideHistory.visibility = View.GONE
            layoutEmptyState.visibility = View.VISIBLE
        } else {
            rvRideHistory.visibility = View.VISIBLE
            layoutEmptyState.visibility = View.GONE
            rideAdapter.updateRides(rides)
        }
    }

    private fun filterRidesToday(): List<RideEntity> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        return allRides.filter { it.startTime >= startOfDay }
    }

    private fun filterRidesThisWeek(): List<RideEntity> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfWeek = calendar.timeInMillis

        return allRides.filter { it.startTime >= startOfWeek }
    }

    private fun filterRidesThisMonth(): List<RideEntity> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis

        return allRides.filter { it.startTime >= startOfMonth }
    }

    inner class RideAdapter : RecyclerView.Adapter<RideAdapter.RideViewHolder>() {

        private var rides = listOf<RideEntity>()

        fun updateRides(newRides: List<RideEntity>) {
            rides = newRides
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RideViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_ride, parent, false)
            return RideViewHolder(view)
        }

        override fun onBindViewHolder(holder: RideViewHolder, position: Int) {
            holder.bind(rides[position])
        }

        override fun getItemCount() = rides.size

        inner class RideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvRideDate: TextView = itemView.findViewById(R.id.tvRideDate)
            private val tvRideDuration: TextView = itemView.findViewById(R.id.tvRideDuration)
            private val tvRideDistance: TextView = itemView.findViewById(R.id.tvRideDistance)
            private val tvRideMaxSpeed: TextView = itemView.findViewById(R.id.tvRideMaxSpeed)
            private val tvRideBattery: TextView = itemView.findViewById(R.id.tvRideBattery)
            private val tvRideAvgSpeed: TextView = itemView.findViewById(R.id.tvRideAvgSpeed)
            private val tvRideScooter: TextView = itemView.findViewById(R.id.tvRideScooter)

            fun bind(ride: RideEntity) {
                // Format date
                val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                tvRideDate.text = dateFormat.format(Date(ride.startTime))

                // Calculate duration
                val durationMinutes = TimeUnit.MILLISECONDS.toMinutes(ride.endTime - ride.startTime)
                tvRideDuration.text = "${durationMinutes} min"

                // Distance
                tvRideDistance.text = String.format("%.2f km", ride.distance)

                // Max speed
                tvRideMaxSpeed.text = String.format("%.1f km/h", ride.maxSpeed)

                // Battery used
                val batteryUsed = ride.startBattery - ride.endBattery
                tvRideBattery.text = "-${batteryUsed}%"

                // Average speed
                val avgSpeed = if (durationMinutes > 0) {
                    (ride.distance / durationMinutes) * 60 // km/h
                } else {
                    0f
                }
                tvRideAvgSpeed.text = String.format("%.1f km/h", avgSpeed)

                // Scooter MAC (last 4 characters)
                tvRideScooter.text = ride.scooterMac.takeLast(8)
            }
        }
    }
}
