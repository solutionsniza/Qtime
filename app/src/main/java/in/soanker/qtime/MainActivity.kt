package `in`.soanker.qtime

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.TextView
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener {


    //private var btnPlay: Button? = null

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    companion object {
        private const val TAG = "soanker"
        private const val REQUESTCODE = 1234
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnLogin.setOnClickListener(this)
        btnLogout.setOnClickListener(this)
        btnContinue.setOnClickListener(this)

        // Configure Google Sign In object to request the user data
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        mAuth = FirebaseAuth.getInstance()
    }



    override fun onStart() {
        super.onStart()
        mAuth = FirebaseAuth.getInstance()
        val cInternet = isInternetAvailable(this)
        //Toast.makeText(this, "is internet available $cInternet", Toast.LENGTH_LONG).show()
        val currentUser = mAuth.currentUser
        Log.e(TAG, "user id in on start line 65 is ${currentUser?.uid}")
        updateUI(currentUser)
    }

    private fun signIn() {
        Log.e(TAG, "inside sign in function")
        //val intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        val intent = mGoogleSignInClient.signInIntent
        startActivityForResult(intent, REQUESTCODE)
    }

    private fun signOut() {
        // sign out Firebase
        mAuth.signOut()

        // sign out Google
        mGoogleSignInClient.signOut().addOnCompleteListener(this){
            updateUI(null)
        }
    }

    private fun intoTheGame() {
        val intent = Intent(this, GamePlay::class.java)
        intent.putExtra("userId", userId)
        Log.e(TAG, "user id in intent is $userId")
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e(TAG, "Inside on activity result method line 116!")
        if (requestCode == REQUESTCODE) {
            Log.e(TAG, "Inside on request code sign in method line 117!")
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                // successful -> authenticate with Firebase
                val account = result.signInAccount
                Log.e(TAG, "Sign in username is ${account!!.displayName.toString()}")

                firebaseAuthWithGoogle(account)
            } else {
                // failed -> update UI
                updateUI(null)
                Log.e(TAG, "SignIn: failed!")
            }
        }
    }

    private var database = FirebaseDatabase.getInstance()
    private var myRef = database.reference
    private lateinit var dataRef: DatabaseReference

    var userId: String = ""

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.e(TAG, "firebaseAuthWithGoogle():" + acct.id!!)

        Log.e(TAG, "Inside on firebase auth google method line 131!")
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success
                        Log.e(TAG, "signInWithCredential: Success!")
                        val user = mAuth.currentUser
                        userId = mAuth.currentUser!!.uid
                        dataRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)

                        // read from database to check whether user exists or not
                        dataRef.addListenerForSingleValueEvent(object : ValueEventListener {

                            override fun onCancelled(p0: DatabaseError) {
                                Log.e(TAG, "line 144 Data update cancelled, err = ${p0.message}, detail = ${p0.details}")
                            }

                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if(dataSnapshot.exists()){
                                    //         val children = dataSnapshot.children
                                    val userData = dataSnapshot.getValue(User1::class.java)
                                    Log.e(TAG, dataSnapshot.value.toString())
                                    Log.e(TAG, "Inside on listener for single value event method database exists line 155! user name is ${userData?.userName}")
                                }
                                else {
                                    // write to database if user does not exist
                                    val currentDate = Calendar.getInstance().get(Calendar.DATE)
                                    Log.e(TAG, "current date is $currentDate")
                                    val userData = User(acct.id.toString(), acct.displayName.toString(), acct.email.toString(), 25, 100, currentDate, false)
                                    myRef.child("Users").child(userId).setValue(userData)
                                }
                            }

                        })

                        updateUI(user)

                    } else {
                        // Sign in fails
                        Log.e(TAG, "signInWithCredential: Failed!", task.exception)
                        updateUI(null)
                    }
                }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            txtUser.text = user.displayName
            val snack = Snackbar.make(root_layout,"welcome ${user.displayName}",Snackbar.LENGTH_INDEFINITE)
            snack.setAction("Close") {
                snack.dismiss()
            }
            snack.setActionTextColor(ContextCompat.getColor(this, R.color.green))
            val view = snack.view
            val text = view.findViewById<TextView>(android.support.design.R.id.snackbar_text)
            text.setTextColor(ContextCompat.getColor(this, R.color.red))
            snack.show()

            btnLogin?.visibility = View.GONE
            btnContinue?.visibility = View.VISIBLE
            btnLogout?.visibility = View.VISIBLE
        } else {
            txtUser.text = getString(R.string.appname)
            btnLogin?.visibility = View.VISIBLE
            btnContinue?.visibility = View.GONE
            btnLogout?.visibility = View.GONE
        }
    }

    override fun onClick(v: View){
        Log.e(TAG, "inside on click function")
        val i = v.id
        when(i){
            R.id.btnLogin -> signIn()
            R.id.btnLogout -> signOut()
            R.id.btnContinue -> intoTheGame()
        }
    }
}
