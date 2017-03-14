package connections

import slick.jdbc.PostgresProfile

trait PostgresConnector extends DBProvider {

  val driver = PostgresProfile

  import driver.api._

  val db = Database.forConfig("myPostgresDB")

}
