package com.example.myapplication.Models
import android.os.Parcel
import android.os.Parcelable

data class Student(
    val ID: Int,
    val Name: String?,
    val Surname: String?,
    val Patronymic: String?,
    val ID_Group: Int?,
    val GroupName: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(ID)
        parcel.writeString(Name)
        parcel.writeString(Surname)
        parcel.writeString(Patronymic)
        parcel.writeValue(ID_Group)
        parcel.writeString(GroupName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Student> {
        override fun createFromParcel(parcel: Parcel): Student {
            return Student(parcel)
        }

        override fun newArray(size: Int): Array<Student?> {
            return arrayOfNulls(size)
        }
    }
}
