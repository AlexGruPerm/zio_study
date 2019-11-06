import zio.console.Console
import zio.{IO, Task, ZIO}

val st :Seq[Task[Int]] = Seq(Task(1),Task(2),Task(3))

val t : Task[List[Int]]= IO.collectAll(st)

val r : ZIO[Console, Throwable, List[Int]] = t

r.fold(
  f => {
    println(s"fail f=$f");
    0
  },
  s => {
    println(s"success res = ${s.sum}");
    1
  }
)