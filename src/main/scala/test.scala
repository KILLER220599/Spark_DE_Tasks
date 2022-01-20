import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object test extends App {

  val spark = SparkSession.builder()
    .appName("test")
    .master("local[1]")
    .getOrCreate()

  spark.catalog.listTables("database") // показать таблицы в конкретной базе данных

  import test.spark.implicits._ // Или spark.implicits._

  val u_data = spark.read.textFile("C:/Users/KRyzhov/Desktop/Tasks_DE/ml-100k/u.data")
  val s_udata = u_data.withColumn("tmp", split($"value", "\t")).select(
    $"tmp".getItem(0).as("user_id"),
    $"tmp".getItem(1).as("item_id"),
    $"tmp".getItem(2).as("rating"),
    $"tmp".getItem(3).as("timestamp")
  )

  s_udata.select(col("user_id"), col("rating"), lit(1).as("lit_1"))
         //.show(false)

  s_udata.select(col("user_id"), conv(col("user_id"), 5, 10))//.show()

  s_udata.select(count("*"))//.show(false)

  s_udata.select(covar_samp(col("user_id"), col("rating"))).distinct()//.show(false)

  s_udata.select(encode(col("user_id"), "windows-1251"))//.show(false)

  s_udata.select(first("user_id"))//.show(false)

  s_udata.select(hash(col("user_id")))//.show(false)

  s_udata.select(hex(col("user_id")))//.show(false)

  s_udata.select(col("user_id"), coalesce(col("user_id"), lit(1))).show(false)



//###############################################
//  val ah = spark.sparkContext.textFile("C:/Users/KRyzhov/Desktop/Tasks_DE/books.csv")
//  //ah.take(10).foreach(println)
//  val head = ah.first()
//  head.foreach(print)
//
//  val awh = ah.filter(_.!= (head))
//
//  val a = awh.take(10).foreach(println)
//###############################################

//###############################################
//  val stroka: String = "Type in expressions to have them evaluated"
//  val t = stroka.split(" ").foreach(println)

  //t.flatMap(value => value.split("a"))
  //res0: Array[String] = Array(Type, in, expressions, to, h, ve, them, ev, lu, ted.)

  //t.map(value => value.split("a"))
  //res1: Array[Array[String]] = Array(Array(Type), Array(in), Array(expressions), Array(to), Array(h, ve), Array(them), Array(ev,

  //t.flatMap(value => value.split("a"))
//###############################################

}
