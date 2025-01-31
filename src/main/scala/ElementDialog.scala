import scalafx.geometry.Insets
import scalafx.scene.control.{Button, ButtonType, ChoiceBox, ColorPicker, Dialog, Label, TextField}
import scalafx.scene.layout.GridPane
import scalafx.scene.shape.{Arc, ArcType, Circle, Ellipse, Rectangle, Shape}
import scalafx.scene.paint.Color.{Black, Red, White}
import scalafx.scene.paint.Color
import scalafx.scene.transform.Rotate
import scala.language.postfixOps

class ElementDialog(element: Element, defaultColor: Color) extends Dialog[Element]:

  title = "Element"

  val name = new TextField():
    promptText =
      element match
        case furniture: Furniture => furniture.name
        case appliance: Appliance => appliance.name
        case _ => ""
  var prevName = ""

  val fheight = new TextField():
    promptText = "50"
  val fwidth = new TextField():
    promptText = "60"

  private var allowedW: Boolean = false
  private var allowedH: Boolean = false

  private val errorH = new Label(""):
    textFill = Red
  private val errorW = new Label(""):
    textFill = Red

  /** The illustration is initially created using the values stored in the class Element. */
  val illustration: Shape =
    element.shape match
      /** There are multiple types of rectangles required in the sidebars, so they need to be
       * created according to their element's type. */
      case r: Rectangle =>
        element match
          case furniture: Furniture =>
            new Rectangle:
              width = element.w
              height = element.h
          case wall: WallOrWindow =>
            new Rectangle:
              width = 5
              height = 50
          case appliance: Appliance =>
            new Rectangle:
              width = 50
              height = 50
              fill = White
              strokeWidth = 3
              stroke = Black
          case _ => throw NoSuchElementsException("Illustrating unexpected element as a rectangle.")

      case c: Circle =>
        new Circle:
          radius = element.w / 2

      case e: Ellipse =>
        new Ellipse:
          radiusX = element.w / 2
          radiusY = element.h / 2
      /** There are two types of arcs in the program so they are also distinguished by
       * element type. */
      case a: Arc =>
        element match
          case furniture: Furniture =>
            val a = Arc(0, 0, element.w / 2, element.h, 0, - 180)
            a.`type` = ArcType.Round
            a
          case fixture: Fixture =>
            val a = Arc(0, 0, 60, 60, 0, - 90)
            a.stroke = Black
            a.strokeWidth = 3
            a.fill = White
            a.`type` = ArcType.Round
            a
          case _ => throw NoSuchElementsException("Illustrating unexpected element as an arc.")

      case _ => throw NoSuchShapeException("Illustrating inappropriate shape.")


  /** Button for rotating the elements in the GUI. */
  private val rotateButton = new Button("Rotate"):
    onAction = eventHandled =>
      illustration match
        /** The rotation axis is determined by the shape and type of the element. The elements are rotated
         * clockwise in 45° increments. */
        case rec: Rectangle =>
          rec.getTransforms.add(new Rotate(45, rec.width.toDouble / 2, rec.height.toDouble / 2))

        case arc: Arc =>
          element match
            case furniture: Furniture =>
              arc.getTransforms.add(new Rotate(45, 0, arc.radiusY.toDouble / 2))
            case fixture: Fixture =>
              arc.getTransforms.add(new Rotate(45, arc.radiusX.toDouble / 2, arc.radiusY.toDouble / 2))
            case _ =>
              throw NoSuchElementsException("No such element exists.")

        case _ =>
          illustration.getTransforms.add(new Rotate(45, 0, 0))


  /** The pickers for color and material are created and initialized. If the element is
   * furniture, its color is set to be the default color. */
  val color = ColorPicker(defaultColor)
  element match
    case furniture: Furniture =>
      illustration.setFill(color.getValue)
    case _ =>
  color.setOnAction( e => illustration.setFill(color.getValue) )
  var prevColor = defaultColor

  private val allMaterials = Array("Wood", "Metal", "Plastic", "Rattan", "Glass", "Wool")
  val materials = new ChoiceBox[String]():
    prefWidth = 70
    value = "Wood"
  allMaterials.foreach(materials.getItems.add(_))
  var prevMaterial: String = "Wood"


  this.getDialogPane.getButtonTypes.add(ButtonType.Cancel)
  this.getDialogPane.getButtonTypes.add(ButtonType.Apply)
  private val apply = this.dialogPane().lookupButton(ButtonType.Apply)
  /** Apply is disabled until there exist proper input in the textfields. */
  apply.setDisable(true)

  /** Continually based on the inputs in the textfields, update the illustration. The
   * illustration can never shrink too small or grow too large, as it is fit to always have
   * at least one side to be 60 pixels wide. The ratio of the two sides is conserved. */
  private def resize(): Unit =
    illustration match
      case rectangle: Rectangle =>
        if element.w == element.h then
          rectangle.width = 60
          rectangle.height = 60
        else if element.w > element.h then
          rectangle.width = 60
          rectangle.height = 60 * (element.h / element.w)
        else
          rectangle.width = 60 * (element.w / element.h)
          rectangle.height = 60
      /** Resizing an arc requires a lot of match-case statements as one of the arcs is a
       * "pie slice" and one is a semicircle. */
      case arc: Arc =>
        if element.w == element.h then
          element match
            case furniture: Furniture => arc.radiusX = 30
            case fixture: Fixture => arc.radiusX = 60
            case _ => throw NoSuchElementsException("Cannot resize an unknown element.")
          arc.radiusY = 60
        else if element.w > element.h then
          element match
            case furniture: Furniture => arc.radiusX = 30
            case fixture: Fixture => arc.radiusX = 60
            case _ => throw NoSuchElementsException("Cannot resize an unknown element.")
          arc.radiusY = 60 * (element.h / element.w)
        else
          element match
            case furniture: Furniture => arc.radiusX = (60 * (element.w / element.h)) / 2
            case fixture: Fixture => arc.radiusX = 60 * (element.w / element.h)
            case _ => throw NoSuchElementsException("Cannot resize an unknown element.")
          arc.radiusY = 60

      case ellipse: Ellipse =>
        if element.w == element.h then
          ellipse.radiusX = 30
          ellipse.radiusY = 30
        else if element.w > element.h then
          ellipse.radiusX = 30
          ellipse.radiusY = (60 * (element.h / element.w)) / 2
        else
          ellipse.radiusX = (60 * (element.w / element.h)) / 2
          ellipse.radiusY = 30
      /** Circles are not resized as the preview exists to demonstrate ratios. */
      case circle: Circle =>
      case _ => throw NoSuchShapeException("Resizing inappropriate shape.")


  /** Input validation for the height and width/diameter textFields. Allowed input is a
   * positive number. When both textfields contain proper inputs, apply is not disabled. */
  private def validate(textField: TextField, isWidth: Boolean): Unit =
    textField.text.onChange( (_, _, newValue) =>
      apply.setDisable(
        newValue.trim.toDoubleOption match

          case Some(number) =>
            if number <= 0 then
              if isWidth then
                errorW.text = "The input must be positive"
                allowedW = false
                true
              else
                errorH.text = "The input must be positive"
                allowedH = false
                true

            else
              if isWidth then
                errorW.text = ""
                allowedW = true
                element.w = number
                resize()
                /** When the shape is a circle, the value of the other textfield is not checked. */
                element.shape match
                  case circle: Circle =>
                    !allowedW
                  case _ =>
                    !(allowedH && allowedW)
              else
                errorH.text = ""
                allowedH = true
                element.h = number
                resize()
                !(allowedH && allowedW)

          case None =>
            if isWidth then
              allowedW = false
              errorW.text = "Please input a number"
              true
            else
              allowedH = false
              errorH.text = "Please input a number"
              true))

  validate(fheight, false)
  validate(fwidth, true)


  /** Create a grid and set it to be the content of the dialog. */
  private val grid = new GridPane():
    hgap = 15
    vgap = 15
    padding = Insets(20, 30, 15, 15)
    var rowIndex = 0

    /** The nodes added to the grid depend on the type of dialog required, i.e. is the
     * element handled furniture or a fixture. */
    element match

      case furniture: Furniture =>
        add(new Label("Name:"), 0, 0)
        add(name, 1, 0)
        /** Select the displayed labels based on furniture shape. */
        element.shape match
          case circle: Circle =>
            add(new Label("Diameter:"), 0, 1)
            add(fwidth, 1, 1)
            add(new Label("cm"), 2, 1)
          case _ =>
            add(new Label("Height:"), 0, 1)
            add(fheight, 1, 1)
            add(new Label("cm"), 2, 1)
            add(new Label("Width:"), 0, 2)
            add(fwidth, 1, 2)
            add(new Label("cm"), 2, 2)
            rowIndex = 1
        add(new Label("Material:"), 0, 2 + rowIndex)
        add(materials, 1, 2 + rowIndex)
        add(new Label("Color:"), 0, 3 + rowIndex)
        add(color, 1, 3 + rowIndex)
        add(new Label("Preview:"), 0, 4 + rowIndex)
        add(illustration, 1, 4 + rowIndex, 1, 2)
        /** If the shape is not a circle, add a button to rotate the furniture. */
        element.shape match
          case circle: Circle =>
          case _ => add(rotateButton, 2, 4 + rowIndex)
        add(errorH, 0, 6 + rowIndex, 2, 1)
        add(errorW, 0, 7 + rowIndex, 2, 1)

      case fixture: Fixture =>
        fixture match
          case appliance: Appliance =>
            add(new Label("Name:"), 0, 0)
            add(name, 1, 0)
            rowIndex = 1
          case _ =>
        add(new Label("Length:"), 0, 0 + rowIndex)
        add(fheight, 1, 0 + rowIndex)
        add(new Label("cm"), 2, 0 + rowIndex)
        add(new Label("Width:"), 0, 1 + rowIndex)
        add(fwidth, 1, 1 + rowIndex)
        add(new Label("cm"), 2, 1 + rowIndex)
        add(new Label("Preview:"), 0, 2 + rowIndex)
        add(illustration, 1, 2 + rowIndex, 1, 2)
        add(rotateButton, 3, 2 + rowIndex)
        add(errorH, 0, 4 + rowIndex, 2, 1)
        add(errorW, 0, 5 + rowIndex, 2, 1)
      case _ => throw NoSuchElementsException("Cannot create content for dialog for unknown element.")

  this.dialogPane().setContent(grid)

end ElementDialog