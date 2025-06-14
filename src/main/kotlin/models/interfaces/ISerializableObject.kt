package models.interfaces

interface ISerializableObject {
    fun toObservable(): IObservableObject
}