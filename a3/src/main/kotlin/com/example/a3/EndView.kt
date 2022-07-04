package com.example.a3

import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.scene.text.Font

class EndView (val finalScore: Int, val winLose: Boolean): VBox() {
    val congrats_text = Label("Congratualations!")
    val beatGame_txt = Label("You Win!!! \n\n")
    val loseGame_txt = Label("You Lost!!!\n\n")
    val score_txt = Label("Your final score was $finalScore\n\n")
    val nextRound_instr = Label("Press R to start over or Q to quit!")
    init{
        alignment = Pos.CENTER
        congrats_text.font = Font.font(50.0)
        loseGame_txt.font = Font.font(50.0)
        beatGame_txt.font = Font.font(50.0)
        score_txt.font = Font.font(50.0);
        nextRound_instr.font = Font.font(50.0);
        if (winLose){
            children.addAll(congrats_text, beatGame_txt, score_txt, nextRound_instr);
        } else{
            children.addAll(loseGame_txt, score_txt, nextRound_instr);
        }
    }
}