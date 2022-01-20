import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions._
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.types.StringType
import org.apache.spark.sql.types.StructField

object Task3_v2 extends App {

  val spark = SparkSession.builder()
    .appName("Spark_Session_3v2")
    .master("local[1]")
    .getOrCreate()

  val schema_df1 = StructType(Array(
    StructField("user_id", StringType),
    StructField("item_id", StringType),
    StructField("rating", StringType),
    StructField("timestamp", StringType)))

  val u_data = spark.read.format("csv")
    .option("sep", "\t")
    .schema(schema_df1)
    .option("header", "false")
    .load("C:/Users/KRyzhov/Desktop/Tasks_DE/ml-100k/u.data")
  //u_data.show(false)

  val schema_df2 = StructType(Array(
    StructField("movie_id", StringType),
    StructField("movie_title", StringType),
    StructField("release_date", StringType),
    StructField("video_release_date", StringType),
    StructField("IMDb_URL", StringType),
    StructField("unknown", StringType),
    StructField("Action", StringType),
    StructField("Adventure", StringType),
    StructField("Animation", StringType),
    StructField("Children's", StringType),
    StructField("Comedy", StringType),
    StructField("Crime", StringType),
    StructField("Documentary", StringType),
    StructField("Drama", StringType),
    StructField("Fantasy", StringType),
    StructField("Film-Noir", StringType),
    StructField("Horror", StringType),
    StructField("Musical", StringType),
    StructField("Mystery", StringType),
    StructField("Romance", StringType),
    StructField("Sci-Fi", StringType),
    StructField("Thriller", StringType),
    StructField("War", StringType),
    StructField("Western", StringType)))

  val u_item = spark.read.format("csv")
    .option("sep", "|")
    .schema(schema_df2)
    .option("header", "false")
    .load("C:/Users/KRyzhov/Desktop/Tasks_DE/ml-100k/u.item")
  //u_item.show(false)

  val ij_dat_it = u_data.join(u_item, u_data("item_id") === u_item("movie_id"), "inner")
    .orderBy("item_id", "rating")
  //.drop(u_data("item_id"))
  //ij_dat_it.show(false)

  val mov_rat = ij_dat_it
    .select(col("item_id").as("m_itid"),
      col("movie_title").as("mt"),
      col("rating").as("mov_r"))
    .groupBy("m_itid", "mt", "mov_r")
    .agg(count("mov_r").as("count_rating"))
    .orderBy("m_itid", "mov_r")

  //mov_rat.show(false)

  val d_iid = u_data.select("item_id").distinct()
  val dr = u_data.select("rating").distinct()
  val i_mt = u_item.select("movie_id", "movie_title")

  val cj_ij_id_r = d_iid.crossJoin(dr).join(i_mt, d_iid("item_id") === i_mt("movie_id"))
    .select(d_iid("item_id").as("film_id"),
      i_mt("movie_title").as("mt"),
      dr("rating").as("f_rating"))
    .orderBy(d_iid("item_id"), dr("rating"))

  //cj_ij_id_r.show(false)


  val fin_table = cj_ij_id_r.join(mov_rat, cj_ij_id_r("film_id") === mov_rat("m_itid")
    && cj_ij_id_r("f_rating") === mov_rat("mov_r"), "left")
    .select(cj_ij_id_r("film_id").as("film_id"),
      cj_ij_id_r("mt").as("movie_title"),
      cj_ij_id_r("f_rating").as("film_rating"),
      mov_rat("count_rating")).na.fill(0)
    .orderBy(cj_ij_id_r("film_id"), cj_ij_id_r("f_rating"))

  //fin_table.show(false)


  val all_mov_rat = ij_dat_it.groupBy("rating").agg(count("rating")
    .as("hist_all")).orderBy("rating")

  //all_mov_rat.show(false)

  val ft = fin_table.groupBy("film_id", "movie_title")
    .agg(collect_list("count_rating").as("rating"))
    .orderBy("film_id")
    .drop("film_id")

  //ft.show(false)


//  val a = ft.withColumn("films_rating",
//             concat(col("movie_title"), lit(":"), col("rating"))).toJSON
//            .show(false)


  val am = all_mov_rat.agg(collect_list("hist_all").as("hist_all"))

  //am.show(false)

//    ft.coalesce(1).write.json("task3_v2/fin_table")
//    am.coalesce(1).write.json("task3_v2/all_movies")

}

//###########################################################################

//  fin_table.coalesce(1).write.option("header", "false").csv("task3_v2/fin_table.csv")
//  all_mov_rat.coalesce(1).write.option("header", "false").csv("task3_v2/all_movies.csv")
//
//
//  val r_fin_tab = spark.sparkContext.textFile("task3_v2/fin_table.csv")
//      r_fin_tab.take(10).foreach(println)
//
//    val s_fin_tab = r_fin_tab.map(s => s.split(","))
//    val fm_sfintab = s_fin_tab.flatMap(arr => {
//      val movie_title = (arr(0) + ":")
//      val film_rating = ("[" + arr(1) + ":")
//      val count_ratng = (arr(2) + "]")
//      val words = movie_title.split(",")
//      words.map(movie_title => (movie_title, film_rating, count_ratng))
//    })
//
//    fm_sfintab.take(10).foreach({ case (movie_title, film_rating, count_rating) =>
//    println(s"$movie_title $film_rating $count_rating")})
//
//
//  val r_all_mov = spark.sparkContext.textFile("task3_v2/all_movies.csv")
//      r_all_mov.take(10).foreach(println)
//
//  val s_all_mov = r_all_mov.map(s => s.split(","))
//  val fm_sall_mov = s_all_mov.flatMap(arr => {
//    val rating = (arr(0) + ":")
//    val hist_all = ("[" + arr(1) + "]")
//    val words = rating.split(",")
//    words.map(rating => (rating, hist_all))
//  })
//
//  fm_sall_mov.take(10).foreach({ case (rating, hist_all) =>
//                      println(s"$rating $hist_all")})
//
//
//  val ft = spark.createDataFrame(fm_sfintab)
//                .toDF("movie_title", "film_rating", "count_rating")
//  ft.show(false)
//
//  val am = spark.createDataFrame(fm_sall_mov)
//                .toDF("rating", "hist_all")
//  am.show(false)
//
//
//  ft.coalesce(1).write.option("index", "false")
//    .option("header", "false").json("task3_v2/fin_table.json")
//  am.coalesce(1).write.option("index", "false")
//    .option("header", "false").json("task3_v2/all_movies.json")


  //val a: Unit = mov_rat.select("m_itid").take(10).foreach(println)
  //val b = fin_table.select("*").take(10).foreach(println)
  //val a = fin_table.select("*").collect().map(_.mkString(",")).take(10).foreach(println)

//  val a = mov_rat.collect().take(10).foreach(println)
//   .take(10).foreach({ case (m_itid, mt, mov_r, count_rating) =>
//   println(s"{user: $m_itid, item: $mt, rating: $mov_r, timestamp: $count_rating}")})

  //val a = mov_rat.collect().take(10).foreach(println)
  //mov_rat.rdd.map(r => r(3)).collect().toList.take(10).foreach(println)

//###########################################################################


//###########################################################################

//  val t0 = df3.filter((df2("movie_id") === 32) && (df1("rating") === 2)).first()
//  println()
//  val t1 = df3.filter("rating = 1").filter("movie_id = 32").count()
//  val t2 = df3.filter("rating = 2").filter("movie_id = 32").count()
//  val t3 = df3.filter("rating = 3").filter("movie_id = 32").count()
//  val t4 = df3.filter("rating = 4").filter("movie_id = 32").count()
//  val t5 = df3.filter("rating = 5").filter("movie_id = 32").count()
//
//  val h1 = df3.filter("rating=1").count()
//  val h2 = df3.filter("rating=2").count()
//  val h3 = df3.filter("rating=3").count()
//  val h4 = df3.filter("rating=4").count()
//  val h5 = df3.filter("rating=5").count()
//
//  val data = Array(
//    (t1,h1),
//    (t2,h2),
//    (t3,h3),
//    (t4,h4),
//    (t5,h5)
//  )
//
//  val fin_schema = StructType(Array(
//    StructField(s"$t0", StringType, true),
//    StructField("hist_all", StringType, true)))
//
//  val rdd = spark.sparkContext.parallelize(data)
//
//  val fin_tab = spark.createDataFrame(spark.sparkContext.parallelize(data), fin_schema)
//  fin_tab.show(false)

//###########################################################################

