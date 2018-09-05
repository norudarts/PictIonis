package etna.hyvernparede.pictionis;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.firebase.ui.database.SnapshotParser;

import de.hdodenhof.circleimageview.CircleImageView;
import etna.hyvernparede.pictionis.chat.ChatMessage;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        TextView authorTextView;
        CircleImageView authorImageView;

        public MessageViewHolder(View view) {
            super(view);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            authorTextView = itemView.findViewById(R.id.authorTextView);
            authorImageView = itemView.findViewById(R.id.authorImageView);
        }
    }

    private String username;
    private String profilePicUrl;
    private GoogleApiClient googleApiClient;

    // Activity Elements
    private Button sendButton;
    private RecyclerView messageRecyclerView;
    private LinearLayoutManager linearLayoutManager;
    private EditText messageEditText;

    // Firebase variables
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private FirebaseRecyclerAdapter<ChatMessage, MessageViewHolder> firebaseAdapter;

    // Constants
    private static final String TAG = "MainActivity";
    private static final String ANONYMOUS = "Anonymous";
    public static final String MESSAGES_CHILD = "messages";
    private static final int MESSAGE_LENGTH = 140;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username = ANONYMOUS;

        // Signing in
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            username = firebaseUser.getDisplayName();
            if (firebaseUser.getPhotoUrl() != null) {
                profilePicUrl = firebaseUser.getPhotoUrl().toString();
            }
        }

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        // Setting layouts
        messageRecyclerView = findViewById(R.id.messageRecyclerView);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messageRecyclerView.setLayoutManager(linearLayoutManager);

        // Retrieving messages from database
        databaseReference = FirebaseDatabase.getInstance().getReference();
        SnapshotParser<ChatMessage> parser = new SnapshotParser<ChatMessage>() {
            @Override
            public ChatMessage parseSnapshot(DataSnapshot snapshot) {
                ChatMessage message = snapshot.getValue(ChatMessage.class);
                if (message != null) {
                    message.setId(snapshot.getKey());
                }
                return message;
            }
        };

        DatabaseReference messagesReference = databaseReference.child(MESSAGES_CHILD);
        FirebaseRecyclerOptions<ChatMessage> options = new FirebaseRecyclerOptions.Builder<ChatMessage>()
                .setQuery(messagesReference, parser)
                .build();
        firebaseAdapter = new FirebaseRecyclerAdapter<ChatMessage, MessageViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull MessageViewHolder holder, int position, @NonNull ChatMessage message) {
                holder.messageTextView.setText(message.getText());
                holder.authorTextView.setVisibility(TextView.VISIBLE);

                holder.authorTextView.setText(message.getUsername());
                if (message.getProfilePicUrl() != null) {
                    Glide.with(MainActivity.this)
                            .load(message.getProfilePicUrl())
                            .into(holder.authorImageView);
                } else { // Default profile pic
                    holder.authorImageView.setImageDrawable(ContextCompat.getDrawable(
                            MainActivity.this, R.drawable.ic_default_profile_pic_36dp));
                }
                System.out.println(message.getText());
            }

            @NonNull
            @Override
            public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                return new MessageViewHolder(inflater.inflate(R.layout.item_message, parent, false));
            }
        };

        // Update after message insertion
        firebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int messageCount = firebaseAdapter.getItemCount();
                int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();

                if (lastVisiblePosition == -1 || (positionStart >= messageCount - 1 && lastVisiblePosition == positionStart - 1))
                    messageRecyclerView.scrollToPosition(positionStart);
            }
        });

        messageRecyclerView.setAdapter(firebaseAdapter);

        // Typing messages
        messageEditText = findViewById(R.id.messageEditText);

        messageEditText.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(MESSAGE_LENGTH)
        });
        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    sendButton.setEnabled(true);
                } else {
                    sendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // Sending messages
        sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChatMessage newMessage = new ChatMessage(messageEditText.getText().toString(),
                        username,
                        profilePicUrl);
                databaseReference.child(MESSAGES_CHILD).push().setValue(newMessage);
                messageEditText.setText("");
            }
        });

    }

    // Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                firebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(googleApiClient);
                username = ANONYMOUS;
                startActivity(new Intent(this, SignInActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        firebaseAdapter.stopListening();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        firebaseAdapter.startListening();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "Erreur de connexion : " + connectionResult);
        Toast.makeText(this, "Erreur de connexion.", Toast.LENGTH_SHORT).show();
    }
}
