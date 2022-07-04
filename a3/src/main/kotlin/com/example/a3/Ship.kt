package com.example.a3

import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle

class Ship(initX: Double, initY: Double): ImageView() {
    init{
        image = Image("player.png")
        fitWidth = 50.0 * 1.2
        fitHeight = 50.0;
        x = initX;
        y = initY;
    }
    fun handleKey(key: KeyCode){
//        if (key == KeyCode.LEFT){
//            x = Math.max(0.0, x - 10.0);
//        } else if (key == KeyCode.RIGHT){
//            x = Math.min(1200.0 - fitWidth, x + 10.0);
//        }
    }
}