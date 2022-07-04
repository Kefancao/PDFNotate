package com.example.a3

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.input.KeyCode
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.Stage


class HelloApplication : Application() {
    override fun start(stage: Stage) {

        val model = Model(); 
        val rootGame = Scene(model, 1600.0, 1200.0) 
        rootGame.setOnKeyPressed {
            model.keyPress(it.code)
        } 
        rootGame.setOnKeyReleased {
            model.keyRelease(it.code);
        }
        stage.title = "My Space Invader"
        stage.scene = rootGame
        stage.isResizable = false;
        stage.show()
    }
}

fun main() {
    Application.launch(HelloApplication::class.java)
}