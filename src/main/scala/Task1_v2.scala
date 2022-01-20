object Task1_v2 extends App {

  def recurs_task(start: Int, end: Int, num_in_s: Int): Unit  =
  {
    for(i <- 0 until num_in_s)
      {
        if(start + i <= end)
          {
            print(s"${start + i} ")
          }
        else
          {
            return
          }
      }

    println()

    if(start + num_in_s <= end)
      {
        recurs_task(start + num_in_s,end, num_in_s)
      }
    else
      {
        return
      }
  }

  recurs_task(1, 15, 2)
}

