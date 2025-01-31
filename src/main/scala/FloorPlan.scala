import scala.collection.mutable.Buffer

/** The floor plan stores information about all of the furniture and fixtures it contains. */
class FloorPlan:
  
  var scale: Double = 1

  val furniture: Buffer[Furniture] = Buffer[Furniture]()

  val fixtures: Buffer[Fixture] = Buffer[Fixture]()
  
  def addFurniture(newFurniture: Furniture): Unit =
    furniture += newFurniture
  
  def deleteFurniture(deleted: Furniture): Unit =
    furniture -= deleted

  def addFixture(newFixture: Fixture): Unit =
    fixtures += newFixture

  def deleteFixture(deleted: Fixture): Unit =
    fixtures -= deleted
  
  def deleteAll(): Unit =
    furniture.clear()
    fixtures.clear()

end FloorPlan
