import javafx.application.Application
import javafx.application.Platform
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.Stage
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import java.io.File
import java.io.FileInputStream
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.*

class Main : Application() {
    private fun previousDir(path : String): String {
        return path.split('/').dropLast(1).joinToString("/")
    }
    override fun start(stage: Stage) {

        // CREATE WIDGETS TO DISPLAY
        // menubar & toolbar
        val menuBar = MenuBar()
        val fileMenu = Menu("File")
        val optionMenu = Menu("Option")
        val actionMenu = Menu("Action")

        val fileQuit = MenuItem("Quit")

        val optionShowHidden = MenuItem("Show Hidden Files")

        val actionRename = MenuItem("Rename")
        val actionMove = MenuItem("Move")
        val actionDelete = MenuItem("Delete")

        fileQuit.setOnAction { Platform.exit() }

        fileMenu.items.addAll(fileQuit)
        actionMenu.items.addAll(actionMove, actionRename, actionDelete)
        optionMenu.items.addAll(optionShowHidden)
        menuBar.menus.addAll(fileMenu, optionMenu, actionMenu)

        // Tool bar
        val toolbar = ToolBar()
        val homeButton= Button("🏠 Home")
        val prevButton = Button("⬅️ Prev")
        val nextButton = Button("➡️ Next")
        val deleteButton = Button("🗑 Delete")
        val renameButton = Button("⌨️ Rename")
        val moveButton = Button("\uD83D\uDCEE Move")
        toolbar.items.addAll(homeButton, prevButton, nextButton, deleteButton, renameButton, moveButton)

        // stack menu and toolbar in the top region
        val vbox = VBox(menuBar, toolbar)
        val homeDir = "${System.getProperty("user.dir")}/test/"
        var curr = homeDir
        val hideGlob = "[!.]*"
        val showAllGlob = "*"
        var glob = hideGlob
        // panel on left side, need to replace with a tree
        var entries = Path.of(curr).listDirectoryEntries(glob).map{it.fileName}.plus("../")
        var oEntries = FXCollections.observableArrayList(entries)
        var directoryPane = ListView(oEntries)
        // textInput for renaming
        // SETUP LAYOUT
        val border = BorderPane()
        border.top = vbox
        border.left = directoryPane
        border.bottom = Label("")

        // Show Hidden files event handler
        val showHandler = {
            glob = if (glob == hideGlob) showAllGlob else hideGlob
            directoryPane.items.setAll(Path.of(curr).listDirectoryEntries(glob).map{it.fileName}.plus("../"))
        }
        val toggleHandler = {
            if (glob == showAllGlob) {
                optionShowHidden.text = "Hide Hidden Files"
            } else{
                optionShowHidden.text = "Show Hidden Files"
            }
        }
        // Rename Event Handler
        val renameHandler = {
            val td = TextInputDialog()
            td.title = "Rename"
            td.contentText = "Please enter the new name"
            td.headerText = "New Name!!!"
            val res = td.showAndWait()
            if (!res.isEmpty){
                var item = Path.of(curr).listDirectoryEntries(glob)[directoryPane.selectionModel.selectedIndex]
                if (!Path.of("${curr}/${td.editor.text}").exists()){
                    try{
                        item.toFile().renameTo(File("${curr}/${td.editor.text}"))
                        directoryPane.items.setAll(Path.of(curr).listDirectoryEntries(glob).map{it.fileName}.plus("../"))
                    } catch (e : Throwable){
                        val alert = Alert(Alert.AlertType.ERROR, "Invalid Name!")
                        alert.showAndWait()
                    }
                }
            }
        }

        val moveHandler = {
            val td = TextInputDialog()
            td.title = "Move"
            td.headerText = "Please enter the destination!"
            val res = td.showAndWait()
            if (!res.isEmpty){
                var item = Path.of(curr).listDirectoryEntries(glob)[directoryPane.selectionModel.selectedIndex]
                if (Path.of("${td.editor.text}").exists()){
                    try{
                        Files.move(item, Paths.get("${td.editor.text}/${directoryPane.selectionModel.selectedItem}"))
                        directoryPane.items.setAll(Path.of(curr).listDirectoryEntries(glob).map{it.fileName}.plus("../"))
                    } catch (e : Throwable){
                        val alert = Alert(Alert.AlertType.ERROR, "ERROR: Invalid Target Directory")
                        alert.showAndWait()
                    }
                } else {
                    val alert = Alert(Alert.AlertType.ERROR, "Error: Target Directory Does Not Exist")
                    alert.showAndWait()
                }
            }
        }
        // Delete Event Handler
        val deleteHandler = {
            val selection = directoryPane.selectionModel.selectedItem
            if (selection != "../"){
                val alert = Alert(Alert.AlertType.CONFIRMATION, "Delete ${selection}?", ButtonType.YES, ButtonType.CANCEL)
                alert.showAndWait()
                if (alert.result == ButtonType.YES){
                    Path.of(curr).listDirectoryEntries(glob)[directoryPane.selectionModel.selectedIndex].toFile().deleteRecursively()
                    directoryPane.items.setAll(Path.of(curr).listDirectoryEntries(glob).map{it.fileName}.plus("../"))
                }
            }
        }
        // Text Display Handler
        val textDisplayHandler = { indice : Int ->
            val path = Path.of(curr).listDirectoryEntries(glob)[indice]
            val encoding = Charset.defaultCharset()
            val encoded = Files.readAllBytes(path)
            val lines = String(encoded, encoding)
            val txtArea = TextArea(lines)
            txtArea.isWrapText = true
            border.center = txtArea
        }
        // Image Display Handler
        val imgDisplayHandler = { indice : Int ->
            val stream = FileInputStream("${Path.of(curr).listDirectoryEntries(glob)[indice]}")
            val imgView = ImageView(Image(stream))
            imgView.isPreserveRatio = true
            imgView.fitWidthProperty().bind(ReadOnlyObjectWrapper<Double>(550.0))
            imgView.fitHeightProperty().bind(ReadOnlyObjectWrapper<Double>(500.0))
            border.center = imgView
        }

        renameButton.setOnMouseClicked { renameHandler() }
        deleteButton.setOnMouseClicked { deleteHandler() }
        moveButton.setOnMouseClicked { moveHandler() }
        actionRename.setOnAction { renameHandler() }
        actionDelete.setOnAction { deleteHandler() }
        optionShowHidden.setOnAction {
            showHandler()
            toggleHandler()
        }
        actionMove.setOnAction { moveHandler() }


        // Event handler for mouse clicks
        val setHomeDir = {
            curr = homeDir
            directoryPane.items.setAll(Path.of(curr).listDirectoryEntries(glob).map{it.fileName}.plus("../"))
        }
        val setPreviousDirectory = {
            if (curr != homeDir){
                curr = previousDir(curr)
                directoryPane.items.setAll(Path.of(curr).listDirectoryEntries(glob).map{it.fileName}.plus("../"))
            }
        }

        // Add an exception check to make sure it's a valid directory you're descending into.
        fun setNextDirectory(e : String = ""){
            var firstDir = e
            if (firstDir == ""){
                for (path in Path.of(curr).listDirectoryEntries(glob)){
                    if (path.isDirectory()){
                        firstDir = path.toString()
                        break
                    }
                }
            }
            if (firstDir != ""){
                curr = firstDir
                directoryPane.items.setAll(Path.of(curr).listDirectoryEntries(glob).map{it.fileName}.plus("../"))
            }
        }
        val selectionHandler = {
            val indice = directoryPane.selectionModel.selectedIndex
            val type = directoryPane.selectionModel.selectedItem.toString().split(".").last()
            // See if the clicked indice is a directory. Because some elements are hidden,
            // the index may not produce the right element. Need a way to display
            // only the file names AND keep the path properties.
            if (Path.of(curr).listDirectoryEntries(glob)[indice].isDirectory()){
                // If it is a directory, update the directory pane with whatever
                // is inside of the current directory.
                border.center = null
            }
            // Case when it's an image clicked
            else if (type == "jpg" || type == "png" || type == "bmp"){
                if (Path.of(curr).listDirectoryEntries(glob)[indice].isReadable()){
                    imgDisplayHandler(indice)
                } else{
                    border.center = Text("File Cannot Be Read")
                }

            } else if (type == "txt" || type == "md") {
                if (Path.of(curr).listDirectoryEntries(glob)[indice].isReadable()){
                    textDisplayHandler(indice)
                } else{
                    border.center = Text("File Cannot Be Read")
                }
            } else{
                border.center = Text("Unsupported Type")
            }
        }
        directoryPane.setOnMouseClicked{
            // If double clicks
            val idx = directoryPane.selectionModel.selectedIndex
            if (directoryPane.selectionModel.selectedItem != "../" && !directoryPane.selectionModel.isEmpty){
                border.bottom = Label(Path.of(curr).listDirectoryEntries(glob)[idx].toString())
                selectionHandler()
            } else{
                border.center = null
                border.bottom = Label(curr)
            }
            if (it.clickCount == 2 && directoryPane.selectionModel.selectedItem == "../"){
                setPreviousDirectory()
            }
            else if (it.clickCount == 2 && Path.of(curr).listDirectoryEntries(glob)[idx].isDirectory()){
                setNextDirectory(Path.of(curr).listDirectoryEntries(glob)[idx].toString())
            }
        }
        prevButton.setOnMouseClicked { setPreviousDirectory() }
        nextButton.setOnMouseClicked { setNextDirectory() }
        homeButton.setOnMouseClicked { setHomeDir() }
        directoryPane.setOnKeyPressed { e ->
            val idx = directoryPane.selectionModel.selectedIndex
            if (directoryPane.selectionModel.selectedItem != "../" && !directoryPane.selectionModel.isEmpty){
                border.bottom = Label(Path.of(curr).listDirectoryEntries(glob)[idx].toString())
                selectionHandler()
            } else if (directoryPane.selectionModel.selectedItem == "../"){
                border.center = null
                border.bottom = Label(curr)
            }
            if (e.code == KeyCode.ENTER){
                if (directoryPane.selectionModel.selectedItem != "../"){
                    setNextDirectory(Path.of(curr).listDirectoryEntries(glob)[idx].toString())
                } else {
                    setPreviousDirectory()
                }
            }
            else if (e.code == KeyCode.BACK_SPACE){
                setPreviousDirectory()
            }
        }

        // CREATE AND SHOW SCENE
        val scene = Scene(border, 800.0, 600.0)
        stage.scene = scene
        stage.title = "A1 Demo"
        stage.isResizable = false
        stage.show()
    }
}