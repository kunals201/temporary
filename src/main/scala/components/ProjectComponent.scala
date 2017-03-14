package components
import components.EmployeeTable
import connections.{DBProvider, MySqlConnector}
import models.Project

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

trait ProjectTable extends EmployeeTable with  MySqlConnector {
  this: DBProvider =>

  import driver.api._


  class ProjectTable(tag: Tag) extends Table[Project](tag, "project_table") {
    val empId = column[Int]("empId")
    val projName = column[String]("projName")
    val teamMembers=column[Int]("teamMembers")
    def employeeProjectFk = foreignKey(
      "employee_project_fk", empId, employeeTableQuery)(_.id)
    def * = (empId, projName,teamMembers) <>(Project.tupled, Project.unapply)
  }
  val projectTableQuery = TableQuery[ProjectTable]
}

trait ProjectComponent extends ProjectTable {

  this: DBProvider =>

  import driver.api._

  //val db = Database.forConfig("myPostgresDB")
  def create = db.run(projectTableQuery.schema.create)
   def insert(proj: Project) = db.run {
   projectTableQuery += proj
   }
  def delete(proName: String) = {
    val query = projectTableQuery.filter(x => x.projName === proName)
    val action = query.delete
    db.run(action)
  }
  def updateName(id:Int,name:String):Future[Int] = {
    val query = projectTableQuery.filter(_.empId === id).map(_.projName).update(name)
    db.run(query)
  }
  def getAll: Future[List[Project]] = { db.run { projectTableQuery.to[List].result}}

  def upsert(emp : Project) = {
    val data: List[Project] = Await.result(getAll,Duration.Inf)

    val flag: List[Boolean] = data.map(x => if (x.projName == emp.projName) true else false)

    if (flag.contains(true)) {


      val action = projectTableQuery.filter(_.projName === emp.projName).map(x => (x.teamMembers)).update((emp.teamMembers))
      db.run(action)
    }
    else {
      val action: Future[Int] = insert(emp)

    }
  }

    def sortByProjectName() =  {
    projectTableQuery.sortBy(_.projName)
  }
}
//object ProjectComponent extends ProjectComponent