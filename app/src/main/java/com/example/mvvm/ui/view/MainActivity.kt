package com.example.mvvm.ui.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import com.example.mvvm.databinding.ActivityMainBinding
import com.example.mvvm.ui.viewmodel.QuoteViewModel
import com.example.mvvm.ui.viewmodel.ScannerViewModel
import com.google.android.material.textfield.TextInputEditText
import com.keyence.autoid.sdk.scan.DecodeResult
import com.keyence.autoid.sdk.scan.ScanManager
import com.keyence.autoid.sdk.scan.scanparams.CodeType
import com.keyence.autoid.sdk.scan.scanparams.DataOutput
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ScanManager.DataListener {

    private lateinit var _scanManager: ScanManager

    private lateinit var binding: ActivityMainBinding

    private val quoteViewModel: QuoteViewModel by viewModels()
    private val scannerViewModel: ScannerViewModel by viewModels()

    private val dataOutput = DataOutput()
    private var _defaultKeyStrokeEnabled = true
    private val _defaultCodeType = CodeType()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        quoteViewModel.onCreate()

        initComponents()
        initListeners()
        initObservers()

    }

    private fun initComponents() {
        _scanManager = ScanManager.createScanManager(this)
        _defaultCodeType.setDefault()
    }

    private fun initListeners() {
        binding.btnPress.setOnClickListener { quoteViewModel.randomQuote() }
        binding.textInputScan.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                _scanManager.unlockScanner()
            } else {
                _scanManager.lockScanner()
            }
        }
        binding.textInputScan.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Ejecutar la función cuando el usuario presiona "Enter"
                scannerViewModel.onTextInputEditTextValueChanged(binding.textInputScan.text.toString())
            }
            true
        }
    }

    private fun initObservers() {
        quoteViewModel.quoteModel.observe(this, Observer { currentQuote ->
            binding.tvQuote.text = currentQuote.quote
            binding.tvAuthor.text = currentQuote.author
        })

        quoteViewModel.isLoading.observe(this, Observer {
            binding.loading.isVisible = it
        })

        scannerViewModel.textInputEditTextValue.observe(this, Observer {
            binding.tvScannerText.text = it
        })
    }

    override fun onResume() {
        super.onResume()
        configScanner()
        _scanManager.addDataListener(this)
    }

    override fun onPause() {
        super.onPause()
        _scanManager.unlockScanner()
        _scanManager.setConfig(_defaultCodeType)
        dataOutput.keyStrokeOutput.enabled = _defaultKeyStrokeEnabled
        _scanManager.setConfig(dataOutput)
        _scanManager.removeDataListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        _scanManager.releaseScanManager()
    }

    private fun configScanner() {
        _scanManager.getConfig(dataOutput)
        _defaultKeyStrokeEnabled = dataOutput.keyStrokeOutput.enabled
        dataOutput.keyStrokeOutput.enabled = false
        _scanManager.setConfig(dataOutput)
        _scanManager.lockScanner()
    }

    override fun onDataReceived(decodeResult: DecodeResult) {
        val view = currentFocus
        val result = decodeResult.result
        if (view is TextInputEditText && decodeResult.result == DecodeResult.Result.SUCCESS) {
            view.setText(decodeResult.data)
            scannerViewModel.onTextInputEditTextValueChanged(decodeResult.data)
        }
    }

    private fun showToast(text: String) {
        val duration = Toast.LENGTH_SHORT
        val toast = Toast.makeText(this, text, duration)
        toast.show()
    }
}