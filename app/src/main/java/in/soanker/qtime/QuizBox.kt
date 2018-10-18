package `in`.soanker.qtime

import `in`.izba.woot.FirstImageFragment
import `in`.izba.woot.SecondImageFragment
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_question_box.*
import java.util.*

class QuizBox : AppCompatActivity() {

    companion object {
        private val TAG = "quizbox"
    }

    var correctAnswer: String = "skipped"
    lateinit var skipQuestion: Button
    lateinit var answerClicked: Button
    lateinit var radioSet: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_box)

        val calendar = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        //Toast.makeText(this, "current time is $calendar line 37", Toast.LENGTH_LONG).show()

        StartProgressBar()

        val questionToSolve: String = intent.getStringExtra("myQuestion")
        val choice1: String = intent.getStringExtra("choice1")
        val choice2: String = intent.getStringExtra("choice2")
        val choice3: String = intent.getStringExtra("choice3")
        val choice4: String = intent.getStringExtra("choice4")
        val answer: String = intent.getStringExtra("answer")
        Log.e(TAG, "answer in line 57 is $answer")


        skipQuestion = findViewById<View>(R.id.skipQuestion) as Button
        answerClicked = findViewById<View>(R.id.confirm) as Button
        radioSet = findViewById<View>(R.id.answerGroup) as RadioGroup

        val questionBox = findViewById<View>(R.id.qDisplay) as TextView
        questionBox.text = questionToSolve

        val choiceA = findViewById<View>(R.id.choiceOne) as RadioButton
        val choiceB = findViewById<View>(R.id.choiceTwo) as RadioButton
        val choiceC = findViewById<View>(R.id.choiceThree) as RadioButton
        val choiceD = findViewById<View>(R.id.choiceFour) as RadioButton

        choiceA.text = choice1
        choiceB.text = choice2
        choiceC.text = choice3
        choiceD.text = choice4

        // WHEN CONFIRM BUTTON IS CLICKED
        var selChoice: String = ""

        var finalKey = ""
        answerClicked.setOnClickListener {
            if(choiceA.isChecked){
                selChoice = "choice1"
                finalKey = "$questionToSolve\nAnswer:$choice1"
            }
            if(choiceB.isChecked){
                selChoice = "choice2"
                finalKey = "$questionToSolve\nAnswer:$choice2"
            }
            if(choiceC.isChecked){
                selChoice = "choice3"
                finalKey = "$questionToSolve\nAnswer:$choice3"
            }
            if(choiceD.isChecked){
                selChoice = "choice4"
                finalKey = "$questionToSolve\nAnswer:$choice4"
            }
            questionBox.text = finalKey
            if(answer == selChoice){
                //Toast.makeText(this, "choice is checked $selChoice", Toast.LENGTH_LONG).show()
                correctAnswer = "true"
            } else {
                //Toast.makeText(this, "wrong answer $selChoice", Toast.LENGTH_LONG).show()
                correctAnswer = "false"
            }
            updateQuestionUI(correctAnswer)
        }

        //SKIP BUTTON IS CLICKED

        skipQuestion.setOnClickListener {
            progressStatus = 1
//            correctAnswer = "skipped"
//            Log.e(TAG, "in back button pressed correct answer value is $correctAnswer line 93 QuizBox")
//            val result = Intent()
//            result.putExtra("result", correctAnswer)
//            setResult(Activity.RESULT_OK, result)
        }
    }

    private fun updateQuestionUI(result: String){
        radioSet.visibility = View.INVISIBLE
        answerClicked.visibility = View.INVISIBLE
        skipQuestion.visibility = View.INVISIBLE
        if(result == "true"){
            showFragmentResult(result)
        }else {
            showFragmentResult(result)
        }
        progressStatus = 5

    }

    private fun showFragmentResult(choice: String){
        if(choice == "true"){
            val fragment = FirstImageFragment.newInstance()

            val fragmentTransaction = supportFragmentManager.beginTransaction()

            fragmentTransaction.replace(R.id.fragmentContainer, fragment, "#Tag_correct")
            fragmentTransaction.commit()
        } else {
            val fragment = SecondImageFragment.newSecondInstance()
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragmentContainer, fragment)
            fragmentTransaction.commit()
        }

        Log.e(TAG, "Inside show fragment line 104")
    }

    // PROGRESS BAR FOR COUNTDOWN TIMER
    private var progressStatus: Int = 30
    private fun StartProgressBar(){

        val handler: Handler = Handler()

        Thread(Runnable {
            while (progressStatus > 0){
                progressStatus -= 1


                try {
                    Thread.sleep(1000)
                }catch (e: InterruptedException){
                    e.printStackTrace()
                }

                //Update the progress bar
                handler.post {
                    progressBar.progress = progressStatus
                    timerText.text = progressStatus.toString()
                    //
                }
                //
            }
            if(progressStatus == 0){
                Log.e(TAG, "in back button pressed correct answer value is $correctAnswer line 93 QuizBox")
                var result = Intent()
                result.putExtra("result", correctAnswer)
                setResult(Activity.RESULT_OK, result)
                finish()
            }
        }).start()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        correctAnswer = "skipped"
        Log.e(TAG, "in back button pressed correct answer value is $correctAnswer line 175")
        val result = Intent()
        result.putExtra("result", correctAnswer)
        setResult(Activity.RESULT_OK, result)
    }
}
