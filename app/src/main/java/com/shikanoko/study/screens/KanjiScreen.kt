package com.shikanoko.study.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.shikanoko.study.Word
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanjiScreen() {
    Surface (color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxSize()
    ) {
        val context = LocalContext.current
        val padding = 8.dp

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("http://*:30000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService: ApiService = retrofit.create(ApiService::class.java)
        var kanji_list by remember {
            mutableStateOf<List<Kanji>>(emptyList())
        }

        fun fetchItems() {
            apiService.kanji.enqueue(object : Callback<List<Kanji>> {
                override fun onResponse(call: Call<List<Kanji>>, response: Response<List<Kanji>>) {
                    if (response.isSuccessful) {
                        val kanji = response.body()
                        if (kanji != null) {
                            kanji_list = kanji
                        }
                        // Обработка полученных данных, например, обновление UI
                    } else {
                        Log.println(Log.ERROR, "Retrofit", response.message())
                    }
                }

                override fun onFailure(call: Call<List<Kanji>>, t: Throwable) {
                    Log.println(Log.ERROR, "Retrofit_error", t.message.toString())
                }
            })
        }

        Column (
            Modifier
                .padding(top = 40.dp)
                .padding(padding)) {
            Button(onClick = {
                fetchItems()
            }) {
                Text(text = "Get all kanji")
            }

            LazyColumn{
                itemsIndexed(kanji_list) { _, it ->
                    val dismissState = rememberSwipeToDismissBoxState()
                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromEndToStart = true,
                        enableDismissFromStartToEnd = false,
                        backgroundContent = {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.error)
                                    .padding(12.dp, 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            )
                            {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "delete"
                                )
                            }
                        },
                    ) {
                        OutlinedCard(shape = RectangleShape) {
                            ListItem(headlineContent = { Text(text = it.literal) },
                                supportingContent = { Text(text = it.meaning) })
                        }
                    }
                }
            }
        }

    }
}

interface ApiService {
    @get:GET("kanji")
    val kanji: Call<List<Kanji>>

    @GET("kanji/{kanji_id}")
    fun getKanji(@Path("id") kanji_id: Int): Call<Kanji>

    @POST("kanji")
    fun addKanji(@Body newItem: Kanji?): Call<ResponseBody?>?

    @DELETE("kanji/{kanji_id}")
    fun deleteKanji(@Path("id") kanji_id: Int): Call<ResponseBody?>?
}



data class Kanji(val id: Int, val literal: String, val jlpt_level: Int, val ja_on: String, val ja_kun: String, val meaning: String)