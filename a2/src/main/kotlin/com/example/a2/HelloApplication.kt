package com.example.a2

import javafx.application.Application
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.scene.Cursor
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.shape.Circle
import javafx.scene.shape.Line
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.File

var oldColor: Paint = Color.BLACK
var oldFill: Color = Color.WHITE
var oldLine: Color = Color.BLACK
val fillPicker = ColorPicker(Color.WHITE)
val outline = ColorPicker(Color.BLACK)
val slider = Slider(1.0, 10.0, 5.0)
val selectBtn = ToggleButton("Select")
var selected_indice = -1;



class HelloApplication : Application() {
    override fun start(stage: Stage) {
        val model = Model();
        val toolview = ToolView(model)
        val canvasview = CanvasView(model)
        val scroll = ScrollPane()

        val menubar = MenuBar()
        val filemenu = Menu("File")
        val helpmenu = Menu("Help")
        val editmenu = Menu("Edit")

        val fileNew = MenuItem("New")
        val fileLoad = MenuItem("Load")
        val fileSave = MenuItem("Save")
        val fileQuit = MenuItem("Quit")
        val about = MenuItem("About")
        val editCut = MenuItem("Cut")
        val editCopy = MenuItem("Copy")
        val editPaste = MenuItem("Paste")


        val saveFile = {
            val prompt = Alert(Alert.AlertType.CONFIRMATION, "Would you like to save?", ButtonType.YES, ButtonType.NO)
            prompt.showAndWait()
            if (prompt.result == ButtonType.YES){
                val place = FileChooser()
                place.initialFileName = "NewFile.txt"
                val fileName = place.showSaveDialog(stage)
                if (fileName != null){
                    fileName.writeText(model.save())
                }

            }
        }

        val loadFile = {
            val file = FileChooser()
            val out = file.showOpenDialog(stage)
            if (out != null){
                model.load(out.readLines());
            }

        }
        fileQuit.setOnAction {
            saveFile();
            Platform.exit();
        }

        fileSave.setOnAction {
            saveFile();
        }
        fileLoad.setOnAction {
            saveFile()
            loadFile();
        }
        fileNew.setOnAction {
            saveFile();
            model.load(emptyList())
        }

        editCut.setOnAction {
            model.copySelected()
            model.keyEventPass(KeyCode.BACK_SPACE)
        }

        editCopy.setOnAction {
            model.copySelected()
        }

        editPaste.setOnAction {
            model.pasteSelected()
        }

        about.setOnAction {
            val info = Alert(Alert.AlertType.INFORMATION, "Kefan Cao\nk33cao (20898903)");
            info.title = "A2: Sketch It"
            info.headerText = "A2: Sketch It"
            info.showAndWait()
        }

        filemenu.items.addAll(fileNew, fileLoad, fileSave, fileQuit)
        helpmenu.items.add(about)
        editmenu.items.addAll(editCut, editCopy, editPaste)

        menubar.menus.addAll(filemenu, editmenu, helpmenu)


        val pane = BorderPane()
        scroll.content = canvasview
        scroll.isFitToWidth = true
        scroll.isFitToHeight = true

        pane.left = toolview
        pane.center = scroll
        pane.top = menubar
        pane.setOnKeyPressed {
            if (it.code == KeyCode.CONTROL || it.code == KeyCode.COMMAND) model.cmdDown = true
            model.keyEventPass(it.code);
        }
        pane.setOnKeyReleased {
            if (it.code == KeyCode.CONTROL || it.code == KeyCode.COMMAND) model.cmdDown = false
        }

        val scene = Scene(pane, 1200.0, 800.0)
        stage.title = "Paint"
        stage.maxHeight = 1400.0
        stage.maxWidth = 1900.0
        stage.minHeight = 680.0
        stage.minWidth = 640.0
        stage.scene = scene
        stage.show()
    }
}
