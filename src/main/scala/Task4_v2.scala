import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions._

object Task4_v2 extends App {

  val spark = SparkSession.builder()
    .appName("Spark_Session_4v2")
    .master("local[1]")
    .getOrCreate()

  import Task4_v2.spark.implicits._


  val cstmr = spark.read.csv("C:/Users/KRyzhov/Desktop/Tasks_DE/customer.csv")

  val s_cstmr = cstmr.withColumn("tmp", split($"_c0", "\t")).select(
    $"tmp".getItem(0).as("id"),
    $"tmp".getItem(1).as("name"),
    $"tmp".getItem(2).as("email"),
    $"tmp".getItem(3).as("join_date"),
    $"tmp".getItem(4).as("status")
  )
  //s_cstmr.show(false)

  val prdkt = spark.read.csv("C:/Users/KRyzhov/Desktop/Tasks_DE/product.csv")

  val s_prdkt = prdkt.withColumn("tmp", split($"_c0", "\t")).select(
    $"tmp".getItem(0).as("id"),
    $"tmp".getItem(1).as("name"),
    $"tmp".getItem(2).as("price"),
    $"tmp".getItem(3).as("number_of_products")
  )
  //s_prdkt.show(false)


  val order = spark.read.csv("C:/Users/KRyzhov/Desktop/Tasks_DE/order.csv")

  val s_order = order.withColumn("tmp", split($"_c0", "\t")).select(
    $"tmp".getItem(0).as("customer_id"),
    $"tmp".getItem(1).as("order_id"),
    $"tmp".getItem(2).as("product_id"),
    $"tmp".getItem(3).as("number_of_product"),
    $"tmp".getItem(4).as("order_date"),
    $"tmp".getItem(5).as("status")
  )
  //s_order.show(false)


  val ij_all_tab = s_cstmr.join(s_order, s_cstmr("id") === s_order("customer_id"), "inner")
                          .join(s_prdkt, s_order("product_id") === s_prdkt("id"), "inner")
                          .groupBy(s_cstmr("id").as("cust_id"),
                                   s_cstmr("name").as("cust_name"),
                                   s_prdkt("id"),
                                   s_prdkt("name").as("prod_name"))
                          .agg(sum(s_order("number_of_product")).as("sum_num_prod"))
                          .orderBy(s_cstmr("id"), s_prdkt("id"))
  // Если select надо группировать, то надо сразу писать group by без select
  //                        .select(s_cstmr("id").as("cust_id"),
  //                                s_cstmr("name").as("cust_name"),
  //                                s_prdkt("id"),
  //                                s_prdkt("name").as("prod_name"))

  //ij_all_tab.show(false)

  val l = (1 to 1000).toList
  val df = l.toDF("number")

  val l1 = (1 to 1000).toList
  val df1 = l.toDF("number1")

  val a = df.crossJoin(df1).repartition(20)

  //val a = df.repartition(10).coalesce(5)

  //a.show(false)
  //a.write.csv("test")


  val win = Window.partitionBy("cust_id").orderBy("cust_id") // Также это можно написать в самом over

  val win_tab = ij_all_tab.groupBy("cust_id", "cust_name", "prod_name", "sum_num_prod")
                          .agg(max("sum_num_prod").over(win).as("max_prod"))
                          .drop("cust_id")

  //win_tab.show(false)


  val fin_tab = win_tab.select("cust_name", "prod_name", "max_prod")
                       .where("sum_num_prod = max_prod")
                       .orderBy(desc("max_prod"))

  //fin_tab.show(false)

  //fin_tab.coalesce(1).write.csv("task4_v2/fin_result")

}
