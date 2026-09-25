package com.tuempresa.citasmedicas.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.tuempresa.citasmedicas.data.repository.AuthRepository;
import com.tuempresa.citasmedicas.data.repository.RepositoryCallback;
import com.tuempresa.citasmedicas.model.AuthRequest;
import com.tuempresa.citasmedicas.model.User;
import com.tuempresa.citasmedicas.util.Resource;

/**
 * ViewModel de autenticación (MVVM).
 */
public class AuthViewModel extends ViewModel {

    private final AuthRepository repository = new AuthRepository();
    private final MutableLiveData<Resource<User>> authState = new MutableLiveData<>();

    public LiveData<Resource<User>> getAuthState() {
        return authState;
    }

    public void login(String email, String password) {
        authState.setValue(Resource.loading());
        repository.login(new AuthRequest(email, password), new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User data) {
                authState.setValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                authState.setValue(Resource.error(message));
            }
        });
    }

    public void register(String fullName, String email, String password, int age) {
        authState.setValue(Resource.loading());
        repository.register(new AuthRequest(fullName, email, password, age), new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User data) {
                authState.setValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                authState.setValue(Resource.error(message));
            }
        });
    }

    public void resetState() {
        authState.setValue(null);
    }
}
