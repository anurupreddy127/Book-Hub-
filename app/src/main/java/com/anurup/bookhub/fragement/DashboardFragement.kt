package com.anurup.bookhub.fragement

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.anurup.bookhub.R
import com.anurup.bookhub.adapter.DashboardRecyclerAdapter
import com.anurup.bookhub.model.Book
import com.anurup.bookhub.util.ConnectionManager
import org.json.JSONException
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class DashboardFragement : Fragment() {

    lateinit var recyclerDashboard: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var recyclerAdapter: DashboardRecyclerAdapter
    lateinit var progressLayout: RelativeLayout
    lateinit var progressBar: ProgressBar

    val bookInfoList: ArrayList<Book> = ArrayList()

    var ratingComprator = Comparator<Book>{book1, book2 ->

       if( book1.bookRating.compareTo(book2.bookRating, true) == 0) {
           book1.bookName.compareTo(book2.bookRating,true)
       } else {
           book1.bookRating.compareTo(book2.bookRating, true)
       }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard_fragement, container, false)

        setHasOptionsMenu(true)

        recyclerDashboard = view.findViewById(R.id.recycleDashboard)

        progressLayout = view.findViewById(R.id.progressLayout)

        progressBar = view.findViewById(R.id.ProgressBar)

        progressLayout.visibility = View.VISIBLE


        layoutManager = LinearLayoutManager(activity)

        val queue  = Volley.newRequestQueue(activity as Context)

        val url = "http://13.235.250.119/v1/book/fetch_books/"

        if(ConnectionManager().checkConnectivity((activity as Context))){

            val jasonObjectRequest = object: JsonObjectRequest(Request.Method.GET, url, null, Response.Listener {

                try {
                    progressLayout.visibility = View.GONE
                    val success = it.getBoolean("success")

                    if(success){

                        val data = it.getJSONArray("data")
                        for(i in 0 until data.length()){
                            val bookJsonObject = data.getJSONObject(i)
                            val bookObject = Book (
                                bookJsonObject.getString("book_id"),
                                bookJsonObject.getString("name"),
                                bookJsonObject.getString("author"),
                                bookJsonObject.getString("rating"),
                                bookJsonObject.getString("price"),
                                bookJsonObject.getString("image"),
                            )
                            bookInfoList.add(bookObject)

                            recyclerAdapter = DashboardRecyclerAdapter(activity as Context, bookInfoList)

                            recyclerDashboard.adapter = recyclerAdapter

                            recyclerDashboard.layoutManager = layoutManager

                        }
                    } else {
                        Toast.makeText(activity as Context, "Some error has occured", Toast.LENGTH_SHORT).show()
                    }
                } catch ( e: JSONException) {
                    Toast.makeText(activity as Context, "Some unexpected error has occured", Toast.LENGTH_SHORT).show()
                }

            }
                , Response.ErrorListener{

                    if(activity != null) {
                        Toast.makeText(
                            activity as Context,
                            "Volley error occured!!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }){
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "c0b3791fa83791"
                    return headers
                }
            }

            queue.add(jasonObjectRequest)


        } else {
            val dialog = AlertDialog.Builder(activity as Context)
            dialog.setTitle("Internet Connecton is not found")
            dialog.setPositiveButton("Open Settings") { text, listener ->
                val settingIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingIntent)
                activity?.finish()
            }

            dialog.setNegativeButton("Exit") { text, listener ->
                ActivityCompat.finishAffinity(activity as Activity)
            }

            dialog.create()
            dialog.show()
        }


        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater?.inflate(R.menu.menu_dashboard, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item?.itemId
        if(id == R.id.action_sort){
            Collections.sort(bookInfoList, ratingComprator)
            bookInfoList.reverse()
        }

        recyclerAdapter.notifyDataSetChanged()

        return super.onOptionsItemSelected(item)
    }
}