import components.EmployeeComponent
import models.Employee

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

object UsingSlick extends App {

  EmployeeComponent.create
  val insertRes = EmployeeComponent.insert(Employee(104, "Himanshu", 0))
  val res = insertRes.map(res1 => s"$res1 inserted")
  res.map(println(_))
Thread.sleep(10000)
}