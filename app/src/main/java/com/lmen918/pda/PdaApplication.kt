package com.lmen918.pda

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.lmen918.pda.journal.JournalStorage
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PdaApplication : Application() {

	override fun onCreate() {
		super.onCreate()
		createReminderChannel()
		JournalStorage.ensureDefaultDirectory(this)
	}

	private fun createReminderChannel() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

		val channel = NotificationChannel(
			REMINDER_CHANNEL_ID,
			getString(R.string.reminder_channel_name),
			NotificationManager.IMPORTANCE_DEFAULT
		).apply {
			description = getString(R.string.reminder_channel_description)
		}

		val manager = getSystemService(NotificationManager::class.java)
		manager.createNotificationChannel(channel)
	}

	companion object {
		const val REMINDER_CHANNEL_ID = "retrospective_reminders"
	}
}
