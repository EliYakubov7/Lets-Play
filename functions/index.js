const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendLikedNotafication = functions.database.ref("Feeds/{feedId}/likesControl").onCreate((snapshot, context) => {
    var request = snapshot.val();
    // the user who liked the [post]
    var likerUid = Object.keys(snapshot.val())[0];
    console.log("likerUid", likerUid);

    // the user whos post got liked
    var userUid = snapshot.ref.parent.child('author').once('value').then(author => {
        console.log("likedUid", author.val());
        // get the token of the poster
        var userToken = admin.database().ref("UsersTokens/" + author.val()).child("token").once('value').then(token => {
            console.log("userToken", token.val());

            admin.database().ref("Users/" + likerUid).child("firstName").once('value').then(name => {
                console.log("userFirstName", name.val());

                var payload = {
                    data: {
                        title: "Some one liked your post",
                        body: "Click here to find out!",
                        likerUid: likerUid,
                        likedUid: author.val(),
                        likedUserName: name.val()
                    }
                };

                const options = {
                    priority: "high",
                }; 

                if(likerUid != userUid){
                    admin.messaging().sendToDevice(token.val(), payload, options).then(function (response) {
                        console.log("Successfully sent message: ", response);
                    })
                        .catch(function (error) {
                            console.log("Error sending message: ", error);
                        })
                }


            });
        });
    });
});

exports.sendCommentPostNotafication = functions.database.ref("Feeds/{feedId}/comments/{commentId}").onCreate((snapshot, context) => {
    // commnetor Uid
    var commentAuthor = snapshot.child('author').val();
    console.log("commentAuthor", commentAuthor);
    //get poster Uid
    admin.database().ref("Feeds/" + context.params.feedId).child("author").once('value').then(uid => {
        // poster Uid
        var posterUid = uid.val();
        console.log("posterUid", posterUid);

        // get commentor's name
        admin.database().ref("Users/" + commentAuthor).child("firstName").once('value').then(firstName => {
            // commentor's name
            var commenterName = firstName.val();
            console.log("commenterName", commenterName);

            // get poster's Token
            admin.database().ref("UsersTokens/" + posterUid).child("token").once('value').then(token => {
                // poster token
                var posterToken = token.val();
                console.log("posterToken", posterToken);


                var payload = {
                    data: {
                        commentAuthor: commentAuthor,
                        posterUid: posterUid,
                        commenterName: commenterName,
                        posterToken: posterToken
                    }
                };

                const options = {
                    priority: "high",
                }; 

                if(commentAuthor != posterUid){
                    admin.messaging().sendToDevice(posterToken, payload, options).then(function (response) {
                        console.log("Successfully sent message: ", response);
                    })
                        .catch(function (error) {
                            console.log("Error sending message: ", error);
                        })
                }



            });
        });

    });

});

exports.sendNewFriendNotification = functions.database.ref("Users/{userUid}/friends/{friendNumber}").onCreate((snapshot, context) => {
    // added friend Uid
    var addedFriendUid = snapshot.val();
    console.log("addedFriendUid", addedFriendUid);

    // the user who added the friend
    var adderUid = context.params.userUid;
    console.log("adderUid", adderUid);

    admin.database().ref("Users/" + adderUid).child("firstName").once('value').then(name => {
        // adder first name
        var adderName = name.val();
        console.log("adderName", adderName);

        admin.database().ref("UsersTokens/" + addedFriendUid).child("token").once('value').then(token => {
            // added friend token
            var addedFriendToken = token.val();
            console.log("addedFriendToken", addedFriendToken);

            var payload = {
                data: {
                    addedFriendUid: addedFriendUid,
                    adderUid: adderUid,
                    adderName: adderName,
                    addedFriendToken: addedFriendToken
                }
            };

            const options = {
                priority: "high",
            }; 

            admin.messaging().sendToDevice(addedFriendToken, payload, options).then(function (response) {
                console.log("Successfully sent message: ", response);
                console.log("Real error", response.results[0].error);
            })
                .catch(function (error) {
                    console.log("Error sending message: ", error);
                })
        });

    });
});

exports.serverKey = functions.https.onCall((data, context) => {
    return {
        serverKey: "AAAAbNhVpI8:APA91bE028K9HSkzF3kpsA6e5jP4i9aSky7xBuOl9dxKfptI0Xm7NqswPWZkJHxkf0VXh4i02wZPG9vCzOIw1lRFmWUAmWKopvS5VtPPXOwemSc9zhugB8qvZjM9PpWDxtRR9uJnQnc-"
      };
});