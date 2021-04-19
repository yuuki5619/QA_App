package jp.techacademy.satou.qa_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_favorite.*

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
            mfavoriteall.addListenerForSingleValueEvent(aaa)



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
    private val aaa = object : ValueEventListener {
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

        val user = FirebaseAuth.getInstance().currentUser
        mfavoriteRef=mDatabaseReference.child(forvarite).child(user!!.uid)
        mAdapter.mfavoriteArraylist
        mAdapter.setFavoriteArrayList(mfavoriteArraylist)
        favoritelistView.adapter = mAdapter
        mfavoriteRef!!.addChildEventListener(mEventListener)







    }


}