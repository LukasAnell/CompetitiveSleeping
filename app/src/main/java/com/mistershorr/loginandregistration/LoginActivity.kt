package com.mistershorr.loginandregistration

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.mistershorr.loginandregistration.databinding.ActivityLoginBinding
import com.mistershorr.loginandregistration.sleeping.SleepListActivity


class LoginActivity : AppCompatActivity() {

    companion object {
        const val TAG = "LoginActivity"
        // the values to send in intents are called Extras
        // and have the EXTRA_BLAH format for naming the key
        val EXTRA_USERNAME = "username"
        val EXTRA_PASSWORD = "password"
    }

    val startRegistrationForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data!!
            // Handle the Intent to do whatever we need with the returned info
            binding.editTextLoginUsername.setText(intent.getStringExtra(EXTRA_USERNAME))
            binding.editTextLoginPassword.setText(intent.getStringExtra(EXTRA_PASSWORD))
        }
    }
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "CompetitiveSleeping"
        Backendless.initApp(this, Constants.APPLICATION_ID, Constants.API_KEY)

        setListeners()
    }

    private fun setListeners() {
        // launch the Registration Activity
        binding.textViewLoginSignup.setOnClickListener {
            // 1. Create an Intent object with the current activity
            // and the destination activity's class
            val registrationIntent = Intent(this, RegistrationActivity::class.java)
            // 2. optionally add information to send with the intent
            // key-value pairs just like with Bundles
            registrationIntent.putExtra(EXTRA_USERNAME, binding.editTextLoginUsername.text.toString())
            registrationIntent.putExtra(EXTRA_PASSWORD, binding.editTextLoginPassword.text.toString())
//            // 3a. launch the new activity using the intent
//            startActivity(registrationIntent)
            // 3b. Launch the activity for a result using the variable from the
            // register for result contract above
            startRegistrationForResult.launch(registrationIntent)
        }

        binding.buttonLoginLogin.setOnClickListener {
            // do not forget to call Backendless.initApp in the app initialization code
            Backendless.UserService.login(
                binding.editTextLoginUsername.text.toString(),
                binding.editTextLoginPassword.text.toString(),
                object : AsyncCallback<BackendlessUser?> {
                    override fun handleResponse(user: BackendlessUser?) {
                        Log.d(TAG, "handleResponse: ${user?.getProperty("username")} has logged in.")
                        val sleepIntent = Intent(this@LoginActivity, SleepListActivity::class.java)
                        startActivity(sleepIntent)
                    }

                    override fun handleFault(fault: BackendlessFault) {
                        Log.d(TAG, "handleFault: ${fault.code}")
                    }
                })
        }
    }
}