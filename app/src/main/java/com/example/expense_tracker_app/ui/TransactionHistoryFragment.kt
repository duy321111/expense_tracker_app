// Thêm vào app/src/main/java/com/example/expense_tracker_app/ui/TransactionHistoryFragment.kt
package com.example.expense_tracker_app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.expense_tracker_app.R

class TransactionHistoryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout cho fragment này
        return inflater.inflate(R.layout.fragment_transaction_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: Khởi tạo RecyclerView (rv_transactions) và Adapter của nó
        // TODO: Thêm logic cho (btn_back)
    }
}