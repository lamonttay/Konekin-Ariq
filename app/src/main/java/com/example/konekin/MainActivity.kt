package com.example.konekin

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.konekin.databinding.ActivityMainBinding
import com.example.konekin.model.CrudResponse
import com.example.konekin.model.Data
import com.example.konekin.model.UserRequest
import com.example.konekin.model.Users
import com.example.konekin.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val userDataList = ArrayList<Data>()
    private val userNames = ArrayList<String>()
    private lateinit var listAdapter: ArrayAdapter<String>

    private val detailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val data = result.data!!
            val action = data.getStringExtra("ACTION")
            val id = data.getIntExtra("EXTRA_ID", -1)

            if (action == "UPDATE") {
                val newName = data.getStringExtra("EXTRA_NAME") ?: ""
                val newAge = data.getIntExtra("EXTRA_AGE", 0)
                val newSalary = data.getIntExtra("EXTRA_SALARY", 0)

                val index = userDataList.indexOfFirst { it.id == id }
                if (index != -1) {
                    userDataList[index] = Data(id, newName, newSalary, newAge)
                    refreshListView()
                }
            } else if (action == "DELETE") {
                userDataList.removeAll { it.id == id }
                refreshListView()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listAdapter = ArrayAdapter(
            this@MainActivity,
            android.R.layout.simple_list_item_1,
            userNames
        )
        binding.lvNama.adapter = listAdapter

        loadUsers()

        binding.btnAdd.setOnClickListener {
            showAddUserDialog()
        }

        binding.lvNama.setOnItemClickListener { _, _, position, _ ->
            val selectedUser = userDataList[position]
            val intent = Intent(this@MainActivity, UserDetailActivity::class.java).apply {
                putExtra("EXTRA_ID", selectedUser.id)
                putExtra("EXTRA_NAME", selectedUser.employeeName)
                putExtra("EXTRA_AGE", selectedUser.employeeAge)
                putExtra("EXTRA_SALARY", selectedUser.employeeSalary)
            }
            detailLauncher.launch(intent)
        }
    }

    private fun refreshListView() {
        userNames.clear()
        for (user in userDataList) {
            userNames.add(user.employeeName)
        }
        listAdapter.notifyDataSetChanged()
    }

    private fun loadUsers() {
        val client = ApiClient.getInstance()
        client.getAllUsers().enqueue(object : Callback<Users> {
            override fun onResponse(call: Call<Users>, response: Response<Users>) {
                if (response.isSuccessful && response.body() != null) {
                    userDataList.clear()
                    userDataList.addAll(response.body()!!.data)
                    refreshListView()
                } else {
                    Toast.makeText(this@MainActivity, "Gagal mengambil data (Code: ${response.code()})", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Users>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Koneksi error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun showAddUserDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 30, 50, 10)
        }

        val etName = EditText(this).apply { hint = "Nama Karyawan" }
        val etAge = EditText(this).apply {
            hint = "Umur"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        val etSalary = EditText(this).apply {
            hint = "Gaji"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        layout.addView(etName)
        layout.addView(etAge)
        layout.addView(etSalary)

        AlertDialog.Builder(this)
            .setTitle("Tambah User")
            .setView(layout)
            .setPositiveButton("Simpan") { _, _ ->
                val name = etName.text.toString().trim()
                val age = etAge.text.toString().toIntOrNull() ?: 0
                val salary = etSalary.text.toString().toIntOrNull() ?: 0

                if (name.isNotEmpty()) {
                    addUser(UserRequest(name = name, salary = salary, age = age))
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun addUser(request: UserRequest) {
        val client = ApiClient.getInstance()
        client.createUser(request).enqueue(object : Callback<CrudResponse> {
            override fun onResponse(call: Call<CrudResponse>, response: Response<CrudResponse>) {
                if (response.isSuccessful || response.code() == 429) {
                    Toast.makeText(this@MainActivity, "User berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                    val newId = (userDataList.maxOfOrNull { it.id } ?: 0) + 1
                    userDataList.add(Data(newId, request.name, request.salary, request.age))
                    refreshListView()
                } else {
                    Toast.makeText(this@MainActivity, "Gagal menambah user (Code: ${response.code()})", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CrudResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Koneksi error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}