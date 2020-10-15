package com.badoo.mvicore.android

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.hamcrest.core.IsNull.notNullValue
import org.junit.Test

class AndroidTimeCapsuleTest {

    @Test
    fun saveAndRestoreState() {
        val timeCapsule = AndroidTimeCapsule(null)
        timeCapsule.register("test_key") { SimpleTestParcelable("test_value") }

        val bundle = Bundle()
        timeCapsule.saveState(bundle)

        val restoredTimeCapsule = AndroidTimeCapsule(bundle)
        val restoredState = restoredTimeCapsule.get<SimpleTestParcelable>("test_key")

        assertThat(restoredState, notNullValue())
        assertThat(restoredState?.value, equalTo("test_value"))
    }

    class SimpleTestParcelable(val value: String) : Parcelable {

        constructor(source: Parcel) : this(source.readString()!!)

        override fun describeContents() = 0

        override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
            writeString(value)
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<SimpleTestParcelable> = object : Parcelable.Creator<SimpleTestParcelable> {
                override fun createFromParcel(source: Parcel): SimpleTestParcelable = SimpleTestParcelable(source)
                override fun newArray(size: Int): Array<SimpleTestParcelable?> = arrayOfNulls(size)
            }
        }
    }
}
