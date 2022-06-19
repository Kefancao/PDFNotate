package com.example.a2

import javafx.scene.Group
import javafx.scene.control.Alert
import javafx.scene.control.ScrollPane
import javafx.scene.input.ClipboardContent
import javafx.scene.input.KeyCode
import javafx.scene.layout.FlowPane
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Line
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape


class CanvasView(private val model : Model): Pane(), IView {
    var entry_x = 0.0
    var entry_y = 0.0
    var origin_x = 0.0
    var origin_y = 0.0
    var line_endX = 0.0
    var line_endY = 0.0
    val selectedRect = Rectangle(0.0, 0.0);
    val selectedCirc = Circle(0.0, 0.0, 0.0);
    val selectedLine = Line()

    init{
        model.addView(this)
    }

    override fun pasteSelected() {
        var element = model.clipboard.string
        val info = element.substring(element.indexOf('[') + 1, element.indexOf('*') - 2)
        val lty = element[element.indexOf('*') + 1]
        parse(info, lty.digitToInt(), element[0])
    }

    override fun copySelected(){
        if (model.selectedShape == null) return;
        val content = ClipboardContent()
        println(model.selectedShape.toString() + "*${model.selectedShape!!.strokeDashArray.size}\n")
        content.putString(model.selectedShape.toString() + "*${model.selectedShape!!.strokeDashArray.size}\n")
        model.clipboard.setContent(content)
    }

    override fun updateView(){
        if (model.selectedShape != null){
            val type = model.selectedShape.toString()
            if (type.startsWith('R')){
                val rect = (model.selectedShape as Rectangle)
                (model.selectedShape as Rectangle).fill = model.insideFill
                (model.selectedShape as Rectangle).stroke = model.borderColor
                (model.selectedShape as Rectangle).strokeWidth = model.lineWidth
                rect.strokeDashArray.clear()
                if (model.lineSty == LINE.DASHED) rect.strokeDashArray.addAll(5.0)
                if (model.lineSty == LINE.JAGGED) rect.strokeDashArray.addAll(20.0, 5.0, 15.0)

            } else if (type.startsWith('C')){
                val circ = (model.selectedShape as Circle)
                (model.selectedShape as Circle).fill = model.insideFill
                (model.selectedShape as Circle).stroke = model.borderColor
                (model.selectedShape as Circle).strokeWidth = model.lineWidth
                circ.strokeDashArray.clear()
                if (model.lineSty == LINE.DASHED) circ.strokeDashArray.addAll(5.0)
                if (model.lineSty == LINE.JAGGED) circ.strokeDashArray.addAll(20.0, 5.0, 15.0)
            } else if (type.startsWith('L')){
                val line = (model.selectedShape as Line)
                (model.selectedShape as Line).fill = model.insideFill
                (model.selectedShape as Line).stroke = model.borderColor
                (model.selectedShape as Line).strokeWidth = model.lineWidth
                line.strokeDashArray.clear()
                if (model.lineSty == LINE.DASHED) line.strokeDashArray.addAll(5.0)
                if (model.lineSty == LINE.JAGGED) line.strokeDashArray.addAll(20.0, 5.0, 15.0)
            }
        }
    }

    override fun load(shapes: List<String>) {
        children.clear()
        try{
            for (element in shapes) {
                val info = element.substring(element.indexOf('[') + 1, element.indexOf('*') - 2)
                val lty = element[element.indexOf('*') + 1]
                parse(info, lty.digitToInt(), element[0])
            }
        } catch (e: Throwable){
            val alert = Alert(Alert.AlertType.ERROR, "Invalid File Format")
            alert.showAndWait()
        }
    }

    val removeSelectedShape = {
        children.remove(selectedRect)
        children.remove(selectedCirc)
        children.remove(selectedLine)
        model.selectedShape = null
    }

    override fun save(): String{
        removeSelectedShape();
        var stringRep = "";
        for (child in children){
            stringRep += (child as Shape).toString() + "*${(child as Shape).strokeDashArray.size}\n"
        }
        return stringRep
    }

    override fun keyEventPass(key: KeyCode) {
        if (key == KeyCode.ESCAPE){
            removeSelectedShape();
        } else if (key == KeyCode.BACK_SPACE || key == KeyCode.DELETE){
            children.remove(model.selectedShape)
            removeSelectedShape();
        } else if (key == KeyCode.C && model.cmdDown){
            copySelected()
        } else if (key == KeyCode.V && model.cmdDown){
            pasteSelected()
        } else if (key == KeyCode.X && model.cmdDown){
            copySelected()
            children.remove(model.selectedShape)
            removeSelectedShape();
        } else if (key == KeyCode.H){
//            println("${height}")
        } else if (key == KeyCode.W){
//            println("${width}")
        }
    }

    fun drawRectangle(x: Double, y: Double, h: Double, w: Double, stroke_col: Color,
                      fill_col: Color, stroke : Double, lty : Int) : Rectangle {
        val rect = Rectangle(w, h);
        rect.x = x;
        rect.y = y;
        rect.stroke = stroke_col
        rect.fill = fill_col
        rect.strokeWidth = stroke
        rect.strokeDashArray.clear()
        if (lty == LINE.DASHED.ordinal) rect.strokeDashArray.addAll(5.0)
        if (lty == LINE.JAGGED.ordinal) rect.strokeDashArray.addAll(20.0, 5.0, 15.0)
        println("Drawn at ${x}, ${y}")
        rect.setOnMouseClicked { it ->
            // If the tool is selection and this rectangle is not selected, then
            //   we must deselect the previously selected shape if one exists.
            if (model.tool == SELECT.SELECTION && model.selectedShape != rect){
                removeSelectedShape();
            }
            if (model.tool == SELECT.SELECTION && !children.contains(selectedRect)){
                children.remove(rect)
                children.add(rect)
                model.selectedShape = rect;
                selectedRect.x = rect.x
                selectedRect.y = rect.y
                selectedRect.width = rect.width
                selectedRect.height = rect.height
                children.add(selectedRect)
                model.borderColor = rect.stroke as Color
                model.insideFill = rect.fill as Color
                model.lineWidth = rect.strokeWidth
                model.updateViews()
            } else if (model.tool == SELECT.FILL){
                rect.fill = model.insideFill
            } else if (model.tool == SELECT.ERASE){
                children.remove(rect)
            }
            it.consume()
        }
        return rect;
    }

    fun drawCircle(x: Double, y: Double, rad: Double, stroke_col: Color, fill_col: Color,
                   stroke: Double, lty: Int): Circle {
        val circ = Circle(x, y, rad);
        circ.stroke = stroke_col
        circ.fill = fill_col
        circ.strokeWidth = stroke
        if (lty == LINE.DASHED.ordinal) circ.strokeDashArray.addAll(5.0)
        if (lty == LINE.JAGGED.ordinal) circ.strokeDashArray.addAll(20.0, 5.0, 15.0)
        circ.setOnMouseClicked { it ->
            if (model.tool == SELECT.SELECTION && model.selectedShape != circ){
                if (children.contains((selectedRect))) children.remove(selectedRect)
                if (children.contains((selectedCirc))) children.remove(selectedCirc)
                if (children.contains((selectedLine))) children.remove(selectedLine)
            }
            if (model.tool == SELECT.SELECTION && !children.contains(selectedCirc)){
                children.remove(circ)
                children.add(circ)
                model.selectedShape = circ
                selectedCirc.centerX = circ.centerX
                selectedCirc.centerY = circ.centerY
                selectedCirc.radius = circ.radius
                children.add(selectedCirc)
                model.borderColor = circ.stroke as Color
                model.insideFill = circ.fill as Color
                model.lineWidth = circ.strokeWidth
                model.updateViews()
            } else if (model.tool == SELECT.FILL){
                circ.fill = model.insideFill
            } else if (model.tool == SELECT.ERASE){
                children.remove(circ)
            }
            it.consume()
        }
        return circ;
    }

    fun drawLine(start_x: Double, start_y: Double, end_x: Double, end_y: Double,
                 color: Color, width: Double, lty: Int): Line{
        val line = Line(start_x, start_y, end_x, end_y);
        line.fill = color;
        line.stroke = color;
        line.strokeWidth = width
        if (lty == LINE.DASHED.ordinal) line.strokeDashArray.addAll(5.0)
        if (lty == LINE.JAGGED.ordinal) line.strokeDashArray.addAll(20.0, 5.0, 15.0)
        line.setOnMouseClicked { it ->
            if (model.tool == SELECT.SELECTION && model.selectedShape != line){
                if (children.contains((selectedRect))) children.remove(selectedRect)
                if (children.contains((selectedCirc))) children.remove(selectedCirc)
                if (children.contains((selectedLine))) children.remove(selectedLine)
            }
            if (model.tool == SELECT.SELECTION && !children.contains(selectedLine)){
                children.remove(line)
                children.add(line)
                model.selectedShape = line
                selectedLine.startX = line.startX
                selectedLine.startY = line.startY
                selectedLine.endX = line.endX
                selectedLine.endY = line.endY
                children.add(selectedLine)
                model.borderColor = line.stroke as Color
                model.lineWidth = line.strokeWidth
                model.updateViews()
            } else if (model.tool == SELECT.ERASE){
                children.remove(line)
            }
            it.consume()
        }
        return line
    }

    fun parse(line: String, lty: Int, t: Char){
        var sx = 0.0;
        var sy = 0.0;
        var ex = 0.0;
        var ey = 0.0;
        var swidth = 0.0
        var sheight = 0.0
        var sfill = ""
        var sstroke = ""
        var sstrokeWidth = 0.0
        var props = line.split(',')
        if (t == 'R'){
            for (prop in props){
                var temp = prop.split('=')
                when (temp[0].trim()) {
                    "x" -> sx = temp[1].toDouble()
                    "y" -> sy = temp[1].toDouble()
                    "width" -> swidth = temp[1].toDouble()
                    "height" -> sheight = temp[1].toDouble()
                    "fill" -> sfill = temp[1]
                    "stroke" -> sstroke = temp[1]
                    "strokeWidth" -> sstrokeWidth = temp[1].toDouble()
                }
            }
            println("sx: ${sx}, sy: ${sy}, ex: ${ex}, ey: ${ey}, swidth: ${swidth}, sfill: ${sfill}, sstroke: ${sstroke}")
            children.add(drawRectangle(sx, sy, sheight, swidth, Color.web(sstroke), Color.web(sfill), sstrokeWidth, lty));
        } else if (t == 'L'){
            for (prop in props){
                val temp = prop.split("=")
                when (temp[0].trim()){
                    "startX" -> sx = temp[1].toDouble()
                    "startY" -> sy = temp[1].toDouble()
                    "endX" -> ex = temp[1].toDouble()
                    "endY" -> ey = temp[1].toDouble()
                    "stroke" -> sstroke = temp[1]
                    "strokeWidth" -> sstrokeWidth = temp[1].toDouble()
                }
            }
            println("sx: ${sx}, sy: ${sy}, ex: ${ex}, ey: ${ey}, swidth: ${swidth}, sfill: ${sfill}, sstroke: ${sstroke}")
            children.add(drawLine(sx, sy, ex, ey, Color.web(sstroke), sstrokeWidth, lty))
        } else if (t == 'C'){
            for (prop in props){
                val temp = prop.split("=")
                when (temp[0].trim()){
                    "centerX" -> sx = temp[1].toDouble()
                    "centerY" -> sy = temp[1].toDouble()
                    "radius" -> ex = temp[1].toDouble()
                    "fill" -> sfill = temp[1]
                    "stroke" -> sstroke = temp[1]
                    "strokeWidth" -> sstrokeWidth = temp[1].toDouble()
                }
            }
            println("sx: ${sx}, sy: ${sy}, ex: ${ex}, ey: ${ey}, swidth: ${swidth}, sfill: ${sfill}, sstroke: ${sstroke}")
            children.add(drawCircle(sx, sy, ex, Color.web(sstroke), Color.web(sfill), sstrokeWidth, lty))
        }
    }

    init{
//        prefHeight = 800.0
//        prefWidth = 1200.0
        // Rectangle Selector
        selectedRect.fill = Color.TRANSPARENT
        selectedRect.strokeDashArray.addAll(3.0)
        selectedRect.stroke = Color.RED
        // Circle Selector
        selectedCirc.fill = Color.TRANSPARENT
        selectedCirc.strokeDashArray.addAll(3.0)
        selectedCirc.stroke = Color.RED
        // Line Selector
        selectedLine.fill = Color.TRANSPARENT
        selectedLine.strokeDashArray.addAll(3.0)
        selectedLine.stroke = Color.RED
        selectedLine.strokeWidth = 2.0

        // Rectangle Selector Events
        selectedRect.setOnMousePressed {
            entry_x = it.x
            entry_y = it.y;
            origin_x = selectedRect.x;
            origin_y = selectedRect.y;
        }
        selectedRect.setOnMouseDragged {
            if (model.tool == SELECT.SELECTION){
                selectedRect.x = origin_x + it.x - entry_x
                selectedRect.y = origin_y + it.y - entry_y
                (model.selectedShape as Rectangle).x = selectedRect.x
                (model.selectedShape as Rectangle).y = selectedRect.y
            }
        }
        selectedRect.setOnMouseClicked {
        // Just so the event doesn't get passed up the chain to 'this'
        //  and deselect the shape.
            it.consume()
        }
        // Circle Selector Events
        selectedCirc.setOnMousePressed {
            entry_x = it.x
            entry_y = it.y;
            origin_x = selectedCirc.centerX;
            origin_y = selectedCirc.centerY;
        }

        selectedCirc.setOnMouseDragged {
            if (model.tool == SELECT.SELECTION){
                selectedCirc.centerX = origin_x + it.x - entry_x
                selectedCirc.centerY = origin_y + it.y - entry_y
                println(model.selectedShape.toString())
                (model.selectedShape as Circle).centerX = selectedCirc.centerX
                (model.selectedShape as Circle).centerY = selectedCirc.centerY
            }
        }

        selectedCirc.setOnMouseClicked {
            it.consume()
        }

        // Line Selector Events
        selectedLine.setOnMousePressed {
            entry_x = it.x
            entry_y = it.y;
            origin_x = selectedLine.startX
            origin_y = selectedLine.startY
            line_endX = selectedLine.endX
            line_endY = selectedLine.endY
        }
        selectedLine.setOnMouseDragged {
            if (model.tool == SELECT.SELECTION){
                selectedLine.startX = origin_x + it.x - entry_x
                selectedLine.startY = origin_y + it.y - entry_y
                selectedLine.endX = line_endX + it.x - entry_x
                selectedLine.endY = line_endY + it.y - entry_y
                (model.selectedShape as Line).startX = selectedLine.startX
                (model.selectedShape as Line).startY = selectedLine.startY
                (model.selectedShape as Line).endX = selectedLine.endX
                (model.selectedShape as Line).endY = selectedLine.endY
            }
        }
        selectedLine.setOnMouseClicked {
            it.consume()
        }
        setOnMousePressed { e ->
            entry_x = e.x;
            entry_y = e.y;
            if (model.tool == SELECT.RECTANGLE){
                val rect = drawRectangle(e.x, e.y, 0.0, 0.0, model.borderColor, model.insideFill, model.lineWidth, model.lineSty.ordinal);
                model.selectedShape = rect;
                children.add(rect)
            }
            else if (model.tool == SELECT.CIRCLE){
                val circ = drawCircle(e.x, e.y, 0.0, model.borderColor, model.insideFill, model.lineWidth, model.lineSty.ordinal)
                model.selectedShape = circ
                children.add(circ)
            } else if (model.tool == SELECT.LINE){
                val line = drawLine(e.x, e.y, e.x, e.y, model.borderColor, model.lineWidth, model.lineSty.ordinal)
                model.selectedShape = line
                children.add(line)
            }
        }
        setOnMouseDragged {
            if (model.tool == SELECT.RECTANGLE){
                val temp = model.selectedShape as Rectangle
                temp.width = it.x - entry_x
                temp.height = it.y - entry_y
                if (temp.height < 0){
                    temp.y -= temp.y - it.y
                    temp.height *= -1
                }
                if (temp.width < 0){
                    temp.x -= temp.x - it.x
                    temp.width *= -1
                }

            } else if (model.tool == SELECT.CIRCLE){
                println("SCENE GRAPH DRAGGING TRIGGERED")
                val temp = model.selectedShape as Circle
                temp.radius = Math.sqrt((temp.centerX - it.x) * (temp.centerX - it.x) + (temp.centerY - it.y) * (temp.centerY - it.y))
            } else if (model.tool == SELECT.LINE){
                val temp = model.selectedShape as Line
                temp.endX = it.x;
                temp.endY = it.y;
            }
        }
        setOnMouseClicked {
            if (model.selectedShape != null){
                println("GOT IT HERE!!")
                model.selectedShape = null
                children.remove(selectedRect)
                children.remove(selectedCirc)
                children.remove(selectedLine)
            }
        }

    }


}