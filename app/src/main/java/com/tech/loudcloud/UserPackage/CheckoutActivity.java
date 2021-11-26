package com.tech.loudcloud.UserPackage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.stripe.android.ApiResultCallback;
import com.stripe.android.PaymentIntentResult;
import com.stripe.android.Stripe;
import com.stripe.android.model.ConfirmPaymentIntentParams;
import com.stripe.android.model.PaymentIntent;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.view.CardInputWidget;
import com.tech.loudcloud.R;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CheckoutActivity extends AppCompatActivity implements View.OnClickListener {
    // 10.0.2.2 is the Android emulator's alias to localhost
    private static final String BACKEND_URL = "https://loudcloud-gateway.herokuapp.com/";
    //private static final String BACKEND_URL = "http://192.168.43.20:5001/";

    private OkHttpClient httpClient = new OkHttpClient();
    private String paymentIntentClientSecret = null;
    private Stripe stripe;

    static double amount;
    double finalAmount;

    static FirebaseFirestore firebaseFirestore;
    static FirebaseUser user;

    static double totalVotes;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent in = getIntent();
        amount = in.getIntExtra("amount", 5);
        totalVotes = in.getDoubleExtra("TotalVotes", 0);

        finalAmount = amount*1000;

        user = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        // Configure the SDK with your Stripe publishable key so it can make requests to Stripe
        stripe = new Stripe(
                getApplicationContext(),
                Objects.requireNonNull("pk_test_51IYMuqHzo23AUdYs1aNio4NYJVNtYnCHUnKZtAki9zOX1sC8srIztVNDWVYSYN3iCgm4PcafJqjCtRaAOXEdGxO000lpyKwKoB")
        );

        // Create a PaymentIntent by calling the server's endpoint.
        MediaType mediaType = MediaType.get("application/json; charset=utf-8");

        Map<String,Object> payMap = new HashMap<>();
        Map<String,Object> itemMap = new HashMap<>();
        List<Map<String,Object>> itemList = new ArrayList<>();

        payMap.put("currency", "usd");

        itemMap.put("id", "photo_subscription");
        itemMap.put("amount",finalAmount);

        itemList.add(itemMap);
        payMap.put("items", itemList);
        String json = new Gson().toJson(payMap);

        RequestBody body = RequestBody.create(json, mediaType);
        Request request = new Request.Builder()
                .url(BACKEND_URL + "create-payment-intent")
                .post(body)
                .build();
        httpClient.newCall(request)
                .enqueue(new PayCallback(this));

        // Hook up the pay button to the card widget and stripe instance
        Button payButton = findViewById(R.id.payButton);
        payButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        CardInputWidget cardInputWidget = findViewById(R.id.cardInputWidget);
        PaymentMethodCreateParams params = cardInputWidget.getPaymentMethodCreateParams();
        if (params != null && paymentIntentClientSecret!=null) {

            ConfirmPaymentIntentParams confirmParams = ConfirmPaymentIntentParams
                    .createWithPaymentMethodCreateParams(params, paymentIntentClientSecret);
            if(confirmParams!=null) {
                stripe.confirmPayment(this, confirmParams);
            }
        }
    }

    private void displayAlert(@NonNull String title,
                              @Nullable String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message);

        builder.setPositiveButton("Ok", null);
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle the result of stripe.confirmPayment
        stripe.onPaymentResult(requestCode, data, new PaymentResultCallback(this));
    }

    private void onPaymentSuccess(@NonNull final Response response) throws IOException {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        Map<String, String> responseMap = gson.fromJson(
                Objects.requireNonNull(response.body()).string(),
                type
        );

        paymentIntentClientSecret = responseMap.get("clientSecret");
    }



    private static final class PayCallback implements Callback {
        @NonNull private final WeakReference<CheckoutActivity> activityRef;

        PayCallback(@NonNull CheckoutActivity activity) {
            activityRef = new WeakReference<>(activity);
        }

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            final CheckoutActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }

            activity.runOnUiThread(() ->
                    Toast.makeText(
                            activity, "Error: " + e.toString(), Toast.LENGTH_LONG
                    ).show()
            );
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull final Response response)
                throws IOException {
            final CheckoutActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }

            if (!response.isSuccessful()) {
                activity.runOnUiThread(() ->
                        Toast.makeText(
                                activity, "Error: " + response.toString(), Toast.LENGTH_LONG
                        ).show()

                );
            } else {
                activity.onPaymentSuccess(response);
            }
        }
    }

    private static final class PaymentResultCallback
            implements ApiResultCallback<PaymentIntentResult> {
        @NonNull private final WeakReference<CheckoutActivity> activityRef;

        PaymentResultCallback(@NonNull CheckoutActivity activity) {
            activityRef = new WeakReference<>(activity);
        }

        @Override
        public void onSuccess(@NonNull PaymentIntentResult result) {
            final CheckoutActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }

            PaymentIntent paymentIntent = result.getIntent();
            PaymentIntent.Status status = paymentIntent.getStatus();
            if (status == PaymentIntent.Status.Succeeded) {
                // Payment completed successfully

                double finalVotes = totalVotes+amount;
                updateVotes(finalVotes, activity);

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                activity.displayAlert(
                        "Payment completed",
                        "Congrats, you have successfully bought votes!"
                );


            } else if (status == PaymentIntent.Status.RequiresPaymentMethod) {
                // Payment failed – allow retrying using a different payment method
                activity.displayAlert(
                        "Payment failed",
                        "Failed using this card!"
                );
            }
        }

        @Override
        public void onError(@NonNull Exception e) {
            final CheckoutActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }

            // Payment request failed – allow retrying using the same payment method
            activity.displayAlert("Error", "Something is wrong!");
        }
    }


    public static void updateVotes(double votes, CheckoutActivity activity){
        String userID = user.getUid();
        DocumentReference documentReference = firebaseFirestore.collection("user_data").document(userID);
        Map<String , Object> userr = new HashMap<>();
        userr.put("Votes", votes);

        documentReference.update(userr).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(activity, "Congrats!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
