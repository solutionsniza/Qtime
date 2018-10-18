package `in`.soanker.qtime

import `in`.izba.woot.FirstImageFragment
import `in`.izba.woot.SecondImageFragment
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_question_box.*
import java.util.*

class QuestionBox : AppCompatActivity() {

    private var database = FirebaseDatabase.getInstance()
    lateinit var dataRef: DatabaseReference

    private val TAG = "soanker"
    private var rightAnswer: String = ""

    lateinit var nextQuestion: Button
    lateinit var confirmAnswer: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try{
            this.supportActionBar?.hide()
        } catch (e: Exception){
            e.printStackTrace()
        }
        setContentView(R.layout.activity_question_box)

        val userId = intent.getStringExtra("userId")
        dataRef = database.getReference("Users").child(userId)
        Log.i(TAG, "Answer: $rightAnswer")


//        readQuestion()

        //GET DATE AND TIME
          //val currentDate = LocalDateTime.now()
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
//
//        when (answer){
//            "choice1" -> rightAnswer = choice1
//            "choice2" -> rightAnswer = choice2
//            "choice3" -> rightAnswer = choice3
//            "choice4" -> rightAnswer = choice4
//        }




        //questionAsked.text = questionToSolve

//        val answerRadioGroup = findViewById<View>(R.id.answerGroup) as RadioGroup
//        answerRadioGroup.visibility = View.INVISIBLE
//        val choiceA = findViewById<View>(R.id.choiceOne) as RadioButton
//        val choiceB = findViewById<View>(R.id.choiceTwo) as RadioButton
//        val choiceC = findViewById<View>(R.id.choiceThree) as RadioButton
//        val choiceD = findViewById<View>(R.id.choiceFour) as RadioButton
//
//        choiceA.text = choice1
//        choiceB.text = choice2
//        choiceC.text = choice3
//        choiceD.text = choice4

        nextQuestion = findViewById<View>(R.id.next) as Button
        nextQuestion.visibility = View.INVISIBLE

        nextQuestion.setOnClickListener {
            readQuestion()
        }

        confirmAnswer = findViewById<View>(R.id.confirm) as Button
        confirmAnswer.isClickable = false
        var selChoice: String = "none"
        confirmAnswer.setOnClickListener{
            if(choiceA.isChecked){
                selChoice = "choice1"
            }
            if(choiceB.isChecked){
                selChoice = "choice2"
            }
            if(choiceC.isChecked){
                selChoice = "choice3"
            }
            if(choiceD.isChecked){
                selChoice = "choice4"
            }

            if(ans == selChoice){
                Toast.makeText(this, "choice is checked $selChoice", Toast.LENGTH_LONG).show()
                correctAnswer = "true"
            } else {
                Toast.makeText(this, "wrong answer $selChoice", Toast.LENGTH_LONG).show()
                correctAnswer = "false"
            }

            updateQuestionUI(correctAnswer)
        }

    }

    lateinit var questRef: DatabaseReference
    lateinit var question: Questions


    private fun readQuestion(){
        // Generate Random Number to read Questions
        val r= Random()
        val randIndex=r.nextInt(51-0)+0
        Log.e(TAG, "Random generated number is $randIndex")
        var selectedQuestion: String = randIndex.toString()
        questRef = database.getReference("QuestionsBank").child(selectedQuestion)

        questRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    if(dataSnapshot.exists()){
                        question = dataSnapshot.getValue(Questions::class.java)!!
                        displayQuestion(question)
                        confirm.isClickable = true
                        next.visibility = View.INVISIBLE
                        confirm.visibility = View.VISIBLE
                        Log.e(TAG, dataSnapshot.value.toString())
                        Log.e(TAG, question.question)
//                        startGame.visibility = View.VISIBLE
//                        startNewQuestion()
                    }
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                if (p0 != null) {
                    Log.i(TAG, "line 122 Data update cancelled, err = ${p0.message}, detail = ${p0.details}")
                }
            }
        })
    }


    lateinit var choiceA: RadioButton
    lateinit var choiceB: RadioButton
    lateinit var choiceC: RadioButton
    lateinit var choiceD: RadioButton
    lateinit var ans: String
    // DISPLAY QUESTION IN QUESTION BOX
    private fun displayQuestion(q: Questions){
        if(q != null) {
            val choiceA = findViewById<View>(R.id.choiceOne) as RadioButton
            val choiceB = findViewById<View>(R.id.choiceTwo) as RadioButton
            val choiceC = findViewById<View>(R.id.choiceThree) as RadioButton
            val choiceD = findViewById<View>(R.id.choiceFour) as RadioButton

            answerGroup.visibility = View.VISIBLE
            choiceA.text = question.choice1
            choiceB.text = question.choice2
            choiceC.text = question.choice3
            choiceD.text = question.choice4

            val questionDisplay = findViewById<View>(R.id.questionAsked) as TextView
            questionDisplay.text = question.question
            ans = question.answer!!
            progressStatus = 0
            StartProgressBar()
        }
    }


var correctAnswer: String = "skipped"
    private fun updateQuestionUI(result: String){
//        choiceOne.text = "Correct Answer: $rightAnswer"
//        choiceTwo.visibility = View.GONE
//        choiceThree.visibility = View.GONE
//        choiceFour.visibility = View.GONE
          answerGroup.visibility = View.INVISIBLE
        /*
        if(result == "true"){
            showFragmentResult(result)
        }else {
            showFragmentResult(result)
        } */

//        var result = Intent()
//        result.putExtra("result", correctAnswer)
//        setResult(Activity.RESULT_OK, result)
        progressStatus = 25

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
    var progressStatus = 0
    private fun StartProgressBar(){

        val handler: Handler = Handler()

        Thread(Runnable {
            while (progressStatus < 30){
                progressStatus += 1


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
            if(progressStatus == 30){
//                var result = Intent()
//                result.putExtra("result", correctAnswer)
//                setResult(Activity.RESULT_OK, result)
//                finish()
                //readQuestion()
                nextQuestion = findViewById(R.id.next) as Button
                nextQuestion.visibility = View.VISIBLE
                confirmAnswer = findViewById(R.id.confirm) as Button
                confirmAnswer.visibility = View.INVISIBLE
            }
        }).start()
    }


//    override fun onBackPressed() {
//        super.onBackPressed()
//        correctAnswer = "skipped"
//        Log.e(TAG, "in back button pressed correct answer value is $correctAnswer line 175")
//        val result = Intent()
//        result.putExtra("result", correctAnswer)
//        setResult(Activity.RESULT_OK, result)
//    }
}
