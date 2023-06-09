package hcmute.edu.vn.a20110567_objectdetectiontensorflowlife

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import hcmute.edu.vn.a20110567_objectdetectiontensorflowlife.ml.SsdMobilenetV11Metadata1
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp

class MainActivity : AppCompatActivity() {

    val paint = Paint()
    lateinit var imageView: ImageView
    lateinit var button: Button
    lateinit var bitmap: Bitmap
    lateinit var model: SsdMobilenetV11Metadata1
    lateinit var labels: List<String>
    val imageProcessor = ImageProcessor.Builder().add(ResizeOp(300, 300, ResizeOp.ResizeMethod.BILINEAR)).build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = Intent()
        intent.setAction(Intent.ACTION_GET_CONTENT)
        intent.setType("image/*")


        labels = FileUtil.loadLabels(this, "labels.txt")
        model = SsdMobilenetV11Metadata1.newInstance(this)
        imageView = findViewById(R.id.imageV)
        button = findViewById(R.id.btn)

        button.setOnClickListener{

            startActivityForResult(intent, 10)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == 10){
            if (data != null){
                var uri = data?.data
                bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                get_predictions();
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        model.close()
    }

    fun get_predictions(){
        // Creates inputs for reference.
        var image = TensorImage.fromBitmap(bitmap)
        image = imageProcessor.process(image)

        // Runs model inference and gets result.
        val outputs = model.process(image)
        val locations = outputs.locationsAsTensorBuffer.floatArray
        val classes = outputs.classesAsTensorBuffer.floatArray
        val scores = outputs.scoresAsTensorBuffer.floatArray
        val numberOfDetections = outputs.numberOfDetectionsAsTensorBuffer.floatArray

        var mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutable)


        val h = mutable.height
        val w = mutable.width
        paint.textSize = h/15f
        paint.strokeWidth = h/85f
        var x = 0
        scores.forEachIndexed{index, fl ->
            x = index
            //x *= 4
            if(fl > 0.5){
                paint.style = Paint.Style.STROKE
                canvas.drawRect(RectF(locations.get(x+1)*w, locations.get(x)*h, locations.get(x+3)*w, locations.get(x+2)*h), paint)
                paint.style = Paint.Style.FILL
                canvas.drawText(labels.get(classes.get(index).toInt())+" "+fl.toString(), locations.get(x+1)*w, locations.get(x)*h, paint)
            }
        }

        imageView.setImageBitmap(mutable)

    }
}