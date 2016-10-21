package com.sprayTest.mappers

import java.util.Date

import org.json4s.{DefaultFormats, JValue, Reader}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID}

/**
  * Created by vasilisck on 10/17/16.
  */
object Note {

  implicit val formats = DefaultFormats

  implicit object NoteWriter extends BSONDocumentWriter[Note] {
    def write(note: Note): BSONDocument = BSONDocument(
      "title" -> note.title,
      "body" -> note.body
    )
  }

  implicit object NoteReader extends BSONDocumentReader[Note] {
    def read(doc: BSONDocument): Note = {
      Note(
        doc.getAs[BSONObjectID]("_id").get.stringify,
        doc.getAs[String]("title").get,
        doc.getAs[String]("body").get)
    }
  }

  implicit object PersonReader extends Reader[Note] {
         def read(json: JValue): Note = Note(null, (json \ "title").extract[String], (json \ "body").extract[String])
      }

}

case class Note(_id: String, title: String, body: String)
