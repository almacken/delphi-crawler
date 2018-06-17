package de.upb.cs.swt.delphi.crawler.storage

import akka.actor.{Actor, ActorLogging, Props}
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.HttpClient
import de.upb.cs.swt.delphi.crawler.Identifier
import de.upb.cs.swt.delphi.crawler.discovery.git.GitIdentifier
import de.upb.cs.swt.delphi.crawler.discovery.maven.MavenIdentifier
import de.upb.cs.swt.delphi.crawler.storage.ElasticActor.Push

class ElasticActor(client: HttpClient) extends Actor with ActorLogging {

  override def receive = {
    case m : MavenIdentifier => {
      log.info("Pushing new maven identifier to elastic: [{}]", m)
      client.execute {
        indexInto("delphi" / "project").fields( "name" -> (m.groupId +":"+ m.artifactId +":"+ m.version),
                                                     "source" -> "Maven",
                                                     "identifier" -> Map(
                                                       "groupId" -> m.groupId,
                                                       "artifactId" -> m.artifactId,
                                                       "version" -> m.version))
      }
    }
    case g : GitIdentifier => {
      log.info("Pushing new git identifier to elastic: [{}]", g)
      client.execute {
        indexInto("delphi" / "project").fields("name" -> (g.repoUrl +"/"+ g.commitId),
                                                     "source" -> "Git",
                                                     "identifier" -> Map(
                                                       "repoUrl" -> g.repoUrl,
                                                       "commitId" -> g.commitId))
      }
    }
    case x => log.warning("Received unknown message: [{}] ", x)
  }
}

object ElasticActor {
  def props(client: HttpClient): Props = Props(new ElasticActor(client))

  case class Push(identity: Identifier)

}





