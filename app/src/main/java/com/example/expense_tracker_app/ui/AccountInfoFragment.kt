// Sửa file: app/src/main/java/com/example/expense_tracker_app/ui/AccountInfoFragment.kt
package com.example.expense_tracker_app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.expense_tracker_app.R

// --- CÁC DÒNG MỚI ĐỂ SỬA LỖI ---
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
// --- HẾT CÁC DÒNG MỚI ---

class AccountInfoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout cho fragment này
        return inflater.inflate(R.layout.fragment_account_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Tìm các View bằng ID
        val btnChangePassword = view.findViewById<Button>(R.id.btn_change_password)
        val btnSave = view.findViewById<Button>(R.id.btn_save)
        val btnBack = view.findViewById<ImageView>(R.id.btn_back)

        // 2. Gán sự kiện click cho nút "Đổi mật khẩu"
        btnChangePassword.setOnClickListener {
            // Điều hướng đến màn hình đổi mật khẩu
            findNavController().navigate(R.id.action_accountInfoFragment_to_changePasswordFragment)
        }

        // 3. Gán sự kiện click cho nút "Lưu thông tin"
        btnSave.setOnClickListener {
            // TODO: Thêm logic lưu thông tin (lấy text từ EditTexts)

            // Thông báo lưu thành công (ví dụ)
            Toast.makeText(requireContext(), "Đã lưu thông tin", Toast.LENGTH_SHORT).show()
        }

        // 4. Gán sự kiện click cho nút "Quay lại"
        btnBack.setOnClickListener {
            // Quay lại màn hình trước đó
            findNavController().popBackStack()
        }
    }
}