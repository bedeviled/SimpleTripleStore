package com.jthalbert

import scalala.tensor.dense.DenseVector
import scalala.library.Plotting


/**
 * @author ${user.name}
 */
object App {
  val x = DenseVector.range(0,100)/100.0
  Plotting.hold(true)
  Plotting.plot(x,x :^ 2)
  Plotting.xlabel("x axis")
  Plotting.ylabel("y axis")
  Plotting.subplot(2,1,2)
  Plotting.hist(DenseVector.randn(1000000),100)
  def foo(x : Array[String]) = x.foldLeft("")((a,b) => a + b)
  
  def main(args : Array[String]) {
    println( "Hello World!" )
    println("concat arguments = " + foo(args))

  }

}
