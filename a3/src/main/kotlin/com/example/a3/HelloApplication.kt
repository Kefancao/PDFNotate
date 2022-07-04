package com.example.a3

import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage


class HelloApplication : Application() {
    override fun start(stage: Stage) {

        val model = SpaceInvader();
        val rootGame = Scene(model, 1600.0, 1200.0) 
        rootGame.setOnKeyPressed {
            model.keyPress(it.code)
        } 
        rootGame.setOnKeyReleased {
            model.keyRelease(it.code);
        }
        stage.title = "Space Invader"
        stage.scene = rootGame
        stage.isResizable = false;
        stage.show()
    }
}

fun main() {
    Application.launch(HelloApplication::class.java)
}