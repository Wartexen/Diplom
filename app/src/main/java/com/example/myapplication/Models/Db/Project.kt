package com.example.myapplication.Models.Db
import android.os.Parcel
import android.os.Parcelable


data class Project(
    val ID: Int,
    val Title: String,
    val Supervisor: String,
    val Status: String? = null,
    val DefenseStartTime: String? = null

) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(ID)
        parcel.writeString(Title)
        parcel.writeString(Supervisor)
        parcel.writeString(Status)
        parcel.writeString(DefenseStartTime)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Project> {
        override fun createFromParcel(parcel: Parcel): Project {
            return Project(parcel)
        }

        override fun newArray(size: Int): Array<Project?> {
            return arrayOfNulls(size)
        }
    }
}


