import Main.stage
import scalafx.embed.swing.SwingFXUtils
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, ButtonType, Menu, MenuBar, MenuItem}
import scalafx.scene.image.{Image, ImageView, WritableImage}
import scalafx.scene.layout.{Background, Pane}
import scalafx.scene.paint.Color.White
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter

import java.io.FileInputStream
import javax.imageio.ImageIO

class TopMenu(pane: Pane, plan: FloorPlan) extends MenuBar:

  background = Background.fill(White)

  private var hasBackgroundImage: Boolean = false

  /** Make opening images as background images possible and allow the end result to be saved
   * as a picture. */
  private val open = new MenuItem("Open"):
    onAction = eventHandled =>
      val fileChooser = new FileChooser():
        title = "Select background image"
        extensionFilters.add(ExtensionFilter("JPG or PNG", Seq("*.jpg", "*.jpeg", "*.png")))
      val file = fileChooser.showOpenDialog(stage)
      if file != null then
        hasBackgroundImage = true
        val img = Image(FileInputStream(file.getPath))
        val view = ImageView(img)
        val imgAspectRatio = img.getWidth / img.getHeight
        val paneAspectRatio = 600 / 447
        /** The given background image is scaled to fit the pane. */
        if imgAspectRatio > paneAspectRatio then
          view.setFitWidth(600)
          view.setFitHeight(600 / imgAspectRatio)
        else if paneAspectRatio > imgAspectRatio then
          view.setFitWidth(447 * imgAspectRatio)
          view.setFitHeight(447)
        else
          view.setFitWidth(600)
          view.setFitHeight(447)
        /** The program assumes that when a new floorplan is chosen, the user is finished with
         * the old one and discards it. */
        plan.deleteAll()
        plan.scale = 1
        pane.children.clear()
        pane.children.add(view)

  private val save = new MenuItem("Save"):
    onAction = eventHandled =>
      val fileChooser = new FileChooser():
        title = "Save design"
        extensionFilters.add(ExtensionFilter("JPG", Seq("*.jpg", "*.jpeg")))
        extensionFilters.add(ExtensionFilter("PNG", "*.png"))
      val file = fileChooser.showSaveDialog(null)
      if file != null then
        /** If the user has placed a background image to design on, the program saves only it
         * and everything on top of it, but if there is no background image, the whole pane
         * view is saved. */
        if hasBackgroundImage then
          val writableImage = pane.snapshot(null, null)
          val renderedImage = SwingFXUtils.fromFXImage(writableImage, null)
          ImageIO.write(renderedImage, "png", file)
        else
          val writableImage = new WritableImage(600, 447)
          pane.snapshot(null, writableImage)
          val renderedImage = SwingFXUtils.fromFXImage(writableImage, null)
          ImageIO.write(renderedImage, "png", file)

  private val file = new Menu("File"):
    items = Array(open, save)


  /** MenuItems for setting the scale of the floor plan and resetting it. */
  private val setScale = new MenuItem("Set scale"):
    onAction = eventHandled =>
      val y = new ScaleDialog
      val x = y.showAndWait()
      x match
        case Some(ButtonType.Apply) =>
          plan.scale = y.scale
        case _ =>

  private val scaleMenu = new Menu("Scale"):
    items = Array(setScale)


  private val restart = new MenuItem("Restart"):
    onAction = eventHandled =>
      val alert = new Alert(AlertType.Confirmation):
        initOwner(stage)
        title = "Restart"
        headerText = "Are you sure you want to restart?"
        contentText = "Your current design will be discarded."
      val result = alert.showAndWait()
      result match
        case Some(ButtonType.OK) =>
          hasBackgroundImage = false
          plan.deleteAll()
          plan.scale = 1
          pane.children.clear()
        case _ =>

  private val restartMenu = new Menu("Restart"):
    items = Array(restart)


  /** A handy alert that helps the user get started. */
  private val help = new MenuItem("Help"):
    onAction = eventHandled =>
      val alert = new Alert(AlertType.Information):
        initOwner(stage)
        title = "Help"
        headerText = ""
        contentText =
          "Before adding elements to your floor plan, set the desired scale for your floor plan.\n\n" +
            "You can import a ready floor plan or design something completely unique from scratch.\n\n" +
            "Hovering over an icon in the sidebar will display the type of the element. Hovering over a piece of furniture or" +
            "an appliance in the floor plan will display the name of the element.\n\n" +
            "Click on an icon in the sidebar to add it to the design.\n\n" +
            "You can switch between the fixtures-panel and the furniture-panel by clicking \"Next\" or \"Back\".\n\n" +
            "When an element has been added to the floor plan, double click on it to open the editing view.\n\n" +
            "Carpets should be inserted to the plan before other pieces of furniture and lamps should be inserted last.\n\n" +
            "When you are finished, you can save the design as a picture."
        resizable = true
      val result = alert.show()

  private val helpMenu = new Menu("Help"):
    items = Array(help)

  menus = Array(file, scaleMenu, restartMenu, helpMenu)

end TopMenu