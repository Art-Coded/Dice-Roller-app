package com.example.diceroller


import android.os.Bundle
import android.media.MediaPlayer
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var rollTextView: TextView
    private lateinit var imageView: ImageView
    private lateinit var resultTextView: TextView
    private lateinit var playAgainButton: TextView
    private lateinit var mediaPlayer: MediaPlayer
    private var previousRoll = -1
    private var isRolling = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) 
        initViews(savedInstanceState)
        mediaPlayer = MediaPlayer.create(this, R.raw.audio)
    }

    private fun initViews(savedInstanceState: Bundle?) {
        rollTextView = findViewById(R.id.rollTextView)
        imageView = findViewById(R.id.imageView)
        resultTextView = findViewById(R.id.resultTextView)
        playAgainButton = findViewById(R.id.rollTextView2)

        rollTextView.visibility = TextView.VISIBLE
        playAgainButton.visibility = TextView.GONE

        rollTextView.setOnClickListener {
            showBottomSheetDialog()
            rollTextView.visibility = TextView.GONE
        }

        playAgainButton.setOnClickListener {
            resultTextView.visibility = TextView.GONE
            playAgainButton.visibility = TextView.GONE
            showBottomSheetDialog()
        }

        savedInstanceState?.let { state ->
            previousRoll = state.getInt("previousRoll", -1)
            resultTextView.text = state.getString("resultMessage", "")
            resultTextView.visibility = state.getInt("visibilityResult", TextView.GONE)
            playAgainButton.visibility = state.getInt("visibilityPlayAgain", TextView.GONE)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("previousRoll", previousRoll)
        outState.putString("resultMessage", resultTextView.text.toString())
        outState.putInt("visibilityResult", resultTextView.visibility)
        outState.putInt("visibilityPlayAgain", playAgainButton.visibility)
    }

    private fun showBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet, null)

        val startButton: Button = bottomSheetView.findViewById(R.id.buttonStartRoll)
        val userRollInput: EditText = bottomSheetView.findViewById(R.id.editTextUserRoll)

        startButton.setOnClickListener {
            val userRoll = userRollInput.text.toString().toIntOrNull()
            if (userRoll == null || userRoll !in 1..6) {
                Toast.makeText(this, "Please enter a valid number between 1 and 6", Toast.LENGTH_SHORT).show()
            } else {
                bottomSheetDialog.dismiss()
                rollDiceMultipleTimes(6, 1000L, userRoll)
            }
        }

        bottomSheetDialog.setOnDismissListener {
            if (!isRolling) {
                rollTextView.visibility = TextView.VISIBLE
            }
        }

        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }

    private fun rollDiceMultipleTimes(rolls: Int, delayMillis: Long, userBet: Int) {
        rollTextView.visibility = TextView.GONE
        playAgainButton.visibility = TextView.GONE
        isRolling = true

        CoroutineScope(Dispatchers.Main).launch {
            var finalRoll = -1

            repeat(rolls) {
                val diceRoll = rollDice()
                finalRoll = diceRoll
                delay(delayMillis)
            }

            isRolling = false

            val message = if (finalRoll == userBet) {
                "You WON! The dice landed on $finalRoll"
            } else {
                "You LOST. The dice landed on $finalRoll"
            }

            resultTextView.text = message
            resultTextView.visibility = TextView.VISIBLE
            playAgainButton.visibility = TextView.VISIBLE
        }
    }

    private fun rollDice(): Int {
        var n: Int

        do {
            n = (1..6).random()
        } while (n == previousRoll)

        previousRoll = n

        imageView.setImageResource(
            when (n) {
                1 -> R.drawable.dice_1
                2 -> R.drawable.dice_2
                3 -> R.drawable.dice_3
                4 -> R.drawable.dice_4
                5 -> R.drawable.dice_5
                6 -> R.drawable.dice_6
                else -> throw Exception("Invalid dice roll")
            }
        )

        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
        mediaPlayer = MediaPlayer.create(this, R.raw.audio)
        mediaPlayer.start()

        return n
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
    }
}
