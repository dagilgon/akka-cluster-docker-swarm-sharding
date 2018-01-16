package es.dgg.app.domain

object Domain {

  case class Message(from: String, to: String , payload: String)
}