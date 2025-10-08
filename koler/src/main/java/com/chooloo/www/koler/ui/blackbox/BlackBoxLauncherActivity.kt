package com.chooloo.www.koler.ui.blackbox

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chooloo.www.koler.R
import top.niunaijun.blackbox.BlackBoxCore

class BlackBoxLauncherActivity : AppCompatActivity() {

    private data class AppInfo(
        val packageName: String,
        val appName: String,
        val icon: android.graphics.drawable.Drawable?
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blackbox_launcher)
        
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        // Get all installed non-system apps
        val packageManager = packageManager
        val apps = packageManager.getInstalledApplications(0)
            .filter { app -> !(app.flags and ApplicationInfo.FLAG_SYSTEM != 0) }
            .map { app ->
                AppInfo(
                    packageName = app.packageName,
                    appName = app.loadLabel(packageManager).toString(),
                    icon = app.loadIcon(packageManager)
                )
            }
            .sortedBy { it.appName }

        if (apps.isEmpty()) {
            Toast.makeText(this, "No non-system apps found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        recyclerView.adapter = AppAdapter(apps) { app ->
            launchInBlackBox(app.packageName)
        }
    }

    private fun launchInBlackBox(packageName: String) {
        try {
            // Check if app is installed in BlackBox
            if (!BlackBoxCore.get().isInstalled(packageName, 0)) {
                Toast.makeText(this, "Installing $packageName into BlackBox...", Toast.LENGTH_SHORT).show()
                val installResult = BlackBoxCore.get().installPackageAsUser(packageName, 0)
                if (!installResult.success) {
                    Toast.makeText(this, "Installation failed: ${installResult.msg}", Toast.LENGTH_LONG).show()
                    finish()
                    return
                }
                Toast.makeText(this, "Installation successful!", Toast.LENGTH_SHORT).show()
            }
            
            // Launch the app in BlackBox
            Toast.makeText(this, "Launching $packageName in BlackBox...", Toast.LENGTH_SHORT).show()
            BlackBoxCore.get().launchApk(packageName, 0)
            finish()
            
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "BlackBox error: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private class AppAdapter(
        private val apps: List<AppInfo>,
        private val onAppClick: (AppInfo) -> Unit
    ) : RecyclerView.Adapter<AppAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val appIcon: ImageView = view.findViewById(R.id.appIcon)
            val appName: TextView = view.findViewById(R.id.appName)
            val packageName: TextView = view.findViewById(R.id.packageName)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_app, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val app = apps[position]
            holder.appIcon.setImageDrawable(app.icon)
            holder.appName.text = app.appName
            holder.packageName.text = app.packageName
            holder.itemView.setOnClickListener {
                onAppClick(app)
            }
        }

        override fun getItemCount() = apps.size
    }
}
