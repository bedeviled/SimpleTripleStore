package com.jthalbert.datastructures

import collection.mutable.HashMap
import java.net.{URLEncoder, URL}
import io.Source

/**
 * Created by IntelliJ IDEA.
 * User: jthalbert
 * Date: 10/25/11
 * Time: 8:21 AM
 * To change this template use File | Settings | File Templates.
 */

trait TripleMaker {
  def tripleMaker(binding: Map[String,String]*): List[List[String]]
}

trait InferenceRule extends TripleMaker {
  def getQueries(): List[List[List[String]]] = {
    return List(List(List()))
  }
  def makeTriples(binding: List[Map[String,String]]): List[List[String]] = {
    //need a way to pull out a hashmap and grab values to pass in.
    //unfortunately repeated by-name parameters aren't supported.
      return tripleMaker(binding:_*)
  }
 }


class WestCoastRule extends InferenceRule {

  override def getQueries(): List[List[List[String]]] = {
    val sfoQuery = List(List("?company","headquarters","San_Francisco_California"))
    val seaQuery = List(List("?company","headquarters","Seattle_Washington"))
    val laxQuery = List(List("?company","headquarters","Los_Angelese_California"))
    val porQuery = List(List("?company","headquarters","Portland_Oregon"))
    List(sfoQuery,seaQuery,laxQuery,porQuery)
  }

  def tripleMaker(binding: String*): List[List[String]] = {
    val company = binding(0)
    List(List(company, "on_coast", "west_coast"))
  }
}

class EnemyRule extends InferenceRule {

  override def getQueries(): List[List[String]] = {
    val partnerEnemy = List(List("?person","enemy","?enemy"),
      List("?rel","with","?person"),
      List("?rel","with","?partner"))
    List(partnerEnemy)
  }

  def tripleMaker(binding: String*): List[List[String]] = {
    val person = binding(0)
    val enemy = binding(1)
    val rel = binding(2)
    val partner = binding(3)
    List(List(partner, "enemy", enemy))
  }
}

class GeocodeRule extends InferenceRule {
  override def getQueries(): List[List[List[String]]] = {
    val addressQuery = List(List("?place","address","?address"))
    List(addressQuery)
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

  def tripleMaker(binding: String*): List[List[String]] = {
    val ta = binding(0)
    val restaurant = binding(1)
    List(List(restaurant, "is_a", "touristy restaurant"))
  }
}