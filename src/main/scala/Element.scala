import scalafx.scene.paint.Color
import scalafx.scene.paint.Color.*
import scalafx.scene.shape.*

class Element(val shape: Shape):

  /** Each elementkeeps track of its x and y positions on the pane. */
  var x: Double = 0
  var y: Double = 0

  /** Each element also knows its current and previous dimensions. */
  var w: Double = 60
  var h: Double = 50
  var previousW: Double = 60
  var previousH: Double = 50

  /** This is true for lamps and carpets so that they can go under and over other furniture. */
  var lampOrCarpet: Boolean = false

end Element


/** Furniture and Fixture need their own subclasses so that they can be processed differently. */

class Furniture(shape: Shape) extends Element(shape):

  var name: String = "Furniture"

  var color: Color = White

  def changeName(newName: String): Unit =
    name = newName

  def changeColor(newColor: Color): Unit =
    color = newColor
    shape.fill = newColor

end Furniture


class Fixture(shape: Shape) extends Element(shape)


/** Fixture is further broken into separate classes as they have different qualities in Draggable,
 * ShapeMaker and ElementDialog. */
class WallOrWindow extends Fixture(Rectangle(10, 60))


class Appliance extends Fixture(Rectangle(30, 30)):

  this.shape.fill = White
  this.shape.stroke = Black
  this.shape.strokeWidth = 3
  var name = "Appliance"

  def changeName(newName: String): Unit =
    name = newName

end Appliance


class Door(arc: Arc) extends Fixture(arc)