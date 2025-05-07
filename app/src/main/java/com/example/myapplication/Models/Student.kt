package com.example.myapplication.Models
import android.os.Parcel
import android.os.Parcelable

data class Student(
    val ID: Int,
    val ID_Group: Group,
    val Surname: String,
    val Name: String,
    val Patronymic: String,
    val ID_Specialization: Int,
    val ID_Project: Int
) :  Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readParcelable(Group::class.java.classLoader) ?: Group(0, "", 0),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(ID)
        parcel.writeParcelable(ID_Group, flags)
        parcel.writeString(Surname)
        parcel.writeString(Name)
        parcel.writeString(Patronymic)
        parcel.writeInt(ID_Specialization)
        parcel.writeInt(ID_Project)
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