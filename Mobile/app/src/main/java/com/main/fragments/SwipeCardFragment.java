package com.main.fragments;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.group4.matchmingle.R;
import com.main.activities.ChatActivity;
import com.main.activities.FilterActivity;
import com.main.activities.NotificationActivity;
import com.main.activities.UserSessionManager;
import com.main.callbacks.OnSwipeTouchListener;
import com.main.entities.User;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SwipeCardFragment extends Fragment {
    private CardView cardView;
    private TextView userName;
    private TextView userAge;
    private TextView distance;
    private ImageView userProfileImage;
    private ImageView filterIcon;
    private TextView notiIcon;
    private TextView noItemView;
    private RelativeLayout cardWrapper;
    private List<User> users = new ArrayList<>();
    private int currentUserIndex = users.isEmpty() ? -1 : 0;
    private UserSessionManager sessionManager;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://matchmingle-3065c-default-rtdb.asia-southeast1.firebasedatabase.app/");
    String mPhoneNumber;
    private String lastUserId = null;
    public boolean isDisabled = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.swipe_cardview_fragment, container, false);
        initializeViews(view);

        filterIcon.setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), FilterActivity.class);
                startActivity(intent);
            }
        });

        notiIcon.setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), NotificationActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        setupCardSwipe();
        fetchNextUser();
        return view;
    }

    private void initializeViews(View view) {
        cardView = view.findViewById(R.id.card_view);
        cardWrapper = view.findViewById(R.id.card_wrapper);
        userName = cardView.findViewById(R.id.card_name);
        userAge = cardView.findViewById(R.id.card_age);
        distance = cardView.findViewById(R.id.card_distance);
        userProfileImage = cardView.findViewById(R.id.card_image);
        noItemView = cardView.findViewById(R.id.no_item_view);
        filterIcon = cardWrapper.findViewById(R.id.icon_filter);
        notiIcon = cardWrapper.findViewById(R.id.icon_noti);
        sessionManager = new UserSessionManager(getContext());
        mPhoneNumber = sessionManager.getUserDetails().get(UserSessionManager.KEY_PHONE_NUMBER);
    }

    private void displayUser(User user) {
        if (user != null) {
            userName.setText(user.getFullname());
            userAge.setText(user.getDate());
            distance.setText("345 miles"); // Example distance
            Glide.with(getContext()).load(user.getImageUrl()).into(userProfileImage);
            cardView.setTranslationX(0);
            cardView.setAlpha(1);
            cardView.setRotation(0);
        }
    }

    private void setupCardSwipe() {
        cardView.setOnTouchListener(new OnSwipeTouchListener(getContext()) {
            @Override
            public void onSwipeLeft() {
                simulateSwipeLeft();
            }

            @Override
            public void onSwipeRight() {
                simulateSwipeRight();
            }
        });
    }

    public void simulateSwipeRight() {
        cardView.animate()
                .translationX(1000)
                .rotation(40)
                .alpha(0)
                .setDuration(500)
                .withEndAction(() -> {
                    addUserToOneWayMatchList(lastUserId);
                    fetchNextUser();
                });
    }

    public void simulateSwipeLeft() {
        cardView.animate()
                .translationX(-1000)
                .rotation(-40)
                .alpha(0)
                .setDuration(500)
                .withEndAction(() -> {
                    removeUserFromSuggestions(lastUserId);
                    fetchNextUser();
                });
    }

    private void fetchNextUser() {
        DatabaseReference usersRef = databaseReference.child("SuggestionList").child(mPhoneNumber);
        Query query = (lastUserId == null) ? usersRef.orderByKey().limitToFirst(1) : usersRef.orderByKey().startAfter(lastUserId).limitToFirst(1);
//        Query query = usersRef.orderByKey().limitToFirst(1);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        lastUserId = userSnapshot.getKey(); // Update the last fetched user ID
                        String dbFullname = userSnapshot.child("fullname").getValue(String.class);
                        String dbDate = userSnapshot.child("date").getValue(String.class);
                        String dbGender = userSnapshot.child("gender").getValue(String.class);
                        String dbImageUrl = userSnapshot.child("imageUrl").getValue(String.class);
                        User user = new User(dbFullname, dbGender, dbDate, dbImageUrl);
                        Log.d("useraaaaaaa", dbFullname + " " + dbDate + " " + dbImageUrl);
                        displayUser(user);
                        break; // Break after the first user since we are only fetching one
                    }
                    if (noItemView.getVisibility() == View.VISIBLE) {
                        noItemView.setVisibility(View.GONE);
                    }
                } else {
                    isDisabled = true;
                    cardView.setTranslationX(0);
                    cardView.setAlpha(1);
                    cardView.setRotation(0);
                    Log.d(TAG, "No more users to fetch");
                    // Handle the situation when there are no more users
                    if (noItemView != null) {
                        noItemView.setVisibility(View.VISIBLE); // Set noItemView to visible when no users are fetched
                    }
                    disableSwipe();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to fetch user", databaseError.toException());
            }

        });
    }

    private void removeUserFromSuggestions(String userId) {
        if (userId != null) {
            DatabaseReference userRef = databaseReference.child("SuggestionList").child(mPhoneNumber).child(userId);
            userRef.removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "User removed from suggestions: " + userId);
                } else {
                    Log.e(TAG, "Failed to remove user from suggestions", task.getException());
                }
            });
        }
    }

    private void addUserToOneWayMatchList(String userId) {
        DatabaseReference userInfo = databaseReference.child("User").child(userId);

        userInfo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Assuming all the necessary user fields are direct children of this snapshot
                String userFullname = snapshot.child("fullname").getValue(String.class);
                String userDate = snapshot.child("date").getValue(String.class);
                String userGender = snapshot.child("gender").getValue(String.class);
                String userImageUrl = snapshot.child("imageUrl").getValue(String.class);

                // User for oneway match list
                HashMap<String, Object> user = new HashMap<>();
                user.put("fullname", userFullname);
                user.put("date", userDate);
                user.put("gender", userGender);
                user.put("imageUrl", userImageUrl);
                databaseReference.child("OneWayMatchesList").child(mPhoneNumber).child(userId).setValue(user)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Remove user from suggestions once added to OneWayMatchList
                                removeUserFromSuggestions(userId);
                            }
                        });
                databaseReference.child("OneWayMatchesList").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String otherPhone = userSnapshot.getKey();
                            if (otherPhone.equals(mPhoneNumber)) {
                                HashMap<String, Object> otherInfo = new HashMap<>();

                                otherInfo.put("name", userFullname);
                                otherInfo.put("age", userDate);
                                otherInfo.put("pic", userImageUrl);
                                databaseReference.child("User").child(mPhoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        HashMap<String, Object> myInfo = new HashMap<>();
                                        String myFullname = snapshot.child("fullname").getValue(String.class);
                                        String myDate = snapshot.child("date").getValue(String.class);
                                        String myImageUrl = snapshot.child("imageUrl").getValue(String.class);
                                        myInfo.put("name", myFullname);
                                        myInfo.put("age", myDate);
                                        myInfo.put("pic", myImageUrl);

                                        databaseReference.child("Matches").child(userId).child(mPhoneNumber).setValue(myInfo);
                                        addMatchUserToMessage(mPhoneNumber, userId);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                                databaseReference.child("Matches").child(mPhoneNumber).child(userId).setValue(otherInfo);
                                addMatchUserToMessage(userId, mPhoneNumber);
                                addThongBao(userId, mPhoneNumber);
                                addThongBao(mPhoneNumber,userId);
                                sendNotification(userId);


                                // Remove other user and you out of OneWayMatchList of each others
                                DatabaseReference removeRef1 = databaseReference.child("OneWayMatchesList").child(userId).child(mPhoneNumber);
                                DatabaseReference removeRef2 = databaseReference.child("OneWayMatchesList").child(mPhoneNumber).child(userId);





                                removeRef1.removeValue().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "User removed from oneway list: " + userId);
                                    } else {
                                        Log.e(TAG, "Failed to remove user from oneway list", task.getException());
                                    }
                                });

                                removeRef2.removeValue().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "User removed from oneway list: " + mPhoneNumber);
                                    } else {
                                        Log.e(TAG, "Failed to remove user from oneway list", task.getException());
                                    }
                                });
                                removeUserFromSuggestions(userId);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to fetch user info", error.toException());
            }
        });
    }

    public void disableSwipe() {
        cardView.setOnTouchListener(null); // Disable swipe by setting touch listener to null
    }

    private void addMatchUserToMessage(String user1, String user2) {
        DatabaseReference matchRef = databaseReference.child("Matches");
        matchRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String fullname = snapshot.child(user1).child(user2).child("name").getValue(String.class);
                String imageUrl = snapshot.child(user1).child(user2).child("pic").getValue(String.class);
                String myChatBgColor = "purple";

                databaseReference.child("Message").child(user1).child(user2).child("fullname").setValue(fullname);
                databaseReference.child("Message").child(user1).child(user2).child("imageUrl").setValue(imageUrl);
                databaseReference.child("Message").child(user1).child(user2).child("myChatBgColor").setValue(myChatBgColor);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void addThongBao(String user1,String user2)
    {
        FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance("https://matchmingle-3065c-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference databaseReference_chat = firebaseDatabase.getReference("Notification/"+user1);
        databaseReference_chat.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Date currentDate = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE hh:mm a MMM yyyy", Locale.getDefault());
                String time=dateFormat.format(currentDate);
                DatabaseReference newSubscriptionRef = databaseReference_chat.child(time);
                Map<String, Object> newSubscriptionValues = new HashMap<>();
                newSubscriptionValues.put("Description","You've just matched with "+user2);
                newSubscriptionValues.put("Type", "Message");
                newSubscriptionValues.put("Time", time);
                newSubscriptionValues.put("UserId", user2);
                newSubscriptionRef.setValue(newSubscriptionValues);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý khi có lỗi xảy ra
                System.out.println("Error: " + databaseError.getMessage());
            }
        });



    }
//    private void fetchUsers() {
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        String phoneNumber = sessionManager.getUserDetails().get(UserSessionManager.KEY_PHONE_NUMBER);
//        mPhoneNumber = phoneNumber;
//        DatabaseReference usersRef = database.getReference("SuggestionList").child(phoneNumber);
//
//        Query query = usersRef.limitToFirst(20);
//        query.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                List<User> fetchedUsers = new ArrayList<>();
//                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
//                    String account = userSnapshot.child(phoneNumber).getKey();
//                    if (!account.equals(userSnapshot.getKey())) {
//                        String dbFullname = userSnapshot.child("fullname").getValue(String.class);
//                        String dbDate = userSnapshot.child("date").getValue(String.class);
//                        String dbGender = userSnapshot.child("gender").getValue(String.class);
//                        String dbImageUrl = userSnapshot.child("imageUrl").getValue(String.class);
//                        User user = new User(dbFullname, dbGender, dbDate, dbImageUrl);
//                        fetchedUsers.add(user);
//                    }
//                }
//                if (users.isEmpty()) {
//                    users.clear();
//                    users.addAll(fetchedUsers);
//                    currentUserIndex = 0; // Reset index
//                    displayUser(users.get(currentUserIndex));
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                // Khi có lỗi, xử lý lỗi tại đây
//                Log.e("FetchUsers", "Failed to read user", databaseError.toException());
//            }
//        });
//    }

    void sendNotification(String guestPhone) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("User").child(guestPhone);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String token = snapshot.child("token").getValue(String.class);
                try {
                    JSONObject jsonObject = new JSONObject();

                    JSONObject notificationObj = new JSONObject();
                    notificationObj.put("title", "You've just match with someone!");
                    notificationObj.put("body", "Click to see.");

                    JSONObject dataObj = new JSONObject();
                    dataObj.put("userId", guestPhone);
                    jsonObject.put("notification", notificationObj);
                    jsonObject.put("data", dataObj);
                    jsonObject.put("to", token);

                    callApi(jsonObject);
                } catch (Exception e) {
                    Log.e("SendNotification", "Error sending notification: " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    void callApi(JSONObject jsonObject){
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        String url = "https://fcm.googleapis.com/fcm/send";
        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization","Bearer AAAAycOVJIU:APA91bGhJQOYm5KLWUi5G2B75tOLcN172hvPohzuS1CMVWLxr0pFOOH0EhVvX-OKPHFp7ZlFUD06ITrpdnmO6TJyv73-5kTZ4ANSOm_s-SwxLcf3O1hL1w5eM2w6-We4i1-FC13MbuwY")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

            }
        });
    }
}

/*
qweqw
qweqwwqe
        qwe
qweq
                w
qe

 */