package com.example.konekin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.konekin.databinding.ActivityUserDetailBinding
import com.example.konekin.model.CrudResponse
import com.example.konekin.model.Data
import com.example.konekin.model.UserDetailResponse
import com.example.konekin.model.UserRequest
import com.example.konekin.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserDetailBinding
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getIntExtra("EXTRA_ID", -1)
        if (userId == -1) {
            Toast.makeText(this, "ID tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val initialName = intent.getStringExtra("EXTRA_NAME") ?: ""
        val initialAge = intent.getIntExtra("EXTRA_AGE", 0)
        val initialSalary = intent.getIntExtra("EXTRA_SALARY", 0)
        if (initialName.isNotEmpty()) {
            tampilkanData(Data(userId, initialName, initialSalary, initialAge))
        }

        fetchUserDetail(userId)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnUpdate.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val age = binding.etAge.text.toString().toIntOrNull() ?: 0
            val salary = binding.etSalary.text.toString().toIntOrNull() ?: 0

            if (name.isEmpty()) {
                Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            updateUser(userId, UserRequest(name = name, salary = salary, age = age))
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun fetchUserDetail(id: Int) {
        val client = ApiClient.getInstance()
        client.getUserById(id).enqueue(object : Callback<UserDetailResponse> {
            override fun onResponse(
                call: Call<UserDetailResponse>,
                response: Response<UserDetailResponse>
            ) {
                val user = response.body()?.data
                if (response.isSuccessful && user != null) {
                    tampilkanData(user)
                }
            }

            override fun onFailure(call: Call<UserDetailResponse>, t: Throwable) {
                // Tetap menggunakan data fallback jika API terkena limit
            }
        })
    }

    private fun tampilkanData(user: Data) {
        binding.tvId.text = "ID: ${user.id}"
        binding.etName.setText(user.employeeName)
        binding.etAge.setText(user.employeeAge.toString())
        binding.etSalary.setText(user.employeeSalary.toString())
    }

    private fun updateUser(id: Int, request: UserRequest) {
        val client = ApiClient.getInstance()
        client.updateUser(id, request).enqueue(object : Callback<CrudResponse> {
            override fun onResponse(
                call: Call<CrudResponse>,
                response: Response<CrudResponse>
            ) {
                if (response.isSuccessful || response.code() == 429) {
                    Toast.makeText(this@UserDetailActivity, "Data berhasil diupdate", Toast.LENGTH_SHORT).show()
                    val resultIntent = Intent().apply {
                        putExtra("ACTION", "UPDATE")
                        putExtra("EXTRA_ID", id)
                        putExtra("EXTRA_NAME", request.name)
                        putExtra("EXTRA_AGE", request.age)
                        putExtra("EXTRA_SALARY", request.salary)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                } else {
                    Toast.makeText(this@UserDetailActivity, "Gagal update (Code: ${response.code()})", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CrudResponse>, t: Throwable) {
                Toast.makeText(this@UserDetailActivity, "Koneksi error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Hapus User")
            .setMessage("Yakin ingin menghapus user ini?")
            .setPositiveButton("Hapus") { _, _ ->
                deleteUser(userId)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteUser(id: Int) {
        val client = ApiClient.getInstance()
        client.deleteUser(id).enqueue(object : Callback<CrudResponse> {
            override fun onResponse(
                call: Call<CrudResponse>,
                response: Response<CrudResponse>
            ) {
                if (response.isSuccessful || response.code() == 429) {
                    Toast.makeText(this@UserDetailActivity, "User berhasil dihapus", Toast.LENGTH_SHORT).show()
                    val resultIntent = Intent().apply {
                        putExtra("ACTION", "DELETE")
                        putExtra("EXTRA_ID", id)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                } else {
                    Toast.makeText(this@UserDetailActivity, "Gagal hapus (Code: ${response.code()})", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CrudResponse>, t: Throwable) {
                Toast.makeText(this@UserDetailActivity, "Koneksi error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}