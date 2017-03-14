package components

import connections.{DBProvider, MySqlConnector}
import models._
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration


trait EmployeeTable  extends  MySqlConnector {
  this: DBProvider =>

  import driver.api._


  class EmployeeTable(tag: Tag) extends Table[Employee](tag, "experienced_employee") {
    val id = column[Int]("id", O.PrimaryKey)
    val name = column[String]("name")
    val experience = column[Double]("experience")

    def * = (id, name, experience) <> (Employee.tupled, Employee.unapply)
  }
  val employeeTableQuery = TableQuery[EmployeeTable]

}

trait EmployeeComponent extends EmployeeTable {
  this: DBProvider =>

  import driver.api._

  //val db = Database.forConfig("myPostgresDB")
  def create = db.run(employeeTableQuery.schema.create)

  def insert(emp: Employee) = db.run {
    employeeTableQuery += emp
  }

  def delete(exp: Double) = {
    val query = employeeTableQuery.filter(x => x.experience === exp)
    val action = query.delete
    db.run(action)
  }

  def updateName(id: Int, name: String) = {
    val query = employeeTableQuery.filter(_.id === id).map(_.name).update(name)
    db.run(query)
  }

  def getAll: Future[List[Employee]] = {
    db.run {
      employeeTableQuery.to[List].result
    }
  }

  def upsert(emp: Employee) = {
    //  removing  EmployeeComponent.
    val data: List[Employee] = Await.result(getAll, Duration.Inf)

    val flag: List[Boolean] = data.map(x => if (x.id == emp.id) true else false)

    if (flag.contains(true)) {


      val action = employeeTableQuery.filter(_.id === emp.id).map(x => (x.name, x.experience)).update((emp.name, emp.experience))
      db.run(action)
    }
    else
    {
      val action: Future[Int] = insert(emp)
    }

  }

  def insertOrUpdate(employee:Employee) = {
    val query = employeeTableQuery.insertOrUpdate(employee)
    db.run(query)
  }

  def sortByExperience() = {
    employeeTableQuery.sortBy(_.experience)
  }
}


  object EmployeeComponent extends EmployeeComponent
