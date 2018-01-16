package es.dgg.app

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings}
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import es.dgg.app.Settings.SettingsExtension
import es.dgg.app.actors.RandomSharding
import es.dgg.app.domain.Domain.Message
import com.typesafe.config.{Config, ConfigFactory}
import es.dgg.app.Settings.SettingsExtension
import es.dgg.app.actors.RandomSharding

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.{global => scalaEc}

trait HelloAkka {

  lazy val config: Config = ConfigFactory.load()

  //val systemName = "HelloAkka"

  //implicit val system: ActorSystem = ActorSystem(systemName, config)

  implicit val system : ActorSystem = ActorSystem("HelloAkka")
  val settings = SettingsExtension(system)

  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  /*

  val tickActorRojo: ActorRef = system.actorOf(
    ClusterSingletonManager.props(
      singletonProps = Props(classOf[TickActor], executionContext),
      terminationMessage = End,
      settings = ClusterSingletonManagerSettings(system).withRole("rojo")
    ),
    name = "counter"
  )

  val tickActorVerde: ActorRef = system.actorOf(
    ClusterSingletonManager.props(
      singletonProps = Props(classOf[TickActor], executionContext),
      terminationMessage = End,
      settings = ClusterSingletonManagerSettings(system).withRole("verde")
    ),
    name = "counter"
  )

  val tickActorAzul: ActorRef = system.actorOf(
    ClusterSingletonManager.props(
      singletonProps = Props(classOf[TickActor], executionContext),
      terminationMessage = End,
      settings = ClusterSingletonManagerSettings(system).withRole("azul")
    ),
    name = "counter"
  )

  */

  ClusterSharding(system).start(
    typeName = RandomSharding.name,
    entityProps = RandomSharding.props,
    settings = ClusterShardingSettings(system),
    extractShardId = RandomSharding.extractShardId,
    extractEntityId = RandomSharding.extractEntityId)

  val decider = ClusterSharding(system).shardRegion(RandomSharding.name)

  def log: LoggingAdapter = Logging(system, this.getClass)

  val route =
    path("ramdom") {
      get {
        parameters('from, 'to, 'payload) { (from, to, payload) =>
          log.info(s"Mensaje recibido de: $from para_ $to con contenido -> $payload")
          val m = new Message(from, to, payload)
          //val fromActor = system.actorOf(Props(new RandomSharding(decider)), from)
          val actor = system.actorOf(Props(classOf[RandomSharding]))
          actor ! m
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Creado usuario en nodo random</h1>"))

        }
      }
    }

  val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 8888)
  println("Server online en http://0.0.0.0:8888/ ")

}


object Boot extends App with HelloAkka {
  log.info(s"server is online")
}
