object Task1_v3 extends App {

  def recurs(start: Int, i: Int, end: Int, num_in_s: Int): Unit =
    {
      if (i <= end)
        {
          print(i + " ")

          if((i - start + 1) % num_in_s == 0)
            {
              println()
            }

          recurs(start, i + 1, end, num_in_s)
        }
      else
        {
          return
        }
    }


  recurs(4, 4,16,3)
}
