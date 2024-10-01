package com.paylog.app;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AssistantFragment extends Fragment {
    DatabaseHelper myDB;
    private RecyclerView chatRecyclerView;
    private EditText inputItem;
    private Button callApi;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_assistant, container, false);
        chatRecyclerView = view.findViewById(R.id.chatRecyclerView);
        inputItem = view.findViewById(R.id.inputItem);
        callApi = view.findViewById(R.id.callapi);
        myDB = new DatabaseHelper(view.getContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRecyclerView.setAdapter(chatAdapter);

        callApi.setOnClickListener(v -> {
            String userInput = inputItem.getText().toString();
            if (!TextUtils.isEmpty(userInput)) {
                chatMessages.add(new ChatMessage(userInput, "You"));
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                inputItem.setText("");

                getBotResponse(userInput, new BotResponseCallback() {
                    @Override
                    public void onResponse(String response) {
                        // Ensure this code runs on the main UI thread
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                chatMessages.add(new ChatMessage(response, "Bot"));
                                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                                chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                            });
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e("Error", t.toString());
                    }
                });


            }
        });

        return view;
    }

    private void getBotResponse(String userInput, BotResponseCallback callback) {
        String currentDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
        String todayData = myDB.getFormattedTodayData(currentDate);

        StringBuilder longStringBuilder = new StringBuilder();
        longStringBuilder.append("You are a highly knowledgeable financial advisor, trained to assist users with their financial queries using a comprehensive table of data. ")
                .append("This table includes important details such as the date of purchase, the items purchased, the purpose of each expense, and the corresponding amounts spent. ")
                .append("Your primary role is to analyze this data to provide personalized financial advice.\n\n")
                .append("The user is based in India, and all amounts are in INR. They prefer short, summarized one-line answers unless they request further details. ")
                .append("When a user poses a question, consider their specific financial situation and the context of their expenses. ")
                .append("Offer tailored recommendations on budgeting, saving, and spending strategies while also providing insights on how past purchases can inform future financial decisions.\n\n")
                .append("Your responses should be informative, clear, and actionable, empowering users to make smarter financial choices. ")
                .append("Always reference the relevant data from the table to support your advice and ensure the information is directly applicable to the user's query. ")
                .append("Strive to enhance their understanding of personal finance and encourage positive financial habits.\n\n")
                .append("Additionally, be mindful of the cultural and economic factors unique to India, ")
                .append("and ensure your advice reflects local financial practices and considerations. ")
                .append("Maintain a friendly and supportive tone to foster trust and encourage open communication with the user.\n\n")
                .append("User's Question: ")
                .append(userInput)
                .append("\n\n")
                .append("User's Transaction Records: \n")
                .append(todayData);

        String prompt = longStringBuilder.toString().trim();
        System.out.println(prompt);

        CallGemini.getInfo(prompt, new GeminiCallback() {
            @Override
            public void onResult(String result) {
                callback.onResponse(result);  // Pass the result via the callback
            }

            @Override
            public void onError(Throwable t) {
                callback.onError(t);  // Pass the error via the callback
            }
        });
    }

}
