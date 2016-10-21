package com.sprayTest.mappers

import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

/**
  * Created by vasilisck on 10/17/16.
  */

object User {

  implicit object UserWriter extends BSONDocumentWriter[User] {
    def write(user: User): BSONDocument = BSONDocument(
      "login" -> user.login,
      "password" -> user.password
    )
  }

  implicit object UserReader extends BSONDocumentReader[User] {
    def read(doc: BSONDocument): User = {
      User(
        doc.getAs[String]("login").get,
        doc.getAs[String]("password").get)
    }
  }
}

case class User(login: String, password: String)

