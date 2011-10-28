package com.jthalbert.datastructures

import java.net.{URLEncoder, URL}
import io.Source
import collection.mutable.{ListBuffer, HashMap}

/**
 * Created by IntelliJ IDEA.
 * User: jthalbert
 * Date: 10/25/11
 * Time: 8:21 AM
 * To change this template use File | Settings | File Templates.
 */

trait InferenceRule {
  def getQueries(): List[List[List[String]]] = {
    return List(List(List()))
  }
  def makeTriples(binding: List[Map[String,String]]): List[List[String]]
    //need a way to pull out a hashmap and grab values to pass in.
    //unfortunately repeated by-name parameters aren't supported.

 }


class WestCoastRule extends InferenceRule {

  override def getQueries(): List[List[List[String]]] = {
    val sfoQuery = List(List("?company","headquarters","San_Francisco_California"))
    val seaQuery = List(List("?company","headquarters","Seattle_Washington"))
    val laxQuery = List(List("?company","headquarters","Los_Angelese_California"))
    val porQuery = List(List("?company","headquarters","Portland_Oregon"))
    List(sfoQuery,seaQuery,laxQuery,porQuery)
  }

  def makeTriples(binding: List[Map[String, String]]): List[List[String]] = {
    //this will work if ?company in binding... if not it will return an empty list
    for (b <- binding if b.contains("?company")) yield List(b("?company"),"on_coast","west_coast")
    //TODO: so check for it.
  }
}

class EnemyRule extends InferenceRule {

  override def getQueries(): List[List[String]] = {
    val partnerEnemy = List(List("?person","enemy","?enemy"),
      List("?rel","with","?person"),
      List("?rel","with","?partner"))
    List(partnerEnemy)
  }

  def makeTriples(binding: List[Map[String, String]]): List[List[String]] = {
    for (b <- binding if b.contains("?enemy") & b.contains("?partner")) yield List(b("?partner"), "enemy", b("?enemy"))
  }
}

class GeocodeRule extends InferenceRule {
  override def getQueries(): List[List[List[String]]] = {
    val addressQuery = List(List("?place","address","?address"))
    List(addressQuery)
  }


  def makeTriples(binding: List[Map[String, String]]): List[List[String]] = {
    //for comprehension doesn't work (or I can't figure it out) for this one
    // since I need to emit two things per binding element
    val outList = new ListBuffer[List[String]]()
    for (b <- binding) {
      if (b.contains("?place") & b.contains("?address")) {
        val place = b("?place")
        val address = b("?address")
        val url = new URL("http://rpc.geocoder.us/service/csv?address=%s" format URLEncoder.encode(address,"UTF-8"))
        val data = Source.fromInputStream(url.openStream()).getLines().mkString
        Thread.sleep(1500)  //a courtesy to the geocoder folks
        val parts = data.split(",")
        if (parts.size>=5) {
          outList.append(List(place,"longitude",parts(0)))
          outList.append(List(place, "latitude",parts(1)))
        }
      }
    }
    return outList.toList
  }

  def tripleMaker(binding: String*): List[List[String]] = {
    val place = binding(0)
    val address = binding(1)
    val url = new URL("http://rpc.geocoder.us/service/csv?address=%s" format URLEncoder.encode(address,"UTF-8"))
    val data = Source.fromInputStream(url.openStream()).getLines().mkString
    Thread.sleep(1500)  //a courtesy to the geocoder folks
    val parts = data.split(",")
    if (parts.size>=5) {
      return List(List(place,"longitude",parts(0)), List(place, "latitude",parts(1)))
    } else {
      //address couldn't be geo-coded
      return List(List())
    }
  }

}

class CloseToRule(place: String,  graph: SimpleTripleStore) extends InferenceRule {
  val laq = List(graph.triples(Some(place),Some("latitude"),None))
  val loq = List(graph.triples(Some(place),Some("longitude"),None))
  if (laq(0)(0).isEmpty | loq(0)(0).isEmpty) {
    throw new Exception("%s is not geocoded in the graph" format place)
  }
  val lat = laq(0)(0)(2).toFloat
  val long = loq(0)(0)(2).toFloat

  override def getQueries(): List[List[List[String]]] = {
    val geoq = List(List("?place","latitude","?lat"),List("?place","longitude","?long"))
    List(geoq)
  }

  def makeTriples(binding: List[Map[String, String]]): List[List[String]] = {
    for (b <- binding if b.contains("?place") & b.contains("?lat") & b.contains("?long")) yield {
      val distance = Math.pow(Math.pow(69.1*(lat-b("?lat").toFloat),2) + Math.pow (53*(long - b("?long").toFloat),2),0.5)
      if (distance < 1) {
        List(b("?place"),"close_to",b("place"))
      } else {
        List(b("?place"),"far_from",b("place"))
      }
    }
  }

  def tripleMaker(binding: String*): List[List[String]] = {
    //Formula for approximate distance (if you want to do large distances you need the great circle distance
    val myplace = binding(0)
    val mylat = binding(1)
    val mylong = binding(2)
    val distance = Math.pow(Math.pow(69.1*(lat-mylat.toFloat),2) + Math.pow (53*(long - mylong.toFloat),2),0.5)

    //Are they less than 1 mile apart
    if (distance < 1) {
      return List(List(place,"close_to",myplace))
    } else {
      return List(List(place,"far_from",myplace))
    }
  }
}

class TouristyRule extends InferenceRule {
  override def getQueries(): List[List[List[String]]] = {
    val tr = List(List("?ta","is_a","Tourist Attraction"),
      List("?ta","close_to","?restaurant"),
      List("?restaurant","is_a","restaurant"),
      List("?restaurant","cost","cheap"))
    List(tr)
  }

  def makeTriples(binding: List[Map[String, String]]): List[List[String]] = {
    for (b <-  binding if b.contains("?restaurant")) yield List(b("?restaurant"),"is_a","touristy restaurant")
  }

  def tripleMaker(binding: String*): List[List[String]] = {
    val ta = binding(0)
    val restaurant = binding(1)
    List(List(restaurant, "is_a", "touristy restaurant"))
  }
}