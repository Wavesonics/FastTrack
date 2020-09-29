package com.darkrockstudios.apps.fasttrack.screens.fasting

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.darkrockstudios.apps.fasttrack.data.Stages
import kotlin.time.ExperimentalTime

class GaugeView @JvmOverloads constructor(
		context: Context,
		attrs: AttributeSet? = null,
		defStyleAttr: Int = 0): View(context, attrs, defStyleAttr)
{
	var elapsedHours = 0L
		set(value)
		{
			field = value
			invalidate()
		}

	override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int)
	{
		super.onSizeChanged(w, h, oldw, oldh)

		invalidate()
		gaugeRect = null
	}

	private var gaugeRect: RectF? = null

	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec)

		setMeasuredDimension(measuredWidth, measuredWidth / 2)
	}

	private val phasePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
		style = Paint.Style.FILL
		color = Color.BLACK
	}

	private val needlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
		style = Paint.Style.FILL
		color = Color.BLACK
		strokeWidth = 32f
	}

	private val gaugeColors = arrayOf(Color.WHITE, Color.GREEN, Color.YELLOW, Color.RED, Color.MAGENTA)

	private val m = Matrix()

	@ExperimentalTime
	override fun onDraw(canvas: Canvas)
	{
		val lastPhase = Stages.phases.last()
		val lastPhaseHours = Stages.END_TIME
		val lastPhaseHoursWeighted = lastPhase.hours * 1.5f

		canvas.apply {
			translationX = 0f
			translationY = 0f
			rotation = 0f

			drawARGB(32, 64, 64, 64)

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

					drawArc(
							rect,
							180f + (percentStart * 180f),
							percentSweep * 180f,
							true,
							phasePaint)
				}

				val avalibleRot = 130f
				val rotStart = -(avalibleRot / 2f)

				val percent = elapsedHours / lastPhaseHoursWeighted
				val rot = avalibleRot * percent

				val nStart = height.toFloat() * 1.25f
				val needelLength = rect.height() * 0.35f
				rotate(rotStart + rot, width / 2f, nStart)
				drawLine(width / 2f, nStart, width / 2f, nStart - needelLength, needlePaint)
			}
		}
	}
}