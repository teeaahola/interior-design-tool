import scalafx.geometry.Insets
import scalafx.scene.control.{ButtonType, Dialog, Label, TextField}
import scalafx.scene.layout.GridPane
import scalafx.scene.paint.Color.Red
import scalafx.scene.shape.Rectangle
import scalafx.scene.text.Text

class ScaleDialog extends Dialog[Unit]:

  title = "Scale"

  headerText = "Set the scale for your floorplan."

  private val content = new Label():
    text = "How many pixels should 100 cm be?"

  private val note = new Text():
    text = "Note that the scale should be set before\n" +
      "adding any elements to the floorplan."

  private val input = new TextField():
    promptText = "e. g. 50"

  /** A variable to store the error message displayed. */
  private val error = new Label(""):
    textFill = Red

  /** An internal storage for the scale of the floor plan. */
  var scale: Double = 1

  private val illustration = Rectangle(100, 3)

  /** Add required button types and disable "Apply". */
  this.getDialogPane.getButtonTypes.add(ButtonType.Cancel)
  this.getDialogPane.getButtonTypes.add(ButtonType.Apply)
  private val apply = this.dialogPane().lookupButton(ButtonType.Apply)
  apply.setDisable(true)

  /** Input validator for the textField that disables the "Apply" button when there is no proper input
   * (a positive number at most 200). */
  input.text.onChange((_, _, newValue) =>
    apply.setDisable(
      newValue.trim.toDoubleOption match
        case Some(number) =>
          if number <= 0 then
            error.text = "The input scale must be positive"
            true
          else if number > 200 then
            error.text = "The input scale is too large"
            true
          else
            error.text = ""
            scale = number / 100
            illustration.width = number
            false
        case None =>
          error.text = "Please input a number"
          true))

  /** Set the content of the dialogPane to be a grid. */
  private val grid = new GridPane():
    vgap = 10
    hgap = 0
    padding = Insets.apply(18, 30, 8, 15)

    add(content, 0, 0, 2, 1)
    add(input, 0, 1)
    add(note, 0, 2, 2, 2)
    add(error, 0, 4, 2, 1)
    add(new Label("100 cm will look like this:"), 0, 5, 2, 1)
    add(illustration, 0, 6, 2, 1)

  this.dialogPane().setContent(grid)

end ScaleDialog