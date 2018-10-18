package `in`.soanker.qtime

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class GamePlay : AppCompatActivity() {

    companion object {
        private val TAG = "soanker"
        private val resultRequest = 1
    }

    private var userId: String = ""

    private var database = FirebaseDatabase.getInstance()
    lateinit var dataRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    lateinit var questRef: DatabaseReference
    lateinit var question: Questions
    lateinit var qBox: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try{
            this.supportActionBar?.hide()
        } catch (e: Exception){
            e.printStackTrace()
        }
        Log.e(TAG, "Inside Game Play")
        setContentView(R.layout.activity_game_play)

//        startGame = findViewById<View>(R.id.gamePlay) as ImageButton
//        startGame.visibility = View.INVISIBLE

        mAuth = FirebaseAuth.getInstance()
        userId = mAuth!!.currentUser!!.uid
        Log.e(TAG, "Current user id in Game Play line 47 is:: $userId")

        dataRef = database.getReference("Users").child(userId)
        readDataFromDatabase(dataRef)

        //BUTTON FOR ANSWERING QUESTION
        qBox = findViewById(R.id.questionBox)
        qBox.isClickable = false

        // BUTTON FOR GETTING PREMIUM MEMBERSHIP
        val getPremium = findViewById<View>(R.id.premium) as Button
        getPremium.setOnClickListener{
            getPremium.text = "Clicked"
        }

        // BUTTON FOR KNOWING USER POSITION


        // BUTTON TO KNOW HOW TO PLAY THE GAME


        // BUTTON CLICK TO START THE GAME
        val sGame = findViewById<View>(R.id.questionBox) as Button
        sGame.setOnClickListener(){
            startGameNow()
        }

    }



    // START ACTIVITY FOR GAME PLAY
    private fun startGameNow(){
        var lastClickTime: Long = 0
        if(SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            Log.e(TAG, "multiple clicks detected gameplay line 73")
        } else {
            var intent = Intent(applicationContext, QuizBox::class.java)
            val mQuestion = question.question
            val choiceA = question.choice1
            val choiceB = question.choice2
            val choiceC = question.choice3
            val choiceD = question.choice4
            val answer = question.answer
            intent.putExtra("myQuestion", mQuestion)
            intent.putExtra("choice1", choiceA)
            intent.putExtra("choice2", choiceB)
            intent.putExtra("choice3", choiceC)
            intent.putExtra("choice4", choiceD)
            intent.putExtra("answer", answer)
            intent.putExtra("userId", userId)
            if(mQuestion != ""){
                Log.e(TAG, "starting activity ${currentUser.tokens} gameplay line 89 answer $answer")

                if(currentUser.tokens > 9){
                    startActivityForResult(intent, resultRequest)
                }
            } else {
                readQuestionFromDatabase()
            }
        }

    }


    override fun onResume() {
        super.onResume()
        Log.e(TAG, "Inside on Resume line 99")
        if(isInternetAvailable(this)) {
            readQuestionFromDatabase()
        }
    }

    //Read random question from database
    private fun readQuestionFromDatabase(){
        // Generate Random Number to read Questions
        val r= Random()
        val randIndex=r.nextInt(51-1)+0
        Log.e(TAG, "Random generated number is $randIndex")
        var selectedQuestion: String = randIndex.toString()
        questRef = database.getReference("QuestionsBank").child(selectedQuestion)

        questRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    if(dataSnapshot.exists()){
                        question = dataSnapshot.getValue(Questions::class.java)!!
                        Log.e(TAG, dataSnapshot.value.toString())
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

    lateinit var currentUser: User1
    //lateinit var quest: Questions
    //READ USER DATA FROM DATABASE
    private fun readDataFromDatabase(databaseReference: DatabaseReference){
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    currentUser = dataSnapshot.getValue(User1::class.java)!!
                    val userData = dataSnapshot.getValue(User1::class.java)
                    Log.w(TAG, dataSnapshot.value.toString())
                    updateUi()
                }
            }
            override fun onCancelled(p0: DatabaseError) {
                Log.i(TAG, "line 144 Data update cancelled, err = ${p0.message}, detail = ${p0.details}")
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == resultRequest){
            if(resultCode == Activity.RESULT_OK){
                var selResult = data!!.getStringExtra("result")

                when(selResult){
                    "true" -> currentUser.score += 10
                    "false" -> currentUser.score -= 10
                    "skipped" -> currentUser.score -= 5
                }
                if(currentUser.score <= 0) currentUser.score = 0
                currentUser.tokens -= 1
                dataRef.setValue(currentUser)
                updateUi()
                Toast.makeText(this, "returned value is $selResult and score is ${currentUser.score}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateUi(){
        Log.w(TAG, "Current user credits are in updateUi ${currentUser.tokens}")
        val creditsButton = findViewById<View>(R.id.credits) as Button
        creditsButton.text = "Credits Available " + currentUser.tokens.toString()

        val scoreButton = findViewById<View>(R.id.scoreBoard) as Button
        scoreButton.text = "Score : " + currentUser.score.toString()

        val currentDate = Calendar.getInstance().get(Calendar.DATE)
        if(currentUser.tokensAddDate != currentDate){
            currentUser.tokensAddDate = currentDate
            currentUser.tokens += 100
            dataRef.setValue(currentUser)
        }
    }
}
