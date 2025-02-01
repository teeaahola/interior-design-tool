import scalafx.scene.control.{ButtonType, Tooltip}
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color.{Black, White}
import scalafx.scene.shape.{Arc, ArcType, Circle, Ellipse, Rectangle, Shape}
import scalafx.Includes.jfxColor2sfx
import scalafx.scene.layout.Pane

/** A class for creating shapes in the sidebar of the main view. */
class ShapeMaker:

  /** Create a buttontype for deleting furniture. */
  private val delete = new ButtonType("Delete")


  /** Helper function to create the proper icons for different furniture to the sidebar. */
  private def furnitureIconShapeMatch(shape: Shape, color: Color, isLampOrCarpet: Boolean): Shape =
    shape match
      case circle: Circle =>
        new Circle:
          radius = if isLampOrCarpet then 10 else 30
          fill = color
      case rectangle: Rectangle =>
        new Rectangle:
          width = 60
          height = 45
          fill = color
      case ellipse: Ellipse =>
        new Ellipse:
          radiusX = 30
          radiusY = 25
          fill = color
      case arc: Arc =>
        val a = Arc(0, 0, 30, 40, 0, - 180)
        a.fill = color
        a.`type` = ArcType.Round
        a
      case _ => throw NoSuchShapeException("Cannot create icon from unknown shape.")


  /** Helper function for deciding which shape should a new Furniture be intialized with. */
  private def furnitureShapeMatch(shape: Shape, isLampOrCarpet: Boolean): Shape =
    shape match
      case circle: Circle => Circle(if isLampOrCarpet then 10 else 30)
      case rectangle: Rectangle => Rectangle(50, 60)
      case ellipse: Ellipse => Ellipse(30, 25)
      case arc: Arc =>
        val a = Arc(0, 0, 40, 50, 0, - 180)
        a.`type` = ArcType.Round
        a
      case _ => throw NoSuchShapeException("Cannot initialize furniture from unknown shape.")


  /** Create a resultconverter for the furniture's dialog. This is the function that does
   * the bulk of the work when the dialog is closed by clicking any of the buttons. */
  private def furnitureConverter(dialog: ElementDialog, pane: Pane, plan: FloorPlan, furniture: Furniture, shape: Shape, tooltip: Tooltip): Unit =
    dialog.resultConverter = dialogButton =>
      if dialogButton == delete then
        /** Remove the furniture from the GUI and the internal state of the floor plan. */
        pane.children.remove(shape)
        plan.deleteFurniture(furniture)
        null
      else if dialogButton == ButtonType.Apply then
        /** Update the variables storing data about the previous version of the furniture. */
        furniture.previousW = furniture.w
        furniture.previousH = furniture.h
        dialog.prevColor = dialog.color.getValue
        dialog.prevMaterial = dialog.materials.getValue
        shape.fill = dialog.color.getValue
        /** Rename */
        if dialog.name.text().nonEmpty then
          furniture.changeName(dialog.name.text())
          dialog.prevName = furniture.name
          Tooltip.uninstall(shape, tooltip)
          Tooltip.install(shape, new Tooltip(furniture.name))
        /** Resize the furniture based on user input and scale it to the floor plan's scale. */
        shape match
          case circle: Circle =>
            circle.radius = (furniture.w / 2) * plan.scale
          case rectangle: Rectangle =>
            rectangle.width = furniture.w * plan.scale
            rectangle.height = furniture.h * plan.scale
          case ellipse: Ellipse =>
            ellipse.radiusX = (furniture.w / 2) * plan.scale
            ellipse.radiusY = (furniture.h / 2) * plan.scale
          case arc: Arc =>
            arc.radiusX = (furniture.w / 2) * plan.scale
            arc.radiusY = furniture.h * plan.scale
          case _ => throw NoSuchShapeException("No such shape exists to add to the plan.")
        /** Rotate */
        shape.getTransforms.clear()
        shape.getTransforms.addAll(dialog.illustration.getTransforms)
        furniture
      else
        /** Cancel all edits, i.e. return the dialog to the state before the edits. */
        dialog.name.text = dialog.prevName
        dialog.fwidth.text = furniture.previousW.toString
        dialog.fheight.text = furniture.previousH.toString
        dialog.illustration.setFill(dialog.prevColor)
        dialog.materials.setValue(dialog.prevMaterial)
        dialog.color.setValue(dialog.prevColor)
        dialog.illustration.getTransforms.clear()
        dialog.illustration.getTransforms.addAll(shape.getTransforms)
        null


  /** The function responsible for actually creating the shape in the sidebar and initializing a furniture and a dialog
   * for it when clicked. Also makes sure that the new shape in the floor plan can be moved. */
  def furnitureShapeMaker(shape: Shape, color: Color, plan: FloorPlan, pane: Pane, isLampOrCarpet: Boolean) =
    val sideBarShape = furnitureIconShapeMatch(shape, color, isLampOrCarpet)

    if isLampOrCarpet then
      shape match
        case rectangle: Rectangle => Tooltip.install(sideBarShape, new Tooltip("Carpet"))
        case circle: Circle => Tooltip.install(sideBarShape, new Tooltip("Lamp"))
        case _ => NoSuchShapeException("Inappropriate shape passed as lamp or carpet.")
    else
      Tooltip.install(sideBarShape, new Tooltip("Regular furniture"))

    sideBarShape.setOnMouseClicked( (eventHandled) =>
      val furniture =
        val s = furnitureShapeMatch(shape, isLampOrCarpet)
        if isLampOrCarpet then
          new Furniture(s):
            name =
              this.shape match
                case circle: Circle => "Lamp"
                case rectangle: Rectangle => "Carpet"
                case _ => throw NoSuchShapeException("Only circles and rectangles can be lamps or carpets.")
            lampOrCarpet = true
        else
          new Furniture(s)

      /** Initialize shape to be the furniture's shape and install a tooltip. */
      val movedShape = furniture.shape
      val t = new Tooltip(furniture.name)
      Tooltip.install(movedShape, t)

      val dialog = new ElementDialog(furniture, color)
      /** Convert the result obtained from the dialog. */
      furnitureConverter(dialog, pane, plan, furniture, movedShape, t)
      val result = dialog.showAndWait()

      result match
        case Some(x) =>
          dialog.getDialogPane.getButtonTypes.add(delete)
          /** Make sure the furniture can be edited after it has been added to the plan by double clicking it. */
          movedShape.setOnMouseClicked( (event) =>
            if event.getClickCount == 2 then
              furnitureConverter(dialog, pane, plan, furniture, movedShape, t)
              dialog.show())

          val draggable = new Draggable()
          draggable.makeDraggable(furniture, plan)

          plan.addFurniture(furniture)
          pane.children.add(movedShape)
        case None => )

    sideBarShape



  /** Helper function to create the proper icons for different fixtures to the sidebar. */
  private def fixtureIconShapeMatch(shape: Shape, isAppliance: Boolean): Shape =
    shape match
      case rectangle: Rectangle =>
        if isAppliance then
          new Rectangle:
            width = 50
            height = 50
            stroke = Black
            strokeWidth = 3
            fill = White
        else
          new Rectangle:
            width = 10
            height = 60
      case arc: Arc =>
        val a = Arc(0, 0, 50, 50, 0, - 90)
          a.stroke = Black
          a.strokeWidth = 3
          a.fill = White
          a.`type` = ArcType.Round
          a
      case _ => throw NoSuchShapeException("Cannot create icon from unknown shape.")


  /** Helper function for deciding which kind of Fixture will be created. */
  private def fixtureMatch(shape: Shape, isAppliance: Boolean): Fixture =
    shape match
      case rectangle: Rectangle =>
        if isAppliance then
          new Appliance()
        else
          new WallOrWindow()
      case arc: Arc =>
        val a = Arc(0, 0, 40, 40, 0, - 90)
        a.stroke = Black
        a.strokeWidth = 3
        a.fill = White
        a.`type` = ArcType.Round
        new Door(a)
      case _ => throw NoSuchShapeException("Cannot initialize a fixture from unknown shape.")


  /** Create a resultconverter for the fixture's dialog. */
  private def fixtureConverter(dialog: ElementDialog, pane: Pane, plan: FloorPlan, fixture: Fixture, shape: Shape, tooltip: Option[Tooltip]): Unit =
    dialog.resultConverter = dialogButton =>
      if dialogButton == delete then
        /** Remove the fixture from the GUI and the internal state of the floor plan. */
        pane.children.remove(shape)
        plan.deleteFixture(fixture)
        null
      else if dialogButton == ButtonType.Apply then
        fixture.previousW = fixture.w
        fixture.previousH = fixture.h
        /** Rename */
        tooltip match
          case Some(tip) =>
            if dialog.name.text().nonEmpty then
              fixture match
                case appliance: Appliance =>
                  appliance.changeName(dialog.name.text())
                  dialog.prevName = appliance.name
                  Tooltip.uninstall(shape, tip)
                  Tooltip.install(shape, new Tooltip(appliance.name))
                case _ =>
          case None =>
        /** Resize the fixture based on user input and scale it to the floor plan's scale. */
        shape match
          case rectangle: Rectangle =>
            rectangle.width = fixture.w * plan.scale
            rectangle.height = fixture.h * plan.scale
          case arc: Arc =>
            arc.radiusX = fixture.w * plan.scale
            arc.radiusY = fixture.h * plan.scale
          case _ => throw NoSuchShapeException("No such shape exists to add to the plan.")
        shape.getTransforms.clear()
        shape.getTransforms.addAll(dialog.illustration.getTransforms)
        fixture
      else
        /** Cancel all edits. */
        dialog.name.text = dialog.prevName
        dialog.fwidth.text = fixture.previousW.toString
        dialog.fheight.text = fixture.previousH.toString
        dialog.illustration.getTransforms.clear()
        dialog.illustration.getTransforms.addAll(shape.getTransforms)
        null


  /** The function responsible for actually creating the shape in the sidebar and initializing a fixture and a dialog
   * for it when clicked. Also makes sure that the new shape in the floor plan can be moved. */
  def fixtureShapeMaker(shape: Shape, plan: FloorPlan, pane: Pane, isAppliance: Boolean): Shape =
    val sideBarShape = fixtureIconShapeMatch(shape, isAppliance)
    shape match
      case rectangle: Rectangle =>
        if isAppliance then
          Tooltip.install(sideBarShape, new Tooltip("Appliance"))
        else
          Tooltip.install(sideBarShape, new Tooltip("Wall or window"))
      case arc: Arc => Tooltip.install(sideBarShape, new Tooltip("Door"))
      case _ => throw NoSuchShapeException("Installing tooltip on unknown shape.")

    sideBarShape.setOnMouseClicked( (eventHandled) =>
      val fixture = fixtureMatch(shape, isAppliance)
      val movedShape = fixture.shape
      /** If the fixture is an appliance, create a tooltip for it. */
      val t =
        fixture match
          case appliance: Appliance => Option(new Tooltip(appliance.name))
          case _ => None
      t match
        case Some(tooltip) => Tooltip.install(movedShape, tooltip)
        case None =>

      val dialog = new ElementDialog(fixture, Black)

      fixtureConverter(dialog, pane, plan, fixture, movedShape, t)
      val result = dialog.showAndWait()

      result match
        case Some(x) =>
          dialog.getDialogPane.getButtonTypes.add(delete)
          /** Make sure the fixture can be edited after it has been added to the plan by double clicking it. */
          movedShape.setOnMouseClicked( (event) =>
            if event.getClickCount == 2 then
              fixtureConverter(dialog, pane, plan, fixture, movedShape, t)
              dialog.show())

          val draggable = new Draggable()
          draggable.makeDraggable(fixture, plan)

          plan.addFixture(fixture)
          pane.children.add(movedShape)
        case None => )

    sideBarShape


end ShapeMaker

