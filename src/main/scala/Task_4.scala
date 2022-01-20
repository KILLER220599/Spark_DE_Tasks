import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions._

object Task_4 extends App {
  val spark = SparkSession.builder()
    .appName("Spark_Session_4")
    .master("local[1]")
    .getOrCreate()

//######################################################################

  import Task_4.spark.implicits._

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

  s_cstmr.createOrReplaceTempView("c")
  s_prdkt.createOrReplaceTempView("p")
  s_order.createOrReplaceTempView("o")

  println()

  val ij_all_tab = spark.sql(
    "SELECT c.id AS cust_id, c.name AS cust_name, p.id, p.name AS prod_name, " +
            "SUM(o.number_of_product) AS sum_num_prod " +
            "FROM c INNER JOIN o " +
              "ON c.id = o.customer_id " +
              "INNER JOIN p " +
              "ON o.product_id = p.id " +
            "GROUP BY c.id, c.name, p.id, p.name " +
            "ORDER BY c.id, p.id")
  //ij_all_tab.show(false)
  ij_all_tab.createOrReplaceTempView("ij_at")

  val fin_tab = spark.sql(
    "SELECT cust_name, prod_name, max_prod FROM " +
            "(SELECT cust_name, prod_name, sum_num_prod, MAX(sum_num_prod) OVER " +
            "(PARTITION BY cust_id ORDER BY cust_id) AS max_prod " +
            "FROM ij_at) AS nt " +
            "WHERE sum_num_prod = max_prod " +
            "ORDER BY max_prod DESC")
 //fin_tab.show(false)

  //fin_tab.write.csv("C:/Users/KRyzhov/Desktop/Tasks_DE/Task_4.csv")

}
