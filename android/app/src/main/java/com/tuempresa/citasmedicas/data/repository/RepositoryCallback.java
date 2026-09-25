package com.tuempresa.citasmedicas.data.repository;

/**
 * Callback simple para operaciones asíncronas de los repositorios.
 */
public interface RepositoryCallback<T> {

    void onSuccess(T data);

    void onError(String message);
}
