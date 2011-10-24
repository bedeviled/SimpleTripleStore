package com.jthalbert.datastructures

/**
 * @author jthalbert
 */
object App {
  val ts = new SimpleTripleStore()

  def main(args : Array[String]) {
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
    print("\n"*3)
    println("-------------------------")
    println("query test")
    println("-------------------------")
    println(ts.query(List(List("?movie","starring","Harrison Ford"))))
  }

}
