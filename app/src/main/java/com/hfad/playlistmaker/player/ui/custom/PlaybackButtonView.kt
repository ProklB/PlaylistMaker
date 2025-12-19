package com.hfad.playlistmaker.player.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.toRect
import com.hfad.playlistmaker.R

class PlaybackButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var playDrawable: Drawable? = null
    private var pauseDrawable: Drawable? = null
    private var currentDrawable: Drawable? = null
    private var drawableRect = RectF()

    var isPlaying: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                updateDrawable()
                invalidate()
            }
        }

    init {
        setupAttributes(attrs)
        updateDrawable()
    }

    private fun setupAttributes(attrs: AttributeSet?) {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.PlaybackButtonView,
            0,
            0
        ).apply {
            try {
                val playIconResId = getResourceId(R.styleable.PlaybackButtonView_playIcon, R.drawable.button_play)
                val pauseIconResId = getResourceId(R.styleable.PlaybackButtonView_pauseIcon, R.drawable.button_pause)

                playDrawable = ContextCompat.getDrawable(context, playIconResId)
                pauseDrawable = ContextCompat.getDrawable(context, pauseIconResId)
            } finally {
                recycle()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredSize = 100

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> minOf(dpToPx(desiredSize), widthSize)
            else -> dpToPx(desiredSize)
        }

        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> minOf(dpToPx(desiredSize), heightSize)
            else -> dpToPx(desiredSize)
        }

        setMeasuredDimension(width, height)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val padding = (minOf(w, h) * 0.1f).toInt()
        val drawableSize = minOf(w, h) - 2 * padding

        drawableRect.set(
            padding.toFloat(),
            padding.toFloat(),
            (padding + drawableSize).toFloat(),
            (padding + drawableSize).toFloat()
        )

        playDrawable?.bounds = drawableRect.toRect()
        pauseDrawable?.bounds = drawableRect.toRect()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        currentDrawable?.draw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                return true
            }

            MotionEvent.ACTION_UP -> {
                isPlaying = !isPlaying
                performClick()
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun updateDrawable() {
        currentDrawable = if (isPlaying) pauseDrawable else playDrawable
    }

    fun setPlayingState(playing: Boolean) {
        isPlaying = playing
    }
}
