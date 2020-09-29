package com.darkrockstudios.apps.fasttrack.screens.fasting

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View

class GaugeView @JvmOverloads constructor(
		context: Context,
		attrs: AttributeSet? = null,
		defStyleAttr: Int = 0): View(context, attrs, defStyleAttr)
{
	override fun draw(canvas: Canvas?)
	{
		super.draw(canvas)

		canvas?.drawColor(0)
	}
}