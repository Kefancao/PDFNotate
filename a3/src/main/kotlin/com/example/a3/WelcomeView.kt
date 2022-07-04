package com.example.a3

import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.layout.BorderStroke
import javafx.scene.layout.VBox
import javafx.scene.text.Font

class WelcomeView(private val model: Model) : VBox(), IView{
    val siLogo = ImageView(Image("logo.png"))
    val instructions = Group()
    val gameTitle = Label("Instructions")
    val startPrompt = Label("Enter - Start Game")
    val instr1 = Label("A or ◀︎, D or ▶︎ - Move ship left or right︎")
    val instr2 = Label("Space - Fire!")
    val instr3 = Label("Q - Quit Game")
    val instr4 = Label("1 or 2 or 3 - Start Game at a specific level")
    init{

        gameTitle.font = Font.font(30.0)
        val words = arrayOf(startPrompt, instr1, instr2, instr3, instr4)
        for (lab in words){
            lab.font = Font.font(20.0)
        }

        children.add(siLogo);
        children.addAll(gameTitle, startPrompt, instr1, instr2, instr3, instr4)
        setOnKeyPressed {
            if (it.code == KeyCode.ENTER){
                println("HERE");
            }
        }
        alignment = Pos.CENTER
    }

    override fun updateView(){}
}