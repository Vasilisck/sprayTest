package com.sprayTest


import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import com.sprayTest.service.SprayTestServiceActor
import spray.can.Http

import scala.concurrent.duration._


/**
  * Created by vasilisck on 10/17/16.
  */
object Main extends App {

  implicit val system = ActorSystem("on-spray-can")
  val service = system.actorOf(Props[SprayTestServiceActor], "demo-service")
  implicit val timeout = Timeout(5.seconds)
  IO(Http) ? Http.Bind(service, interface = "localhost", port = 3000)

}
