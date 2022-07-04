package com.example.a3

import javafx.scene.image.Image
import javafx.scene.image.ImageView

class AlienBullet (coorX: Double, coorY: Double, private val speed: Double = 7.0, private val type: Int = 1)
    : ImageView(){


    init{
        image = Image("bullet${type}.png")
        fitHeight = 30.0
        fitWidth = 15.0
        // To adjust for bullet width.
        x = coorX - fitWidth / 2;
        // Just to adjust for the bullet height.
        y = coorY + fitHeight + 5.0;
    }

    // Returns false if it goes off the border
    fun update(): Boolean{
        y -= speed;
        if (y + fitHeight < 0){
            return false;
        }
        return true;
    }



}
