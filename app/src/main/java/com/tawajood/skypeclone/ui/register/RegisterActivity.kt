package com.tawajood.skypeclone.ui.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.tawajood.skypeclone.R
import com.tawajood.skypeclone.databinding.ActivityRegisterBinding
import com.tawajood.skypeclone.ui.main.MainActivity
import java.util.concurrent.TimeUnit

class RegisterActivity : AppCompatActivity() {

    companion object{
        private const val TAG = "RegisterActivity"
    }

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var credential: PhoneAuthCredential
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    private var storedVerificationId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        checkLogin()
        setupUI()
        onClick()
    }

    private fun checkLogin(){
        if (auth.currentUser != null){
            gotoMain()
        }
    }

    private fun setupUI(){
        Glide.with(this)
            .load(R.drawable.register)
            .into(binding.bgImage)
    }

    private fun onClick(){
        binding.continueBtn.setOnClickListener {
            if (validate()){
                binding.progress.visibility = View.VISIBLE
                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber("+${binding.ccp.selectedCountryCode}${binding.phoneEt.text}")// Phone number to verify
                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                    .setActivity(this)       // Activity (for callback binding)
                    .setCallbacks(callback)          // OnVerificationStateChangedCallbacks
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)
            }
        }

        binding.submitBtn.setOnClickListener {
            if (TextUtils.isEmpty(binding.codeEt.text)){
                binding.codeEt.error = "This Field is Requered"
            }else{
                val code = binding.codeEt.text.toString()
                credential = PhoneAuthProvider.getCredential(storedVerificationId, code)
                signInWithPhoneAuthCredential(credential)
            }
        }
    }

    private fun validate(): Boolean{
        if (TextUtils.isEmpty(binding.phoneEt.text)){
            binding.phoneEt.error = "This Field is Required"
            return false
        }

        return true
    }

    private val callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.d(TAG, "onVerificationCompleted:$credential")
            signInWithPhoneAuthCredential(credential)
        }


        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.w(TAG, "onVerificationFailed", e)
            binding.progress.visibility = View.GONE

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                Toast.makeText(this@RegisterActivity, e.message.toString(), Toast.LENGTH_SHORT).show()
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                Toast.makeText(this@RegisterActivity, e.message.toString(), Toast.LENGTH_SHORT).show()
            }

            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            binding.progress.visibility = View.GONE
            binding.continueBtn.visibility = View.GONE
            binding.codeEt.visibility = View.VISIBLE
            binding.submitBtn.visibility = View.VISIBLE
            Toast.makeText(this@RegisterActivity, "Code Sent Successfully", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "onCodeSent:$verificationId")

            // Save verification ID and resending token so we can use them later
            storedVerificationId = verificationId
            resendToken = token
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                binding.progress.visibility = View.GONE
                if (task.isSuccessful) {
                    task.result?.user
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    gotoMain()
                } else {
                    val msg = task.exception.toString()
                    Toast.makeText(this@RegisterActivity, msg, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun gotoMain(){
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK
                    or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

}
