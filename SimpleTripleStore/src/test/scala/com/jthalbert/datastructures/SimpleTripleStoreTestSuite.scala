package com.jthalbert.datastructures

import org.scalatest.Assertions
import org.junit.Test

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
/**
 * Created by IntelliJ IDEA.
 * User: jthalbert
 * Date: 10/23/11
 * Time: 7:52 AM
 * To change this template use File | Settings | File Templates.
 */

class SimpleTripleStoreTestSuite extends Assertions {
  val ts = new SimpleTripleStore()
  val ts2 = new SimpleTripleStore()
  @Test
  def simpleTripleStoreShouldAcceptAdds() {

    try {
      ts.add("blade_runner", "name", "Blade Runner")
      ts.add("blade_runner", "release_date", "June 25, 1982")
      ts.add("blade_runner", "directed_by", "Ridley Scott")
    } catch {
      case e: Exception => assert(false)
    }
  }

  @Test
  def simpleTripleStoreTriplesShouldReturnCorrectValues() {
    ts.add("blade_runner", "name", "Blade Runner")
    ts.add("star_wars","name","Star Wars")
    ts.add("blade_runner", "release_date", "June 25, 1982")
    ts.add("blade_runner", "directed_by", "Ridley Scott")
    ts.add("blade_runner","starring","ABC")
    ts.add("blade_runner","starring","ABC2")
    ts.add("blade_runner","starring","ABC3")
    //ts.triples(Some("blade_runner"),Some("starring"),None).foreach(println)
    if (ts.triples(None, Some("name"), None).size != 2) assert(false)
  }

  @Test
  def simpleTripleStoreShouldMergeNicely() {
    ts.add("blade_runner", "name", "Blade Runner")
    ts.add("blade_runner", "release_date", "June 25, 1982")
    ts.add("blade_runner", "directed_by", "Ridley Scott")

    ts2.add("star_wars","name","Star Wars")
    ts2.add("star_wars","directed_by","George Lucas")

    val mergeStore = new SimpleTripleStore()
    for (triplesList <- ts.triples(None,None,None)) {
      mergeStore.add(triplesList(0), triplesList(1), triplesList(2))
    }
    for (triplesList <- ts2.triples(None,None,None)){
      mergeStore.add(triplesList(0), triplesList(1), triplesList(2))
    }
    if (mergeStore.triples(None,None,None).size != 5 ) assert(false)
  }

  /*@Test
  def simpleTripleStoreShouldLoadFromFileNicely() {
    ts.load("/Users/jthalbert/Documents/code/SimpleTripleStore/src/main/scala/com/jthalbert/data/movies.csv")
    val bladeRunnerID = ts.value(None,Some("name"), Some("Blade Runner"))
    val bladeRunnerActorIDS: List[String] = for(triple <- ts.triples(bladeRunnerID,Some("starring"),None)) yield triple(2)
    //ts.triples(bladeRunnerID,Some("starring"),None).foreach(println)
    val bladeRunnerActorNames: List[Option[String]] = for (actorID <- bladeRunnerActorIDS) yield ts.value(Some(actorID), Some("name"),None)
    println("Blade Runner Actors: ")
    println("-------------------------")
    bladeRunnerActorNames.foreach(l=>println(l.get))
    println("-------------------------")
    val harrisonFordID = ts.value(None,Some("name"),Some("Harrison Ford"))

    val moviesStarringHarrisonFord: List[Option[String]] = for (movieIDTriple <- ts.triples(None,
                                                                  Some("starring"),
                                                                  harrisonFordID)) yield ts.value(Some(movieIDTriple(0)),Some("name"),None)
    println("Movies Starring Harrison Ford:")
    println("-------------------------")
    moviesStarringHarrisonFord.foreach(l=>println(l.get))
    println("-------------------------")

    val spielbergID = ts.value(None,Some("name"),Some("Steven Spielberg"))
    val spielbergMovieIDs = for (movieIDTriple <- ts.triples(None,Some("directed_by"),spielbergID)) yield movieIDTriple(0)
    val harrisonFordMovieIDs = for (movieIDTriple <- ts.triples(None,Some("starring"),harrisonFordID)) yield movieIDTriple(0)
    val spieldbergAndHarrisonMovies = for (movieID <- spielbergMovieIDs.toSet & harrisonFordMovieIDs.toSet) yield ts.value(Some(movieID), Some("name"),None)
    println("Movies Starring Harrison Ford and Directed by Steven Spielberg")
    println("-------------------------")
    spieldbergAndHarrisonMovies.foreach(l=>println(l.get))
    println("-------------------------")
  }   */
}
