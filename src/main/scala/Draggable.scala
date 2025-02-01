import javafx.scene.input.MouseEvent
import scalafx.scene.paint.Color.Red
import scalafx.scene.shape.Shape

/** This class is used to make the elements of the floorplan draggable, i.e. they can be dragged
 * with the mouse to the desired position on the floorplan. Each element requires its own Draggable. */

class Draggable:

  private var yAnchor: Double = 0
  private var xAnchor: Double = 0
  private var yOffset: Double = 0
  private var xOffset: Double = 0
  private var previousY: Double = 0
  private var previousX: Double = 0

  /** This function is called in makeDraggable to check whether the new position of the element
   * intersects with any other element. */
  private def intersectionCheck(element: Element, plan: FloorPlan): Boolean =
    var posNotAllowed = false
    
    val furnitures =
      element match
        case furniture: Furniture => plan.furniture.filterNot(_ == furniture)
        case _ => plan.furniture
    
    var fixtures =
      element match
        case wall: WallOrWindow =>
          /** The program allows walls to overlap for ease of planning, and so walls are not
           * checked for intersection with each other. */
          for each <- plan.fixtures yield
            each match
              case w: WallOrWindow => null
              case _ => each
        case fixture: Fixture => plan.fixtures.filterNot(_ == fixture)
        case _ => plan.fixtures

    /** Check if the element overlaps furniture. */
    for each <- furnitures do
      if element.shape != each.shape then
        val intersection = Shape.intersect(element.shape, each.shape)
        if intersection.getBoundsInLocal.getWidth != -1 then
          /** If one of the overlapping elements is a lamp or a carpet, they can be overlaid if
           * both elements are furniture. */
          element match
            case fixture: Fixture =>
              posNotAllowed = true
            case furniture: Furniture =>
              if !(element.lampOrCarpet || each.lampOrCarpet) then
                posNotAllowed = true
            case _ => throw NoSuchElementsException("No such element exists.")

    /** Check if the element overlaps fixtures. */
    fixtures = fixtures.filterNot(_ == null)
    for each <- fixtures do
      if element.shape != each.shape then
        val intersection = Shape.intersect(element.shape, each.shape)
        if intersection.getBoundsInLocal.getWidth != -1 then
          posNotAllowed = true
    
    posNotAllowed

  /** This is the actual function responsible for moving the elements */
  def makeDraggable(element: Element, plan: FloorPlan): Unit =
    val shape = element.shape

    /** When the mouse is first pressed, the program records the element's current position and
     * the mouse's position on the element. */
    shape.setOnMousePressed((event) =>
      yAnchor = event.getSceneY
      xAnchor = event.getSceneX
      previousY = shape.getLayoutY
      previousX = shape.getLayoutX
      yOffset = element.y - yAnchor
      xOffset = element.x - xAnchor)

    /** Set the shape to move with the mouse. */
    shape.setOnMouseDragged((event) =>
      shape.setTranslateY(event.getSceneY - yAnchor)
      shape.setTranslateX(event.getSceneX - xAnchor))

    /** When the mouse is released, either return the shape to its previous position or set it
     * where the mouse was released and update the internal state. */
    shape.setOnMouseReleased((event) =>
      if intersectionCheck(element, plan) then
        shape.setLayoutY(previousY)
        shape.setLayoutX(previousX)
        element match
          case furniture: Furniture =>
            shape.setFill(Red)
          case _ =>
      else
        shape.setLayoutY(event.getSceneY + yOffset)
        shape.setLayoutX(event.getSceneX + xOffset)
        element.y = event.getSceneY + yOffset
        element.x = event.getSceneX + xOffset

      shape.setTranslateY(0)
      shape.setTranslateX(0))

end Draggable