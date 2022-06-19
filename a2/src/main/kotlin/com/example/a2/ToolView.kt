package com.example.a2

import javafx.geometry.Insets
import javafx.scene.Cursor
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Line

class ToolView(private val model: Model): VBox(), IView {
    val selectBtn = ToggleButton("Select")
    val lineBtn = ToggleButton("Line")
    val rectbtn = ToggleButton("Rectange")
    val circlebtn = ToggleButton("Circle")
    val fillbtn = ToggleButton("Fill")
    val erasebtn = ToggleButton("Eraser")
    val toolsArr = arrayOf(selectBtn, lineBtn, rectbtn, circlebtn, fillbtn, erasebtn)
    val tools = ToggleGroup()
    val slider = Slider(1.0, 10.0, 5.0)
    val outlinePicker = ColorPicker(Color.BLACK)
    val fillPicker = ColorPicker(Color.WHITE)
    val line = Line(0.0, 30.0, 80.0, 30.0)

    override fun load(shapes : List<String>) {}
    override fun save(): String{
        return ""
    }

    init {
        spacing = 10.0
        for (tool in toolsArr) {
            tool.toggleGroup = tools
            tool.minWidth = 90.0
            tool.cursor = Cursor.HAND
        }
        slider.isShowTickLabels = true
        slider.isShowTickMarks = true
        val line_color = Label("Line Color")
        val fill_color = Label("Fill Color")
        val line_style = Label("Line Style:")
        val line_style_group = ToggleGroup()
        val solid_line = RadioButton("Solid")
        val dotted_line = RadioButton("Dashed")
        val jagged_line = RadioButton("Jagged")

        solid_line.toggleGroup = line_style_group
        solid_line.isSelected = true
        dotted_line.toggleGroup = line_style_group
        jagged_line.toggleGroup = line_style_group

        val line_width = Label("Line Width")
        val line_lab = Label("Line")
        line.strokeWidth = slider.value

        children.addAll(
            selectBtn, lineBtn, rectbtn, circlebtn, fillbtn, erasebtn, line_color, outlinePicker, fill_color, fillPicker,
            line_width, slider, line_style, solid_line, dotted_line, jagged_line, line_lab, line
        )
        padding = Insets(10.0)
        style = "-fx-background-color: #999"
        prefWidth = 100.0
        model.addView(this)
        selectBtn.setOnMouseClicked {
            model.tool = SELECT.SELECTION
        }
        rectbtn.setOnMouseClicked {
            model.tool = SELECT.RECTANGLE
            model.keyEventPass(KeyCode.ESCAPE)
        }
        circlebtn.setOnMouseClicked {
            model.tool = SELECT.CIRCLE
            model.keyEventPass(KeyCode.ESCAPE)
        }
        lineBtn.setOnMouseClicked {
            model.tool = SELECT.LINE
            model.keyEventPass(KeyCode.ESCAPE)
        }
        fillbtn.setOnMouseClicked {
            model.tool = SELECT.FILL
            model.keyEventPass(KeyCode.ESCAPE)
        }
        erasebtn.setOnMouseClicked {
            model.tool = SELECT.ERASE
        }
        outlinePicker.setOnAction {
            model.borderColor = outlinePicker.value
            model.updateViews()
        }
        fillPicker.setOnAction {
            model.insideFill = fillPicker.value
            model.updateViews()
        }
        slider.setOnMouseDragged {
            model.lineWidth = slider.value
            model.updateViews()
        }
        solid_line.setOnAction {
            model.lineSty = LINE.SOLID
            model.updateViews()
        }
        dotted_line.setOnAction {
            model.lineSty = LINE.DASHED
            model.updateViews()
        }
        jagged_line.setOnAction {
            model.lineSty = LINE.JAGGED
            model.updateViews()
        }
    }


    override fun updateView(){
        val lty = model.lineSty.ordinal
        outlinePicker.value = model.borderColor
        fillPicker.value = model.insideFill
        slider.value = model.lineWidth
        line.stroke = model.borderColor
        line.strokeWidth = model.lineWidth
        line.strokeDashArray.clear()
        if (lty == LINE.DASHED.ordinal) line.strokeDashArray.addAll(5.0)
        if (lty == LINE.JAGGED.ordinal) line.strokeDashArray.addAll(20.0, 5.0, 15.0)
    }
}