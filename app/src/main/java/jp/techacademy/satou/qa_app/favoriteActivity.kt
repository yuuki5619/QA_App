package jp.techacademy.satou.qa_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_favorite.*
import kotlinx.android.synthetic.main.content_main.*

class favoriteActivity : AppCompatActivity() {
    private lateinit var mfavoriteArraylist: ArrayList<Question>
    private lateinit var mAdapter: favoriteListAdapter
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mfavoriteRef: DatabaseReference



    private val mEventListener = object : ChildEventListener {

        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>
            val genre = map["genre"] ?: ""
            val questionfavorite = dataSnapshot.key ?: ""




            val mfavoriteall = mDatabaseReference.child(ContentsPATH).child(genre).child(questionfavorite)
            mfavoriteall.addListenerForSingleValueEvent(favoliteselect)



        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

        }

        override fun onChildRemoved(snapshot: DataSnapshot) {

        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

        }

        override fun onCancelled(error: DatabaseError) {

        }
    }
    private val favoliteselect  = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
                    val map = snapshot.value as Map<String, String>
                    val title = map["title"] ?: ""
                    val body = map["body"] ?: ""
                    val name = map["name"] ?: ""
                    val uid = map["uid"] ?: ""
                    val imageString = map["image"] ?: ""
                    val forvarite = map["forvarite"] ?: ""
                    val bytes =
                        if (imageString.isNotEmpty()) {
                            Base64.decode(imageString, Base64.DEFAULT)
                        } else {
                            byteArrayOf()
                        }

                    val answerArrayList = ArrayList<Answer>()
                    val answerMap = map["answers"] as Map<String, String>?
                    if (answerMap != null) {
                        for (key in answerMap.keys) {
                            val temp = answerMap[key] as Map<String, String>
                            val answerBody = temp["body"] ?: ""
                            val answerName = temp["name"] ?: ""
                            val answerUid = temp["uid"] ?: ""
                            val answer = Answer(answerBody, answerName, answerUid, key)
                            answerArrayList.add(answer)
                        }
                    }

                    val question = Question(
                        title, body, name, uid, snapshot.key!!,
                        null, bytes, answerArrayList, forvarite
                    )

                    mfavoriteArraylist.add(question)
                    Log.d("favoritelistview", "addlist")
                    mAdapter.notifyDataSetChanged()


                }

                override fun onCancelled(firebaseError: DatabaseError) {}




    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        title="お気に入り"
        setContentView(R.layout.activity_favorite)
        mDatabaseReference = FirebaseDatabase.getInstance().reference
        // ListViewの準備
        mAdapter = favoriteListAdapter(this)
        mfavoriteArraylist = ArrayList<Question>()
        mAdapter.notifyDataSetChanged()



        favoritelistView.setOnItemClickListener{parent, view, position, id ->
            // Questionのインスタンスを渡して質問詳細画面を起動する

            val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
            intent.putExtra("question", mfavoriteArraylist[position])
            startActivity(intent)
        }








    }

    override fun onResume() {
        super.onResume()
        val user = FirebaseAuth.getInstance().currentUser
       mfavoriteArraylist.clear()
        mAdapter.setFavoriteArrayList(mfavoriteArraylist)
        mfavoriteRef=mDatabaseReference.child(forvarite).child(user!!.uid)
        favoritelistView.adapter = mAdapter
        mfavoriteRef!!.addChildEventListener(mEventListener)


    }


}