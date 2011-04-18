package atomicmap

import org.specs._
import scala.collection.mutable.ConcurrentMap
import java.util.concurrent.atomic._

/**
 * Use this as a base for your test.  You need merely override createMap[A,B] to create
 * a ConcurrentMap[A,B] with the correct behavior.
 */
abstract class AtomicMapSpec extends Specification {
  def createMap[A,B] : ConcurrentMap[A,B]
  
  "AtomicMap" should {
    val map = createMap[String,Int]
    
    "be a full concurrentmap implementation" in {
      "getOrElseUpdate" in {
        map.getOrElseUpdate("blah", 1) must ==(1)
        map.getOrElseUpdate("blah", 2) must ==(1)
      }

      "replace(k,v)" in {
        map.replace("blah", 1) must beNone
        map.put("blah", 1)
        map.replace("blah", 2) must beSome(1)
      }

      "replace(k, o, n)" in {
        map.replace("blah", 2, 1) must ==(false)
        map.put("blah", 1)
        map.replace("blah", 1, 2) must ==(true)
        map("blah") must ==(2)
      }

      "remove" in {
        map.put("blah", 1)
        map.remove("blah", 2) must ==(false)
        map.remove("blah", 1) must ==(true)
        map.get("blah") must beNone
      }

      "putIfAbsent" in {
        map.putIfAbsent("blah", 1) must beNone
        map.putIfAbsent("blah", 2) must beSome(1)
        map.putIfAbsent("derp", 3) must beNone
      }

      "-=" in {
        map.put("herp", 1)
        map -= "herp"
        map.get("herp") must beNone
      }

      "+=" in {
        map += (("herp", 1))
        map.get("herp") must beSome(1)
      }

      "iterator" in {
        map.put("herp", 1)
        map.put("derp", 2)
        
        val otherMap = createMap[String,Int]

        for ((key,value) <- map) {
          otherMap.put(key,value)
        }
        
        otherMap.get("herp") must beSome(1)
        otherMap.get("derp") must beSome(2)
      }
      
      "get" in {
        map.put("herp", 5)
        map.get("herp") must beSome(5)
      }
    }
    
    "evaluate op only once" in {
      val counter = new AtomicInteger(0)
      
      val threads = for (i <- (0 to 5)) yield {
        new Thread {
          override def run {
            map.getOrElseUpdate("blah", counter.incrementAndGet)
            Thread.sleep(100)
          }
        }
      }
      threads.foreach(_.start)
      threads.foreach(_.join)
      map("blah") must ==(1)
      counter.get must ==(1)
    }
  }
}