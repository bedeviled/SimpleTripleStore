package com.jthalbert.datastructures

import java.io.{FileWriter, FileReader}
import au.com.bytecode.opencsv.{CSVWriter, CSVReader}
import scala.collection.JavaConversions._
import collection.immutable.Nil
import collection.mutable.{ListBuffer, HashSet, HashMap}
import io.{Codec, Source}

/**
 * Created by IntelliJ IDEA.
 * User: jthalbert
 * Date: 10/22/11
 * Time: 1:48 PM
 * To change this template use File | Settings | File Templates.
 */

class SimpleTripleStore {
  val SPO = new HashMap[String, HashMap[String, HashSet[String]]]
  val POS = new HashMap[String, HashMap[String, HashSet[String]]]
  val OSP = new HashMap[String, HashMap[String, HashSet[String]]]

  private def addToIndex(index: HashMap[String, HashMap[String, HashSet[String]]],
                 a: String, b: String, c: String): Unit = {
    if (!index.contains(a)) {
      index += a -> HashMap(b -> HashSet(c))
    } else {
      if (!index(a).contains(b)) {
        index(a) += b -> HashSet(c)
      } else {
        index(a)(b).add(c)
      }
    }
  }

  def add(sub: String, pred: String, obj: String): Unit = {
    addToIndex(SPO, sub, pred, obj)
    addToIndex(POS, pred, obj, sub)
    addToIndex(OSP, obj, sub, pred)
  }

  private def removeFromIndex(index: HashMap[String, HashMap[String, HashSet[String]]],
                              a: Any, b: Any, c: Any): Unit = {
    //Removes a triple from an index and clears up empty intermediate structures
    try {
      var bs = index(a.toString())
      var cset = bs(b.toString())
      cset -= c.toString()
      if (cset.size == 0) {
        bs -= b.toString()
      }
      if (bs.size == 0) {
        index -= a.toString()
      }
    } catch {
      case e: Exception => null
    }
  }
    //TODO: add test for this.
  def remove(sub: String,  pred: String,  obj: String): Unit = {
    //Remove a triple pattern from the graph
    val tpls = triples(Some(sub), Some(pred), Some(obj))
    for (List(delSub, delPred, delObj) <- tpls) {
      removeFromIndex(SPO,delSub,delPred,delObj)
      removeFromIndex(POS,delPred,delObj,delSub)
      removeFromIndex(OSP,delObj,delSub,delPred)
    }
  }

  def triples(sub: Option[String],  pred: Option[String],  obj: Option[String]): List[List[String]] = {

    try {
       if (sub != None) {
         if (pred != None) {
           val outList = new ListBuffer[List[String]]()
           //Sub Pred Obj
           if (obj != None) {
             if (SPO(sub.get)(pred.get).contains(obj.get)) {
               outList.append(List(sub.get.toString,pred.get.toString,obj.get.toString))
             }
           } else {
             //Sub Pred None
             for (retObj <- SPO(sub.get)(pred.get)) {
               outList.append(List(sub.get.toString, pred.get.toString, retObj.toString))
             }
           }
           return outList.toList
         } else {
           //Sub None obj
           if (obj != None) {
             val outList = new ListBuffer[List[String]]()
             for (retPred <- OSP(obj.get)(sub.get)) {
               outList.append(List(sub.get.toString, retPred.toString, obj.get.toString))
             }
             return outList.toList
           } else {
             //Sub None None
             val outList = new ListBuffer[List[String]]()
             for ((retPred, objSet) <- SPO(sub.get)) {
               for (retObj <- objSet) {
                 outList.append(List(sub.get.toString, retPred.toString, retObj.toString))
               }
             }
             return outList.toList
           }
         }
       } else {
         if (pred != None) {
           //None pred obj
           if (obj != None) {
             val outList = new ListBuffer[List[String]]()
             for (retSub <- POS(pred.get)(obj.get)){
               outList.append(List(retSub.toString, pred.get.toString, obj.get.toString))
             }
             return outList.toList
           } else {
             //None pred None
             val outList = new ListBuffer[List[String]]()
             for ( (retObj, subSet) <- POS(pred.get)) {
               for (retSub <- subSet){
                  outList.append(List(retSub.toString, pred.get.toString, retObj.toString))
               }
             }
             return outList.toList
           }
         } else {
           //None None obj
           if (obj != None) {
             val outList = new ListBuffer[List[String]]()
             for ((retSub, predSet) <- OSP(obj.get)) {
               for (retPred <- predSet) {
                 outList.append(List(retSub.toString, retPred.toString, obj.get.toString))
               }
             }
             return outList.toList
           } else {
             //None None None
             val outList = new ListBuffer[List[String]]()
             for ((retSub,predMap) <- SPO) {
               for ((retPred, objSet) <- predMap) {
                 for (retObj <- objSet) {
                   outList.append(List(retSub.toString, retPred.toString, retObj.toString))
                 }
               }
             }
             return outList.toList
           }
         }
       }
    } catch {
      case e: Exception => {e.printStackTrace()
        return List(List())}
    }
  }

  def query(clauses: List[List[String]]): List[HashMap[String, String]] = {
    var bindings = new ListBuffer[HashMap[String, String]]()
    //Query building
        for (clause <- clauses) {
          val bpos = new HashMap[String,  Int]()
          val qc = new ListBuffer[Option[String]]()
          for (x <- clause) {
            if (x.startsWith("?")) {
              qc.append(None)
              bpos += x -> clause.indexOf(x)
            } else {
              qc.append(Some(x))
            }
          }
          //query passing... this could pass back an empty list of lists....so surround with try/catch
          val rows = triples(qc(0), qc(1), qc(2))
          if (bindings.size == 0) {
            // This is the first pass, everything matches
            try{
              for (row <- rows) {
                val binding = new HashMap[String, String]()
                for ((vr, pos) <- bpos) {
                  binding += vr -> row(pos)
                }
                bindings.append(binding)
              } } catch {
              case e: Exception => return bindings.toList //no need to go further
            }
          } else {
            // In subsequent passes, eliminate bindings that don't work
            val newb = new ListBuffer[HashMap[String, String]]()
            for (binding <- bindings) {
              for (row <- rows) {
                var validMatch = true
                val tempBinding = binding.clone() //Does this make a copy?
                for ((vr, pos) <- bpos) {
                  if (tempBinding.contains(vr)) {
                    if (tempBinding(vr) != row(pos)) {
                      validMatch = false
                    }
                  } else {
                    tempBinding += vr -> row(pos)
                  }
                }
                if (validMatch) newb.append(tempBinding)
              }
            }
            bindings = newb
          }
        }
    return bindings.toList
  }

  //apply inference, that is grab bindings, transform, put more triples in
  def applyInference(rule: InferenceRule): Unit= {

  }
  //Convenience method to grab a single value quickly
  def value(sub: Option[String] = None,
            pred: Option[String] = None,
            obj: Option[String] = None): Option[String] = {
    for (triplesList <- triples(sub, pred, obj)) {
      if (sub == None) return Some(triplesList(0))
      if (pred == None) return Some(triplesList(1))
      if (obj == None) return Some(triplesList(2))
    }
    return None
  }
  def load(filename: String): Unit ={
    var sub = ""
    var pred = ""
    var obj = ""
    var row: Array[String] = Array()
    Source.fromFile(filename).getLines().foreach{line =>
      row = line.split(",")
      sub = row(0)
      pred = row(1)
      obj = row(2)
      add(sub,pred,obj)
    }
    /*for (line <- Source.fromFile(filename).getLines()) {
      row = line.split(",")
      sub = row(0)
      pred = row(1)
      obj = row(2)
      add(sub,pred,obj)
    } */
  }

  def save(filename: String): Unit = {
    val writer = new CSVWriter(new FileWriter(filename))
    for (tripleList <- triples(None, None, None)) {
      writer.writeNext(tripleList.toArray)
    }
    writer.close()
  }
}