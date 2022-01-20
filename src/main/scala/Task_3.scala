import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions._

object Task_3 extends App {

  val spark = SparkSession.builder()
    .appName("Spark_Session_3")
    .master("local[1]")
    .getOrCreate()

//######################################################################

  import Task_3.spark.implicits._

  val u_data = spark.read.textFile("C:/Users/KRyzhov/Desktop/Tasks_DE/ml-100k/u.data")
  val s_udata = u_data.withColumn("tmp", split($"value", "\t")).select(
    $"tmp".getItem(0).as("user_id"),
    $"tmp".getItem(1).as("item_id"),
    $"tmp".getItem(2).as("rating"),
    $"tmp".getItem(3).as("timestamp")
  )

  // Второй способ для u_data(для u_item тоже подойдёт):
  //  val s_udata = u_data.withColumn("user_id", split($"value", "\t").getItem(0))
  //                      .withColumn("item_id", split($"value", "\t").getItem(1))
  //                      .withColumn("rating", split($"value", "\t").getItem(2))
  //                      .withColumn("timestamp", split($"value", "\t").getItem(3))
  //                      .drop($"value")
  //s_udata.show(false)
  s_udata.createOrReplaceTempView("ud")

  val u_item = spark.read.textFile("C:/Users/KRyzhov/Desktop/Tasks_DE/ml-100k/u.item")
  val s_uitem = u_item.withColumn("tmp", split($"value", "\\|")).select(
    $"tmp".getItem(0).as("movie_id"),
    $"tmp".getItem(1).as("movie_title"),
    $"tmp".getItem(2).as("release_date"),
    $"tmp".getItem(3).as("video_release_date"),
    $"tmp".getItem(4).as("IMDb_URL"),
    $"tmp".getItem(5).as("unknown"),
    $"tmp".getItem(6).as("Action"),
    $"tmp".getItem(7).as("Adventure"),
    $"tmp".getItem(8).as("Animation"),
    $"tmp".getItem(9).as("Children's"),
    $"tmp".getItem(10).as("Comedy"),
    $"tmp".getItem(11).as("Crime"),
    $"tmp".getItem(12).as("Documentary"),
    $"tmp".getItem(13).as("Drama"),
    $"tmp".getItem(14).as("Fantasy"),
    $"tmp".getItem(15).as("Film-Noir"),
    $"tmp".getItem(16).as("Horror"),
    $"tmp".getItem(17).as("Musical"),
    $"tmp".getItem(18).as("Mystery"),
    $"tmp".getItem(19).as("Romance"),
    $"tmp".getItem(20).as("Sci-Fi"),
    $"tmp".getItem(21).as("Thriller"),
    $"tmp".getItem(22).as("War"),
    $"tmp".getItem(23).as("Western")
  )
  //s_uitem.show(30, false)
  s_uitem.createOrReplaceTempView("ui")

  val ij_dat_it = s_udata.join(s_uitem, s_udata("item_id") === s_uitem("movie_id"), "inner")
                         .orderBy("item_id")
    ij_dat_it.show(10, false)
    ij_dat_it.createOrReplaceTempView("ij_di")

  val mov_rat = spark.sql(
      "SELECT item_id AS m_itid, movie_title AS mt, rating AS mov_r, COUNT(rating) AS count_rating " +
              "FROM ij_di " +
              "GROUP BY item_id, movie_title, rating " +
              "ORDER BY item_id, rating"
    )
  //mov_rat.show(false)
  mov_rat.createOrReplaceTempView("mr")
//          Замена COUNT(rating): "(CASE " +
//                                          "WHEN rating = 1 THEN COUNT(rating) " +
//                                          "WHEN rating = 2 THEN COUNT(rating) " +
//                                          "WHEN rating = 3 THEN COUNT(rating) " +
//                                          "WHEN rating = 4 THEN COUNT(rating) " +
//                                          "WHEN rating = 5 THEN COUNT(rating) " +
//                                          "ELSE 0 END) AS cnt_rating " +

  // Второй вариант для mov_rat://
  //  val mov_rat = ij_dat_it.select("item_id", "movie_title", "rating")
  //                          .groupBy("item_id", "movie_title", "rating")
  //                          .agg(count("rating").as("count_rating"))
  //                          .orderBy("item_id", "rating").distinct()

  //  val ft = mov_rat.withColumn("_col4",
  //                              when(col("rating") === "1", count("rating"))
  //                              .when(col("rating") === "2", count("rating"))
  //                              .when(col("rating") === "3", count("rating"))
  //                              .when(col("rating") === "4", count("rating"))
  //                              .when(col("rating") === "5", count("rating"))
  //  )
  //
  //  ft.show(10,false)

  val cj_fid_rat = spark.sql(
    "SELECT d_iid.item_id AS film_id, i_mt.movie_title AS mt, dr.rating AS f_rating " +
            "FROM (SELECT DISTINCT item_id FROM ud) AS d_iid " +
              "CROSS JOIN (SELECT DISTINCT rating FROM ud) AS dr " +
              "INNER JOIN (SELECT movie_id, movie_title FROM ui) AS i_mt " +
              "ON d_iid.item_id = i_mt.movie_id " +
            "ORDER BY d_iid.item_id, dr.rating"
  )
  //cj_fid_rat.show(false)
  cj_fid_rat.createOrReplaceTempView("f_id_rat")

  val fin_table = spark.sql(
    "SELECT fir.mt AS movie_title, fir.f_rating AS film_rating, " +
            "IFNULL(mr.count_rating, 0) AS count_rating FROM " +
              "f_id_rat AS fir LEFT JOIN mr " +
              "ON fir.film_id = mr.m_itid AND fir.f_rating = mr.mov_r " +
            "ORDER BY fir.film_id, fir.f_rating")
  fin_table.show(false)

//  val s_uitem1 = s_uitem.groupBy("movie_id")
//    val s_uitem1 = s_uitem.where("movie_title = 'Toy Story (1995)'")
//    s_uitem1.show(false)

  val all_mov_rat = ij_dat_it.groupBy("rating").agg(count("rating")
      .as("hist_all")).orderBy("rating")

  all_mov_rat.show(false)

}

//mov_rat.coalesce(1).write.json("task_3/out.json")

//UNION//
  //val fin_table = mov_rat.union(all_mov_rat).show(false)

//JOIN//
  //val fin_table = mov_rat.join(all_mov_rat, mov_rat("rating") === all_mov_rat("rating"), "inner")
  //val fin_table1 = fin_table.select("mov_rat.rating", "movie_title","count_rating", "hist_all")
  //.orderBy("mov_rat.rating").show(false)

  //val ij_dat_it = s_udata1.join(s_uitem1, s_udata1("item_id") === s_uitem1("movie_id"), "inner")

//######################################################################

//######################################################################

//  val u_data = spark.sparkContext.textFile("C:/Users/KRyzhov/Desktop/Tasks_DE/ml-100k/u.data")
//  u_data.take(10).foreach(println)
//
//  val s_udata = u_data.map(s => s.split("\t"))
//  val fm_sudata = s_udata.flatMap(arr => {
//     val user_id = arr(0)
//     val item_id = arr(1)
//     val rating = arr(2)
//     val timestamp = arr(3)
//     val words = user_id.split(" ")
//     words.map(user_id => (user_id, item_id, rating, timestamp))
//    })
//  fm_sudata.take(10).foreach({ case (user_id, item_id, rating, timestamp) =>
//    println(s"{$user_id, $item_id, $rating, $timestamp}")})

//######################################################################
