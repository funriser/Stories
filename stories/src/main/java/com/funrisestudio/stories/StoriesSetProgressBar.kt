package com.funrisestudio.stories

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.LinearLayout

class StoriesSetProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    private val styling: Styling? = null
) : LinearLayout(context, attrs) {

    private val progressSpacing: Int

    private var storiesCount = 0
    private var storyDurationMillis = 0L

    private var activeProgressView: ProgressView? = null
    private var storiesIterator = StoriesIterator()

    var onStoryCompleted: (() -> Unit)? = null

    init {
        orientation = HORIZONTAL
        progressSpacing = if (styling != null && styling.progressSpacing != -1) {
            styling.progressSpacing
        } else {
            context.resources.getDimensionPixelSize(R.dimen.mrg_progress_spacing)
        }
    }

    fun setUp(storiesCount: Int, storyDurationMillis: Long) {
        this.storiesCount = storiesCount
        this.storyDurationMillis = storyDurationMillis
        initChildren()
        storiesIterator = StoriesIterator()
    }

    private fun initChildren() {
        removeAllViews()
        repeat(storiesCount) { i ->
            val pv = ProgressView(context, storyDurationMillis, styling?.progressStyling).apply {
                layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT).apply {
                    weight = 1f
                    if (i != 0) {
                        leftMargin = progressSpacing
                    }
                }
            }
            addView(pv)
        }
    }

    fun start() {
        startActiveProgressView()
    }

    fun isStarted(): Boolean {
        return activeProgressView?.isStarted() == true
    }

    fun pause() {
        activeProgressView?.pause()
    }

    fun resume() {
        activeProgressView?.resume()
    }

    fun hasNext() = storiesIterator.hasNext()

    fun nextIndex() = storiesIterator.nextIndex()

    fun completeCurrent() {
        activeProgressView?.setCompleted()
    }

    fun next() {
        activeProgressView = storiesIterator.next()
    }

    fun hasPrevious() = storiesIterator.hasPrevious()

    fun previousIndex() = storiesIterator.previousIndex()

    fun unCompleteCurrent() {
        activeProgressView?.setUncompleted()
    }

    fun previous() {
        activeProgressView = storiesIterator.previous()
    }

    fun currentIndex() = storiesIterator.cursor

    private fun startActiveProgressView() {
        activeProgressView?.start(onCompleted = ::onStoryCompleted)
    }

    /**
     * Set active progress bar at index
     * Marks all bars before the active one as completed
     * Ensures that progress bar at the index and all bars
     * after index are marked as uncompleted
     *
     * @param index - the index of an active progress bar
     */
    fun setCurrentItem(index: Int) {
        activeProgressView = null
        var i = 0
        StoriesIterator().forEach {
            if (i < index) {
                it.setCompleted()
            } else {
                if (i == index) {
                    activeProgressView = it
                }
                it.setUncompleted()
            }
            i ++
        }
        storiesIterator.cursor = index
    }

    private fun onStoryCompleted() {
        onStoryCompleted?.invoke()
    }

    inner class StoriesIterator : ListIterator<ProgressView> {

        var cursor = -1

        override fun hasNext(): Boolean = cursor < childCount - 1
        override fun next(): ProgressView =
            getChildAt(++cursor) as ProgressView? ?: throw IndexOutOfBoundsException()
        override fun nextIndex(): Int = cursor + 1

        override fun hasPrevious(): Boolean = cursor > 0
        override fun previous(): ProgressView =
            getChildAt(--cursor) as ProgressView? ?: throw IndexOutOfBoundsException()
        override fun previousIndex(): Int = cursor - 1

    }

    data class Styling(
        val progressSpacing: Int = -1,
        val progressStyling: ProgressView.Styling? = null
    ): Parcelable {

        constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readParcelable(ProgressView.Styling::class.java.classLoader)
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(progressSpacing)
            parcel.writeParcelable(progressStyling, flags)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Styling> {
            override fun createFromParcel(parcel: Parcel): Styling {
                return Styling(parcel)
            }

            override fun newArray(size: Int): Array<Styling?> {
                return arrayOfNulls(size)
            }
        }

    }

}