package com.darkrockstudios.apps.fasttrack.utils

import android.os.Binder
import android.os.Bundle
import androidx.core.app.BundleCompat
import androidx.fragment.app.Fragment

class NullableFragArg<T: Any?>: kotlin.properties.ReadWriteProperty<Fragment, T>
{
	var value: T? = null

	@Suppress("UNCHECKED_CAST")
	override operator fun getValue(thisRef: Fragment, property: kotlin.reflect.KProperty<*>): T
	{
		if(value == null)
		{
			value = thisRef.arguments?.get(property.name) as T
		}

		return value as T
	}

	override operator fun setValue(thisRef: Fragment, property: kotlin.reflect.KProperty<*>, value: T)
	{
		if(thisRef.arguments == null) thisRef.arguments = Bundle()

		thisRef.arguments?.let { args ->
			val key = property.name

			when(value)
			{
				is String -> args.putString(key, value)
				is Int -> args.putInt(key, value)
				is Short -> args.putShort(key, value)
				is Long -> args.putLong(key, value)
				is Byte -> args.putByte(key, value)
				is ByteArray -> args.putByteArray(key, value)
				is Char -> args.putChar(key, value)
				is CharArray -> args.putCharArray(key, value)
				is CharSequence -> args.putCharSequence(key, value)
				is Float -> args.putFloat(key, value)
				is Bundle -> args.putBundle(key, value)
				is Binder -> BundleCompat.putBinder(args, key, value)
				is android.os.Parcelable -> args.putParcelable(key, value)
				is java.io.Serializable -> args.putSerializable(key, value)
				else                     -> throw IllegalStateException("Type of property ${property.name} is not supported")
			}
		}
	}
}