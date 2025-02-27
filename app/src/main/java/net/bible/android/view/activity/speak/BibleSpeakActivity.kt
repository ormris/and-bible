/*
 * Copyright (c) 2020-2022 Martin Denham, Tuomas Airaksinen and the AndBible contributors.
 *
 * This file is part of AndBible: Bible Study (http://github.com/AndBible/and-bible).
 *
 * AndBible is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * AndBible is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with AndBible.
 * If not, see http://www.gnu.org/licenses/.
 */

package net.bible.android.view.activity.speak

import android.annotation.SuppressLint
import androidx.appcompat.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import net.bible.android.activity.R
import net.bible.android.activity.databinding.SpeakBibleBinding
import net.bible.android.control.event.ABEventBus
import net.bible.android.control.event.ToastEvent
import net.bible.android.control.navigation.NavigationControl
import net.bible.android.control.page.window.ActiveWindowPageManagerProvider
import net.bible.android.control.speak.*
import net.bible.android.database.bookmarks.PlaybackSettings
import net.bible.android.database.bookmarks.SpeakSettings
import net.bible.android.view.activity.ActivityScope
import net.bible.android.view.activity.base.ActivityBase
import net.bible.android.view.activity.navigation.GridChoosePassageBook
import net.bible.service.common.speakHelpVideo
import org.crosswire.jsword.passage.Verse
import org.crosswire.jsword.passage.VerseFactory
import org.crosswire.jsword.passage.VerseRange
import javax.inject.Inject

@ActivityScope
class BibleSpeakActivity : AbstractSpeakActivity() {
    companion object {
        const val TAG = "BibleSpeakActivity"
    }

    @Inject lateinit var activeWindowPageManagerProvider: ActiveWindowPageManagerProvider
    @Inject lateinit var navigationControl: NavigationControl

    lateinit var binding: SpeakBibleBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SpeakBibleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        buildActivityComponent().inject(this)
        ABEventBus.getDefault().register(this)
        binding.apply {
            speakSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    speedStatus.text = "$progress %"
                    if (fromUser) {
                        updateSettings()
                    }
                }
            })
            speakChapterChanges.setOnClickListener { updateSettings() }
            speakTitles.setOnClickListener { updateSettings() }
            speakFootnotes.setOnClickListener { updateSettings() }
            repeatPassageCheckbox.setOnClickListener { setRepeatPassage() }
            sleepTimer.setOnClickListener { setSleepTime() }
        }

        resetView(SpeakSettings.load())
    }

    override val sleepTimer: CheckBox get() = binding.sleepTimer

    override fun onDestroy() {
        ABEventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    override fun resetView(settings: SpeakSettings) = binding.run {
        speakChapterChanges.isChecked = settings.playbackSettings.speakChapterChanges
        speakTitles.isChecked = settings.playbackSettings.speakTitles
        speakFootnotes.isChecked = settings.playbackSettings.speakFootnotes
        speakSpeed.progress = settings.playbackSettings.speed
        speedStatus.text = "${settings.playbackSettings.speed} %"
        sleepTimer.isChecked = settings.sleepTimer > 0
        sleepTimer.text = if(settings.sleepTimer>0) getString(R.string.sleep_timer_set, settings.sleepTimer) else getString(R.string.conf_speak_sleep_timer)
        repeatPassageCheckbox.text = settings.playbackSettings.verseRange?.name?: getString(R.string.speak_verse_range_to_repeat)
        repeatPassageCheckbox.isChecked = settings.playbackSettings.verseRange != null
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.speak_bible_actionbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.advancedSettings -> {
                startActivity(Intent(this, SpeakSettingsActivity::class.java))
                return true
            }
            R.id.systemSettings -> {
                startActivity(Intent("com.android.settings.TTS_SETTINGS"))
                return true
            }
            R.id.help -> {
                onHelpButtonClick()
                return true
            }
        }
        return false
    }

    fun onEventMainThread(ev: SpeakSettingsChangedEvent) {
        currentSettings = ev.speakSettings
        resetView(ev.speakSettings)
    }

    fun onHelpButtonClick() {
        val htmlMessage = (
                "<b>${getString(R.string.speak)}</b><br><br>"
                + "<b><a href=\"$speakHelpVideo\">"
                + "${getString(R.string.watch_tutorial_video)}</a></b>"
                )

        val spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(htmlMessage, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(htmlMessage)
        }

        val d = AlertDialog.Builder(this)
                .setMessage(spanned)
                .setPositiveButton(android.R.string.ok) { _, _ ->  }
                .create()

        d.show()
        d.findViewById<TextView>(android.R.id.message)!!.movementMethod = LinkMovementMethod.getInstance()
    }

    fun setRepeatPassage() {
        val s = SpeakSettings.load()
        if(s.playbackSettings.verseRange != null) {
            s.playbackSettings.verseRange = null
            s.save(updateBookmark = true)
        }
        else {
            val intent = Intent(this, GridChoosePassageBook::class.java)
            intent.putExtra("isScripture", true)
            intent.putExtra("navigateToVerse", true)
            intent.putExtra("title", getString(R.string.speak_beginning_of_passage))
            startVerse = null
            endVerse = null
            binding.repeatPassageCheckbox.isChecked = false // not yet!
            startActivityForResult(intent, ActivityBase.STD_REQUEST_CODE)
        }
    }

    private var startVerse: Verse? = null
    private var endVerse: Verse? = null

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i(TAG, "Activity result:$resultCode")
        val verseStr = data?.extras?.getString("verse")
        val v11n = navigationControl.versification
        if(verseStr != null) {
            val verse = VerseFactory.fromString(v11n, verseStr)
            if(startVerse == null) {
                startVerse = verse
                val intent = Intent(this, GridChoosePassageBook::class.java)
                intent.putExtra("isScripture", true)
                intent.putExtra("navigateToVerse", true)
                intent.putExtra("title", getString(R.string.speak_ending_of_passage))
                startActivityForResult(intent, STD_REQUEST_CODE)
            }
            else {
                endVerse = verse
                val settings = SpeakSettings.load()
                if(endVerse!!.ordinal > startVerse!!.ordinal){
                    val verseRange = VerseRange(v11n, startVerse, endVerse)
                    settings.playbackSettings.verseRange = verseRange
                    settings.save(updateBookmark = true)
                }
                else {
                    startVerse = null
                    endVerse = null
                    ABEventBus.getDefault().post(ToastEvent(R.string.speak_ending_verse_must_be_later))
                    resetView(settings)
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }


    fun updateSettings() {
        val settings = SpeakSettings.load()

        settings.apply {
            playbackSettings = PlaybackSettings(
                    speakChapterChanges = binding.speakChapterChanges.isChecked,
                    speakTitles = binding.speakTitles.isChecked,
                    speakFootnotes = binding.speakFootnotes.isChecked,
                    speed = binding.speakSpeed.progress,
                    verseRange = settings.playbackSettings.verseRange
            )
            sleepTimer = currentSettings.sleepTimer
            lastSleepTimer = currentSettings.lastSleepTimer
            save(updateBookmark = true)
        }
    }
}
