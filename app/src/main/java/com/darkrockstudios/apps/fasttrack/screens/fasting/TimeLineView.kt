package com.darkrockstudios.apps.fasttrack.screens.fasting

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.darkrockstudios.apps.fasttrack.data.Stages
import com.darkrockstudios.apps.fasttrack.dp2px
import kotlin.math.min
import kotlin.time.ExperimentalTime
import kotlin.time.hours

class TimeLineView @JvmOverloads constructor(
		context: Context,
		attrs: AttributeSet? = null,
		defStyleAttr: Int = 0): View(context, attrs, defStyleAttr)
{
	// If anyone sets this value, we need to redraw
	var elapsedHours = 0.0
		set(value)
		{
			field = value
			invalidate()
		}

	override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int)
	{
		super.onSizeChanged(w, h, oldw, oldh)

		invalidate()
	}

	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec)

		val height = padding + barSize

		setMeasuredDimension(measuredWidth, height)
	}

	private val padding by lazy { resources.dp2px(16) }
	private val spacing by lazy { resources.dp2px(18) }
	private val barSize by lazy { resources.dp2px(16) }
	private val needleSize by lazy { resources.dp2px(3) }

	private val phasePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
		style = Paint.Style.STROKE
		color = Color.BLACK
		strokeWidth = barSize.toFloat()
		strokeCap = Paint.Cap.ROUND
	}

	private val currentPhasePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
		style = Paint.Style.FILL
		color = Color.BLACK
		strokeWidth = barSize.toFloat()
		strokeCap = Paint.Cap.ROUND
	}

	private val needlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
		style = Paint.Style.FILL
		color = Color.DKGRAY
		strokeWidth = needleSize.toFloat()
		strokeCap = Paint.Cap.SQUARE
	}

	private val gaugeColors = arrayOf(Color.WHITE, Color.GREEN, Color.YELLOW, Color.RED, Color.MAGENTA)

	@ExperimentalTime
	override fun onDraw(canvas: Canvas)
	{
		val lastPhase = Stages.phases.last()
		val lastPhaseHoursWeighted = lastPhase.hours * 1.5f

		canvas.apply {

			val availableWidth = width - (padding)
			val phaseWidth = (availableWidth / Stages.phases.size) - spacing

			val curPhase = Stages.getCurrentPhase(elapsedHours.hours)

			// Draw the bubbles
			Stages.phases.forEachIndexed { index, phase ->

				val startX = ((index * phaseWidth) + (index * spacing) + padding).toFloat()
				val startY = padding.toFloat()

				if(curPhase == phase)
				{
					currentPhasePaint.color = gaugeColors[index]
					drawLine(startX, startY, startX + phaseWidth, startY, currentPhasePaint)
				}
				else
				{
					phasePaint.color = gaugeColors[index]
					drawLine(startX, startY, startX + phaseWidth, startY, phasePaint)
				}
			}

			// Draw the needle
			if(elapsedHours > 0L)
			{
				val curPhaseIndex = Stages.phases.indexOf(curPhase)
				val nextPhaseHours: Float = if(curPhaseIndex + 1 < Stages.phases.size)
				{
					val nextPhase = Stages.phases[curPhaseIndex + 1]
					nextPhase.hours.toFloat()
				}
				else
				{
					lastPhaseHoursWeighted
				}
				val phaseLength = (nextPhaseHours - curPhase.hours)
				val timeIntoPhase = elapsedHours - curPhase.hours

				val percent = min(timeIntoPhase / phaseLength, 1.0)

				val halfPadding = padding.toFloat() / 2f

				val startX = (curPhaseIndex * phaseWidth) + (curPhaseIndex * spacing) + padding
				val x = startX + (phaseWidth * percent)
				drawLine(x.toFloat(), halfPadding, x.toFloat(), barSize.toFloat() + halfPadding, needlePaint)
			}
		}
	}
}