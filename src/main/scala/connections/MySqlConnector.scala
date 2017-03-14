package connections

import slick.jdbc.MySQLProfile

trait MySqlConnector extends DBProvider{
  val driver = MySQLProfile

  import driver.api._

  val db = Database.forConfig("mysqlDB")

}
