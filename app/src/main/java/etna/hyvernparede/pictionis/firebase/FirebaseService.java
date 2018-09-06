package etna.hyvernparede.pictionis.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseService {
    private static FirebaseService serviceInstance = null;

    private FirebaseDatabase firebaseDatabaseInstance;
    private FirebaseAuth firebaseAuthInstance;

    private FirebaseService() {
        firebaseDatabaseInstance = FirebaseDatabase.getInstance();
        firebaseAuthInstance = FirebaseAuth.getInstance();
    }

    public static FirebaseService Firebase() {
        if (serviceInstance == null) {
            serviceInstance = new FirebaseService();
        }
        return serviceInstance;
    }

    // Auth
    public FirebaseUser getUser() {
        return firebaseAuthInstance.getCurrentUser();
    }

    public void signOut() {
        firebaseAuthInstance.signOut();
    }

    // Database
    public DatabaseReference getReference() {
        return firebaseDatabaseInstance.getReference();
    }

    public DatabaseReference getChildReference(String childName) {
        return getReference().child(childName);
    }

    public <T> void push(T value, String childName) {
        getChildReference(childName).push().setValue(value);
    }
}
