import scala.io.StdIn
import org.apache.spark.sql.SparkSession

object Task_1 extends App {

  val spark = SparkSession.builder()
    .appName("SparkSession_1")
    .master("local[1]")
    .getOrCreate()

  println("On the next string input numbers:")

  var n: Int = StdIn.readInt()
  val c: Int = StdIn.readInt()
  var i = 1
  val m = n

  println("Numbers: ")

  while (n >= c)
  {
    for (i <- i until (i + c))
      {
        if (i != m)
          {
            print(s"$i, ")
          }
        else
          {
            print(s"$i.")
          }
      }
    n -= c
    i += c
    print("\n")

  }

  for (i <- i until (i + n))
    {
      if (i != m)
      {
        print(s"$i, ")
      }
      else
      {
        print(s"$i.")
      }
    }
  print("\n")
}
