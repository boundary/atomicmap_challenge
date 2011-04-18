import sbt._
import sbt.StringUtilities._

class AtomicMapProject(info : ProjectInfo) extends DefaultProject(info) {
  
  val specs = "org.scala-tools.testing" %% "specs" % "1.6.7" % "test"
}