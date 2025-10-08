package com.darkrockstudios.apps.fasttrack.data

object Data
{
	const val STORAGE_PATH = "storage"
	const val KEY_INTRO_SEEN = "intro_seen"
	const val KEY_PROFILE = "profile"
	const val KEY_FAST_START = "fast_start"
	const val KEY_FAST_END = "fast_end"
	const val KEY_FAST_ALERTS = "fast_alerts"
	const val KEY_FANCY_BACKGROUND = "fancy_background"

	private const val CM_INCH_RATIO = 2.54
	fun inchToCm(inches: Int): Double = inches.toDouble() * CM_INCH_RATIO
	fun cmToInch(cm: Double): Double = cm / CM_INCH_RATIO

	private const val LBS_KG_RATIO = 0.45359237
	fun lbsToKg(pounds: Double): Double = pounds * LBS_KG_RATIO
	fun kgToLbs(kg: Double): Double = kg / LBS_KG_RATIO
}