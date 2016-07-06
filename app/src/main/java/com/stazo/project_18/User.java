package com.stazo.project_18;

import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.MutableData;
import com.firebase.client.Transaction;
import com.firebase.client.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by ericzhang on 4/25/16.
 */
public class User {
    private String ID; // generated by facebook api
    private String name;
    private int explorerLevel;

    private static final String DEFAULT_BIO = "This user does not have a bio.";
    private String bio = DEFAULT_BIO;
    private ArrayList<String> myEvents = new ArrayList<String>();
    private ArrayList<String> attendingEvents = new ArrayList<String>();
    private ArrayList<String> reportedEvents = new ArrayList<String>();
    private ArrayList<Integer> categoryTrails = new ArrayList<Integer>();
    private ArrayList<String> userTrails = new ArrayList<String>(); // String of creator IDs
    private ArrayList<String> userFollowers = new ArrayList<>(); // String of follower IDs

    // hashmap of user's friends from name to id
    private HashMap<String, String> friends = new HashMap<String, String>();
    //private ArrayList<String> tagTrails = new ArrayList<String>();

    public String getBio() {
        return bio;
    }
    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setBio(Firebase fb, String bio) {
        bio = filterBio(bio);

        this.bio = bio;

        fb.child("Users").child(getID()).child("bio").setValue(bio);
    }

    public String filterBio(String bio) {
        if (bio.length() == 0) {
            return DEFAULT_BIO;
        }
        int newlines = 0;
        int i = 0;

        // remove newlines
        while (i < bio.length()) {
            char c = bio.charAt(i);
            if (c == '\n') {
                bio = bio.substring(0, i) + bio.substring(i + 1);
                continue;
            }
            i++;
        }

        return bio;
    }

    public HashMap<String, String> getFriends() {
        return friends;
    }


    public void setFriends(HashMap<String, String> friends) {
        this.friends = friends;
    }

    public int getExplorerLevel() {
        return explorerLevel;
    }

    public void setExplorerLevel(int explorerLevel) {
        this.explorerLevel = explorerLevel;
    }

    public ArrayList<Integer> getCategoryTrails() {
        return categoryTrails;
    }

    public void setCategoryTrails(ArrayList<Integer> categoryTrails) {
        this.categoryTrails = categoryTrails;
    }

    public ArrayList<String> getUserTrails() {
        return userTrails;
    }

    public void setUserTrails(ArrayList<String> userTrails) {
        this.userTrails = userTrails;
    }
    // can add further stuff later such as event host list, rating, history, upcoming event lists

    public ArrayList<String> getUserFollowers() { return userFollowers; }

    public void setUserFollowers(ArrayList<String> followers) { userFollowers = followers; }

    public boolean addTrail(Firebase fb, Integer type) {
        if (categoryTrails.contains(type)) {
            return false;
        }
        categoryTrails.add(type);
        fb.child("Users").child(ID).child("categoryTrails").setValue(categoryTrails);
        return true;
    }

    public boolean addTrail(Firebase fb, String user_id) {
        if (userTrails.contains(user_id)) {
            return false;
        }
        userTrails.add(user_id);
        fb.child("Users").child(ID).child("userTrails").setValue(userTrails);
        addToFollowers(fb, user_id, ID);

        // send notification

        ArrayList<String> notList = new ArrayList<>();
        notList.add(user_id);

                (new NotificationNewFollow(Notification2.TYPE_NEW_FOLLOW, name, ID)).
                pushToFirebase(fb, notList);

        return true;
    }

    public boolean removeTrail(Firebase fb, String user_id) {
        if (!userTrails.contains(user_id)) {
            return false;
        }
        userTrails.remove(user_id);
        fb.child("Users").child(ID).child("userTrails").setValue(userTrails);
        removeFromFollowers(fb, user_id, ID);
        return true;
    }

    public void addToFollowers(Firebase fb, String receivingFollowID, final String givingFollowID) {
        // increment reports
        fb.child("Users").child(receivingFollowID).
                child("userFollowers").child(givingFollowID).setValue(true);
    }

    public void removeFromFollowers(Firebase fb, String receivingFollowID, final String givingFollowID) {
        // remove reports
        fb.child("Users").child(receivingFollowID).
                child("userFollowers").child(givingFollowID).setValue(null);
    }

    /**
     * Default constructor for firebase
     */
    public User() {
    }

    public User(String user_id) {
        this.ID = user_id;
        this.name = "";
    }

    // for new users
    public User(String user_id, String name) {
        this.ID = user_id;
        this.name = name;
    }

    // constructor with HashMap for existing users
    public User(HashMap<String, Object> userMap) {
        this.name = (String) userMap.get("name");
        this.ID = (String) userMap.get("id");

        if (userMap.containsKey("bio")) {
            bio = (String) userMap.get("bio");
        }

        // remove this after all users have myEvents reference in firebase
        if (userMap.containsKey("myEvents")) {
            myEvents.clear();

            // if stored as ArrayList (from user.pushToFirebase)
            if (userMap.get("myEvents") instanceof ArrayList) {
                for (String val: (ArrayList<String>) userMap.get("myEvents")) {
                    myEvents.add(val);
                }
            }

            // if stored as HashMap (from event.pushToFirebase, firebase "push" method)
            else {
                Log.d("else", "ELSE CASE myEvents");

                for (String val : ((HashMap<String, String>) userMap.get("myEvents")).values()) {
                    myEvents.add(val);
                }
            }
        }
        if (userMap.containsKey("reportedEvents")) {
            reportedEvents.clear();

            // if stored as ArrayList (from user.pushToFirebase)
            if (userMap.get("reportedEvents") instanceof ArrayList) {
                for (String val: (ArrayList<String>) userMap.get("reportedEvents")) {
                    reportedEvents.add(val);
                }
            }

            // if stored as HashMap (from event.pushToFirebase, firebase "push" method)
            else {
                Log.d("else", "ELSE CASE reportedEvents");
                for (String val : ((HashMap<String, String>) userMap.get("reportedEvents")).values()) {
                    reportedEvents.add(val);
                }
            }
        }
        if (userMap.containsKey("attendingEvents")) {
            attendingEvents.clear();

            // if stored as ArrayList (from user.pushToFirebase)
            if (userMap.get("attendingEvents") instanceof ArrayList) {
                for (String val: (ArrayList<String>) userMap.get("attendingEvents")) {
                    attendingEvents.add(val);
                }
            }

            // if stored as HashMap (from event.pushToFirebase, firebase "push" method)
            else {
                Log.d("else", "ELSE CASE attendingEvents");

                for (String val : ((HashMap<String, String>) userMap.get("attendingEvents")).values()) {
                    attendingEvents.add(val);
                }
            }
        }
        if (userMap.containsKey("categoryTrails")) {
            categoryTrails.clear();

            // if stored as ArrayList (from user.pushToFirebase)
            if (userMap.get("categoryTrails") instanceof ArrayList) {
                for (Long val: (ArrayList<Long>) userMap.get("categoryTrails")) {
                    categoryTrails.add(new Integer(val.intValue()));
                }
            }

            // if stored as HashMap (from event.pushToFirebase, firebase "push" method)
            else {
                for (Long val : ((HashMap<String, Long>) userMap.get("categoryTrails")).values()) {
                    categoryTrails.add(val.intValue());
                }
            }
        }
        if (userMap.containsKey("userTrails")) {
            userTrails.clear();

            // if stored as ArrayList (from user.pushToFirebase)
            if (userMap.get("userTrails") instanceof ArrayList) {
                for (String val: (ArrayList<String>) userMap.get("userTrails")) {
                    userTrails.add(val);
                }
            }

            // if stored as HashMap (from event.pushToFirebase, firebase "push" method)
            else {
                Log.d("else", "ELSE CASE + userTrails");
                for (String val : ((HashMap<String, String>) userMap.get("userTrails")).values()) {
                    userTrails.add(val);
                }
            }
        }

        // Followers, people who are subscribed to this user
        if (userMap.containsKey("userFollowers")) {
            userFollowers.clear();

            // if stored as ArrayList (from user.pushToFirebase)
            if (userMap.get("userFollowers") instanceof ArrayList) {
                for (String val: (ArrayList<String>) userMap.get("userFollowers")) {
                    userFollowers.add(val);
                }
            }

            // if stored as HashMap (from event.pushToFirebase, firebase "push" method)
            else {
                for (String val : ((HashMap<String, Boolean>) userMap.get("userFollowers")).keySet()) {
                    userFollowers.add(val);
                }
            }
        }

        // test trails
        //addTrail(new Firebase("https://stazo-project-18.firebaseio.com/"), new Integer(0));
        //addTrail(new Firebase("https://stazo-project-18.firebaseio.com/"), "10209766334938822");
        //addTrail(new Firebase("https://stazo-project-18.firebaseio.com/"), new Integer(2));
        //addTrail(new Firebase("https://stazo-project-18.firebaseio.com/"), "1070949549640758");
        /*friends.put("10209766334938822", "Justin Ang");
        friends.put("1070949549640758", "Gates Zeng");
        friends.put("1076100269116381", "Eric Zhang");
        friends.put("1131880253542315", "Luke Thomas");
        friends.put("1138117392898486", "Matthew Ung");
        friends.put("1177156832304841", "Ansel Blume");
        friends.put("1184188798300386", "Brian Chan");
        friends.put("1196215920412322", "Isaac Wang");*/

    }

    /**
     * Pushes the user's info onto Firebase
     */
    public void pushToFirebase(Firebase fb) {fb.child("Users").child(ID).setValue(this);}

    // true for successful report, false for unsuccessful report
    public boolean reportEvent(String event_id, Firebase fb) {
        if (reportedEvents.contains(event_id)) {
            return false;
        }
        // increment reports
        fb.child("Events").child(event_id).child("reports").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                if (currentData.getValue() == null) {
                    currentData.setValue(1);
                } else {
                    currentData.setValue((Long) currentData.getValue() + 1);
                }
                return Transaction.success(currentData); //we can also abort by calling Transaction.abort()
            }

            @Override
            public void onComplete(FirebaseError firebaseError, boolean committed, DataSnapshot currentData) {
                //This method will be called once with the results of the transaction.
            }
        });

        // user-stuff
        reportedEvents.add(event_id);
        fb.child("Users").child(ID).child("reportedEvents").setValue(reportedEvents);
        return true;
    }

    // true for successful attend, false for unsuccessful attend
    public boolean attendEvent(String event_id, String event_name, String creator_id, Firebase fb) {

        if (attendingEvents.contains(event_id)) {
            return false;
        }

        // send notification only if i'm not the creator
        if (!creator_id.equals(ID)) {
            ArrayList<String> creatorList = new ArrayList<>();
            creatorList.add(creator_id);

            (new NotificationJoinedEvent(Notification2.TYPE_JOINED_EVENT,
                    name, event_id, event_name, ID)).
                    pushToFirebase(fb, creatorList);
        }

        // increment popularity
        fb.child("Events").child(event_id).child("popularity").runTransaction(new Transaction.Handler() {

            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                if (currentData.getValue() == null) {
                    //Log.d("AttendEvents", "not attending events");
                    currentData.setValue(1);
                } else {
                    //Log.d("AttendEvents", "actually attending events");
                    currentData.setValue((Long) currentData.getValue() + 1);
                }
                return Transaction.success(currentData); //we can also abort by calling Transaction.abort()
            }

            @Override
            public void onComplete(FirebaseError firebaseError, boolean committed, DataSnapshot currentData) {
                //This method will be called once with the results of the transaction.
            }
        });

        // update event's attendees
        fb.child("Events").child(event_id).child("attendees").push().setValue(ID);

        // update user's attending events
        attendingEvents.add(event_id);

        System.out.println("AttendingEvents is " + attendingEvents);
        System.out.println("User id is" + ID);
        fb.child("Users").child(ID).child("attendingEvents").setValue(attendingEvents);
        return true;
    }

    // true for successful, false for unsuccessful unattend
    public boolean unattendEvent(String event_id, final Firebase fb) {
        if (!(attendingEvents.contains(event_id))) {
            return false;
        }

        // decrement popularity
        fb.child("Events").child(event_id).child("popularity").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                if (currentData.getValue() == null) {
                    Log.d("AttendEvents", "Unattend aborting");
                    // inclusion of line causes first transaction to fail!!!
                    //return Transaction.abort();
                } else {
                    currentData.setValue((Long) currentData.getValue() - 1);
                    Log.d("AttendEvents", "decremented, data now " + currentData.getValue());
                }
                return Transaction.success(currentData); //we can also abort by calling Transaction.abort()
            }

            @Override
            public void onComplete(FirebaseError firebaseError, boolean committed, DataSnapshot currentData) {
                //This method will be called once with the results of the transaction.
            }
        });

        // update event's attending
        fb.child("Events").child(event_id).child("attendees").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                            // if the ID is ours, remove it and return
                            if (eventSnapshot.getValue().equals(ID)) {
                                System.out.println("removing...");
                                eventSnapshot.getRef().setValue(null);
                                return;
                            }
                        }
                        // remove this listener
                        fb.child("Events").removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });

        // update user's attendingEvents
        attendingEvents.remove(event_id);
        fb.child("Users").child(ID).child("attendingEvents").setValue(attendingEvents);
        return true;
    }


    //Getters and setters
    public String getID() {return this.ID;}

    public String getName() {return this.name;}

    public ArrayList<String> getMyEvents() {return this.myEvents;}

    public void setAttendingEvents(ArrayList<String> attendingEvents) {
        this.attendingEvents = attendingEvents;
    }

    public void setReportedEvents(ArrayList<String> reportedEvents) {
        this.reportedEvents = reportedEvents;
    }

    public ArrayList<String> getAttendingEvents() {return attendingEvents;}

    public ArrayList<String> getReportedEvents() {return reportedEvents;}

    public void setMyEvents(ArrayList<String> events) { myEvents = events; }

    public void setID(String user_id) {this.ID = user_id;}

    public void setName(String name) {this.name = name;}

    public void addEvent(String a) {myEvents.add(a);}

    public boolean isSubscribedEvent(Event event) {
        // Check user subscriptions
        String creatorID = event.getCreator_id();

        for (String followedID : userTrails) {
            if (creatorID == followedID) {
                return true;
            }
        }

        return false;
    }

    /* construct friend's list */
    public void constructFriends(final Firebase fb) {
        /* make the API call */
        if (AccessToken.getCurrentAccessToken() == null) {
            Log.d("myTag", "access token was null");
            return;
        }

        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/friends",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        try {
                            final JSONArray json = response.getJSONObject().getJSONArray("data");
                            Log.d("json", "JSON is " + json.toString());
                            for(int i=0; i < json.length(); i++){
                                String name = (String)((JSONObject)json.get(i)).get("name");
                                String id = (String)((JSONObject)json.get(i)).get("id");
                                Log.d("friends", "Name: " + name + " ID: " + id);
                                // if this user is not already in, add to friends
                                if (!friends.values().contains(id)) {
                                    //addFriend(fb, id, name);
                                    friends.put(id, name);
                                }
                            }
                        }
                        catch (JSONException e) {

                        }
                    }
                }
        ).executeAsync();
    }


    private void addFriend(final Firebase fb, final String id, final String name) {
        fb.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // if the friend also has our app, add them
                if (dataSnapshot.hasChild(id)) {
                    Log.d("valid_friends", "VALID Name: " + name + " ID: " + id);
                    friends.put(name, id);
                }
                Log.d("invalid_friends", "INVALID Name: " + name + " ID: " + id);
                fb.child("Users").removeEventListener(this);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                fb.child("Users").removeEventListener(this);
            }
        });
    }
}
