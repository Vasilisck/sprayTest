package com.sprayTest.mappers

import java.util.Date

import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

/**
  * Created by vasilisck on 10/17/16.
  */
object Token {

  implicit object TokenWriter extends BSONDocumentWriter[Token] {
    def write(token: Token): BSONDocument = BSONDocument(
      "value" -> token.value
    )
  }

  implicit object TokenReader extends BSONDocumentReader[Token] {
    def read(doc: BSONDocument): Token = {
      Token(
        doc.getAs[String]("value").get)
    }
  }
}

case class Token(value: String)
