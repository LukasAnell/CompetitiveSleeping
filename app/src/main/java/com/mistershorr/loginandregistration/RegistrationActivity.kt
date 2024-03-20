package com.mistershorr.loginandregistration

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.mistershorr.loginandregistration.databinding.ActivityRegistrationBinding


class RegistrationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistrationBinding

    companion object {
        const val TAG = "RegistrationActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Backendless.initApp(this, Constants.APPLICATION_ID, Constants.API_KEY)

        // retrieve any information from the intent using the extras keys
        val username = intent.getStringExtra(LoginActivity.EXTRA_USERNAME) ?: ""
        val password = intent.getStringExtra(LoginActivity.EXTRA_PASSWORD) ?: ""

        // prefill the username & password fields
        // for EditTexts, you actually have to use the setText functions
        binding.editTextRegistrationUsername.setText(username)
        binding.editTextTextPassword.setText(password)

        // register an account and send back the username & password
        // to the login activity to prefill those fields
        binding.buttonRegistrationRegister.setOnClickListener {
            val password = binding.editTextTextPassword.text.toString()
            val confirm = binding.editTextRegistrationConfirmPassword.text.toString()
            val username = binding.editTextRegistrationUsername.text.toString()
            val email = binding.editTextRegistrationEmail.text.toString()
            val name = binding.editTextRegistrationName.text.toString()
            if(validateFields(email, name, username, password, confirm))  {  // && do the rest of the validations
                val user = BackendlessUser()
                user.setProperty("email", email)
                user.setProperty("name", name)
                user.setProperty("username", username)
                user.password = password

                Backendless.UserService.register(user, object: AsyncCallback<BackendlessUser?> {
                    override fun handleResponse(registeredUser: BackendlessUser?) {
                        Log.d(TAG, "handleResponse: ${registeredUser?.getProperty("username")} has registered.")

                        val resultIntent = Intent().apply {
                            putExtra(LoginActivity.EXTRA_PASSWORD, password)
                            putExtra(LoginActivity.EXTRA_USERNAME, username)
                        }
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    }

                    override fun handleFault(fault: BackendlessFault) {
                        Log.d(TAG, "handleFault: Code ${fault.code}\n${fault.detail}")
                        Toast.makeText(this@RegistrationActivity, fault.message, Toast.LENGTH_LONG).show()
                    }
                })
            }
        }
    }

    private fun validateFields(email: String, name: String, username: String, password: String, confirm: String): Boolean {
        val validateEmail = RegistrationUtil.validateEmail(email)
        val validateName = RegistrationUtil.validateName(name)
        val validateUsername = RegistrationUtil.validateUsername(username)
        val validatePassword = RegistrationUtil.validatePassword(password, confirm)

        if(!validateEmail) {
            Toast.makeText(this, "Invalid Email!", Toast.LENGTH_SHORT).show()
            return false
        }
        if(!validateName) {
            Toast.makeText(this, "Invalid Name!", Toast.LENGTH_SHORT).show()
            return false
        }
        if(!validateUsername) {
            Toast.makeText(this, "Invalid Username!", Toast.LENGTH_SHORT).show()
            return false
        }
        if(!validatePassword) {
            Toast.makeText(this, "Invalid Password!", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}