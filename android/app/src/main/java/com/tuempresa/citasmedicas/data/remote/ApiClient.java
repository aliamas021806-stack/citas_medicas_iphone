package com.tuempresa.citasmedicas.data.remote;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Cliente Retrofit (patrón Singleton).
 * <p>
 * Instala {@link MockInterceptor}, por lo que todas las peticiones devuelven
 * datos sintéticos generados por IA.
 */
public final class ApiClient {

    private static final String BASE_URL = "https://mock.citasmedicas.local/api/";

    private static Retrofit retrofit;

    private ApiClient() {
    }

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(new MockInterceptor())
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static MedicalApiService getApi() {
        return getRetrofit().create(MedicalApiService.class);
    }
}
