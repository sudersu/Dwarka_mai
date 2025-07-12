
package com.dailylist.pro

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.dailylist.pro.databinding.ActivityMainBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var taskAdapter: TaskAdapter
    private val taskDao by lazy { AppDatabase.getDatabase(this).taskDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Mobile Ads SDK
        MobileAds.initialize(this) {}

        // Load Ad
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        // Setup RecyclerView
        taskAdapter = TaskAdapter(this, emptyList(), { task ->
            // Handle edit
            val dialog = EditTaskDialog(task)
            dialog.show(supportFragmentManager, EditTaskDialog.TAG)
        }, { task ->
            // Handle delete
            lifecycleScope.launch {
                taskDao.delete(task)
            }
        })
        binding.recyclerView.adapter = taskAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // Observe tasks from database
        lifecycleScope.launch {
            taskDao.getAllTasks().collect {
                taskAdapter.updateTasks(it)
            }
        }

        // FAB click listener
        binding.fab.setOnClickListener {
            val dialog = AddTaskDialog()
            dialog.show(supportFragmentManager, AddTaskDialog.TAG)
        }
    }

    fun onTaskSaved(task: Task) {
        lifecycleScope.launch {
            taskDao.insert(task)
        }
    }

    fun onTaskUpdated(task: Task) {
        lifecycleScope.launch {
            taskDao.update(task)
        }
    }
}



