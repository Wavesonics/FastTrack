package com.darkrockstudios.apps.fasttrack.screens.fasting

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.darkrockstudios.apps.fasttrack.data.Stages
import kotlin.math.min
import kotlin.time.ExperimentalTime

class GaugeView @JvmOverloads constructor(
		context: Context,
		attrs: AttributeSet? = null,
		defStyleAttr: Int = 0): View(context, attrs, defStyleAttr)
{
	private var gaugeRect: RectF? = null

	// If anyone sets this value, we need to redraw
	var elapsedHours = 0L
		set(value)
		{
			field = value
			invalidate()
		}

	override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int)
	{
		super.onSizeChanged(w, h, oldw, oldh)

		gaugeRect = null
		invalidate()
	}

	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec)

		setMeasuredDimension(measuredWidth, measuredWidth / 3)
	}

	private val phasePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
		style = Paint.Style.FILL
		color = Color.BLACK
	}

	private val needlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
		style = Paint.Style.FILL
		color = Color.BLACK
		strokeWidth = 16f
		strokeCap = Paint.Cap.ROUND
	}

	private val gaugeColors = arrayOf(Color.WHITE, Color.GREEN, Color.YELLOW, Color.RED, Color.MAGENTA)

	@ExperimentalTime
	override fun onDraw(canvas: Canvas)
	{
		val lastPhase = Stages.phases.last()
		val lastPhaseHoursWeighted = lastPhase.hours * 1.5f

		canvas.apply {
			translationX = 0f
			translationY = 0f
			rotation = 0f

			//drawARGB(32, 64, 64, 64)

			if(gaugeRect == null)
			{
				gaugeRect = RectF(0f, 0f, width.toFloat(), height * 2.5f)
			}

			gaugeRect?.let { rect ->
				Stages.phases.forEachIndexed { index, phase ->

					val nextPhaseHours: Float = if(index + 1 < Stages.phases.size)
					{
						val nextPhase = Stages.phases[index + 1]
						nextPhase.hours.toFloat()
					}
					else
					{
						lastPhaseHoursWeighted
					}

					val percentStart = phase.hours / lastPhaseHoursWeighted
					val percentSweep = (nextPhaseHours - phase.hours) / lastPhaseHoursWeighted

					phasePaint.color = gaugeColors[index]

					val arc = 180f
					drawArc(
							rect,
							arc + (percentStart * arc),
							percentSweep * arc,
							true,
							phasePaint)
				}

				val availableRot = 130f
				val rotStart = -(availableRot / 2f)

				val percent = min(elapsedHours / lastPhaseHoursWeighted, 1.0f)
				val rot = availableRot * percent

				val nStart = height.toFloat() * 1.25f
				val needleLength = rect.height() * 0.40f
				rotate(rotStart + rot, width / 2f, nStart)
				drawLine(width / 2f, nStart, width / 2f, nStart - needleLength, needlePaint)
			}
		}
	}
}