<?php
// send_notification.php

function sendNotification($token, $title, $body) {
    $firebaseServerKey = "YOUR_SERVER_KEY"; // Replace this with your real server key

    $data = [
        "to" => $token,
        "notification" => [
            "title" => $title,
            "body" => $body,
            "sound" => "default"
        ],
        "data" => [
            "click_action" => "FLUTTER_NOTIFICATION_CLICK",
            "message" => $body
        ]
    ];

    $headers = [
        "Authorization: key=" . $firebaseServerKey,
        "Content-Type: application/json"
    ];

    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, "https://fcm.googleapis.com/fcm/send");
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));

    $result = curl_exec($ch);

    if (curl_errno($ch)) {
        error_log('FCM error: ' . curl_error($ch));
    }

    curl_close($ch);
}
?>
