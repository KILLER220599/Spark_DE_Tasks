import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.{SparkConf, SparkContext}

object Task_2 extends App {

  val spark = SparkSession.builder()
    .appName("SparkSession_2")
    .master("local[1]")
    .getOrCreate()


  //#################################################################

  // Решение через csv с созданием DataFrame

  var rd_csv = spark.read.csv("C:/Users/KRyzhov/Desktop/Tasks_DE/books.csv").toDF(
    "bookID", "title", "authors", "average_rating", "isbn", "isbn13",
    "language_code", "num_pages", "ratings_count", "text_reviews_count", "publication_date", "publisher")
  val header = rd_csv.head()

  val csv_wt_h = rd_csv.filter(_ != header)
  csv_wt_h.show(false)

  val avg_rating = csv_wt_h.select("average_rating").where("average_rating > 4.50")
  avg_rating.show(false)

  val avg_rating_allb = csv_wt_h.select("average_rating").agg(round(avg("average_rating"), 2)
    .as("avg_rtg_ab"))
  avg_rating_allb.show(false)

  val group_title = csv_wt_h.groupBy("title").agg(count("title")
    .as("cnt_bk_title")) // Или так: select("title").groupBy("title").agg(count("title").as("cnt_bk_title"))
  group_title.show(false)

  val cnt_book_w_r = group_title.withColumn("book_range",
    when(col("cnt_bk_title") === "0" ||
      col("cnt_bk_title") === "1", "0 - 1")
      .when(col("cnt_bk_title") === "1" ||
        col("cnt_bk_title") === "2", "1 - 2")
      .when(col("cnt_bk_title") === "2" ||
        col("cnt_bk_title") === "3", "2 - 3")
      .when(col("cnt_bk_title") === "3" ||
        col("cnt_bk_title") === "4", "3 - 4")
      .when(col("cnt_bk_title") === "4" ||
        col("cnt_bk_title") === "5", "4 - 5")
      .otherwise("More, then 4 - 5")
  )
  cnt_book_w_r.show(false)

  cnt_book_w_r.groupBy("book_range").agg(count("cnt_bk_title").as("book_in_range"))
    .orderBy(asc("book_range")).show(false)

  spark.stop()
}

//#################################################################

//#################################################################

// Решение через textfile

//  val rd_file = spark.sparkContext.textFile("C:/Users/KRyzhov/Desktop/Tasks_DE/books.csv")
//  println(rd_file.count)
//  rd_file.take(5).foreach(println)
//
//  val col_avg_raiting = rd_file.map(line =>
//    { val col_arr = line.split(",")
//      col_arr(3)
//    }) //.take(10).foreach(println)
//  val big_rating = col_avg_raiting.filter(r => r > "4.50").foreach(println)

//#################################################################

// Первый вариант решения вторго задания

//  val read_csv = spark
//    .read
//    .option("header", "true")
//    .csv("C:/Users/KRyzhov/Desktop/Tasks_DE/books.csv")
//    read_csv.printSchema()
//    val c = read_csv.count()
//    println(s"Count records: $c")
//    var table = read_csv.show(10)
//    println(table)
//    val col_avg_raiting = rd_file.map(line =>
//      { val col_arr = line.split(",")
//        col_arr(3)
//      }) //.take(10).foreach(println)
//    col_avg_raiting.filter(r => r > "4.50").foreach(println)

//#################################################################

// Использование SparkContext и SparkConf

//val SparkConf = new SparkConf()
//SparkConf.setAppName("Test")
//SparkConf.setMaster("Local")
//
//val sc = new SparkContext(SparkConf)
//
//val arr = Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 0)
//val arrRDD = sc.parallelize(arr, 2)
//
//println("Number of elements in RDD: ", arrRDD.count())
//arrRDD.foreach(println)

//  val arr = Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
//  val arrRDD = spark.sparkContext.parallelize(arr, 3)
//  arrRDD.foreach(println)
//
//  val file = "C:/Users/KRyzhov/Desktop/Tasks_DE/books.csv"
//  val fileRDD = spark.sparkContext.textFile(file)
//  println("Number of records: " + fileRDD.count())
//  fileRDD.take(10).foreach(println)

//#################################################################

