package components
import connections.{DBProvider, MySqlConnector, PostgresConnector}
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import models._

trait DependentTable extends EmployeeTable  with  MySqlConnector {

  this: DBProvider =>

  import driver.api._

  class DependentTable(tag: Tag) extends Table[Dependent](tag, "employee_dependents"){
    val empId = column[Int]("empId")
    val name = column[String]("name")
    val relation = column[String]("relation")
    val age = column[Option[Int]]("age")
    def dependentEmployeeFK = foreignKey("dependent_employee_fk",empId,employeeTableQuery)(_.id)
    def * =(empId,name,relation,age) <> (Dependent.tupled, Dependent.unapply)
  }

  val dependentTableQuery = TableQuery[DependentTable]


}

object DependentComponent extends DependentTable{

  this: DBProvider =>


  import driver.api._

  def create = db.run(dependentTableQuery.schema.create)

  def insert(dep: Dependent) = db.run {
    dependentTableQuery += dep
  }

  def delete(relation: String ) {
    val query = dependentTableQuery.filter((x => x.relation === relation)).delete
    db.run(query)
  }


  def update(id: Int,relation: String) = {
    val query = dependentTableQuery.filter(x => x.empId === id).map(_.relation).update(relation)
    db.run(query)
  }

  def getAll : Future[List[Dependent]] = db run {
    dependentTableQuery.to[List].result
  }

//  def upsert(dept : Dependent) {
//    val res: List[Dependent] = Await.result(DependentComponent.getAll,Duration.Inf)
//    val flag = res.map(x => if(x.name == dept.name) true else false)
//    if(flag.contains(true)){
//      val query = dependentTableQuery.filter(_.name === dep.name).map(x => (x.relation,x.age)).update((dep.relation,dep.age))
//      db.run(query)
//    }
//    else{
//      db run{
//        dependentTableQuery += dep
//      }
//    }
//
//  }
  def sortByDependentName() =  {
    dependentTableQuery.sortBy(_.name)
  }



}