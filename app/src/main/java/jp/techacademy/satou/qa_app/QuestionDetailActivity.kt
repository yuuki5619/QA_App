package jp.techacademy.satou.qa_app

import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.view.isInvisible
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_question_detail.*
import kotlinx.android.synthetic.main.list_questions.view.*

class QuestionDetailActivity : AppCompatActivity() {

    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference
    private lateinit var mfavoriteRef: DatabaseReference

    var cuurentstate = 0

    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<*, *>

            val answerUid = dataSnapshot.key ?: ""

            for (answer in mQuestion.answers) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid == answer.answerUid) {
                    return
                }
            }

            val body = map["body"] as? String ?: ""
            val name = map["name"] as? String ?: ""
            val uid = map["uid"] as? String ?: ""

            val answer = Answer(body, name, uid, answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {


        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)


        // 渡ってきたQuestionのオブジェクトを保持する
        val extras = intent.extras
        mQuestion = extras!!.get("question") as Question

        title = mQuestion.title

        // ListViewの準備
        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()


        fab.setOnClickListener {
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser


            if (user == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)

            } else {
                // Questionを渡して回答作成画面を起動する
                // Questionを渡して回答作成画面を起動する
                // --- ここから ---
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", mQuestion)
                startActivity(intent)
                // --- ここまで ---
            }
        }

        val dataBaseReference = FirebaseDatabase.getInstance().reference
        mAnswerRef = dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid).child(AnswersPATH)
        mAnswerRef.addChildEventListener(mEventListener)

        val favoritefab = findViewById<FloatingActionButton>(R.id.favoritefab)
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            favoritefab.visibility = View.INVISIBLE
        }


        favoritefab.setOnClickListener {
            if (cuurentstate == 0 ){
                val favoriteid = mQuestion.questionUid
                mfavoriteRef = dataBaseReference.child(forvarite).child(user!!.uid).child(favoriteid)
                val data = HashMap<String, String>()
                data["genre"] = mQuestion.genre.toString()
                mfavoriteRef.setValue(data)
                favoritefab.setImageResource(R.drawable.ic_star)
                cuurentstate = 1

            }else{
                val favoriteid = mQuestion.questionUid
                mfavoriteRef = dataBaseReference.child(forvarite).child(user!!.uid).child(favoriteid)
                mfavoriteRef.removeValue()
                favoritefab.setImageResource(R.drawable.ic_star_border)
                cuurentstate = 0
            }





        }
    }

    override fun onResume() {
        super.onResume()
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null){
            favoritefab.visibility= View.VISIBLE
            val dataBaseReference = FirebaseDatabase.getInstance().reference
            val favoriteid = mQuestion.questionUid
            mfavoriteRef = dataBaseReference.child(forvarite).child(user!!.uid).child(favoriteid)


            mfavoriteRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val data = snapshot.value as Map<*, *>?
                    val favoritestate = data
                    if (favoritestate == null){
                        favoritefab.setImageResource(R.drawable.ic_star_border)
                        cuurentstate = 0

                    }else{
                        favoritefab.setImageResource(R.drawable.ic_star)
                        cuurentstate = 1
                    }


                }

                override fun onCancelled(firebaseError: DatabaseError) {}
            })


        }








    }
}



