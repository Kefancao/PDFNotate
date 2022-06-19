/*
  * ===================================================
  *                      contents
  * ===================================================
  * 00- Free draw
  * 01- rubber
  * 02- draw Line
  * 03- draw Rectangele
  * 04- draw Circle
  * 05- draw Ellipse
  * 06- Text
  *
  * ----------------------------------------------------
  *                     Features
  * ----------------------------------------------------
  * - the ability to change Line color
  * - the ability to change Fill color
  * - the ability to change Line width
  * - Undo & Redo
  * - Open Image && save Image
  *
  * ____________________________________________________
  * problems
  * - undo & redo : not working with free draw and rubber
  * - Line & Rect & Circ ... aren't be updated while drawing
  * ===================================================
*/
package com.example.a2
import javafx.application.Application
import javafx.beans.Observable
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.Cursor
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import javafx.scene.input.MouseEvent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.*
import javafx.scene.text.Font
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*

class CopApplication : Application() {
    override fun start(primaryStage: Stage) {
        /* ----------btns---------- */
        val drowbtn = ToggleButton("Draw")
        val rubberbtn = ToggleButton("Rubber")
        val linebtn = ToggleButton("Line")
        val rectbtn = ToggleButton("Rectange")
        val circlebtn = ToggleButton("Circle")
        val elpslebtn = ToggleButton("Ellipse")
        val textbtn = ToggleButton("Text")
        val toolsArr = arrayOf(drowbtn, rubberbtn, linebtn, rectbtn, circlebtn, elpslebtn, textbtn)
        val tools = ToggleGroup()
        for (tool in toolsArr) {
            tool.minWidth = 90.0
            tool.toggleGroup = tools
            tool.cursor = Cursor.HAND
        }
        val cpLine = ColorPicker(Color.BLACK)
        val cpFill = ColorPicker(Color.TRANSPARENT)
        val text = TextArea()
        text.prefRowCount = 1
        val slider = Slider(1.0, 50.0, 3.0)
        slider.isShowTickLabels = true
        slider.isShowTickMarks = true
        val line_color = Label("Line Color")
        val fill_color = Label("Fill Color")
        val line_width = Label("3.0")
        val undo = Button("Undo")
        val redo = Button("Redo")
        val save = Button("Save")
        val open = Button("Open")
        val basicArr = arrayOf(undo, redo, save, open)
        for (btn in basicArr) {
            btn.minWidth = 90.0
            btn.cursor = Cursor.HAND
            btn.textFill = Color.WHITE
            btn.style = "-fx-background-color: #666;"
        }
        save.style = "-fx-background-color: #80334d;"
        open.style = "-fx-background-color: #80334d;"
        val btns = VBox(10.0)
        btns.children.addAll(
            drowbtn, rubberbtn, linebtn, rectbtn, circlebtn, elpslebtn,
            textbtn, text, line_color, cpLine, fill_color, cpFill, line_width, slider, undo, redo, open, save
        )
        btns.padding = Insets(5.0)
        btns.style = "-fx-background-color: #999"
        btns.prefWidth = 100.0
        /* ----------Drow Canvas---------- */
        val canvas = Canvas(1080.0, 790.0)
        val gc: GraphicsContext
        gc = canvas.graphicsContext2D
        gc.lineWidth = 1.0
        val line = Line()
        val rect = Rectangle()
        val circ = Circle()
        val elps = Ellipse()
        canvas.onMousePressed = EventHandler { e: MouseEvent ->
            if (drowbtn.isSelected) {
                gc.stroke = cpLine.value
                gc.beginPath()
                gc.lineTo(e.x, e.y)
            } else if (rubberbtn.isSelected) {
                val lineWidth = gc.lineWidth
                gc.clearRect(e.x - lineWidth / 2, e.y - lineWidth / 2, lineWidth, lineWidth)
            } else if (linebtn.isSelected) {
                gc.stroke = cpLine.value
                line.startX = e.x
                line.startY = e.y
            } else if (rectbtn.isSelected) {
                gc.stroke = cpLine.value
                gc.fill = cpFill.value
                rect.x = e.x
                rect.y = e.y
            } else if (circlebtn.isSelected) {
                gc.stroke = cpLine.value
                gc.fill = cpFill.value
                circ.centerX = e.x
                circ.centerY = e.y
            } else if (elpslebtn.isSelected) {
                gc.stroke = cpLine.value
                gc.fill = cpFill.value
                elps.centerX = e.x
                elps.centerY = e.y
            } else if (textbtn.isSelected) {
                gc.lineWidth = 1.0
                gc.font = Font.font(slider.value)
                gc.stroke = cpLine.value
                gc.fill = cpFill.value
                gc.fillText(text.text, e.x, e.y)
                gc.strokeText(text.text, e.x, e.y)
            }
        }
        canvas.onMouseDragged = EventHandler { e: MouseEvent ->
            if (drowbtn.isSelected) {
                gc.lineTo(e.x, e.y)
                gc.stroke()
            } else if (rubberbtn.isSelected) {
                val lineWidth = gc.lineWidth
                gc.clearRect(e.x - lineWidth / 2, e.y - lineWidth / 2, lineWidth, lineWidth)
            }
        }
        canvas.onMouseReleased = EventHandler { e: MouseEvent ->
            if (drowbtn.isSelected) {
                gc.lineTo(e.x, e.y)
                gc.stroke()
                gc.closePath()
            } else if (rubberbtn.isSelected) {
                val lineWidth = gc.lineWidth
                gc.clearRect(e.x - lineWidth / 2, e.y - lineWidth / 2, lineWidth, lineWidth)
            } else if (linebtn.isSelected) {
                line.endX = e.x
                line.endY = e.y
                gc.strokeLine(line.startX, line.startY, line.endX, line.endY)

            } else if (rectbtn.isSelected) {
                rect.width = Math.abs(e.x - rect.x)
                rect.height = Math.abs(e.y - rect.y)
                //rect.setX((rect.getX() > e.getX()) ? e.getX(): rect.getX());
                if (rect.x > e.x) {
                    rect.x = e.x
                }
                //rect.setY((rect.getY() > e.getY()) ? e.getY(): rect.getY());
                if (rect.y > e.y) {
                    rect.y = e.y
                }
                gc.fillRect(rect.x, rect.y, rect.width, rect.height)
                gc.strokeRect(rect.x, rect.y, rect.width, rect.height)

            } else if (circlebtn.isSelected) {
                circ.radius = (Math.abs(e.x - circ.centerX) + Math.abs(e.y - circ.centerY)) / 2
                if (circ.centerX > e.x) {
                    circ.centerX = e.x
                }
                if (circ.centerY > e.y) {
                    circ.centerY = e.y
                }
                gc.fillOval(circ.centerX, circ.centerY, circ.radius, circ.radius)
                gc.strokeOval(circ.centerX, circ.centerY, circ.radius, circ.radius)
            } else if (elpslebtn.isSelected) {
                elps.radiusX = Math.abs(e.x - elps.centerX)
                elps.radiusY = Math.abs(e.y - elps.centerY)
                if (elps.centerX > e.x) {
                    elps.centerX = e.x
                }
                if (elps.centerY > e.y) {
                    elps.centerY = e.y
                }
                gc.strokeOval(elps.centerX, elps.centerY, elps.radiusX, elps.radiusY)
                gc.fillOval(elps.centerX, elps.centerY, elps.radiusX, elps.radiusY)
            }
        }
        // color picker
        cpLine.onAction = EventHandler { e: ActionEvent? ->
            gc.stroke = cpLine.value
        }
        cpFill.onAction = EventHandler { e: ActionEvent? ->
            gc.fill = cpFill.value
        }
        // slider
        slider.valueProperty().addListener { e: Observable? ->
            val width = slider.value
            if (textbtn.isSelected) {
                gc.lineWidth = 1.0
                gc.font = Font.font(slider.value)
                line_width.text = String.format("%.1f", width)
                return@addListener
            }
            line_width.text = String.format("%.1f", width)
            gc.lineWidth = width
        }
        /*------- Undo & Redo ------*/
// Open
        open.onAction = EventHandler { e: ActionEvent? ->
            val openFile = FileChooser()
            openFile.title = "Open File"
            val file = openFile.showOpenDialog(primaryStage)
            if (file != null) {
                try {
                    val io: InputStream = FileInputStream(file)
                    val img = Image(io)
                    gc.drawImage(img, 0.0, 0.0)
                } catch (ex: IOException) {
                    println("Error!")
                }
            }
        }
        /* ----------STAGE & SCENE---------- */
        val pane = BorderPane()
        pane.left = btns
        pane.center = canvas
        val scene = Scene(pane, 1200.0, 800.0)
        primaryStage.title = "Paint"
        primaryStage.scene = scene
        primaryStage.show()
    }

}