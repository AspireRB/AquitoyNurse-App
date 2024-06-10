package com.aspire.aquitoy.nurse.ui.home.model

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.aspire.aquitoy.nurse.R
import com.aspire.aquitoy.nurse.data.DatabaseService
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Singleton

@AndroidEntryPoint
class ButtomSheetService(private val serviceID: String) : BottomSheetDialogFragment() {
    @Inject lateinit var databaseService: DatabaseService
    private lateinit var textTitle: TextView
    private lateinit var textBody: TextView
    private lateinit var btnAccept: Button
    private lateinit var btnDecline: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_service_request, container, false)
        textTitle = view.findViewById(R.id.idName)
        textBody = view.findViewById(R.id.idTack)
        btnAccept = view.findViewById(R.id.btnAccept)
        btnDecline = view.findViewById(R.id.btnDecline)

        btnAccept.setOnClickListener { acceptService(serviceID) }
        btnDecline.setOnClickListener { declineService(serviceID) }

        return view
    }

    private fun acceptService(serviceID: String) {
        databaseService.updateStateService(serviceID, "accept").addOnCompleteListener {
            if(it.isSuccessful) {
                if (it.isSuccessful) {
                    dismissBottomSheet()
                    databaseService.initialSendHistoryService(serviceID).addOnCompleteListener {
                        if (it.isSuccessful){
                            Log.d("HistoryInitial", "OK")
                        } else {
                            Log.d("HistoryInitial", "NO OK")
                        }
                    }
                    Toast.makeText(context, "Servicio aceptado", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "No se pudo aceptar el servicio", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun declineService(serviceID: String) {
        databaseService.updateStateService(serviceID, "decline").addOnCompleteListener {
            if(it.isSuccessful) {
                databaseService.updateState("OK").addOnCompleteListener {
                    if (it.isSuccessful) {
                        dismissBottomSheet()
                        Toast.makeText(context, "Servicio cancelado", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "No se pudo cancelar el servicio", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun dismissBottomSheet() {
        Handler(Looper.getMainLooper()).post {
            dismiss()
        }
    }

    companion object {
        const val TAG = "ButtomSheet"
    }
}