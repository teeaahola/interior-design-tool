import scalafx.application.JFXApp3
import scalafx.scene.{Node, Scene}
import scalafx.scene.layout.{Background, GridPane, HBox, Pane, VBox}
import scalafx.scene.shape.*
import scalafx.scene.paint.Color.*
import scalafx.geometry.Insets
import scalafx.geometry.Orientation.Vertical
import scalafx.scene.control.{Button, Label, ScrollPane, Separator, Tooltip}
import scalafx.scene.paint.Color
import scalafx.scene.text.Font

object Main extends JFXApp3:

  def start() =

    stage = new JFXApp3.PrimaryStage:
      title = "Love It Or List It"
      width = 800
      height = 550
      resizable = false

    val root = new GridPane()

    val pane = new Pane()

    /** Create a ShapeMaker to use it to create all required icons to the sidebar. */
    val shapeMaker = new ShapeMaker
    /** Create a floor plan for the program to use. */
    val plan = new FloorPlan

    val mainScene = Scene(parent = root)
    stage.scene = mainScene


    val menu = new TopMenu(pane, plan)

    /** The label is duplicated to ease switching between the sidebars. */
    val title1 = new Label("Click on an element to add it to your floor plan."):
      font = Font("Segoe UI", 14)
    val title2 = new Label("Click on an element to add it to your floor plan."):
      font = Font("Segoe UI", 14)

    /** Create the clickable icons to be set in the sidebars. */
    val wall: Shape = shapeMaker.fixtureShapeMaker(new Rectangle, plan, pane, false)
    val appl: Shape = shapeMaker.fixtureShapeMaker(new Rectangle, plan, pane, true)
    val door: Shape = shapeMaker.fixtureShapeMaker(new Arc, plan, pane, false)

    val circle: Shape = shapeMaker.furnitureShapeMaker(new Circle, Color.web("#8066cc"), plan, pane, false)
    val rect: Shape = shapeMaker.furnitureShapeMaker(new Rectangle, Color.web("#6680e6"), plan, pane, false)
    val ellip: Shape = shapeMaker.furnitureShapeMaker(new Ellipse, Color.web("#ff9999"), plan, pane, false)
    val chair: Shape = shapeMaker.furnitureShapeMaker(new Arc, Color.web("#cc8033"), plan, pane, false)
    val lamp: Shape = shapeMaker.furnitureShapeMaker(new Circle, Color.web("#ffd700"), plan, pane, true)
    val carpet: Shape = shapeMaker.furnitureShapeMaker(new Rectangle, Color.web("#1a3399"), plan, pane, true)


    val sideBox = new VBox:
      padding = Insets.apply(50, 70, 50, 70)
      spacing = 50
      prefHeight = 450
      background = Background.fill(White)
      children = Array(wall, appl, door)

    val sideBox2 = new VBox:
      padding = Insets.apply(50, 60, 50, 62)
      spacing = 40
      maxWidth = 200
      background = Background.fill(White)
      children = Array(circle, rect, ellip, chair, lamp, carpet)

    val scroll = new ScrollPane():
      prefWidth = 200
      content = sideBox2

    val separator = new Separator():
      orientation = Vertical

    val middle = new HBox:
      children = Array(sideBox, separator, pane)

    /** Two boxes to switch between the views. Two separate boxes are required as "next" and
     * "back" cannot add each other to the children of the same HBox recursively. */
    val bottomBox = new HBox:
      padding = Insets.apply(10, 10, 10, 10)
      spacing = 40
      prefWidth = 800
      background = Background.fill(White)

    val bottomBox2 = new HBox:
      padding = Insets.apply(10, 10, 10, 10)
      spacing = 40
      prefWidth = 800
      background = Background.fill(White)


    val all = new VBox:
      children = Array(menu, new Separator(), middle, new Separator(), bottomBox)


    /** Create buttons for switching between the views. Does not actually switch the root of
     * the scene but the content of "middle" and "all". */
    val next = new Button("Next"):
      prefWidth = 48
      onAction = (eventHandled) =>
        middle.children = Array(scroll, pane)
        all.children = Array(menu, new Separator(), middle, new Separator(), bottomBox2)

    val back = new Button("Back"):
      prefWidth = 48
      onAction = (eventHandled) =>
        middle.children = Array(sideBox, separator, pane)
        all.children = Array(menu, new Separator(), middle, new Separator(), bottomBox)

    bottomBox.children = Array(next, title1)
    bottomBox2.children = Array(back, title2)

    root.add(all, 0, 0)

  end start

end Main

