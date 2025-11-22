package com.example.expense_tracker_app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.expense_tracker_app.data.model.User;
import com.example.expense_tracker_app.data.repository.UserRepository;

public class LoginViewModel extends AndroidViewModel {

    public UserRepository repository;
    public MutableLiveData<Boolean> loginResult = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
    }

    public void login(String email, String password) {
        boolean success = repository.checkLogin(email, password);
        loginResult.setValue(success);
    }

}
