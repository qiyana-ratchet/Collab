package com.example.collab

import PersonalCalendarAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.collab.databinding.ActivityDashBoardBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_dash_board.*
import kotlinx.android.synthetic.main.plan_row.view.*
import kotlinx.android.synthetic.main.work_row.view.*

class DashBoardActivity : AppCompatActivity() {
    lateinit var binding: ActivityDashBoardBinding
    var context = this
    var firestore : FirebaseFirestore?= null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initLayout()
    }

    private fun initLayout() {
            var teamName = intent.getStringExtra("teamName")

        binding.apply{
            projectName.text = teamName

            progressLayout.setOnClickListener {
                Intent(this@DashBoardActivity,WorkActivity::class.java).apply{
                    putExtra("teamName", teamName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.run{startActivity(this)}
            }
            teamSetting.setOnClickListener{
                Intent(this@DashBoardActivity,ManageTeamActivity::class.java).apply{
                    putExtra("teamName", teamName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.run{startActivity(this)}
            }

            teamPlanAddBtn.setOnClickListener {
                Intent(this@DashBoardActivity,TeamCalendarActivity::class.java).apply{
                    putExtra("teamName",teamName)
                }.run{startActivity(this)}
            }

            val items = ArrayList<CalendarData>()
            teamPlanRecyclerView.adapter = PersonalCalendarAdapter(items)
            teamPlanRecyclerView.layoutManager = LinearLayoutManager(context)
            firestore = FirebaseFirestore.getInstance()
            firestore?.collection("Team")
                ?.document(teamName!!)
                ?.addSnapshotListener { value, error ->
                    Log.i("data", value?.data.toString())

                    items.clear()
                    if(value?.contains("plans")==true){
                        val list = value?.get("plans") as ArrayList<String>
                        for(str in list!!){
                            val container = str.split("!")
                            items.add(CalendarData(
                                    container[0],
                                    container[1].split("/")[0],
                                    container[1].split("/")[1],
                                    container[2].split("/")[0],
                                    container[2].split("/")[1])) } }
                    teamPlanRecyclerView.adapter?.notifyDataSetChanged()
                    if(value?.contains("todoList")==true){
                        val todoList = value?.get("todoList") as ArrayList<String>
                        firestore?.collection("Team")
                            ?.document(teamName!!)
                            ?.collection("info")
                            ?.document("todoList")
                            ?.addSnapshotListener { value2, error ->
                                var progress = 0
                                var progress_count = 0
                                if(value2?.exists()==true){
                                    for(todo in todoList){
                                        if(value2?.contains(todo+"_progress")){
                                            progress += (value2?.get(todo+"_progress") as Number).toInt()
                                            progress_count++
                                        }
                                    }
                                    if(progress_count == 0)
                                        progress = 0
                                    else
                                        progress /= progress_count
                                    totalProgressRateNum.text = progress.toString()
                                    totalProgressRate.progress = progress
                                }
                            }
                    }



                }





        }
    }

}