package models.interfaces

interface IObservableObject {
    fun toSerializable(): ISerializableObject
}